package com.techwhizer.medicalshop.model;

public class SalutationModel {

    private int salutationId;
    private String salutationName;

    public SalutationModel(int salutationId, String salutationName) {
        this.salutationId = salutationId;
        this.salutationName = salutationName;
    }

    @Override
    public String toString() {
        return salutationName;
    }

    public int getSalutationId() {
        return salutationId;
    }

    public void setSalutationId(int salutationId) {
        this.salutationId = salutationId;
    }

    public String getSalutationName() {
        return salutationName;
    }

    public void setSalutationName(String salutationName) {
        this.salutationName = salutationName;
    }
}
