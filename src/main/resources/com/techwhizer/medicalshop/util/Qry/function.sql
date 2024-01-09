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
