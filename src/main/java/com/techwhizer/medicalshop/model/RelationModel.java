package com.techwhizer.medicalshop.model;

public class RelationModel {
    private int relationID;
    private String relationCode;
    private String relationType;
    private String description;

    public RelationModel(int relationID, String relationCode, String relationType, String description) {
        this.relationID = relationID;
        this.relationCode = relationCode;
        this.relationType = relationType;
        this.description = description;
    }

    @Override
    public String toString() {
        return getRelationType();
    }

    public int getRelationID() {
        return relationID;
    }

    public void setRelationID(int relationID) {
        this.relationID = relationID;
    }

    public String getRelationCode() {
        return relationCode;
    }

    public void setRelationCode(String relationCode) {
        this.relationCode = relationCode;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
