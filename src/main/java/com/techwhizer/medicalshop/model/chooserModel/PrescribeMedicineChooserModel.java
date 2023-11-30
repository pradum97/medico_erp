package com.techwhizer.medicalshop.model.chooserModel;

public class PrescribeMedicineChooserModel {
   private int prescribe_master_medicine_id,consultation_id;
   private String invoiceNumber,consultDate;

    public PrescribeMedicineChooserModel(int prescribe_master_medicine_id, int consultation_id,
                                         String invoiceNumber, String consultDate) {
        this.prescribe_master_medicine_id = prescribe_master_medicine_id;
        this.consultation_id = consultation_id;
        this.invoiceNumber = invoiceNumber;
        this.consultDate = consultDate;
    }

    public int getPrescribe_master_medicine_id() {
        return prescribe_master_medicine_id;
    }

    public void setPrescribe_master_medicine_id(int prescribe_master_medicine_id) {
        this.prescribe_master_medicine_id = prescribe_master_medicine_id;
    }

    public int getConsultation_id() {
        return consultation_id;
    }

    public void setConsultation_id(int consultation_id) {
        this.consultation_id = consultation_id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getConsultDate() {
        return consultDate;
    }

    public void setConsultDate(String consultDate) {
        this.consultDate = consultDate;
    }
}
