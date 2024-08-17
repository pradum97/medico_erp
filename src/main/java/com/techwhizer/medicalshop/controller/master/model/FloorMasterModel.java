package com.techwhizer.medicalshop.controller.master.model;

public class FloorMasterModel {
    private int floorMasterId;
    private String floorNumber,floorName;
    private int buildingId;
    private String createdDate,buildingName;

    public FloorMasterModel(int floorMasterId, String floorNumber, String floorName,
                            int buildingId, String createdDate, String buildingName) {
        this.floorMasterId = floorMasterId;
        this.floorNumber = floorNumber;
        this.floorName = floorName;
        this.buildingId = buildingId;
        this.createdDate = createdDate;
        this.buildingName = buildingName;
    }

    @Override
    public String toString() {
        return getFloorName()+" ( "+getFloorNumber()+" ) ";
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public int getFloorMasterId() {
        return floorMasterId;
    }

    public void setFloorMasterId(int floorMasterId) {
        this.floorMasterId = floorMasterId;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
