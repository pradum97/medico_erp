package com.techwhizer.medicalshop.model;

public class MedicalHistoryMainModel {

    private int historyMainId;
    private String consultDate;
    private int totalItems;

    public MedicalHistoryMainModel(int historyMainId, String consultDate, int totalItems) {
        this.historyMainId = historyMainId;
        this.consultDate = consultDate;
        this.totalItems = totalItems;
    }

    public int getHistoryMainId() {
        return historyMainId;
    }

    public void setHistoryMainId(int historyMainId) {
        this.historyMainId = historyMainId;
    }

    public String getConsultDate() {
        return consultDate;
    }

    public void setConsultDate(String consultDate) {
        this.consultDate = consultDate;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
