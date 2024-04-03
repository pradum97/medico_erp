package com.techwhizer.medicalshop.model;

import java.util.Objects;

public class DoctorModel {

    private int doctorId;
    private String drName;
    private String drPhone;
    private String drAddress;
    private String drRegNo;
    private String drSpeciality;
    private String qualification;
    private String createdDate,  doctorType;
    String drFullName;

    public DoctorModel(int doctorId, String drName, String drPhone,
                       String drAddress, String drRegNo, String drSpeciality, String qualification,
                       String createdDate,String doctorType) {
        this.doctorId = doctorId;
        this.drName = drName;
        this.drPhone = drPhone;
        this.drAddress = drAddress;
        this.drRegNo = drRegNo;
        this.drSpeciality = drSpeciality;
        this.qualification = qualification;
        this.createdDate = createdDate;
        this.doctorType = doctorType;
        drFullName = Objects.equals(drName, "SELF") ?drName:"Dr. "+drName;
    }

    public DoctorModel(int doctorId, String drName) {
        this.doctorId = doctorId;
        this.drFullName = drName;
    }

    @Override
    public String toString() {
        return drFullName;
    }

    public String getDrFullName() {
        return drFullName;
    }

    public String getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(String doctorType) {
        this.doctorType = doctorType;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDrName() {
        return drName;
    }

    public void setDrName(String drName) {
        this.drName = drName;
    }

    public String getDrPhone() {
        return drPhone;
    }

    public void setDrPhone(String drPhone) {
        this.drPhone = drPhone;
    }

    public String getDrAddress() {
        return drAddress;
    }

    public void setDrAddress(String drAddress) {
        this.drAddress = drAddress;
    }

    public String getDrRegNo() {
        return drRegNo;
    }

    public void setDrRegNo(String drRegNo) {
        this.drRegNo = drRegNo;
    }

    public String getDrSpeciality() {
        return drSpeciality;
    }

    public void setDrSpeciality(String drSpeciality) {
        this.drSpeciality = drSpeciality;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
