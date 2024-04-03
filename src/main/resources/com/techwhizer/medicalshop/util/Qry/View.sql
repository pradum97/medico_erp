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
       concat(tp.gender,'/',date_part('year', age(current_date,tp.dob))) as gender_age,tp.phone,
       TO_CHAR(pc.creation_date,'DD-MM-YYYY')  AS consult_date,
       tu.username as preparedBy,pc.receipt_type,tp.patient_category

from patient_consultation pc
         left join tbl_patient tp on pc.patient_id = tp.patient_id
         left join tbl_salutation ts on ts.salutation_id = tp.salutation_id
         left join tbl_doctor tdRef on tdRef.doctor_id = pc.referred_by_doctor_id
         left join tbl_doctor tdCon on tdCon.doctor_id = pc.consultation_doctor_id
         left join tbl_users tu on tu.user_id = pc.created_by;

CREATE OR REPLACE VIEW consultant_history_v as
select consultation_id,
       tp.patient_id,
       pc.referred_by_doctor_id,
       pc.consultation_doctor_id,
       tp.guardian_name,date_part('year',age(current_date,tp.dob))as age,tp.gender,tp.address,
       regexp_replace(trim( concat(COALESCE(ts.name, ''), ' ',
              COALESCE(tp.first_name, ''), ' ',
              COALESCE(tp.middle_name, ''), ' ',
              COALESCE(tp.last_name, ''))   )  ,'  ',' ' )             as patient_name,
       (TO_CHAR(pc.creation_date, 'DD-MM-YYYY HH12:MI AM')) as consult_date_time,
       (TO_CHAR(pc.creation_date, 'DD-MM-YYYY')) as consult_date,
       (case
            when tdRef.dr_name is null then pc.referred_by_name
            else
                ( case when tdRef.doctor_type ='OTHER' then tdRef.dr_name else concat('Dr. ', tdRef.dr_name) end) end)  as referred_by_name,
       concat('Dr. ', tdCon.dr_name)                        as consult_name,
       pc.consultant_status,pc.receipt_num,pc.receipt_type,pc.remarks,pc.description
from patient_consultation pc
         left join tbl_patient tp on pc.patient_id = tp.patient_id
         left join tbl_salutation ts on ts.salutation_id = tp.salutation_id
         left join tbl_doctor tdRef on tdRef.doctor_id = pc.referred_by_doctor_id
         left join tbl_doctor tdCon on tdCon.doctor_id = pc.consultation_doctor_id;



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


