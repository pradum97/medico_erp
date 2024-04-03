-----------------Start-----------------
CREATE MATERIALIZED VIEW patient_v as
select patient_id, tbl_patient.salutation_id, first_name, patient_category, uhid_no,
       patient_number, middle_name, last_name, gender, address, TO_CHAR(dob,'DD-MM-YYYY') AS dob, phone,
       id_type, id_number, guardian_name, weight, bp, pulse, sugar, spo2,
       temp, cvs, cns, chest, created_by, (TO_CHAR(last_update, 'DD-MM-YYYY')) as last_update,
       last_update_by,(TO_CHAR(creation_date, 'DD-MM-YYYY')) as creation_date,
       date_part('year',age(current_date,dob))as age,
       concat ( COALESCE(ts.name,''),' ',COALESCE(first_name,''),' ',
                COALESCE(middle_name,''),' ',COALESCE(last_name,'')) as fullName,
       ts.name as salutation_name
from tbl_patient
         left join tbl_salutation ts on tbl_patient.salutation_id = ts.salutation_id;
CREATE UNIQUE INDEX ON patient_v (patient_id);

-----------------------End---------------------------------------------------

------------------Start------------------------
CREATE MATERIALIZED VIEW stock_v as
select ts.stock_id,
       tim.items_name,
       tim.packing,
       tpi.batch,
       tpi.expiry_date,
       tpi.purchase_rate,
       tpi.mrp,
       tpi.sale_price,
       ts.quantity,
       ts.quantity_unit,
       tim.strip_tab,
       tpm.purchase_main_id,
       tpi.purchase_items_id,
       tab_to_strip(ts.quantity, tim.strip_tab,
                    ts.quantity_unit) as full_quantity,
       EXTRACT(EPOCH FROM ( convert_expiry_date(expiry_date)::timestamp - to_char(now(),'yyyy-MM-dd')::timestamp)) / 86400 as expiry_days_left,
       tim.composition,
       tim.dose, tim.mr_id, tim.gst_id, tim.type, tim.narcotic, tim.item_type, tim.status, tim.created_by, tim.created_date,
       (case when tim.is_active = 1 then (true) else (false) end )as is_active
        ,tim. tag,
       convert_expiry_date(expiry_date) as full_expiry_date,
       td.dealer_name,td.address as dealer_address,td.dealer_id,tim.item_id,
       (concat((tpt.igst + tpt.cgst + tpt.sgst), ' %')) as totalGst,tpt.tax_id,tpt.sgst,tpt.cgst,tpt.igst,
       tpt.gstname,tpt.hsn_sac,tpt.description,
       tdep.department_id,tdep.department_code,tdep.department_name,tim.is_stockable,
       tim.unit, tim.company_id, tim.mfr_id,tim.discount_id,
       CASE
           WHEN convert_expiry_date(tpi.expiry_date) < CURRENT_DATE THEN (true)
           ELSE (false)
           END AS is_expired
from tbl_stock ts
         left join tbl_items_master tim on tim.item_id = ts.item_id
         left join tbl_purchase_items tpi on ts.purchase_items_id = tpi.purchase_items_id
         left join tbl_purchase_main tpm on tpm.purchase_main_id = tpi.purchase_main_id
         left join tbl_dealer td on td.dealer_id = tpm.dealer_id
         left join tbl_product_tax tpt on tpt.tax_id = tim.gst_id
         left join tbl_departments tdep on tim.department_code = tdep.department_code
where tim.is_active = 1 and tim.status = 1;
CREATE UNIQUE INDEX ON stock_v (stock_id);
----------------------End----------------------

-------------------------------------Start-------------------------------------------------------
CREATE MATERIALIZED VIEW item_chooser_v as
SELECT tim.items_name, tim.packing,  tim.strip_tab,tim.composition,
       tim.dose,tim.mr_id, tim.gst_id, tim.type,  tim.narcotic, tim.item_type,
       tim.status,tim.created_by,   tim.created_date,
       CASE WHEN tim.is_active = 1 THEN true ELSE false END  AS is_active,
       tim.tag, tim.item_id, concat(tpt.igst + tpt.cgst + tpt.sgst, ' %') AS totalgst,
       tpt.tax_id,tpt.sgst, tpt.cgst,tpt.igst,tpt.gstname, tpt.hsn_sac, tpt.description,
       tdep.department_id, tdep.department_code, tdep.department_name, tim.is_stockable,
       tim.unit, tim.company_id, tim.mfr_id, tim.discount_id
FROM tbl_items_master tim
         LEFT JOIN tbl_product_tax tpt ON tpt.tax_id = tim.gst_id
         LEFT JOIN tbl_departments tdep ON tim.department_code = tdep.department_code
WHERE tim.is_active = 1 AND tim.status = 1;
CREATE UNIQUE INDEX ON item_chooser_v (item_id);
------------------END--------------------------

----------------START-----------------------

CREATE MATERIALIZED VIEW billing_report_v AS
select tsm.sale_main_id ,tc.patient_id,tsm.seller_id,tsm.additional_discount_amount as additional_discount,
       tsm.tot_tax_amount,tsm.net_amount,tsm.payment_mode,tsm.invoice_number,
       tsm.bill_type, (TO_CHAR(tsm.sale_date , 'YYYY-MM-DD HH12:MI:SS AM')) as saleDate,
       regexp_replace(trim( concat(COALESCE(ts.name, ''), ' ',
                                   COALESCE(tc.first_name, ''), ' ',
                                   COALESCE(tc.middle_name, ''), ' ',
                                   COALESCE(tc.last_name, '')) ),'  ',' ' ) as name
        ,tc.phone,tc.address ,
        tsm.sale_date,
       tu.first_name,tu.last_name ,(select sum(tsi.net_amount) as netAmount from tbl_sale_items tsi
                                    where tsi.sale_main_id = tsm.sale_main_id group by tsm.sale_main_id),
       coalesce(tblRe.received_amount,tsm.net_amount) as received_amount
from tbl_sale_main tsm
         LEFT JOIN tbl_patient tc on (tsm.patient_id = tc.patient_id)
         left join tbl_salutation ts on ts.salutation_id = tc.salutation_id
         LEFT JOIN tbl_users tu on (tsm.seller_id = tu.user_id)
LEFT JOIN (select tsm.sale_main_id, coalesce(coalesce(received_amount,0)+
    (select coalesce(sum(pi.amount),0) from tbl_dues td
    left join payment_information pi on pi.document_id = td.dues_id
    where td.source_id = tsm.sale_main_id and pi.document_source = 'DUES'),0) as received_amount
    from tbl_sale_main tsm
    ) tblRe on tblRe.sale_main_id = tsm.sale_main_id;
CREATE UNIQUE INDEX ON billing_report_v (sale_main_id);
----------------------END-------------------------------------
