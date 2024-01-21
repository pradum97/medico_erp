CREATE OR REPLACE FUNCTION tab_to_strip(total_tab int, strip_tab int, unit varchar(50))
    RETURNS VARCHAR(1000) AS
$$
DECLARE
    result VARCHAR;
    strip  int;
    tab    int;

BEGIN

    IF unit = 'TAB' THEN

        strip = coalesce(total_tab, 0) / coalesce(strip_tab, 1);
        tab = coalesce(total_tab, 0) % coalesce(strip_tab, 1);

        if strip > 0 then
            result = strip || '-STR';
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


    RETURN coalesce(result,'0');
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

CREATE OR REPLACE FUNCTION get_available_quantity(itemId int,format varchar(50))
    RETURNS VARCHAR(300)
    LANGUAGE plpgsql AS
$func$

BEGIN

    return

    (case when format = 'PCS' or format = 'TAB' THEN
              cast((select distinct sum(quantity) from tbl_stock ts where item_id = itemId limit 1) as varchar(1000))
         ELSE

            cast(( select distinct

                       tab_to_strip(cast(

                                            (select sum(quantity) from tbl_stock ts where item_id = itemId) as int),
                                    cast(tim.strip_tab as int),ts.quantity_unit)


                   from stock_v ts

                            left join public.tbl_items_master tim on ts.item_id = tim.item_id

                   where ts.item_id = itemId limit 1) as varchar(1000))
        END
    );



END ;
$func$;


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
