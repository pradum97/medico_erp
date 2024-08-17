package com.techwhizer.medicalshop.controller.master.model;

public class BedModel {
    private  int bedId;

    private  int buildingId;
    private  int floorId;
    private  int wardId;
    private int rowNumber;
    private int columnNumber;
    private String bedNumber;
    private String bedName;
    private String bedType;
    private String bedStatus;
    private String bedFor;
    private String createdDate;

    public BedModel(int bedId, int buildingId, int floorId, int wardId, int rowNumber, int columnNumber,
                    String bedNumber, String bedName, String bedType, String bedStatus, String bedFor,
                    String createdDate) {
        this.bedId = bedId;
        this.buildingId = buildingId;
        this.floorId = floorId;
        this.wardId = wardId;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.bedNumber = bedNumber;
        this.bedName = bedName;
        this.bedType = bedType;
        this.bedStatus = bedStatus;
        this.bedFor = bedFor;
        this.createdDate = createdDate;
    }

    public int getBedId() {
        return bedId;
    }

    public void setBedId(int bedId) {
        this.bedId = bedId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getFloorId() {
        return floorId;
    }

    public void setFloorId(int floorId) {
        this.floorId = floorId;
    }

    public int getWardId() {
        return wardId;
    }

    public void setWardId(int wardId) {
        this.wardId = wardId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getBedName() {
        return bedName;
    }

    public void setBedName(String bedName) {
        this.bedName = bedName;
    }

    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }

    public String getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(String bedStatus) {
        this.bedStatus = bedStatus;
    }

    public String getBedFor() {
        return bedFor;
    }

    public void setBedFor(String bedFor) {
        this.bedFor = bedFor;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
