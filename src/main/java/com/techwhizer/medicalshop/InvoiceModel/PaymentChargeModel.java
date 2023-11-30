package com.techwhizer.medicalshop.InvoiceModel;

public class PaymentChargeModel {

    private String purpose, paymentMode,referenceNum,remarks;
    private double amount;

    public PaymentChargeModel(String purpose, String paymentMode, String referenceNum,
                              String remarks, double amount) {
        this.purpose = purpose;
        this.paymentMode = paymentMode;
        this.referenceNum = referenceNum;
        this.remarks = remarks;
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getReferenceNum() {
        return referenceNum;
    }

    public void setReferenceNum(String referenceNum) {
        this.referenceNum = referenceNum;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
