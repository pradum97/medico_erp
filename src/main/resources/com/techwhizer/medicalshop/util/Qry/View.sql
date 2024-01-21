CREATE OR REPLACE VIEW print_consultant_slip_v as
select consultation_id,
       pc.referred_by_doctor_id,
       pc.consultation_doctor_id,
       tp.patient_id,
       regexp_replace(trim( concat(COALESCE(ts.name, ''), ' ',
              COALESCE(tp.first_name, ''), ' ',
              COALESCE(tp.middle_name, ''), ' ',
              COALESCE(tp.last_name, '')) ),'  ',' ' )               as patient_name,
       tp.uhid_no,
       tp.address,
       concat('Dr. ', tdCon.dr_name)                        as consult_name,
       (case
            when tdRef.dr_name is null then pc.referred_by_name
            else concat('Dr. ', tdRef.dr_name) end)         as referred_by,
       pc.receipt_num,
       (TO_CHAR(pc.creation_date, 'DD-MM-YYYY HH12:MI AM')) as receipt_date,
       concat(tp.gender,'/',date_part('year', age(current_date,TO_DATE(tp.dob,'dd/MM/yyyy')))) as gender_age,tp.phone,
       TO_CHAR(pc.creation_date,'DD-MM-YYYY')  AS consult_date,
       tu.username as preparedBy,pc.receipt_type,tp.patient_category

from patient_consultation pc
         left join tbl_patient tp on pc.patient_id = tp.patient_id
         left join tbl_salutation ts on ts.salutation_id = tp.salutation_id
         left join tbl_doctor tdRef on tdRef.doctor_id = pc.referred_by_doctor_id
         left join tbl_doctor tdCon on tdCon.doctor_id = pc.consultation_doctor_id
         left join tbl_users tu on tu.user_id = pc.created_by

;

CREATE OR REPLACE VIEW consultant_history_v as
select consultation_id,
       tp.patient_id,
       pc.referred_by_doctor_id,
       pc.consultation_doctor_id,
       tp.guardian_name,date_part('year',age(current_date,TO_DATE(tp.dob,'dd/MM/yyyy')))as age,tp.gender,tp.address,
       regexp_replace(trim( concat(COALESCE(ts.name, ''), ' ',
              COALESCE(tp.first_name, ''), ' ',
              COALESCE(tp.middle_name, ''), ' ',
              COALESCE(tp.last_name, ''))   )  ,'  ',' ' )             as patient_name,
       (TO_CHAR(pc.creation_date, 'DD-MM-YYYY HH12:MI AM')) as consult_date_time,
       (TO_CHAR(pc.creation_date, 'DD-MM-YYYY')) as consult_date,
       (case
            when tdRef.dr_name is null then pc.referred_by_name
            else concat('Dr. ', tdRef.dr_name) end)         as referred_by_name,
       concat('Dr. ', tdCon.dr_name)                        as consult_name,
       pc.consultant_status,pc.receipt_num,pc.receipt_type,pc.remarks,pc.description
from patient_consultation pc
         left join tbl_patient tp on pc.patient_id = tp.patient_id
         left join tbl_salutation ts on ts.salutation_id = tp.salutation_id
         left join tbl_doctor tdRef on tdRef.doctor_id = pc.referred_by_doctor_id
         left join tbl_doctor tdCon on tdCon.doctor_id = pc.consultation_doctor_id
;

CREATE OR REPLACE VIEW patient_v as
select patient_id, tbl_patient.salutation_id, first_name, patient_category, uhid_no,
       admission_number, middle_name, last_name, gender, address, dob, phone,
       id_type, id_number, guardian_name, weight, bp, pulse, sugar, spo2,
       temp, cvs, cns, chest, created_by, (TO_CHAR(last_update, 'DD-MM-YYYY')) as last_update,
       last_update_by,(TO_CHAR(creation_date, 'DD-MM-YYYY')) as creation_date,
       date_part('year',age(current_date,TO_DATE(dob,'dd/MM/yyyy')))as age,
       concat ( COALESCE(ts.name,''),' ',COALESCE(first_name,''),' ',
                COALESCE(middle_name,''),' ',COALESCE(last_name,'')) as fullName,
       ts.name as salutation_name
from tbl_patient
left join tbl_salutation ts on tbl_patient.salutation_id = ts.salutation_id
;


CREATE OR REPLACE VIEW stock_v as
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
       tab_to_strip(cast(ts.quantity as int), cast(tim.strip_tab as int),
                    ts.quantity_unit) as full_quantity,
       EXTRACT(EPOCH FROM ( convert_expiry_date(expiry_date)::timestamp - to_char(now(),'yyyy-MM-dd')::timestamp)) / 86400 as expiry_days_left,
       tim.composition,
       tim.dose,
       convert_expiry_date(expiry_date) as full_expiry_date,
       td.dealer_name,td.address as dealer_address,td.dealer_id,tim.item_id
from tbl_stock ts
         left join tbl_items_master tim on tim.item_id = ts.item_id
         left join tbl_purchase_items tpi on ts.purchase_items_id = tpi.purchase_items_id
    left join public.tbl_purchase_main tpm on tpm.purchase_main_id = tpi.purchase_main_id
left join tbl_dealer td on td.dealer_id = tpm.dealer_id;

CREATE OR REPLACE VIEW available_quantity_v as
SELECT tim.item_id, items_name, unit, strip_tab, packing, company_id, mfr_id,
       discount_id, mr_id, gst_id, type, narcotic, item_type, status, created_by, created_date,
       is_active, composition, dose, tag,(concat((tpt.igst + tpt.cgst + tpt.sgst), ' %')) as totalGst,
    coalesce(get_available_quantity(tim.item_id,'STRIP'),'0')
                                                        as avl_qty_strip
        ,
       cast( coalesce(get_available_quantity(tim.item_id,'TAB'),'0') as numeric)
           as avl_qty_pcs,tpt.tax_id, tpt.sgst, tpt.cgst, tpt.igst,
       tpt.gstname, tpt.hsn_sac, tpt.description

from tbl_items_master as tim
         left join tbl_product_tax tpt on tpt.tax_id = tim.gst_id;


CREATE OR REPLACE VIEW dues_v as
select distinct  td.source_id,td.dues_id, concat ( COALESCE(ts.name,''),' ',COALESCE(first_name,''),' ',
                                                   COALESCE(middle_name,''),' ',COALESCE(last_name,'')) as full_name,
                 tp.phone,tp.address,to_char(td.created_date,'DD/MM/YYYY') as dues_date,
                 td.dues_type,td.dues_amount as total_dues,
                 get_remaining_dues(td.source_id) as remaining_dues,
                 (select concat(greatest(amount,0),'₹ / ',to_char(payment_date,'DD-MM-YYYY')) from
                     payment_information where document_id = td.dues_id and document_source='DUES' order by 1 desc limit 1) as last_payment_amt_date,

                 greatest((select sum(amount) from payment_information where document_id =
                 td.dues_id and document_source='DUES'),0) as total_received_amount

from tbl_dues td
         left join tbl_sale_main tsm on tsm.sale_main_id = td.source_id
         left join tbl_patient tp on tp.patient_id = tsm.patient_id
         left join tbl_salutation ts on ts.salutation_id = tp.salutation_id;








