package com.techwhizer.medicalshop.controller.dues;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.dues.model.DuesHistoryModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class DuesHistory implements Initializable {

    public TableView<DuesHistoryModel> tableView;

    public TableColumn<DuesHistoryModel, Integer> colSrNo;
    public TableColumn<DuesHistoryModel, String> colPaid;
    public TableColumn<DuesHistoryModel, String> colPaymentMode;

    public TableColumn<DuesHistoryModel, String> colDate;

    public TableColumn<DuesHistoryModel, String> colReferenceNumber;
    public TableColumn<DuesHistoryModel, String> colRemark;
    public Label patientNameL;
    public Label patientAddressL;

    private CustomDialog customDialog;
    private Method method;
    private final ObservableList<DuesHistoryModel> historyList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();


        if (null != Main.primaryStage.getUserData() &&
                Main.primaryStage.getUserData() instanceof Map) {

            Map<String,Object> map = (Map<String, Object>) Main.primaryStage.getUserData();

            String patientName = (String) map.get("patient_name");
            String patientAddress = (String) map.get("patient_address");
            patientNameL.setText(patientName);
            patientAddressL.setText(patientAddress);
            new MyAsyncTask(map).execute();

        } else {
            customDialog.showAlertBox("", "Something went wrong. Please re-open.");
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) patientNameL.getScene().getWindow();
            stage.setMaximized(true);
        });
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        Map<String, Object> userData;

        public MyAsyncTask(Map<String, Object> userData) {
            this.userData = userData;
        }

        @Override
        public void onPreExecute() {



            tableView.setPlaceholder(method.getProgressBarRed(40, 40));
        }

        @Override
        public Boolean doInBackground(String... params) {



            int duesId = (int) userData.get("dues_id");

            getDuesHistory(duesId);

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setPlaceholder(new Label("Dues History Not Found."));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getDuesHistory(int duesId) {
        historyList.clear();
        historyList.removeAll();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = """
select pi.amount,
       to_char(pi.payment_date,'DD/MM/YYYY') as payment_date,
       pi.payment_method,
       pi.transaction_id,
       pi.remarks,
       pi.payment_id
from payment_information pi
where pi.document_id = ? and pi.document_source = 'DUES'

                    """;

            ps = connection.prepareStatement(qry);
            ps.setInt(1, duesId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int paymentId = rs.getInt("payment_id");
                String payment_method = rs.getString("payment_method");
                String transaction_id = rs.getString("transaction_id");
                double amount = rs.getDouble("amount");
                String remarks = rs.getString("remarks");
                String paymentDate = rs.getString("payment_date");
                historyList.add(new DuesHistoryModel(paymentId,amount,payment_method,paymentDate,transaction_id,remarks));
            }

            tableView.setItems(historyList);
            colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableView.getItems().indexOf(cellData.getValue()) + 1));
            colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
            colPaymentMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
            colReferenceNumber.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
            colRemark.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        } catch (SQLException e) {
            customDialog.showAlertBox("","Something went wrong.");
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

}
