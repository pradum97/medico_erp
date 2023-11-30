package com.techwhizer.medicalshop.model;

public class MedicineTimeModel {

    String time;

    public MedicineTimeModel(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return getTime();
    }
}
