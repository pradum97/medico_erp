package com.techwhizer.medicalshop.controller.consultant;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.GenerateInvoice;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.ConsultantModel;
import com.techwhizer.medicalshop.model.ConsultationSetupModel;
import com.techwhizer.medicalshop.model.DoctorModel;
import com.techwhizer.medicalshop.model.PatientModel;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.type.DoctorType;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

enum Type {
    GET_PATIENT, GET_HISTORY, CREATE, UPDATE,PRINT
}

public class ConsultantForm implements Initializable {
    public Button submitBn;
    public Label patientNameL;
    public Label genderL;
    public Label ageL;
    public Label phoneNumL;
    public Label addressL;
    public Label guardianNameL;
    public ComboBox<DoctorModel> referByCom;
    public TextField referByName;
    public ComboBox<DoctorModel> consultNameCom;
    public ComboBox<String> paymentMethodCom;
    public TextField consultantFeeTf;
    public TextField referenceNumTf;
    public TextField remarkTf;


    public TextField searchNameTf;
    public TableView<PatientModel> tableViewPatient;
    public TableColumn<PatientModel, Integer> colPatientSr;
    public TableColumn<PatientModel, String> colAdmNum;
    public TableColumn<PatientModel, String> colPatientName;
    public Pagination pagination;
    public ComboBox<String> comReceiptType;
    public TextField consultRemarksTf;
    public TableView<ConsultantModel> tableViewHistory;
    public TableColumn<ConsultantModel,Integer> colHisSr;
    public TableColumn<ConsultantModel,String> colReceiptNum;
    public TableColumn<ConsultantModel,String> colReceiptDate;
    public TableColumn<ConsultantModel,String> colReferredBy;
    public TableColumn<ConsultantModel,String> colConsultantName;

    private CustomDialog customDialog;
    private FilteredList<PatientModel> filteredData;
    private Method method;

    static private int rowsPerPage = 20;

    private PatientModel patientModel;


    private ObservableList<PatientModel> patientList = FXCollections.observableArrayList();
    private ObservableList<ConsultantModel> consultList = FXCollections.observableArrayList();
    private ObservableList<String> receiptTypeList = FXCollections.observableArrayList("OPD", "IPD");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        tableViewHistory.setFixedCellSize(28.0);
        tableViewPatient.setFixedCellSize(28.0);

        callThread(Type.GET_PATIENT, null);
        config();
    }

    private void config() {
        referByCom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DoctorModel>() {
            @Override
            public void changed(ObservableValue<? extends DoctorModel> observableValue,
                                DoctorModel doctorModel, DoctorModel t1) {

                if (t1 != null) {
                    if (t1.getDoctorId() == 0) {
                        referByName.setText("Self");
                        referByName.setDisable(false);
                    } else {
                        referByName.setDisable(true);
                        referByName.setText("");
                    }
                }

            }
        });

        comReceiptType.setItems(receiptTypeList);
        comReceiptType.getSelectionModel().select("OPD");
        referByCom.getSelectionModel().select(0);
    }

    private void callThread(Type type, Map<String, Object> data) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(type, data);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void clearBnClick(ActionEvent actionEvent) {

        clearFiled();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private Type type;
        private Map<String, Object> data;

        public MyAsyncTask(Type type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public void onPreExecute() {

            if (type == Type.GET_PATIENT) {
                tableViewPatient.setPlaceholder(method.getProgressBarRed(40, 40));
            } else if (type == Type.CREATE) {
                if (data.get("button") != null) {
                    Button bn = (Button) data.get("button");
                    bn.setDisable(true);
                    bn.setGraphic(method.getProgressBarRed(30, 30));
                    bn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            } else if (type == Type.GET_HISTORY || type == Type.PRINT) {

                Hyperlink printBn = (Hyperlink) data.get("button");

                printBn.setGraphic(method.getProgressBarRed(18, 18));


            }

        }

        @Override
        public Boolean doInBackground(String... params) {
            if (type == Type.GET_PATIENT) {
                getPatient();
                referByCom.setItems(CommonUtil.getDoctor(DoctorType.OUT_SIDE));
                consultNameCom.setItems(CommonUtil.getDoctor(DoctorType.IN_HOUSE));
                paymentMethodCom.setItems(CommonUtil.getPaymentMethod());

                ConsultationSetupModel csm = CommonUtil.getConsultationSetup();
                Platform.runLater(()->{
                    consultantFeeTf.setText(csm == null?"": String.valueOf(csm.getConsultation_fee()));
                });


                Platform.runLater(() -> {
                    paymentMethodCom.getSelectionModel().select(0);
                    referByCom.getSelectionModel().select(0);
                });
            } else if (type == Type.CREATE) {

                insertConsultData(data);
            } else if (type == Type.GET_HISTORY) {
                int patientId = (int) data.get("patient_id");
             getConsultPatient(patientId);
            }else if (type == Type.PRINT){
                int patientId = (int) data.get("patient_id");
                int consultId = (int) data.get("consult_id");
                new GenerateInvoice().generatePaymentSlip(patientId,
                        consultId,"Consultant");
            }


            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {

              if (type == Type.GET_HISTORY || type == Type.PRINT) {

                Hyperlink printBn = (Hyperlink) data.get("button");
                printBn.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void insertConsultData(Map<String, Object> data) {

        int patient_id = (int) data.get("patient_id");
        int referredById = (int) data.get("referredById");
        String referredByName = (String) data.get("referredByName");
        int consultantById = (int) data.get("consultantById");
        String paymentMethod = (String) data.get("paymentMethod");
        String consultantFee = (String) data.get("consultantFee");

        String refNum = (String) data.get("refNum");
        String paymentReMarks = (String) data.get("paymentReMarks");
        String receiptType = comReceiptType.getSelectionModel().getSelectedItem();
        String consultRemarks = consultRemarksTf.getText();

        Button bn = (Button) data.get("button");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();
            connection.setAutoCommit(false);

            String insertConsultQuery = """
                    INSERT INTO patient_consultation(PATIENT_ID, REFERRED_BY_DOCTOR_ID, REFERRED_BY_NAME, CONSULTATION_DOCTOR_ID,
                                                     CREATED_BY,receipt_type,receipt_num,remarks)VALUES (?,?,?,?,?,?,?,?);
                    """;

            String receiptNum = new GenerateBillNumber()
                    .generatorConsultReceiptNumber(receiptType);

            ps = connection.prepareStatement(insertConsultQuery, new String[]{"consultation_id"});
            ps.setInt(1, patient_id);
            ps.setInt(2, referredById);
            ps.setString(3, referredByName);
            ps.setInt(4, consultantById);
            ps.setInt(5, Login.currentlyLogin_Id);
            ps.setString(6, receiptType);
            ps.setString(7, receiptNum);
            ps.setString(8, consultRemarks);

            int res = ps.executeUpdate();
            if (res > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int consultation_id = rs.getInt(1);
                    res = 0;
                    ps = null;
                    rs = null;

                    String insertPaymentQuery = """
                            INSERT INTO  payment_information(PURPOSE, DOCUMENT_SOURCE, DOCUMENT_ID,
                            AMOUNT, PAYMENT_METHOD,TRANSACTION_ID,REMARKS) values (?,?,?,?,?,?,?)
                            """;

                    ps = connection.prepareStatement(insertPaymentQuery);
                    ps.setString(1, "OPD CONSULTATION CHARGE");
                    ps.setString(2, "consultation_form");
                    ps.setInt(3, consultation_id);
                    ps.setBigDecimal(4, BigDecimal.valueOf(Double.parseDouble(consultantFee)));
                    ps.setString(5, paymentMethod);
                    ps.setString(6, refNum);
                    ps.setString(7, paymentReMarks);
                    res = ps.executeUpdate();

                    if (res > 0) {
                        connection.commit();
                        clearFiled();
                        getConsultPatient(patient_id);
                        customDialog.showAlertBox("Success", "Data Successfully Inserted.");
                    } else {
                        customDialog.showAlertBox("Failed", "Something went wrong");
                    }
                }

            }


        } catch (SQLException e) {
            customDialog.showAlertBox("Failed", "Something went wrong");
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> {
                if (bn != null) {
                    bn.setDisable(false);
                    bn.setGraphic(null);
                    bn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            });
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void clearFiled() {

        Platform.runLater(() -> {
            referByCom.getSelectionModel().clearSelection();
            consultNameCom.getSelectionModel().clearSelection();
        });

        referenceNumTf.setText("");
        remarkTf.setText("");
        referByName.setText("");
    }

    private void getPatient() {

        if (null != patientList) {
            patientList.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = new DBConnection().getConnection();

            String qry = """
                            select * from patient_v
                    order by patient_id  desc
                     """;

            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();


            while (rs.next()) {

                int patient_id = rs.getInt("PATIENT_ID");
                int salutation_id = rs.getInt("salutation_id");
                int created_by = rs.getInt("created_by");
                int last_update_by = rs.getInt("last_update_by");

                String salutation_name = rs.getString("salutation_name");
                String first_name = rs.getString("first_name");
                String middle_name = rs.getString("middle_name");
                String last_name = rs.getString("last_name");

                String fullName = rs.getString("fullName");

                String gender = rs.getString("gender");
                String age = rs.getString("age");
                String address = rs.getString("address");
                String dob = rs.getString("dob");
                String phone = rs.getString("phone");

                String idType = rs.getString("id_type");
                String idNum = rs.getString("id_number");
                String guardianName = rs.getString("guardian_name");

                String weight = rs.getString("weight");
                String bp = rs.getString("bp");
                String pulse = rs.getString("pulse");
                String sugar = rs.getString("sugar");
                String spo2 = rs.getString("SPO2");
                String temp = rs.getString("temp");
                String cvs = rs.getString("cvs");
                String cns = rs.getString("cns");
                String chest = rs.getString("chest");
                String creationDate = rs.getString("creation_date");
                String lastUpdate = rs.getString("last_update");
                String admission_number = rs.getString("admission_number");
                String uhidNum = rs.getString("uhid_no");

                PatientModel pm = new PatientModel(patient_id, salutation_id, created_by, last_update_by, salutation_name, first_name,
                        middle_name, last_name, fullName, gender, age, address, dob, phone, idType, idNum, guardianName, weight, bp, pulse,
                        sugar, spo2, temp, cvs, cns, chest, creationDate, lastUpdate, admission_number,uhidNum);
                patientList.add(pm);
            }
            if (null != patientList) {
                if (!patientList.isEmpty()) {
                    pagination.setVisible(true);
                    search_Item();
                }
            }

            Platform.runLater(() -> {

                if (!patientList.isEmpty()) {
                    tableViewPatient.setPlaceholder(new Label(""));
                } else {
                    tableViewPatient.setPlaceholder(new Label("Patient not available"));
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {

                tableViewPatient.setPlaceholder(new Label("An error occurred while fetching the item"));
            });

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(patientList, p -> true);

        searchNameTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(patient -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(patient.getPhone()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return patient.getFullName().toLowerCase().contains(lowerCaseFilter);
            });

            changeTableViewPatient(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableViewPatient(0, rowsPerPage);
        Platform.runLater(() -> {
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        changeTableViewPatient(newValue1.intValue(), rowsPerPage);
                    });
        });

    }

    private void changeTableViewPatient(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));
        colPatientSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableViewPatient.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, patientList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<PatientModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableViewPatient.comparatorProperty());

        tableViewPatient.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<PatientModel, String>, TableCell<PatientModel, String>>
                cellEdit = (TableColumn<PatientModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink admNumHl = new Hyperlink(tableViewPatient.getItems().get(getIndex()).getAdmissionNumber());

                    admNumHl.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 12;-fx-alignment: center-left");

                    admNumHl.setMinWidth(130);

                    admNumHl.setOnAction(actionEvent -> {
                        tableViewPatient.getSelectionModel().select(getIndex());
                        patientModel = tableViewPatient.getSelectionModel().getSelectedItem();
                        patientNameL.setText(patientModel.getFullName());
                        genderL.setText(patientModel.getGender());
                        ageL.setText(patientModel.getAge());
                        phoneNumL.setText(patientModel.getPhone());
                        addressL.setText(patientModel.getAddress());
                        guardianNameL.setText(patientModel.getGuardianName());
                        Map<String,Object> data = new HashMap<>();
                        data.put("patient_id",patientModel.getPatientId());
                        data.put("button",admNumHl);
                        callThread(Type.GET_HISTORY,data);
                    });
                    HBox managebtn = new HBox(admNumHl);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colAdmNum.setCellFactory(cellEdit);
    }

    public void submitBnClick(ActionEvent actionEvent) {

        if (patientModel == null) {

            customDialog.showAlertBox("", "Please select patient");
            return;
        }

        if (referByCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select referred by", referByCom);
            return;
        }


        String referredByName = referByName.getText();
        String consultantFee = consultantFeeTf.getText();
        String paymentMethod = paymentMethodCom.getSelectionModel().getSelectedItem();
        String refNum = referenceNumTf.getText();
        String paymentReMarks = remarkTf.getText();


        if (referByCom.getSelectionModel().getSelectedItem().getDoctorId() == 0) {
            if (referredByName.isEmpty()) {
                method.show_popup("Please enter referred by name ", referByName);
                return;
            }
        }

        if (consultNameCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select consult doctor", consultNameCom);
            return;
        } else if (consultantFee.isEmpty()) {
            method.show_popup("Please enter consultant fee", consultantFeeTf);
            return;
        }

        try {
            Double.parseDouble(consultantFee);
        } catch (Exception e) {
            method.show_popup("Please enter valid consultant fee in digit", consultantFeeTf);
            return;
        }

        int referredById = referByCom.getSelectionModel().getSelectedItem().getDoctorId();
        int consultantById = consultNameCom.getSelectionModel().getSelectedItem().getDoctorId();

        Map<String, Object> data = new HashMap<>();
        data.put("patient_id", patientModel.getPatientId());
        data.put("referredById", referredById);
        data.put("referredByName", referredByName);
        data.put("consultantById", consultantById);
        data.put("paymentMethod", paymentMethod);
        data.put("consultantFee", consultantFee);

        data.put("refNum", refNum);
        data.put("paymentReMarks", paymentReMarks);
        data.put("button", submitBn);
        callThread(Type.CREATE, data);
    }

    public void addPatient(ActionEvent event) {
        customDialog.showFxmlDialog2("patient/addPatient.fxml", "Add New Patient");

        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isSuccess = (Boolean) Main.primaryStage.getUserData();

            if(isSuccess){
                callThread(Type.GET_PATIENT, null);
            }
        }

    }

    private void getConsultPatient(int patientId) {
        if (null != consultList) {
            consultList.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();


            String qry = """                                  
                    select * from consultant_history_v
                       where patient_id = ? limit  10""";

            ps = connection.prepareStatement(qry);
            ps.setInt(1,patientId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int consultation_id = rs.getInt("consultation_id");
                int referred_by_doctor_id = rs.getInt("referred_by_doctor_id");
                int consultation_doctor_id = rs.getInt("consultation_doctor_id");
                int patient_id = rs.getInt("patient_id");

                String patient_name = rs.getString("patient_name");


                String receiptDate = rs.getString("consult_date");


                String referred_by_name = rs.getString("referred_by_name");
                String consult_name = rs.getString("consult_name");
                String consultant_status = rs.getString("consultant_status");
                String receipt_num = rs.getString("receipt_num");
                String receipt_type = rs.getString("receipt_type");
                String remarks = rs.getString("remarks");
                String description = rs.getString("description");
                String guardian_name = rs.getString("guardian_name");
                String age = rs.getString("age");
                String gender = rs.getString("gender");
                String address = rs.getString("address");

                ConsultantModel cm = new ConsultantModel(consultation_id, referred_by_doctor_id,patient_id, consultation_doctor_id, patient_name,
                        receiptDate, referred_by_name, consult_name, consultant_status,receipt_num,
                        receipt_type,remarks,description,guardian_name,gender,age,address);
                consultList.add(cm);
            }

            tableViewHistory.setItems(consultList);

            colHisSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableViewHistory.getItems().indexOf(cellData.getValue()) + 1));
            colReceiptDate.setCellValueFactory(new PropertyValueFactory<>("consult_date"));
            colReferredBy.setCellValueFactory(new PropertyValueFactory<>("referred_by_name"));
            colConsultantName.setCellValueFactory(new PropertyValueFactory<>("consult_name"));

            setOptionalCellHistory();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void setOptionalCellHistory() {

        Callback<TableColumn<ConsultantModel, String>, TableCell<ConsultantModel, String>>
                cellReceiptNum = (TableColumn<ConsultantModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink receiptNum = new Hyperlink(tableViewHistory.getItems().get(getIndex()).getReceiptNum());
                    receiptNum.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 12;-fx-alignment: center-left");

                    receiptNum.setMinWidth(130);
                    receiptNum.setOnAction(actionEvent -> {
                        tableViewHistory.getSelectionModel().select(getIndex());
                        ConsultantModel pm = tableViewHistory.getSelectionModel().getSelectedItem();

                        Map<String,Object> map = new HashMap<>();
                        map.put("patient_id",pm.getPatient_id());
                        map.put("consult_id",pm.getConsultation_id());
                        map.put("button",receiptNum);

                        callThread(Type.PRINT,map);

                    });

                    HBox managebtn = new HBox(receiptNum);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };
        colReceiptNum.setCellFactory(cellReceiptNum);
    }


}
