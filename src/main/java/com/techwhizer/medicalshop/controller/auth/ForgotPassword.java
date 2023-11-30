package com.techwhizer.medicalshop.controller.auth;


import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.PropertiesLoader;
import com.techwhizer.medicalshop.mail.SendMail;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.SecurePassword;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPassword implements Initializable {

    public TextField email_f;
    public VBox verification_container;
    public VBox password_container;
    public Button submit_bn;
    public TextField confirm_password;
    public TextField new_password;
    public VBox otpContainer;
    public TextField otpTf;
    public Button verifyOtpBn;
    public ProgressIndicator progressBar;

    private Method method;
    private DBConnection dbConnection;
    private CustomDialog customDialog;
    private String otp,emailDb;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        password_container.setVisible(false);

        method.hideElement(password_container);
        method.hideElement(otpContainer);
        method.hideElement(progressBar);

    }

    public void forgetPassword_bn(ActionEvent actionEvent) {


        String newPassword = new_password.getText();
        String con_Password = confirm_password.getText();


        if (newPassword.isEmpty()) {
            method.show_popup("Enter New Password", new_password);
            return;
        } else if (con_Password.isEmpty()) {
            method.show_popup("Enter Confirm Password", confirm_password);
            return;
        } else if (!newPassword.equals(con_Password)) {
            method.show_popup("Confirm Password do not match", confirm_password);
            return;
        }
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection  = new DBConnection().getConnection();
            ps = connection.prepareStatement(new PropertiesLoader().getUpdateProp().getProperty("UPDATE_PASSWORD"));
            ps.setString(1, SecurePassword.secure(con_Password));
            ps.setString(2, emailDb);
            int update_result = ps.executeUpdate();

            if (update_result > 0) {
                email_f.setText("");
                new_password.setText("");
                confirm_password.setText("");
                customDialog.showAlertBox("Congratulations🎉🎉🎉", "Successfully Updated");
                Stage stage = ((Stage) email_f.getScene().getWindow());

                email_f.setText("");
                new_password.setText("");
                confirm_password.setText("");

                if (null != stage && stage.isShowing()) {

                    stage.close();
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }


    }

    public void submit(ActionEvent actionEvent) {


        String email = email_f.getText();

        if (email.isEmpty()) {
            method.show_popup("Enter Valid Email", email_f);
            return;
        }
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "SELECT email FROM TBL_USERS WHERE email = ? or username = ?";
            connection = dbConnection.getConnection();

            ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                 emailDb = rs.getString("email");

                MyAsyncTask task = new MyAsyncTask(emailDb);
                task.execute();

            } else {
                customDialog.showAlertBox("Failed", "Invalid Email or Username !");
            }

        } catch (Exception ignored) {
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }

    }

    public void verifyOtp(ActionEvent event) {

        String otpTfText = otpTf.getText();

        if (otp.isEmpty()) {
            method.show_popup("Please enter 6-digit otp", otpTf);
            return;
        }

        if (otp.equals(otpTfText)) {
            // after verify otp
           method.hideElement(otpContainer);
            password_container.setVisible(true);
        } else {

            customDialog.showAlertBox("Invalid!", "Incorrect otp.");
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        String toEmail;

        public MyAsyncTask(String toEmail) {
            this.toEmail = toEmail;
        }

        @Override
        public void onPreExecute() {
            progressBar.setVisible(true);
            method.hideElement(submit_bn);
        }

        @Override
        public Boolean doInBackground(String... params) {
            otp = SendMail.sendOtp(toEmail);
            return !otp.isEmpty();
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            submit_bn.setVisible(true);

            if (success) {
                verification_container.setVisible(false);
                verification_container.managedProperty().bind(verification_container.visibleProperty());
                otpContainer.setVisible(true);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }
}
