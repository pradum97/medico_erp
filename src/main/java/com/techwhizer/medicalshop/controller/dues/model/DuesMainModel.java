package com.techwhizer.medicalshop.controller.dues.model;

public class DuesMainModel {
    private int duesId;
    private int source_id;
    private String duesType;

    private String patientName,patientPhone,patientAddress;

    private double remainingDuesAmount,totalDuesAmount,totalReceivedAmount;
    private String lastDuesAmtAndDate;

    private String duesDate;

    public DuesMainModel(int duesId, int source_id, String duesType, String patientName, String patientPhone, String patientAddress, double remainingDuesAmount,
                         double totalDuesAmount,double totalReceivedAmount, String lastDuesAmtAndDate, String duesDate) {
        this.duesId = duesId;
        this.source_id = source_id;
        this.duesType = duesType;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.remainingDuesAmount = remainingDuesAmount;
        this.totalDuesAmount = totalDuesAmount;
        this.totalReceivedAmount = totalReceivedAmount;
        this.lastDuesAmtAndDate = lastDuesAmtAndDate;
        this.duesDate = duesDate;
    }

    public double getTotalReceivedAmount() {
        return totalReceivedAmount;
    }

    public void setTotalReceivedAmount(double totalReceivedAmount) {
        this.totalReceivedAmount = totalReceivedAmount;
    }

    public int getDuesId() {
        return duesId;
    }

    public void setDuesId(int duesId) {
        this.duesId = duesId;
    }

    public int getSource_id() {
        return source_id;
    }

    public void setSource_id(int source_id) {
        this.source_id = source_id;
    }

    public String getDuesType() {
        return duesType;
    }

    public void setDuesType(String duesType) {
        this.duesType = duesType;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public double getRemainingDuesAmount() {
        return remainingDuesAmount;
    }

    public void setRemainingDuesAmount(double remainingDuesAmount) {
        this.remainingDuesAmount = remainingDuesAmount;
    }

    public double getTotalDuesAmount() {
        return totalDuesAmount;
    }

    public void setTotalDuesAmount(double totalDuesAmount) {
        this.totalDuesAmount = totalDuesAmount;
    }

    public String getLastDuesAmtAndDate() {
        return lastDuesAmtAndDate;
    }

    public void setLastDuesAmtAndDate(String lastDuesAmtAndDate) {
        this.lastDuesAmtAndDate = lastDuesAmtAndDate;
    }

    public String getDuesDate() {
        return duesDate;
    }

    public void setDuesDate(String duesDate) {
        this.duesDate = duesDate;
    }
}
