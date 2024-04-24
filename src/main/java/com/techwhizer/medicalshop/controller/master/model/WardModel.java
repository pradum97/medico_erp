package com.techwhizer.medicalshop.controller.master.model;

public class WardModel {
    private int wardId;
    private int buildingId;
    private int floorId;
    private int facilityId;

    private String buildingName,floorName,facilityName;
    private String wardName;
    private int noOfBeds;
    private String createdDate;

    public WardModel(int wardId, int buildingId, int floorId, int facilityId, String buildingName,
                     String floorName, String facilityName, String wardName, int noOfBeds, String createdDate) {
        this.wardId = wardId;
        this.buildingId = buildingId;
        this.floorId = floorId;
        this.facilityId = facilityId;
        this.buildingName = buildingName;
        this.floorName = floorName;
        this.facilityName = facilityName;
        this.wardName = wardName;
        this.noOfBeds = noOfBeds;
        this.createdDate = createdDate;
    }

    public int getWardId() {
        return wardId;
    }

    public void setWardId(int wardId) {
        this.wardId = wardId;
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

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public int getNoOfBeds() {
        return noOfBeds;
    }

    public void setNoOfBeds(int noOfBeds) {
        this.noOfBeds = noOfBeds;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
