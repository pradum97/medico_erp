package com.techwhizer.medicalshop.model;

public class ConsultantModel {
    private int consultation_id;
    private int referred_by_doctor_id,patient_id;
    private int consultation_doctor_id;
    private String patient_name;
    private String consult_date;
    private String referred_by_name;
    private String consult_name;
    private String consultant_status;
    private  String receiptNum,receipt_type,remarks,description;
    private String guardianName,gender, age,address;

    public ConsultantModel(int consultation_id, int referred_by_doctor_id, int patient_id, int consultation_doctor_id, String patient_name, String consult_date, String referred_by_name, String consult_name, String consultant_status, String receiptNum,
                           String receipt_type, String remarks, String description, String guardianName, String gender, String age, String address) {
        this.consultation_id = consultation_id;
        this.referred_by_doctor_id = referred_by_doctor_id;
        this.patient_id = patient_id;
        this.consultation_doctor_id = consultation_doctor_id;
        this.patient_name = patient_name;
        this.consult_date = consult_date;
        this.referred_by_name = referred_by_name;
        this.consult_name = consult_name;
        this.consultant_status = consultant_status;
        this.receiptNum = receiptNum;
        this.receipt_type = receipt_type;
        this.remarks = remarks;
        this.description = description;
        this.guardianName = guardianName;
        this.gender = gender;
        this.age = age;
        this.address = address;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReceipt_type() {
        return receipt_type;
    }

    public void setReceipt_type(String receipt_type) {
        this.receipt_type = receipt_type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReceiptNum() {
        return receiptNum;
    }

    public void setReceiptNum(String receiptNum) {
        this.receiptNum = receiptNum;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    public int getConsultation_id() {
        return consultation_id;
    }

    public void setConsultation_id(int consultation_id) {
        this.consultation_id = consultation_id;
    }

    public int getReferred_by_doctor_id() {
        return referred_by_doctor_id;
    }

    public void setReferred_by_doctor_id(int referred_by_doctor_id) {
        this.referred_by_doctor_id = referred_by_doctor_id;
    }

    public int getConsultation_doctor_id() {
        return consultation_doctor_id;
    }

    public void setConsultation_doctor_id(int consultation_doctor_id) {
        this.consultation_doctor_id = consultation_doctor_id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getConsult_date() {
        return consult_date;
    }

    public void setConsult_date(String consult_date) {
        this.consult_date = consult_date;
    }

    public String getReferred_by_name() {
        return referred_by_name;
    }

    public void setReferred_by_name(String referred_by_name) {
        this.referred_by_name = referred_by_name;
    }

    public String getConsult_name() {
        return consult_name;
    }

    public void setConsult_name(String consult_name) {
        this.consult_name = consult_name;
    }

    public String getConsultant_status() {
        return consultant_status;
    }

    public void setConsultant_status(String consultant_status) {
        this.consultant_status = consultant_status;
    }
}
