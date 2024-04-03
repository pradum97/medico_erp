

CREATE INDEX CONCURRENTLY idx_stock_v ON stock_v USING btree(stock_id, items_name, batch, expiry_date,
                                                             purchase_rate, mrp, sale_price, quantity, quantity_unit, strip_tab,
                                                             purchase_main_id, purchase_items_id, full_quantity, composition, dose,
                                                             gst_id, status, tag, full_expiry_date, dealer_name, dealer_address,
                                                             dealer_id, item_id, tax_id, sgst, cgst, igst, department_id, department_code,
                                                             department_name, unit) WITH (fillfactor = 90);

CREATE INDEX CONCURRENTLY idx_billing_report_v on billing_report_v(sale_main_id, patient_id, seller_id, additional_discount, tot_tax_amount, net_amount, payment_mode, invoice_number, bill_type, saledate, name, phone, address, received_amount, sale_date, first_name, last_name, netamount) ;


CREATE INDEX CONCURRENTLY idx_patient_v on patient_v(patient_id, salutation_id, first_name, patient_category, uhid_no, patient_number, middle_name, last_name, gender, address, dob, phone, id_type, id_number, guardian_name, weight, bp, pulse, sugar, spo2, temp, cvs, cns, chest, created_by, last_update, last_update_by, creation_date, age, fullname, salutation_name)

