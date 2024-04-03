package com.techwhizer.medicalshop.controller.consultant;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GenerateInvoice;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.ConsultantModel;
import com.techwhizer.medicalshop.util.DBConnection;
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
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class ConsultantList implements Initializable {

    public ComboBox<String> comStatus;
    public TextField searchTf;
    public Pagination pagination;
    public TableColumn<ConsultantModel, Integer> colSrNum;
    public TableColumn<ConsultantModel, String> colPatientName;
    public TableColumn<ConsultantModel, String> colConsultDate;
    public TableColumn<ConsultantModel, String> colReferredBy;
    public TableColumn<ConsultantModel, String> colConsultDoctorName;
    public TableColumn<ConsultantModel, String> colStatus;
    public TableColumn<ConsultantModel, String> colAction;
    public TableColumn<ConsultantModel, String> colReceiptNum;
    public TableColumn<ConsultantModel, String> colRemarks;
    public TableView<ConsultantModel> tableview;

    private ObservableList<ConsultantModel> consultList = FXCollections.observableArrayList();
    private ObservableList<String> consultStatusList = FXCollections.observableArrayList("All","Pending","Done","Cancelled");
    private FilteredList<ConsultantModel> filteredData;
    static private int rowsPerPage = 20;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        config();
    }

    private void config() {
        tableview.setFixedCellSize(30.0);
        comStatus.setItems(consultStatusList);
        comStatus.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue,
                                String String, String t1) {
                if (t1 != null){
                    Map<String, Object> data = new HashMap<>();
                    data.put("status",t1);
                    callThread("GET_CONSULT",data);
                }

            }
        });

        comStatus.getSelectionModel().select(0);


    }

    private void callThread(String type, Map<String, Object> data){

        MyAsyncTask myAsyncTask = new MyAsyncTask(type,data);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String type;
        private Map<String, Object> data;

        public MyAsyncTask(String type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public void onPreExecute() {

            switch (type){

                case "GET_CONSULT"->{
                    tableview.setPlaceholder( new Method().getProgressBarRed(35,35));
                }
                case "PRINT"->{

                    ProgressIndicator pi = new Method().getProgressBarRed(18,18);
                    Hyperlink printBn = (Hyperlink) data.get("button");
                    printBn.setGraphic(pi);
                }

            }

        }

        @Override
        public Boolean doInBackground(String... params) {
            switch (type){

                case "GET_CONSULT"->{

                    getConsultPatient((String) data.get("status").toString());
                }
                case "PRINT"->{

                    int patientId = (int) data.get("patient_id");
                    int consultId = (int) data.get("consult_id");
                    new GenerateInvoice().generatePaymentSlip(patientId,
                            consultId,"Consultant");
                }

            }

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            switch (type){

                case "GET_CONSULT"->{
                    tableview.setPlaceholder( new Label("Not Available"));


                }
                case "PRINT"->{

                    Hyperlink printBn = (Hyperlink) data.get("button");
                   printBn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                }

            }


        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getConsultPatient(String status) {
        if (null != consultList) {
            consultList.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String consultDoctorId = "consultation_doctor_id";

            if (Login.currentRoleName.equalsIgnoreCase("doctor")) {
                consultDoctorId = String.valueOf(Login.currentlyLogin_Id);

                if (null == status || status.isEmpty()) {
                    status = "Pending";
                }

            }

            String whereCondition = "";

            if (null != status && !status.equalsIgnoreCase("all")) {
                whereCondition = " and consultant_status = '"+status+"' ";
            }

            String qry = """
                                       
                    select * from consultant_history_v
                       where consultation_doctor_id ="""+consultDoctorId +
                     whereCondition +" order by consultation_id desc";

            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {
                int consultation_id = rs.getInt("consultation_id");
                int referred_by_doctor_id = rs.getInt("referred_by_doctor_id");
                int consultation_doctor_id = rs.getInt("consultation_doctor_id");
                int patient_id = rs.getInt("patient_id");

                String patient_name = rs.getString("patient_name");


                String consult_date = rs.getString("consult_date_time");
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
                        consult_date, referred_by_name, consult_name, consultant_status,receipt_num,receipt_type,remarks,description,guardian_name,gender,age,address);
                consultList.add(cm);
            }

            System.out.println(consultList.size());

            if (null != consultList) {
                if (!consultList.isEmpty()) {
                    pagination.setVisible(true);
                }
                search_Item();
            }

            search_Item();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(consultList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(patient -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return patient.getPatient_name().toLowerCase().contains(lowerCaseFilter);
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
        colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patient_name"));
        colConsultDate.setCellValueFactory(new PropertyValueFactory<>("consult_date"));
        colReferredBy.setCellValueFactory(new PropertyValueFactory<>("referred_by_name"));
        colConsultDoctorName.setCellValueFactory(new PropertyValueFactory<>("consult_name"));
        colReceiptNum.setCellValueFactory(new PropertyValueFactory<>("receiptNum"));
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("consultant_status"));

        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, consultList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<ConsultantModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<ConsultantModel, String>, TableCell<ConsultantModel, String>>
                cellEdit = (TableColumn<ConsultantModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    String status = tableview.getItems().get(getIndex()).getConsultant_status();
                    ComboBox<String> statusCom = new ComboBox<>(FXCollections.observableArrayList("Pending","Done","Cancelled"));
                    statusCom.setMinHeight(24);
                    statusCom.setPrefHeight(24);
                    statusCom.setPrefWidth(110);
                    statusCom.getSelectionModel().select(status);

                    switch (status){
                        case "Pending"->statusCom.getStyleClass().add("consult_status_pending");
                        case "Done"->statusCom.getStyleClass().add("consult_status_done");
                        case "Cancelled"-> statusCom.getStyleClass().add("consult_status_cancel");
                        }

                    statusCom.getSelectionModel().selectedItemProperty().addListener((observableValue, oldStatus, newStatus) -> {
                        new Method().selectTable(getIndex(),tableview);
                        ConsultantModel cm = tableview.getSelectionModel().getSelectedItem();
                        updateConsultStatus(cm,oldStatus,newStatus,statusCom);
                    });

                    ImageView ivPresc = new ImageView(new ImageLoader().load("img/menu_icon/prescription_icon.png"));
                    ivPresc.setFitHeight(25);
                    ivPresc.setFitWidth(25);
                    ivPresc.setStyle("-fx-cursor: hand ;");

                    ivPresc.setOnMouseClicked(mouseEvent -> {
                        new Method().selectTable(getIndex(),tableview);
                        ConsultantModel cm = tableview.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(cm);
                        new CustomDialog().showFxmlFullDialog("prescription/ePrescription.fxml","E-Prescription");
                    });


                    HBox managebtn = new HBox(statusCom,ivPresc);
                    HBox.setMargin(statusCom, new Insets(4, 0, 0, 0));
                    HBox.setMargin(ivPresc, new Insets(15, 0, 15, 10));
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colAction.setCellFactory(cellEdit);
    }

    private void updateConsultStatus(ConsultantModel cm, String oldStatus,
                                     String newStatus, ComboBox<String> statusCom) {
        Connection connection = null;
        PreparedStatement ps = null;

        try{
            connection = new DBConnection().getConnection();

            String qry = """
                    UPDATE patient_consultation set consultant_status = ? where consultation_id = ?
                    """;
            ps = connection.prepareStatement(qry);

            ps.setString(1,newStatus);
            ps.setInt(2,cm.getConsultation_id());

            int res = ps.executeUpdate();

            if (res > 0){
                cm.setConsultant_status(newStatus);
            }else {
                cm.setConsultant_status(oldStatus);
            }

            tableview.refresh();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,null);
        }


    }
}
