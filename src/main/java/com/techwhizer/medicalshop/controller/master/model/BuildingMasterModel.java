package com.techwhizer.medicalshop.controller.master.model;

public class BuildingMasterModel {
    private int buildingMasterId;
    private String buildingNumber;
    private String buildingName;
    private String buildingAddress;
    private String buildingCreatedDate;

    public BuildingMasterModel(int buildingMasterId, String buildingNumber, String buildingName,
                               String buildingAddress, String buildingCreatedDate) {
        this.buildingMasterId = buildingMasterId;
        this.buildingNumber = buildingNumber;
        this.buildingName = buildingName;
        this.buildingAddress = buildingAddress;
        this.buildingCreatedDate = buildingCreatedDate;
    }

    @Override
    public String toString() {
        return getBuildingName()+"( "+getBuildingNumber()+" )";
    }

    public int getBuildingMasterId() {
        return buildingMasterId;
    }

    public void setBuildingMasterId(int buildingMasterId) {
        this.buildingMasterId = buildingMasterId;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingAddress() {
        return buildingAddress;
    }

    public void setBuildingAddress(String buildingAddress) {
        this.buildingAddress = buildingAddress;
    }

    public String getBuildingCreatedDate() {
        return buildingCreatedDate;
    }

    public void setBuildingCreatedDate(String buildingCreatedDate) {
        this.buildingCreatedDate = buildingCreatedDate;
    }
}
