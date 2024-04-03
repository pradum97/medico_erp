
------billing_report trigger------------------

CREATE OR REPLACE TRIGGER refresh_billing_report_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_sale_main
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_billing_report_v_fn();

CREATE OR REPLACE TRIGGER refresh_billing_report_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_patient
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_billing_report_v_fn();

CREATE OR REPLACE TRIGGER refresh_billing_report_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_salutation
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_billing_report_v_fn();

CREATE OR REPLACE TRIGGER refresh_billing_report_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_users
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_billing_report_v_fn();

------Item Chooser View trigger------------------
CREATE OR REPLACE TRIGGER refresh_item_chooser_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_items_master
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_item_chooser_v_fn();

CREATE OR REPLACE TRIGGER refresh_item_chooser_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_product_tax
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_item_chooser_v_fn();

CREATE OR REPLACE TRIGGER refresh_item_chooser_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_departments
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_item_chooser_v_fn();
-------------------------END-----------------------------


------Stock View Trigger------------------
CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_stock
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_items_master
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_purchase_items
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_purchase_main
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_dealer
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_product_tax
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

CREATE OR REPLACE TRIGGER refresh_stock_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_departments
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_stock_v_fn();

--------------------End---------------------

--------------Patient View Trigger Start----------

CREATE OR REPLACE TRIGGER refresh_patient_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_patient
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_patient_v_fn();

CREATE OR REPLACE TRIGGER refresh_patient_v_trg
    AFTER INSERT OR UPDATE OR DELETE
    ON tbl_salutation
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_patient_v_fn();


-----------------Patient View Trigger End

