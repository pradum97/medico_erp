package com.techwhizer.medicalshop.controller.patient;

import com.techwhizer.medicalshop.*;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.PrintPrescription;
import com.techwhizer.medicalshop.model.PatientModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.RoleKey;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class PatientMain implements Initializable {
    private int rowsPerPage = 50;
    public TableView<PatientModel> tableView;
    public TableColumn<PatientModel, Integer> colSrNo;
    public TableColumn<PatientModel, String> colName;
    public TableColumn<PatientModel, String> colAddress;
    public TableColumn<PatientModel, String> colUhidNum;
    public TableColumn<PatientModel, String> colAdmNum;

    public TableColumn<PatientModel, String> colGender;
    public TableColumn<PatientModel, String> colAge;
    public TableColumn<PatientModel, String> colEdit;

    public Pagination pagination;
    public TextField searchTf;
    private Properties propRead;
    private DBConnection dbConnection;
    private CustomDialog customDialog;
    private FilteredList<PatientModel> filteredData;
    private Method method;

    private ObservableList<PatientModel> patientList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        dbConnection = new DBConnection();
        customDialog = new CustomDialog();
        PropertiesLoader propLoader = new PropertiesLoader();
        propRead = propLoader.getReadProp();
        callThread();
        tableView.setFixedCellSize(28.0);
    }



    private void callThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
            tableView.setPlaceholder(method.getProgressBarRed(40,40));
        }

        @Override
        public Boolean doInBackground(String... params) {

            getPatient();

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getPatient() {

        if (null != patientList) {
            patientList.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();

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



                PatientModel pm = new PatientModel(patient_id,salutation_id,created_by,last_update_by,salutation_name,first_name,
                        middle_name,last_name,fullName,gender,age,address,dob,phone,idType,idNum,guardianName,weight,bp,pulse,
                        sugar,spo2,temp,cvs,cns,chest,creationDate,lastUpdate,admission_number,uhidNum);
                patientList.add(pm);
            }
            if (null != patientList) {
                if (!patientList.isEmpty()) {
                    pagination.setVisible(true);
                    search_Item();
                }
            }

           Platform.runLater(()->{

               if (!patientList.isEmpty()) {
                   tableView.setPlaceholder(new Label(""));
               } else {
                   tableView.setPlaceholder(new Label("Patient not available"));
               }
           });
        } catch (SQLException e) {
            Platform.runLater(()->{

                tableView.setPlaceholder(new Label("An error occurred while fetching the item"));
            });

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(patientList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(patient -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(patient.getPhone()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return patient.getFullName().toLowerCase().contains(lowerCaseFilter);
            });

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        Platform.runLater(() -> {
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        changeTableView(newValue1.intValue(), rowsPerPage);
                    });
        });

    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUhidNum.setCellValueFactory(new PropertyValueFactory<>("uhidNum"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, patientList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<PatientModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    public void addPatient(ActionEvent event) {
        customDialog.showFxmlDialog2("patient/addPatient.fxml", "Add New Patient");
        callThread();
    }

    private void setSpace(HBox... hBoxes){

        for (HBox h:hBoxes ) {
            h.setSpacing(10);
        }

    }

    private void setOptionalCell() {


        Callback<TableColumn<PatientModel, String>, TableCell<PatientModel, String>>
                cellAdmissionNum = (TableColumn<PatientModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink admNumHl = new Hyperlink(tableView.getItems().get(getIndex()).getAdmissionNumber());

                    admNumHl.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 12;-fx-alignment: center-left");

                    admNumHl.setMinWidth(125);

                    admNumHl.setOnAction(actionEvent -> {
                        tableView.getSelectionModel().select(getIndex());
                        PatientModel pm = tableView.getSelectionModel().getSelectedItem();
                        new PrintPrescription().print(0,0,pm.getPatientId(),admNumHl,"", pm.getFullName(), false);
                    });
                    HBox managebtn = new HBox(admNumHl);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };
        colAdmNum.setCellFactory(cellAdmissionNum);




        Callback<TableColumn<PatientModel, String>, TableCell<PatientModel, String>>
                cellEdit = (TableColumn<PatientModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Label bnUpdate = new Label();
                    Hyperlink viewHistory = new Hyperlink("History");
                    Hyperlink viewDetails = new Hyperlink("View Details");
                    viewHistory.setMinWidth(80);
                    viewDetails.setMinWidth(95);
                    viewHistory.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;-fx-border-color: transparent");
                    viewDetails.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;-fx-border-color: transparent");

                    viewHistory.setOnAction(actionEvent -> {
                        tableView.getSelectionModel().select(getIndex());
                        PatientModel pm = tableView.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(pm.getPatientId());
                        customDialog.showFxmlDialog2("patient/medicalHistoryMain.fxml", "History");
                    });

                    viewDetails.setOnAction(actionEvent -> {
                        tableView.getSelectionModel().select(getIndex());
                        PatientModel pm = tableView.getSelectionModel().getSelectedItem();
                        final var alert = new Alert(Alert.AlertType.INFORMATION);

                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setTitle("Patient Details");

                        HBox name = getContainer("Patient Name :",pm.getFullName());
                        HBox address = getContainer("Address :",pm.getAddress());

                        HBox gender_age= new HBox( getContainer("Gender :",pm.getGender()),getContainer("Age :",pm.getAge()));

                        HBox dob_phone = new HBox(getContainer("Dob :",pm.getDateOfBirth()),
                                getContainer("Phone # :",pm.getPhone()));

                        HBox idType_idNumber = new HBox(getContainer("ID Type :",pm.getIdNumber()),getContainer("ID # :",pm.getIdNumber()));



                        HBox guardianName_weight = new HBox( getContainer("Guardian Name :",pm.getGuardianName()),
                                getContainer("Weight :",pm.getWeight()));




                        HBox temp_bp = new HBox(getContainer("TEMP :",pm.getTemp()), getContainer("B. P. :",pm.getBp()));



                        HBox pulse_sugar = new HBox (getContainer("Pulse :",pm.getPulse()), getContainer("Sugar :",pm.getSugar()));

                        HBox cvs_cns = new HBox( getContainer("CVS :",pm.getCvs()),getContainer("CNS :",pm.getCns()));

                        HBox chest_spo2 = new HBox(getContainer("CHEST :",pm.getChest()), getContainer("SPO2 :",pm.getSpo2()));



                        setSpace(name,address,gender_age,dob_phone,idType_idNumber,guardianName_weight
                                ,temp_bp,pulse_sugar,cvs_cns,chest_spo2);


                        VBox mainContainer = new VBox(name,address,gender_age,dob_phone,idType_idNumber,guardianName_weight
                                ,temp_bp,pulse_sugar,cvs_cns,chest_spo2);
                        mainContainer.setSpacing(10);
                        mainContainer.setMinWidth(500);

                        alert.getDialogPane().setContent(mainContainer);
                        alert.initOwner(Main.primaryStage);
                        alert.showAndWait();
                    });

                    bnUpdate.setMinWidth(35);
                    bnUpdate.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                    bnUpdate.setStyle("-fx-background-color:  #006666; -fx-background-radius: 3 ;-fx-font-weight: bold ;-fx-font-family: Arial; " +
                            "-fx-padding: 2 5 2 5 ; -fx-text-fill: white; -fx-alignment: center;-fx-cursor: hand");

                    ImageView add_iv = new ImageView();

                    String path = "img/icon/";

                    add_iv.setFitHeight(12);
                    add_iv.setFitWidth(12);

                    ImageLoader loader = new ImageLoader();
                    add_iv.setImage(loader.load(path.concat("update_ic.png")));
                    bnUpdate.setGraphic(add_iv);

                    bnUpdate.setOnMouseClicked(mouseEvent -> {
                        PatientModel pm = tableView.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(pm);
                        customDialog.showFxmlDialog2("patient/addPatient.fxml", "Update Patient Details");
                        Main.primaryStage.setUserData(null);
                        callThread();
                    });


                    HBox managebtn = new HBox(viewDetails, bnUpdate);
                    managebtn.setStyle("-fx-alignment:center-left");
                    HBox.setMargin(viewHistory, new Insets(0, 0, 0, 0));
                    HBox.setMargin(viewDetails, new Insets(0, 0, 0, 7));
                    HBox.setMargin(bnUpdate, new Insets(0, 0, 0, 7));
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };


        colEdit.setCellFactory(cellEdit);
    }

    private HBox getContainer (String label,String value){

        Label l = new Label(label.toUpperCase());
        l.setAlignment(Pos.CENTER_RIGHT);
        l.setStyle("-fx-font-weight: bold;");
        Label l2 = new Label(value);
        l2.setMinWidth(170);
        l2.setStyle("-fx-font-weight: bold;-fx-text-fill: blue;");

        HBox labelContainer = new HBox(l);
        labelContainer.setMinWidth(113);
        labelContainer.setAlignment(Pos.CENTER_RIGHT);
        HBox h = new HBox(labelContainer,l2);
        h.setSpacing(5);

        return h;
    }

}
