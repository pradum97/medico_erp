package com.techwhizer.medicalshop.controller.master.model;

public class WardFacilityMasterModel {

    private int wardFacilityId;
    private String facilityCode,facilityName;
    private String createdDate;

    public WardFacilityMasterModel(int wardFacilityId, String facilityCode, String facilityName, String createdDate) {
        this.wardFacilityId = wardFacilityId;
        this.facilityCode = facilityCode;
        this.facilityName = facilityName;
        this.createdDate = createdDate;
    }

    public int getWardFacilityId() {
        return wardFacilityId;
    }

    public void setWardFacilityId(int wardFacilityId) {
        this.wardFacilityId = wardFacilityId;
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
