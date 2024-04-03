CREATE OR REPLACE FUNCTION tab_to_strip(total_tab numeric, strip_tab numeric, unit varchar(50))
    RETURNS VARCHAR(1000) AS
$$
DECLARE
    result VARCHAR;
    strip  int;
    tab    int;

BEGIN
    IF unit = 'TAB' THEN
        strip = split_part(cast((coalesce(total_tab, 0) / coalesce(strip_tab, 1))as varchar(30)), '.', 1) ;
        tab = (coalesce(total_tab, 0) % coalesce(strip_tab, 1));

        if strip > 0 then
            result = (strip || '-STR');
        END IF;

        if tab > 0 THEN
            if strip > 0 THEN
                result = concat(result, ',');
            END IF;
            result = concat(result, tab , '-TAB');
       END IF;

    ELSE
        result = total_tab || '-PCS';
    END IF;

    RETURN coalesce(result,concat('0-',unit));
END ;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION convert_expiry_date(expiry_date varchar(30))
    RETURNS date AS
$$

BEGIN

  return  TO_DATE(concat(EXTRACT(DAY FROM (date_trunc('MONTH', concat(split_part(expiry_date, '/', 2), '-',
                                                                split_part(expiry_date, '/', 1), '-', '01')::date) +
                                     INTERVAL '1 MONTH' - INTERVAL '1 day')),'/',expiry_date),'dd/MM/yyyy');

END ;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_available_quantity(stockId int,returnFormat varchar(50))
    RETURNS VARCHAR(300)
    LANGUAGE plpgsql AS
$$
BEGIN
    return (
        case when upper(returnFormat) = 'PCS' or upper(returnFormat) = 'TAB' THEN
        cast((select distinct quantity from tbl_stock ts where ts.stock_id = stockId) as varchar(300))
        when upper(returnFormat) = 'STRIP' THEN
            cast( ( select distinct tab_to_strip(ts.quantity,tim.strip_tab,ts.quantity_unit)
             from stock_v ts
                      left join public.tbl_items_master tim on ts.item_id = tim.item_id
             where ts.stock_id =stockId) as varchar(300))
         ELSE
        concat('0-','PCS')
        END
    ) ;
END ;
$$;

CREATE OR REPLACE FUNCTION get_remaining_dues(dues_source_id int)
    RETURNS VARCHAR(50) AS
$$
DECLARE
    result VARCHAR;
BEGIN

    result = (select td.dues_amount - greatest((select sum(coalesce(amount,0)) as dues_amount from
        payment_information
              where document_id = td.dues_id and document_source = 'DUES'),0) as remaining_dues
              from tbl_dues td where td.source_id = dues_source_id);

    return result;
END ;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_received_amount(saleMainId int)
    RETURNS numeric AS
$$
DECLARE
    result VARCHAR;
BEGIN

    result = (select coalesce(coalesce(received_amount,0)+
    (select coalesce(sum(pi.amount),0) from tbl_dues td
    left join payment_information pi on pi.document_id = td.dues_id
     where td.source_id = tsm.sale_main_id and pi.document_source = 'DUES'),0) as received_amount
    from tbl_sale_main tsm
    where tsm.sale_main_id = saleMainId
    );
    return cast(result as numeric);
END ;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_billing_report_v()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY billing_report_v;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;


----------for materialized view-------
CREATE OR REPLACE FUNCTION refresh_billing_report_v_fn()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY billing_report_v;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_item_chooser_v_fn()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY item_chooser_v;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION refresh_stock_v_fn()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY stock_v;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_patient_v_fn()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY patient_v;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_all_materialized_views_concurrently() RETURNS VOID AS $$
DECLARE
    mv_name TEXT;
BEGIN
    FOR mv_name IN SELECT matviewname FROM pg_matviews WHERE schemaname = 'public' LOOP
            EXECUTE 'REFRESH MATERIALIZED VIEW  CONCURRENTLY  ' || quote_ident(mv_name);
        END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_all_materialized_views() RETURNS VOID AS $$
DECLARE
    mv_name TEXT;
BEGIN
    FOR mv_name IN SELECT matviewname FROM pg_matviews WHERE schemaname = 'public' LOOP
            EXECUTE 'REFRESH MATERIALIZED VIEW  ' || quote_ident(mv_name);
        END LOOP;
END;
$$ LANGUAGE plpgsql;
--------------------------------------------


