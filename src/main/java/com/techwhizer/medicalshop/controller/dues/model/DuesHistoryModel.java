package com.techwhizer.medicalshop.controller.dues.model;

public class DuesHistoryModel {
    private int paymentId;
    private double paidAmount;
    private String paymentMode;
    private String paymentDate;
    private String referenceNumber;
    private String remarks;

    public DuesHistoryModel(int paymentId, double paidAmount,
                            String paymentMode, String paymentDate, String referenceNumber, String remarks) {
        this.paymentId = paymentId;
        this.paidAmount = paidAmount;
        this.paymentMode = paymentMode;
        this.paymentDate = paymentDate;
        this.referenceNumber = referenceNumber;
        this.remarks = remarks;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
