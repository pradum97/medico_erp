package com.techwhizer.medicalshop.controller.patient.admission;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.common.model.DepartmentModel;
import com.techwhizer.medicalshop.controller.master.bed.BedMaster;
import com.techwhizer.medicalshop.controller.master.model.BedModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.model.DoctorModel;
import com.techwhizer.medicalshop.model.PatientModel;
import com.techwhizer.medicalshop.model.RelationModel;
import com.techwhizer.medicalshop.model.chooserModel.StockItemChooserModel;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.type.DoctorType;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdmissionForm implements Initializable {
    public ComboBox<Integer> hourCB;
    public ComboBox<Integer> minuteCB;
    public Label admissionNumL;
    public ComboBox<String> amPmCB;
    public ComboBox<String> filterTypeCom;
    public Label guardianNameL;
    public Label patientNameL;
    public Label ageGenderL;
    public Label registrationNumL;
    public Label patientAddressL;
    public DatePicker admissionDateDT;
    public ComboBox<DoctorModel> referByCom;
    public ComboBox<DepartmentModel> departmentCom;
    public ComboBox<DoctorModel> consultNameCom;
    public ComboBox<String> caseCom;
    public ComboBox<RelationModel> dependentRelationCom;
    public TextField dependentNameTF;
    public TextField attendedAddressTF;
    public Label facilityL;
    public Label floorL;
    public Label roomL;
    public Label bedNumL;
    public Button selectBedBn;
    public TextField reasonForAdmissionTF;
    public TextField patientConditionTF;
    public TextField remarkTF;
    public TextField allergiesTF;
    public ComboBox<RelationModel> attendedRelationCom;
    public TextField attendedNameTF;
    public TextField attendedMobileNumTF;
    public TextField referenceNumTF;
    public VBox admissionDetailsContainer;
    public HBox errorMesageContainer;
    public Label errorMessageL;
    private Method method;
    private CustomDialog customDialog;
    private ObjectProperty<Integer> selectedPatientId;
    private ObjectProperty<BedModel> selectedBedDetails;
    private ObjectProperty<StringBuilder> validationMessage;

    private enum OperationType {
        INIT, SEARCH_PATIENT, RESET_PATIENT
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        initDateTime();
        callThread(OperationType.INIT, null);
        admissionDetailsContainer.setDisable(true);


        method.hideElement(errorMesageContainer);
        validationMessage = new SimpleObjectProperty<>(new StringBuilder());
        selectedBedDetails = new SimpleObjectProperty<>(null);
        selectedPatientId = new SimpleObjectProperty<>(0);
        selectedPatientId.addListener((observableValue, integer, newVal) -> {
            admissionDetailsContainer.setDisable(!validationMessage.getValue().toString().isEmpty() && newVal < 1);
        });

        validationMessage.addListener((observableValue, integer, newVal) -> {

            String errorMsg = validationMessage.getValue().toString();

            System.out.println("validationMessage-tt " + errorMsg);


            if (newVal != null && !errorMsg.isEmpty()) {
                errorMesageContainer.setVisible(true);
                errorMessageL.setText(errorMsg);
            } else {
                method.hideElement(errorMesageContainer);
                errorMessageL.setText("");
            }

        });
    }

    private void callThread(OperationType operationType, Map<String, Object> data) {
        new MyAsyncTask(operationType, data).execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        OperationType operationType;
        Map<String, Object> data;

        public MyAsyncTask(OperationType operationType, Map<String, Object> data) {
            this.operationType = operationType;
            this.data = data;
        }

        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (operationType) {
                case INIT -> bind();
                case SEARCH_PATIENT -> {
                    if (data != null && data.get("registration_num") != null) {
                        String registrationNum = (String) data.get("registration_num");
                        getPatientDetail(registrationNum);
                    }
                }
                case RESET_PATIENT -> {

                    if (data != null && data.get("clear_type") != null) {
                        String clearType = (String) data.get("clear_type");
                        resetPatientDetail(clearType);
                    }
                }
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void bind() {

        amPmCB.setItems(StaticData.getPmAm());
        referByCom.setItems(CommonUtil.getDoctor(DoctorType.OUT_SIDE));
        departmentCom.setItems(CommonUtil.getDepartmentsList());
        consultNameCom.setItems(CommonUtil.getDoctor(DoctorType.IN_HOUSE));
        caseCom.setItems(StaticData.getCaseType());
        filterTypeCom.setItems(StaticData.getIpdOpdFilterType());
        ObservableList<RelationModel> relationList = CommonUtil.getRelations(0);
        attendedRelationCom.setItems(relationList);
        dependentRelationCom.setItems(relationList);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                filterTypeCom.getSelectionModel().select("Registration");
                caseCom.getSelectionModel().select("General");
            }
        });
    }

    public void initDateTime() {
        for (int i = 1; i <= 12; i++) {
            hourCB.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            minuteCB.getItems().add(i);
        }
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        String amPm = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";
        hourCB.getSelectionModel().select(hour);
        minuteCB.getSelectionModel().select(minutes);
        amPmCB.getSelectionModel().select(amPm);
    }

    public void updatePatientDetails(ActionEvent actionEvent) {


    }


    public void searchPatient(ActionEvent actionEvent) {

        String referenceNum = referenceNumTF.getText();

        if (filterTypeCom.getSelectionModel().getSelectedItem().isEmpty()) {
            method.show_popup("Please select filter type", filterTypeCom, Side.RIGHT);
            return;
        }
        String filterType = filterTypeCom.getSelectionModel().getSelectedItem();

        if (referenceNum.isEmpty()) {
            method.show_popup("Please " + filterType + " Number.", referenceNumTF, Side.BOTTOM);
            return;
        }

        fetchPatientIpdOpd(filterType, referenceNum);
    }

    private void fetchPatientIpdOpd(String flag, String referenceNum) {

        Map<String, Object> data = new HashMap<>();
        if (Objects.equals(flag, "Registration")) {
            data.put("registration_num", referenceNum);
        } else if (Objects.equals(flag, "Admission")) {
            // data.put("registration_num", referenceNum); // TODO Please Find uhid num from admission num;
        }

        callThread(OperationType.SEARCH_PATIENT, data);
    }

    private void getPatientDetail(String registrationNum) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = """
                    select * from patient_v where  uhid_no = ?""";

            ps = connection.prepareStatement(qry);
            ps.setString(1, registrationNum);
            rs = ps.executeQuery();

            if (rs.next()) {

                Platform.runLater(() -> validationMessage.set(new StringBuilder()));
                int patientId = rs.getInt("patient_id");
                String fullName = rs.getString("fullName");
                String gender = rs.getString("gender");
                String age = rs.getString("age");
                String address = rs.getString("address");
                String guardianName = rs.getString("guardian_name");
                String uhidNo = rs.getString("uhid_no");

                boolean isValid = true;

                StringBuilder sb = new StringBuilder();

                // validationMessage.getValue().append("The following details are missing; Please update.");
                if (gender == null || gender.isEmpty()) {
                    sb.append("Gender cannot be empty.").append(", ");
                    isValid = false;
                }
                if (age == null || age.isEmpty()) {
                    sb.append("Age cannot be empty").append(",  ");
                    isValid = false;
                }
                if (address == null || address.isEmpty()) {
                    sb.append("Address cannot be empty").append(",  ");
                    isValid = false;
                }
                if (guardianName == null || guardianName.isEmpty()) {
                    sb.append("S/O D/O W/O cannot be empty").append(",  ");
                    isValid = false;
                }


                System.out.println("validationMessage-tt " + validationMessage.getValue());

                Platform.runLater(() -> {
                    validationMessage.setValue(sb);
                    selectedPatientId.setValue(patientId);
                    ageGenderL.setText((age != null ? age + "/" : "") + gender);
                    guardianNameL.setText(guardianName);
                    patientNameL.setText(fullName);
                    patientAddressL.setText(address);
                    registrationNumL.setText(uhidNo);
                });

            } else {
                Platform.runLater(() -> method.show_popup("Patient Not Found.", referenceNumTF, Side.BOTTOM));
                callThread(OperationType.RESET_PATIENT, null);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    public void selectPatient(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("chooser/patientChooser.fxml", "Select Patient");
        if (Main.primaryStage.getUserData() instanceof PatientModel patientModel) {
            if (patientModel != null && patientModel.getUhidNum() != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("registration_num", patientModel.getUhidNum());
                callThread(OperationType.SEARCH_PATIENT, data);
            }

        }
    }

    public void selectBedOnClick(ActionEvent actionEvent) {

    }

    public void clearPatient(ActionEvent actionEvent) {

        referenceNumTF.setText("");
        Map<String, Object> data = new HashMap<>();
        data.put("clear_type", "ALL");
        callThread(OperationType.RESET_PATIENT, data);
    }

    public void resetAdmissionDetail(ActionEvent actionEvent) {

        Map<String, Object> data = new HashMap<>();
        data.put("clear_type", "ADMISSION");
        callThread(OperationType.RESET_PATIENT, data);
    }

    public void submitDetails(ActionEvent actionEvent) {

    }

    private void resetPatientDetail(String clearType) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if (Objects.equals(clearType, "ALL")) {
                    clearRegDetails();
                    clearAdmDetails();
                } else if (Objects.equals(clearType, "ADMISSION")) {
                    clearAdmDetails();
                }
            }
        });
    }

    private void clearRegDetails() {
        ageGenderL.setText("");
        guardianNameL.setText("");
        patientNameL.setText("");
        patientAddressL.setText("");
        registrationNumL.setText("");
        selectedPatientId.setValue(0);
        validationMessage.set(new StringBuilder());
    }

    private void clearAdmDetails() {
        admissionDateDT.setValue(LocalDate.now());
        validationMessage.set(new StringBuilder());
        initDateTime();
        referByCom.getSelectionModel().clearSelection();
        departmentCom.getSelectionModel().clearSelection();
        consultNameCom.getSelectionModel().clearSelection();
        caseCom.getSelectionModel().clearSelection();
        dependentRelationCom.getSelectionModel().clearSelection();
        dependentNameTF.setText("");
        attendedRelationCom.getSelectionModel().clearSelection();
        attendedNameTF.setText("");
        attendedMobileNumTF.setText("");
        attendedAddressTF.setText("");
        selectedBedDetails.setValue(null);
        reasonForAdmissionTF.setText("");
        patientConditionTF.setText("");
        allergiesTF.setText("");
        remarkTF.setText("");
    }
}
