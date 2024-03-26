package com.techwhizer.medicalshop.controller.dues;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class PayDues implements Initializable {
    public ComboBox<String> paymentModeCom;
    public Label duesAmountL;
    public TextField receivedAmountTF;
    public TextField referenceNumberTf;
    public TextField remarkTf;
    public Button payBn;
    public ProgressIndicator progressBar;
    private Method method;
    private CustomDialog customDialog;
    private int duesId;
    private String patientPhone;
    private double totalDuesAmount;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        method.hideElement(progressBar);
        setDefaultValue();

        if (null != Main.primaryStage.getUserData() &&
                Main.primaryStage.getUserData() instanceof Map) {

            Map<String, Object> userData =(Map<String, Object>) Main.primaryStage.getUserData();

            patientPhone = (String) userData.get("patient_phone");
            totalDuesAmount = (Double) userData.get("dues");
            duesId = (int) userData.get("dues_id");
            duesAmountL.setText(String.valueOf(totalDuesAmount));

        } else {
            customDialog.showAlertBox("", "Something went wrong. Please re-open.");
        }
    }

    private void setDefaultValue() {
        paymentModeCom.setItems(CommonUtil.getPaymentMethod());
        paymentModeCom.getSelectionModel().selectFirst();
    }

    public void payDues(ActionEvent event) {
        payDialog();
    }
    public void cancel(ActionEvent event) {

        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        if (stage.isShowing()){
            stage.close();
        }
    }
    public void enterClick(KeyEvent e) {

        if (e.getCode() == KeyCode.ENTER) {
            payDialog();
        }
    }

    private void payDialog(){

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure you want to pay the dues?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            new MyAsyncTask().execute();
        } else {
            alert.close();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
          method.hideElement(payBn);
          progressBar.setVisible(true);
        }

        @Override
        public Boolean doInBackground(String... params) {
            pay();
            return false;
        }
        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            payBn.setVisible(true);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void pay(){

        String paidAmount = receivedAmountTF.getText();
        String referenceNum = referenceNumberTf.getText();
        String remarks = remarkTf.getText();
        String paymentMode = paymentModeCom.getSelectionModel().getSelectedItem();

        if (paidAmount.isEmpty()) {
            method.show_popup("Please Enter Received Amount", receivedAmountTF, Side.RIGHT);
            return;
        }
        double paidAmountD = 0, avlDuesD = 0;
        try {
            paidAmountD = Double.parseDouble(paidAmount);
        } catch (NumberFormatException e) {
            method.show_popup("Enter Valid Amount", receivedAmountTF, Side.RIGHT);
            return;
        }
        if (paidAmountD < 1) {
            method.show_popup("Enter more than 0", receivedAmountTF, Side.RIGHT);
            return;
        }

        if (paidAmountD > totalDuesAmount){
            method.show_popup("You can't pay more then dues amount", receivedAmountTF, Side.RIGHT);
            return;
        }



        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = """
                            INSERT INTO  payment_information(PURPOSE, DOCUMENT_SOURCE, DOCUMENT_ID,
                            AMOUNT, PAYMENT_METHOD,TRANSACTION_ID,REMARKS) values (?,?,?,?,?,?,?)
                            """;

            ps = connection.prepareStatement(qry);
            ps.setString(1, "PAY DUES");
            ps.setString(2, "DUES");
            ps.setInt(3, duesId);
            ps.setBigDecimal(4, BigDecimal.valueOf(Double.parseDouble(paidAmount)));
            ps.setString(5, paymentMode);
            ps.setString(6, referenceNum);
            ps.setString(7, remarks);
            int res = ps.executeUpdate();

            if (res > 0) {
                customDialog.showAlertBox("","Payment Success.");
                Main.primaryStage.setUserData((boolean)true);
                Platform.runLater(() -> method.closeStage(receivedAmountTF));
            }
        } catch (SQLException e) {
            customDialog.showAlertBox("Error","Something went wrong.");
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }
    public void pay100(ActionEvent actionEvent) {
        receivedAmountTF.setText(String.valueOf(totalDuesAmount));
    }

    public void pay50(ActionEvent actionEvent) {
        receivedAmountTF.setText(String.valueOf(totalDuesAmount/2));
    }
}
