package com.techwhizer.medicalshop.controller.doctor;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.DoctorModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddDoctor implements Initializable {
    public TextField nameTf;
    public TextField phoneTf;
    public TextField addressTf;
    public TextField regNoTf;
    public TextField specialityTf;
    public TextField qualificationTf;
    public Button submitBn;
    public ComboBox<String> comDoctorType;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    private DoctorModel dm;
    ObservableList<String> doctorType = FXCollections.observableArrayList("IN HOUSE","OUT SIDE","OTHER");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        comDoctorType.setItems(doctorType);

        if (Main.primaryStage.getUserData() instanceof DoctorModel){
            dm = (DoctorModel) Main.primaryStage.getUserData();
            setDefaultValue();
            submitBn.setText("UPDATE");
        }
    }

    private void setDefaultValue() {
        nameTf.setText(dm.getDrName());
        phoneTf.setText(dm.getDrPhone());
        comDoctorType.getSelectionModel().select(dm.getDoctorType());
        addressTf.setText(dm.getDrAddress());
        regNoTf.setText(dm.getDrRegNo());
        specialityTf.setText(dm.getDrSpeciality());
        qualificationTf.setText(dm.getQualification());
    }

    public void cancelBn(ActionEvent event) {
        Stage stage = (Stage) submitBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void submitBn(ActionEvent event) {

        String name = nameTf.getText();
        String phone = phoneTf.getText();
        String address = addressTf.getText();
        String regNo = regNoTf.getText();
        String speciality = specialityTf.getText();
        String qlf = qualificationTf.getText();

        if (name.isEmpty()) {
            method.show_popup("Please Enter doctor name", nameTf, Side.RIGHT);
            return;
        }else if(comDoctorType.getSelectionModel().toString().isEmpty()){
            method.show_popup("Please select doctor type", comDoctorType, Side.RIGHT);
            return;
        }

        String docType = comDoctorType.getSelectionModel().getSelectedItem();

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dbConnection.getConnection();
            String query = "";

            if (dm == null){
                query = """
                    INSERT INTO TBL_DOCTOR(DR_NAME, DR_PHONE, DR_ADDRESS, DR_REG_NUM, SPECIALITY,
                     QUALIFICATION,DOCTOR_TYPE) VALUES (?,?,?,?,?,?,?)
                    """;

            }else {

                query = """
                    UPDATE TBL_DOCTOR SET DR_NAME = ?, DR_PHONE = ?, DR_ADDRESS = ?, DR_REG_NUM = ?, SPECIALITY = ?,
                     QUALIFICATION = ?,DOCTOR_TYPE = ? WHERE doctor_id = ?
                    """;
            }

            ps = connection.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, regNo);
            ps.setString(5, speciality);
            ps.setString(6, qlf);
            ps.setString(7, docType);

            if (dm != null){
                ps.setInt(8,dm.getDoctorId());
            }

            int res = ps.executeUpdate();

            if (res > 0) {
                if (dm == null){
                    customDialog.showAlertBox("Success", "Successfully created.");
                }else {

                    customDialog.showAlertBox("Success", "Successfully updated.");
                }

                cancelBn(null);
                nameTf.setText("");
                phoneTf.setText("");
                addressTf.setText("");
                specialityTf.setText("");
                regNoTf.setText("");
                qualificationTf.setText("");
            }

        } catch (SQLException e) {
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,null);
        }


    }

    public void keyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            submitBn(null);
        }
    }
}
