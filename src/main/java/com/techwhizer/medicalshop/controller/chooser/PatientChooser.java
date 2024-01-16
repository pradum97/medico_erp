package com.techwhizer.medicalshop.controller.chooser;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.PatientModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PatientChooser implements Initializable {
    private int rowsPerPage = 30;
    public TextField searchTf;
    public TableColumn<PatientModel, Integer> colSrNo;
    public TableColumn<PatientModel, String> colName;
    public TableColumn<PatientModel, String> colAction;
    public TableView<PatientModel> tableView;
    public Pagination pagination;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    private ObservableList<PatientModel> list = FXCollections.observableArrayList();
    private FilteredList<PatientModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        tableView.setFixedCellSize(27);

        callThread();
    }

    private void callThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String msg;

        @Override
        public void onPreExecute() {
            //Background Thread will start
            tableView.setPlaceholder(method.getProgressBarRed(30, 30));
            msg = "";
        }

        @Override
        public Boolean doInBackground(String... params) {
            /* Background Thread is running */

            Map<String, Object> status = getItems();
            msg = (String) status.get("message");
            return (boolean) status.get("is_success");
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setPlaceholder(new Label(msg));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private Map<String, Object> getItems() {

        if (null != list) {
            list.clear();
        }
        Map<String, Object> map = new HashMap<>();

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

            int count = 0;

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
                        sugar, spo2, temp, cvs, cns, chest, creationDate, lastUpdate,admission_number,uhidNum);
                list.add(pm);
                count++;

            }

            if (list.size() > 0) {
                pagination.setVisible(true);
                search_Item();
            }

            if (count > 0) {
                map.put("is_success", true);
                map.put("message", "Many item find");
            } else {
                map.put("is_success", false);
                map.put("message", "Item not available");
            }
        } catch (SQLException e) {
            map.put("is_success", false);
            map.put("message", "Something went wrong ");
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
        return map;
    }

    private void search_Item() {

        filteredData = new FilteredList<>(list, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(products -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (products.getFullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return products.getPhone().toLowerCase().contains(lowerCaseFilter);

            });

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, list.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<PatientModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<PatientModel, String>, TableCell<PatientModel, String>>
                cellFactory = (TableColumn<PatientModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button selectBn = new Button();

                    ImageView iv = new ImageView(new ImageLoader().load("img/icon/rightArrow_ic_white.png"));
                    iv.setFitHeight(12);
                    iv.setFitWidth(12);

                    selectBn.setGraphic(iv);
                    selectBn.setStyle("-fx-cursor: hand ;-fx-padding: 1 7 1 7; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");

                    selectBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        PatientModel icm = tableView.getSelectionModel().getSelectedItem();

                        if (null != icm) {
                            Main.primaryStage.setUserData(icm);
                            Stage stage = (Stage) searchTf.getScene().getWindow();
                            if (null != stage && stage.isShowing()) {
                                stage.close();
                            }
                        }
                    });

                    HBox managebtn = new HBox(selectBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(selectBn, new Insets(0, 0, 0, 0));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellFactory);
    }

    public void add(MouseEvent actionEvent) {
        customDialog.showFxmlDialog("product/patient/addPatient.fxml", "Add new Patient");
        searchTf.setText("");
        callThread();

    }
}
