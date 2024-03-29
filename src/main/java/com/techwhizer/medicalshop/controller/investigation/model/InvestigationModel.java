package com.techwhizer.medicalshop.controller.investigation.model;

import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvestigationModel {
    private int investigationID;
    private String itemName,resultValue;
    private LocalDate prescribeDate;
    private LocalDateTime resultDate;
    private ItemChooserModel itemDetails;
    private int prescriptionMasterId;
    private String prescribedDateFor,resultDateTimeFor;
    public InvestigationModel(int investigationID, String itemName, String resultValue,
                              LocalDate prescribeDate, LocalDateTime resultDate,ItemChooserModel itemDetails,int prescriptionMasterId) {
        this.investigationID = investigationID;
        this.itemName = itemName;
        this.resultValue = resultValue;
        this.prescribeDate = prescribeDate;
        this.resultDate = resultDate;
        this.itemDetails = itemDetails;
        this.prescriptionMasterId = prescriptionMasterId;

        prescribedDateFormat(prescribeDate);
        resultDateFormat(resultDate);
    }

    private void prescribedDateFormat(LocalDate presDate){
        prescribedDateFor = presDate == null?"":presDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void resultDateFormat(LocalDateTime resultDateLocalDateTime){
        resultDateTimeFor =resultDateLocalDateTime == null?"": resultDateLocalDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")).replace("am", "AM").replace("pm","PM");
    }

    public String getPrescribedDateFor() {
        return prescribedDateFor;
    }

    public void setPrescribedDateFor(String prescribedDateFor) {
        this.prescribedDateFor = prescribedDateFor;
    }

    public String getResultDateTimeFor() {
        return resultDateTimeFor;
    }

    public void setResultDateTimeFor(String resultDateTimeFor) {
        this.resultDateTimeFor = resultDateTimeFor;
    }

    public int getPrescriptionMasterId() {
        return prescriptionMasterId;
    }

    public void setPrescriptionMasterId(int prescriptionMasterId) {
        this.prescriptionMasterId = prescriptionMasterId;
    }

    public ItemChooserModel getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(ItemChooserModel itemDetails) {
        this.itemDetails = itemDetails;
    }

    public int getInvestigationID() {
        return investigationID;
    }

    public void setInvestigationID(int investigationID) {
        this.investigationID = investigationID;
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
        prescribedDateFormat(prescribeDate);
        this.prescribeDate = prescribeDate;
    }

    public LocalDateTime getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDateTime resultDate) {
        resultDateFormat(resultDate);
        this.resultDate = resultDate;
    }
}
