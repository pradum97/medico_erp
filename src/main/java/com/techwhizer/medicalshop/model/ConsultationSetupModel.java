package com.techwhizer.medicalshop.model;

public class ConsultationSetupModel {

 private int  consultation_setup_ID ;
    private double  consultation_fee ;
    private int fee_valid_days ;
    private int CREATED_BY ;
    private String CREATION_DATE ;

    public ConsultationSetupModel(int consultation_setup_ID, double consultation_fee,
                                  int fee_valid_days, int CREATED_BY, String CREATION_DATE) {
        this.consultation_setup_ID = consultation_setup_ID;
        this.consultation_fee = consultation_fee;
        this.fee_valid_days = fee_valid_days;
        this.CREATED_BY = CREATED_BY;
        this.CREATION_DATE = CREATION_DATE;
    }

    public int getConsultation_setup_ID() {
        return consultation_setup_ID;
    }

    public void setConsultation_setup_ID(int consultation_setup_ID) {
        this.consultation_setup_ID = consultation_setup_ID;
    }

    public double getConsultation_fee() {
        return consultation_fee;
    }

    public void setConsultation_fee(double consultation_fee) {
        this.consultation_fee = consultation_fee;
    }

    public int getFee_valid_days() {
        return fee_valid_days;
    }

    public void setFee_valid_days(int fee_valid_days) {
        this.fee_valid_days = fee_valid_days;
    }

    public int getCREATED_BY() {
        return CREATED_BY;
    }

    public void setCREATED_BY(int CREATED_BY) {
        this.CREATED_BY = CREATED_BY;
    }

    public String getCREATION_DATE() {
        return CREATION_DATE;
    }

    public void setCREATION_DATE(String CREATION_DATE) {
        this.CREATION_DATE = CREATION_DATE;
    }
}
