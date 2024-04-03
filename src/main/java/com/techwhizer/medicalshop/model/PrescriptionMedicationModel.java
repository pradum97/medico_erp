package com.techwhizer.medicalshop.model;

public class PrescriptionMedicationModel {

    private int medicationId;
    private String medicineName;
    private String medicineTag;
    private String quantity;
    private String frequency;
    private String duration;
    private String times;
    private String composition;
    private String dose;
    private String remark;
    private int itemId;
    private boolean isItemExists;
    private int prescriptionMasterId;


    public PrescriptionMedicationModel(int medicationId, String medicineName, String medicineTag, String quantity, String frequency,
                                       String duration, String times, String composition, String dose, String remark,
                                       int itemId, boolean isItemExists,int prescriptionMasterId) {
        this.medicationId = medicationId;
        this.medicineName = medicineName;
        this.medicineTag = medicineTag;
        this.quantity = quantity;
        this.frequency = frequency;
        this.duration = duration;
        this.times = times;
        this.composition = composition;
        this.dose = dose;
        this.remark = remark;
        this.itemId = itemId;
        this.isItemExists = isItemExists;
        this.prescriptionMasterId = prescriptionMasterId;
    }

    public int getPrescriptionMasterId() {
        return prescriptionMasterId;
    }

    public void setPrescriptionMasterId(int prescriptionMasterId) {
        this.prescriptionMasterId = prescriptionMasterId;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public boolean isItemExists() {
        return isItemExists;
    }

    public void setItemExists(boolean itemExists) {
        isItemExists = itemExists;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineTag() {
        return medicineTag;
    }

    public void setMedicineTag(String medicineTag) {
        this.medicineTag = medicineTag;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
