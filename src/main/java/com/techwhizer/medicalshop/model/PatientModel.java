package com.techwhizer.medicalshop.model;

public class PatientModel {
    private int patientId,salutation_id,createdBy,lastUpdateBy;
    private String salutationName,firstName,middleName,lastName,fullName;
    private String gender;
    private String age;
    private String address;
    private String dateOfBirth;
    private String phone;

    private String idType,idNumber;

    private String guardianName;
    private String weight;
    private String bp;
    private String pulse;
    private String sugar;
    private String spo2;
    private String temp;
    private String cvs;
    private String cns;
    private String chest;
    private String creationDate,lastDateUpdate, admissionNumber,uhidNum;

    public PatientModel(int patientId, int salutation_id, int createdBy, int lastUpdateBy, String salutationName,String firstName, String middleName,
                        String lastName,String fullName, String gender, String age, String address, String dateOfBirth,
                        String phone, String idType, String idNumber, String guardianName,  String weight, String bp, String pulse,
                        String sugar, String spo2, String temp, String cvs, String cns, String chest, String creationDate,
                        String lastDateUpdate,String admissionNumber,String uhidNum) {
        this.patientId = patientId;
        this.salutation_id = salutation_id;
        this.createdBy = createdBy;
        this.lastUpdateBy = lastUpdateBy;
        this.salutationName = salutationName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.idType = idType;
        this.idNumber = idNumber;
        this.guardianName = guardianName;
        this.weight = weight;
        this.bp = bp;
        this.pulse = pulse;
        this.sugar = sugar;
        this.spo2 = spo2;
        this.temp = temp;
        this.cvs = cvs;
        this.cns = cns;
        this.chest = chest;
        this.creationDate = creationDate;
        this.lastDateUpdate = lastDateUpdate;
        this.admissionNumber = admissionNumber;
        this.uhidNum = uhidNum;
    }

    public String getUhidNum() {
        return uhidNum;
    }

    public void setUhidNum(String uhidNum) {
        this.uhidNum = uhidNum;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSalutationName() {
        return salutationName;
    }

    public void setSalutationName(String salutationName) {
        this.salutationName = salutationName;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getSalutation_id() {
        return salutation_id;
    }

    public void setSalutation_id(int salutation_id) {
        this.salutation_id = salutation_id;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(int lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

       public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getSugar() {
        return sugar;
    }

    public void setSugar(String sugar) {
        this.sugar = sugar;
    }

    public String getSpo2() {
        return spo2;
    }

    public void setSpo2(String spo2) {
        this.spo2 = spo2;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getCvs() {
        return cvs;
    }

    public void setCvs(String cvs) {
        this.cvs = cvs;
    }

    public String getCns() {
        return cns;
    }

    public void setCns(String cns) {
        this.cns = cns;
    }

    public String getChest() {
        return chest;
    }

    public void setChest(String chest) {
        this.chest = chest;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastDateUpdate() {
        return lastDateUpdate;
    }

    public void setLastDateUpdate(String lastDateUpdate) {
        this.lastDateUpdate = lastDateUpdate;
    }
}
