--view count \dv
-------------------09-Jan-2024---------------------

drop view if exists print_consultant_slip_v;
drop view if exists consultant_history_v;
drop view if exists patient_v;
drop view if exists stock_v;
drop view if exists available_quantity_v;
drop view if exists stock_v;


--update dob from age
--drop patient_age_column
--create function
--alter view and create patient view
--CREATE DUES TABLE

-- CREATE ZERO GST
-- CTEATE DEPARTMENT TABLE


-- age to dob
update tbl_patient set dob = to_char(CURRENT_DATE - INTERVAL '1 year' *
NULLIF(regexp_replace(age, '\D','','g'), '')::numeric,'DD-MM-YYYY');


ALTER TABLE tbl_sale_main
    RENAME COLUMN additional_discount TO ADDITIONAL_DISCOUNT_AMOUNT;

ALTER TABLE tbl_sale_main
    ADD COLUMN ADDITIONAL_DISCOUNT_PERCENTAGE NUMERIC DEFAULT 0;

ALTER TABLE tbl_sale_main
    ADD COLUMN TOTAL_DISCOUNT_AMOUNT NUMERIC;

ALTER TABLE tbl_sale_main
    ADD COLUMN RECEIVED_AMOUNT NUMERIC;

ALTER TABLE tbl_sale_items ADD ADDITIONAL_DISCOUNT_PERCENTAGE numeric DEFAULT 0;



ALTER TABLE TBL_RETURN_ITEMS ADD DISCOUNT_AMOUNT numeric;
ALTER TABLE TBL_RETURN_ITEMS ADD AMOUNT numeric;
ALTER TABLE TBL_RETURN_ITEMS ADD NET_AMOUNT numeric;

ALTER TABLE tbl_items_master ADD is_stockable boolean default true;


ALTER TABLE tbl_purchase_main DROP CONSTRAINT tbl_purchase_main_dealer_id_fkey;


ALTER TABLE tbl_purchase_items ALTER COLUMN batch DROP NOT NULL;
ALTER TABLE tbl_purchase_items ALTER COLUMN expiry_date DROP NOT NULL;

ALTER TABLE TBL_PURCHASE_MAIN ALTER COLUMN DEALER_ID DROP NOT NULL;

delete from tbl_cart;
ALTER TABLE tbl_cart ADD created_by int not null ;


ALTER table tbl_sale_items add is_stockable boolean default true;


ALTER table tbl_items_master add department_code VARCHAR(50) DEFAULT 'Medicine';


ALTER TABLE tbl_patient
    DROP COLUMN age;
