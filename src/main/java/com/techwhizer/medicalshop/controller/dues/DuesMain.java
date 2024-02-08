package com.techwhizer.medicalshop.controller.dues;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.dues.model.DuesMainModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DuesMain implements Initializable {

    int rowsPerPage = 100;
    public ComboBox<String> comDuesStatus;
    public DatePicker fromDateDP;
    public DatePicker toDataDP;
    public TextField searchTf;

    public TableView<DuesMainModel> tableview;
    public TableColumn<DuesMainModel,Integer> colSrNum;
    public TableColumn<DuesMainModel,String> colName;
    public TableColumn<DuesMainModel,String> colPhone;
    public TableColumn<DuesMainModel,String> colAddress;
    public TableColumn<DuesMainModel,String> colRemainingDues;
    public TableColumn<DuesMainModel,String> colLastPayment;
    public TableColumn<DuesMainModel,String> colTotalDues;
    public TableColumn<DuesMainModel,String> colReceivedDues;
    public TableColumn<DuesMainModel,String> colDuseDate;
    public TableColumn<DuesMainModel,String> colAction;
    public Pagination pagination;
    public Button refresh_bn;

    private  ObservableList<String> statusList =
          FXCollections.observableArrayList("Dues","Cleared");

    private  ObservableList<DuesMainModel> duesList =
            FXCollections.observableArrayList();

    private FilteredList<DuesMainModel> filteredData;
  private Method method;
  private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        tableview.setFixedCellSize(28);
        comDuesStatus.setItems(statusList);
        comDuesStatus.getSelectionModel().select(0);
        method.convertDateFormat(fromDateDP,toDataDP);
       reset();
    }

    private void startThread(Map<String, Object> data) {
        new MyAsyncTask(data).execute();
    }

    private void reset(){
        LocalDate currentDate = LocalDate.now();
        LocalDate prevDate = currentDate.minusYears(1);
        fromDateDP.setValue(prevDate);
        toDataDP.setValue(currentDate);
        searchTf.setText("");
        Map<String,Object> data = new HashMap<>();
        data.put("status","Dues");
        data.put("from_date",fromDateDP.getEditor().getText());
        data.put("to_date",toDataDP.getEditor().getText());

        startThread(data);

        Platform.runLater(() -> {
            Stage stage = (Stage) tableview.getScene().getWindow();
            stage.setMaximized(true);
        });
    }

    public void searchBnClick(ActionEvent actionEvent) {

        String status = comDuesStatus.getSelectionModel().getSelectedItem();

        Map<String,Object> data = new HashMap<>();
        data.put("status",status);
        data.put("from_date",fromDateDP.getEditor().getText());
        data.put("to_date",toDataDP.getEditor().getText());

        searchTf.setText("");
        startThread(data);
    }

    public void refreshBnClick(ActionEvent actionEvent) {
        reset();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Map<String, Object> data;

        public MyAsyncTask(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public void onPreExecute() {
          tableview.setPlaceholder(method.getProgressBarRed(40,40));
        }

        @Override
        public Boolean doInBackground(String... params) {
            getDues(data);
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableview.setPlaceholder(new Label("Not Found."));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getDues(Map<String, Object> data) {
        duesList.clear();
        duesList.removeAll();

        String duesStatus =(String) data.get("status") ;
        String fromDate =(String) data.get("from_date") ;
        String toDate =(String) data.get("to_date");

        LocalDate currentDate = LocalDate.now();
        LocalDate prevDate = currentDate.minusYears(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


        if (fromDate.isEmpty()){
            fromDate = prevDate.format(formatter);
        }

        if (toDate.isEmpty()){
            toDate = currentDate.format(formatter);
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

        String qry = """
                select * from  dues_v  where
                case when ? = 'Dues' then cast(remaining_dues as decimal) > 0.0 else
                    cast(remaining_dues as decimal) < 1 end
                and to_date(dues_date,'DD/MM/YYYY') BETWEEN to_date(?,'DD/MM/YYYY') AND
                    to_date(?,'DD/MM/YYYY') order by dues_id desc
                """;
        ps = connection.prepareStatement(qry);

        ps.setString(1,duesStatus);
        ps.setString(2,fromDate);
        ps.setString(3,toDate);

        rs = ps.executeQuery();

        while (rs.next()){

            int sourceID = rs.getInt("source_id");
            int duesID = rs.getInt("dues_id");
            String fullName = rs.getString("full_name");
            String phone = rs.getString("phone");
            String address = rs.getString("address");
            String duesDate = rs.getString("dues_date");
            String duesType = rs.getString("dues_type");

            double totalDues = rs.getDouble("total_dues");
            double remainingDues = rs.getDouble("remaining_dues");
            String lastPaymentAmtData = rs.getString("last_payment_amt_date");
            double totalReceivedAmount = rs.getDouble("total_received_amount");


            duesList.add(new DuesMainModel(duesID,sourceID,duesType,fullName,phone,address,
                    remainingDues,totalDues,totalReceivedAmount,lastPaymentAmtData,duesDate));
        }

            if (!duesList.isEmpty()) {
                pagination.setVisible(true);
                search_Item();
            }

        }catch (Exception e){

        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }

    }


    private void search_Item() {

        filteredData = new FilteredList<>(duesList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(products -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (products.getPatientName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (products.getPatientAddress() != null && products.getPatientAddress().toLowerCase().contains(lowerCaseFilter)){
                    return true;
                } else return  ( products.getPatientPhone() != null && products.getPatientPhone().toLowerCase().contains(lowerCaseFilter));
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

        colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));
        colName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("patientPhone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("patientAddress"));
       // colRemainingDues.setCellValueFactory(new PropertyValueFactory<>("remainingDuesAmount"));
        colReceivedDues.setCellValueFactory(new PropertyValueFactory<>("totalReceivedAmount"));
        colLastPayment.setCellValueFactory(new PropertyValueFactory<>("lastDuesAmtAndDate"));
        colTotalDues.setCellValueFactory(new PropertyValueFactory<>("totalDuesAmount"));
        colDuseDate.setCellValueFactory(new PropertyValueFactory<>("duesDate"));


        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, duesList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<DuesMainModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());
        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<DuesMainModel, String>, TableCell<DuesMainModel, String>>
                cellViewItems = (TableColumn<DuesMainModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    DuesMainModel phm = tableview.getItems().get(getIndex());

                    Hyperlink pay = new Hyperlink("Pay");
                    Hyperlink history = new Hyperlink("History");
                    pay.setVisible(phm.getRemainingDuesAmount() > 0);

                    pay.setMinWidth(35);
                    history.setMinWidth(50);

                    pay.setStyle("-fx-font-weight: bold;-fx-background-color:inherit;-fx-text-fill:blue;-fx-border-color:transparent");
                    history.setStyle("-fx-font-weight: bold;;-fx-background-color:inherit;-fx-text-fill:blue;-fx-border-color:transparent");


                    pay.setOnAction(actionEvent -> {

                        Map<String,Object> data = new HashMap<>();
                        data.put("patient_phone",phm.getPatientAddress());
                        data.put("dues_id",phm.getDuesId());
                        data.put("dues",phm.getRemainingDuesAmount());

                        Main.primaryStage.setUserData(data);
                        customDialog.showFxmlFullDialog("dues/payDues.fxml",phm.getPatientName());

                        if (Main.primaryStage.getUserData() instanceof Boolean){

                            boolean isSuccess =(Boolean) Main.primaryStage.getUserData();
                            if (isSuccess){

                                reset();
                            }

                        }
                    });

                    history.setOnAction(actionEvent -> {

                        Map<String,Object> data = new HashMap<>();
                        data.put("patient_name",phm.getPatientName());
                        data.put("patient_address",phm.getPatientAddress());
                        data.put("dues_id",phm.getDuesId());

                        Main.primaryStage.setUserData(data);
                        customDialog.showFxmlFullDialog("dues/duesHistory.fxml","Dues History");
                    });

                    HBox managebtn = new HBox(pay,history);
                    managebtn.setStyle("-fx-alignment:CENTER-LEFT");
                    HBox.setMargin(pay, new Insets(0, 0, 0, 5));
                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };


        Callback<TableColumn<DuesMainModel, String>, TableCell<DuesMainModel, String>>
                cellDues = (TableColumn<DuesMainModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    double amount = tableview.getItems().get(getIndex()).getRemainingDuesAmount();

                    Label amtLabel = new Label(String.valueOf(amount));

                    if (amount > 0) {
                        amtLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-background-color: red;" +
                                "-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px");
                    }  else {
                    amtLabel.setStyle("-fx-text-fill: white;-fx-font-weight:bold;-fx-background-color: green;" +
                            "-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px");
                    }

                    HBox managebtn = new HBox(amtLabel);
                    managebtn.setStyle("-fx-alignment:CENTER-LEFT");
                    HBox.setMargin(amtLabel, new Insets(0, 0, 0, 5));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };

        colRemainingDues.setCellFactory(cellDues);
        colAction.setCellFactory(cellViewItems);
    }

}
