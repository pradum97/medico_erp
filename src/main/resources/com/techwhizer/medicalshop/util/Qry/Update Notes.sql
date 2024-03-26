alter table tbl_stock add created_date timestamp default current_timestamp;

------10-March-2024-----


    --ALTER TABLE tbl_patient RENAME COLUMN admission_number TO patient_number;

--For change Date Of Birth Data type To Date---
    -- Drop These View--
        --consultant_history_v
        --print_consultant_slip_v
        --patient_v
-- Then Alter
    --ALTER TABLE tbl_patient ALTER COLUMN dob TYPE Date USING dob::Date ;
    -- Then Execute Dropped View

--ALTER TABLE tbl_sale_main RENAME COLUMN doctor_id TO REFERRED_BY;
    --CREATE OUTDOOR DOCTOR (Doc Name-Self)

--available_quantity_v
--ALTER TABLE tbl_sale_main ADD COLUMN  consultation_doctor_id int

--ALTER TABLE patient_consultation ADD COLUMN sale_main_id int

