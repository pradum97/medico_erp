package com.techwhizer.medicalshop.controller.master.model;

public class FacilityModel {
    private int facilityId;
    private String facilityCode,facilityName;
    private String createdDate;

    public FacilityModel(int facilityId, String facilityCode, String facilityName, String createdDate) {
        this.facilityId = facilityId;
        this.facilityCode = facilityCode;
        this.facilityName = facilityName;
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return getFacilityName();
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityCode() {
        return facilityCode;
    }

    public void setFacilityCode(String facilityCode) {
        this.facilityCode = facilityCode;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
