package com.techwhizer.medicalshop.controller.patient;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.model.PatientModel;
import com.techwhizer.medicalshop.model.SalutationModel;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddPatient implements Initializable {
    public TextField phoneTf;
    public TextField addressTf;
    public TextField idNumberTf;
    public ComboBox<String> genderCom;
    public TextField ageTf;
    public TextField weightTf;
    public TextField bpTf;
    public TextField pulseTf;
    public TextField sugarTf;
    public TextField spo2Tf;
    public TextField tempTf;
    public TextField cvsTf;
    public TextField cnsTf;
    public TextField chestTf;
    public ComboBox<SalutationModel> salutationCom;
    public TextField firstNameTf;
    public TextField middleNameTf;
    public TextField lastNameTf;
    public DatePicker dobDb;
    public ComboBox<String> idTypeCom;
    public TextField guardianNameTf;
    public ProgressIndicator progressBar;
    public Button submitBn;
    private Method method;
    private CustomDialog customDialog;
    private   PatientModel pm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        method.hideElement(progressBar);
        method.convertDateFormat_2(dobDb);
        callTask("START",null);
        config();
    }

    private void config() {

        ageTf.textProperty().addListener((observable, oldValue, newValue) -> {

            try {
                dobDb.setValue(CommonUtil.calculateDOBFromAge(Integer.parseInt(newValue.isEmpty()?"0":newValue)));
            }catch (Exception e){
                dobDb.setValue(null);
                method.show_popup("Please enter valid age",ageTf, Side.RIGHT);
            }
        });
    }

    private void setData() {

        Platform.runLater(()->{

            submitBn.setText("UPDATE");

            genderCom.getSelectionModel().select((pm.getGender()));
            idTypeCom.getSelectionModel().select((pm.getIdType()));

        });

        firstNameTf.setText(pm.getFirstName());
        middleNameTf.setText(pm.getMiddleName());
        lastNameTf.setText(pm.getLastName());
        ageTf.setText(pm.getAge());
        addressTf.setText(pm.getAddress());
        phoneTf.setText(pm.getPhone());
        idNumberTf.setText(pm.getIdNumber());
        guardianNameTf.setText(pm.getGuardianName());
        weightTf.setText(pm.getWeight());
        bpTf.setText(pm.getBp());
        pulseTf.setText(pm.getPulse());
        sugarTf.setText(pm.getSugar());
        spo2Tf.setText(pm.getSpo2());
        tempTf.setText(pm.getTemp());
        cvsTf.setText(pm.getCvs());
        cnsTf.setText(pm.getCns());
        chestTf.setText(pm.getChest());
    }

    public void enterPress(KeyEvent ev) {
        if (ev.getCode() == KeyCode.ENTER) {
           addNewPatient();
        }
    }

    public void submit_bn(ActionEvent event) {
       addNewPatient();
    }

    private void closeStage() {
        Stage stage = (Stage) firstNameTf.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
          Platform.runLater(stage::close);
        }
    }

    private void addNewPatient() {
        Pattern pattern = Pattern.compile("^\\d{10}$");

        String firstName = firstNameTf.getText();
        String middleName = middleNameTf.getText();
        String lastName = lastNameTf.getText();
        String gender = genderCom.getSelectionModel().getSelectedItem();
        String age = ageTf.getText();
        String address = addressTf.getText();
        LocalDate dob = dobDb.getValue();



        String phone = phoneTf.getText();
        String idType = idTypeCom.getSelectionModel().getSelectedItem();
        String idNumber = idNumberTf.getText();
        String guardianName = guardianNameTf.getText();
        String weight = weightTf.getText();
        String bp = bpTf.getText();
        String pulse = pulseTf.getText();
        String sugar = sugarTf.getText();
        String spo2 = spo2Tf.getText();
        String temp  = tempTf.getText();
        String cvs = cvsTf.getText();
        String cns = cnsTf.getText();
        String chest = chestTf.getText();

        if (salutationCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please salutation", salutationCom, Side.RIGHT);
            return;
        }else
        if (firstName.isEmpty()) {
            method.show_popup("Please enter patient first name", firstNameTf, Side.RIGHT);
            return;
        }else if (genderCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select gender", genderCom, Side.RIGHT);
            return;
        } else if (address.isEmpty()) {
            method.show_popup("Please enter patient address", addressTf, Side.RIGHT);
            return;
        }else if (null == dob || age.isEmpty()) {
            method.show_popup("Please enter AGE OR DOB", ageTf, Side.RIGHT);
            return;
        }

        try {
            Integer.parseInt(age);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid age",ageTf, Side.RIGHT);
            return;
        }

        if (!phone.isEmpty()) {
            if (!pattern.matcher(phone).find()) {
                method.show_popup("Enter 10-digit phone number", phoneTf, Side.RIGHT);
                return;
            }
        }

        if (!idNumber.isEmpty()) {

            if (idType == null || idType.isEmpty()) {
                method.show_popup("Please select id type", idTypeCom, Side.RIGHT);
            }

        }
        int salutationId = salutationCom.getSelectionModel()
                .getSelectedItem().getSalutationId();

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure you want to submit.");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {

            PatientInsertUpdateModel pium = new  PatientInsertUpdateModel(salutationId,firstName, middleName, lastName, gender, age, address, dob,
                    phone, idType, idNumber, guardianName, weight, bp, pulse, sugar,
                    spo2, temp, cvs, cns, chest);

            callTask("ADDING_UPDATING",pium);



        } else {
            alert.close();
        }
    }

    private void patientInsertUpdate(PatientInsertUpdateModel pium){

        Connection connection = null;
        PreparedStatement ps = null;

        try {

            method.hideElement(submitBn);
            progressBar.setVisible(true);

            connection = new DBConnection().getConnection();

            String qry = "";

            if (null == pm){

                qry = """

                            INSERT INTO tbl_patient(
                                 salutation_id, first_name, middle_name, last_name, gender, address, dob, phone, id_type, id_number,
                                  guardian_name, weight, bp, pulse, sugar, spo2, temp, cvs, cns, chest, created_by, last_update, last_update_by,
                                  patient_number,UHID_NO)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?,?,?)
                            """;
            }else {

                qry = """
                                                        
                            UPDATE tbl_patient
                            SET salutation_id=?, first_name=?, middle_name=?, last_name=?, gender=?, address=?, dob=?,
                                phone=?, id_type=?, id_number=?, guardian_name=?, weight=?, bp=?, pulse=?, sugar=?, spo2=?, temp=?, cvs=?, cns=?, chest=?,\s
                                last_update_by=?, last_update=?
                            WHERE patient_id = ?;
                            """;

            }

            ps = connection.prepareStatement(qry);
            ps.setInt(1, pium.getSalutation_id());
            ps.setString(2, pium.getFirstName());
            ps.setString(3, pium.getMiddleName());
            ps.setString(4, pium.getLastName());
            ps.setString(5, pium.getGender());
            ps.setString(6, pium.getAddress());
            ps.setDate(7, Date.valueOf(pium.getDob()));
            ps.setString(8, pium.getPhone());
            ps.setString(9, pium.getIdType());
            ps.setString(10, pium.getIdNumber());
            ps.setString(11, pium.getGuardianName());
            ps.setString(12, pium.getWeight());
            ps.setString(13, pium.getBp());
            ps.setString(14, pium.getPulse());
            ps.setString(15, pium.getSugar());
            ps.setString(16, pium.getSpo2());
            ps.setString(17, pium.getTemp());
            ps.setString(18, pium.getCvs());
            ps.setString(19, pium.getCns());
            ps.setString(20, pium.getChest());
            ps.setInt(21, Login.currentlyLogin_Id);

            Calendar cal = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
            ps.setTimestamp(22, timestamp);
            if (null == pm) {
                ps.setInt(23, Login.currentlyLogin_Id);
                ps.setString(24,new  GenerateBillNumber().generatorPatientNumber());
                ps.setString(25,new  GenerateBillNumber().generateUHIDNum());
            } else {
                ps.setInt(23, pm.getPatientId());
            }
            int res = ps.executeUpdate();
            if (res > 0) {

                if(null != pm){
                    customDialog.showAlertBox("","Patient successfully updated.");
                }else {
                    customDialog.showAlertBox("","Patient successfully created.");
                }

                Main.primaryStage.setUserData(true);
                closeStage();
            }
        } catch (SQLException e) {
            customDialog.showAlertBox("","An error occurred during patient registration");
        } finally {
            method.hideElement(progressBar);
            submitBn.setVisible(true);
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    public void cancelBn(ActionEvent event) {
        closeStage();
    }


    private void callTask(String type,PatientInsertUpdateModel pium) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(type,pium);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String type;
        private PatientInsertUpdateModel pium;

        public MyAsyncTask(String type, PatientInsertUpdateModel pium) {
            this.type = type;
            this.pium = pium;
        }

        public MyAsyncTask(String type) {
            this.type = type;
        }

        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type) {

                case "START" -> init();
                case "ADDING_UPDATING" ->patientInsertUpdate(pium);
            }

            return false;

        }

        private void init() {

            genderCom.setItems(new StaticData().getGender());
            idTypeCom.setItems(CommonUtil.getDocumentType());

            if (Main.primaryStage.getUserData() instanceof PatientModel) {
                pm = (PatientModel) Main.primaryStage.getUserData();
                if (null != pm) {
                    setData();
                }
            }

            ObservableList<SalutationModel> salutationList =(CommonUtil.getSalutation(0));
            salutationCom.setItems(salutationList);

            if (null != pm) {

                SalutationModel sm = CommonUtil.getSalutation(pm.getSalutation_id()).get(0);
                for (int i = 0; i < salutationList.size(); i++) {
                    if (salutationList.get(i).getSalutationName().equals(sm.getSalutationName())){
                        int finalI = i;
                        Platform.runLater(() -> salutationCom.getSelectionModel().select(finalI));
                    }
                }
            }

        }

        @Override
        public void onPostExecute(Boolean success) {

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }
}
 class PatientInsertUpdateModel {
    private int salutation_id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String age;
    private String address;
    private LocalDate dob;
    private String phone;
    private String idType;
    private String idNumber;
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

    // Constructor
    public PatientInsertUpdateModel(int salutation_id,String firstName, String middleName, String lastName, String gender,
                   String age, String address, LocalDate dob, String phone, String idType,
                   String idNumber, String guardianName, String weight, String bp,
                   String pulse, String sugar, String spo2, String temp, String cvs,
                   String cns, String chest) {
        this.salutation_id = salutation_id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.dob = dob;
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
    }


     public int getSalutation_id() {
         return salutation_id;
     }

     public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getPhone() {
        return phone;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getWeight() {
        return weight;
    }

    public String getBp() {
        return bp;
    }

    public String getPulse() {
        return pulse;
    }

    public String getSugar() {
        return sugar;
    }

    public String getSpo2() {
        return spo2;
    }

    public String getTemp() {
        return temp;
    }

    public String getCvs() {
        return cvs;
    }

    public String getCns() {
        return cns;
    }

    public String getChest() {
        return chest;
    }
}

