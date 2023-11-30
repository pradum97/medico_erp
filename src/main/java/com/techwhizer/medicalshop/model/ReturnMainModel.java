package com.techwhizer.medicalshop.model;

public class ReturnMainModel {

    private int returnMainId;
    private String patientName;
    private String returnDate;
    private String invoiceNumber;
    private double refundAmount;
    private String remark;

    public ReturnMainModel(int returnMainId, String patientName, String returnDate, String invoiceNumber, double refundAmount, String remark) {
        this.returnMainId = returnMainId;
        this.patientName = patientName;
        this.returnDate = returnDate;
        this.invoiceNumber = invoiceNumber;
        this.refundAmount = refundAmount;
        this.remark = remark;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getReturnMainId() {
        return returnMainId;
    }

    public void setReturnMainId(int returnMainId) {
        this.returnMainId = returnMainId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
}
