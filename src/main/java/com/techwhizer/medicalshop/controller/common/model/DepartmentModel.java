package com.techwhizer.medicalshop.controller.common.model;

public class DepartmentModel {

    private int departmentId;
    private String departmentName;
    private String departmentCode;

    public DepartmentModel(int departmentId, String departmentName, String departmentCode) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
    }

    public DepartmentModel() {

    }

    @Override
    public String toString() {
        return departmentName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
}
