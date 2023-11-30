package com.techwhizer.medicalshop.model;

public class MedicalHistoryItemsModel {

    private int medicalItemId;

    private int itemId;
    private int medicalMainHistoryId;
    private String itemName;
    private boolean isItemExistsInStock;
    private String composition;
    private String tag;
    private String quantity;
    private String time;
    private String dose;
    private String frequency;
    private String duration;
    private String status;
    private String remarks;
    private String creationDate;

    public MedicalHistoryItemsModel(int medicalItemId, int itemId, int medicalMainHistoryId, String itemName, boolean isItemExistsInStock, String composition, String tag, String quantity, String time, String dose,
                                    String frequency, String duration, String status, String remarks, String creationDate) {
        this.medicalItemId = medicalItemId;
        this.itemId = itemId;
        this.medicalMainHistoryId = medicalMainHistoryId;
        this.itemName = itemName;
        this.isItemExistsInStock = isItemExistsInStock;
        this.composition = composition;
        this.tag = tag;
        this.quantity = quantity;
        this.time = time;
        this.dose = dose;
        this.frequency = frequency;
        this.duration = duration;
        this.status = status;
        this.remarks = remarks;
        this.creationDate = creationDate;
    }

    public int getMedicalItemId() {
        return medicalItemId;
    }

    public void setMedicalItemId(int medicalItemId) {
        this.medicalItemId = medicalItemId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getMedicalMainHistoryId() {
        return medicalMainHistoryId;
    }

    public void setMedicalMainHistoryId(int medicalMainHistoryId) {
        this.medicalMainHistoryId = medicalMainHistoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isItemExistsInStock() {
        return isItemExistsInStock;
    }

    public void setItemExistsInStock(boolean itemExistsInStock) {
        isItemExistsInStock = itemExistsInStock;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
