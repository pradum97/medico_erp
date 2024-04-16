package com.techwhizer.medicalshop.controller.reports.model;

public class DepartmentWiseReportModel {

    private String patientName;
    private String patientPhoneNum;
    private String patientAddress;
    private String itemName;
    private String departmentName;
    private String billingDate;
    private double mrp,netAmount;

    public DepartmentWiseReportModel(String patientName, String patientPhoneNum, String patientAddress, String itemName,
                                     String departmentName, String billingDate, double mrp, double netAmount) {
        this.patientName = patientName;
        this.patientPhoneNum = patientPhoneNum;
        this.patientAddress = patientAddress;
        this.itemName = itemName;
        this.departmentName = departmentName;
        this.billingDate = billingDate;
        this.mrp = mrp;
        this.netAmount = netAmount;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhoneNum() {
        return patientPhoneNum;
    }

    public void setPatientPhoneNum(String patientPhoneNum) {
        this.patientPhoneNum = patientPhoneNum;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(String billingDate) {
        this.billingDate = billingDate;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }
}
