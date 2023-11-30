package com.techwhizer.medicalshop.model;

public class FrequencyModel {
    String frequency;

    public FrequencyModel(String frequency) {
        this.frequency = frequency;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }


    @Override
    public String toString() {
        return getFrequency();
    }
}
