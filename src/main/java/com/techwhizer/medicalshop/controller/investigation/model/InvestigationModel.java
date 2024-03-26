package com.techwhizer.medicalshop.controller.investigation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InvestigationModel {
    private int investigationID, itemId;
    private String itemName,resultValue;
    private LocalDate prescribeDate;
    private LocalDateTime resultDate;
    public InvestigationModel(int investigationID, int itemId, String itemName, String resultValue,
                              LocalDate prescribeDate, LocalDateTime resultDate) {
        this.investigationID = investigationID;
        this.itemId = itemId;
        this.itemName = itemName;
        this.resultValue = resultValue;
        this.prescribeDate = prescribeDate;
        this.resultDate = resultDate;
    }

    public int getInvestigationID() {
        return investigationID;
    }

    public void setInvestigationID(int investigationID) {
        this.investigationID = investigationID;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public LocalDate getPrescribeDate() {
        return prescribeDate;
    }

    public void setPrescribeDate(LocalDate prescribeDate) {
        this.prescribeDate = prescribeDate;
    }

    public LocalDateTime getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDateTime resultDate) {
        this.resultDate = resultDate;
    }
}
