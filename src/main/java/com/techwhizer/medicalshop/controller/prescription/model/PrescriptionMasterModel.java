package com.techwhizer.medicalshop.controller.prescription.model;

public class PrescriptionMasterModel {

    private int prescriptionMasterId,consultationId,
            patientId,createdBy,updatedBy;

    private boolean isFinal;
    private String prescriptionDate,prescriptionNum,remark;

    public PrescriptionMasterModel(int prescriptionMasterId, int consultationId, int patientId, int createdBy, int updatedBy, boolean isFinal, String prescriptionDate, String prescriptionNum, String remark) {
        this.prescriptionMasterId = prescriptionMasterId;
        this.consultationId = consultationId;
        this.patientId = patientId;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isFinal = isFinal;
        this.prescriptionDate = prescriptionDate;
        this.prescriptionNum = prescriptionNum;
        this.remark = remark;
    }

    public int getPrescriptionMasterId() {
        return prescriptionMasterId;
    }

    public void setPrescriptionMasterId(int prescriptionMasterId) {
        this.prescriptionMasterId = prescriptionMasterId;
    }

    public int getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(int consultationId) {
        this.consultationId = consultationId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public String getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(String prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getPrescriptionNum() {
        return prescriptionNum;
    }

    public void setPrescriptionNum(String prescriptionNum) {
        this.prescriptionNum = prescriptionNum;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
