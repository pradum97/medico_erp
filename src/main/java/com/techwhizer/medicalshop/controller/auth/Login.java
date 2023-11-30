package com.techwhizer.medicalshop.controller.auth;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.PropertiesLoader;
import com.techwhizer.medicalshop.method.GetUserProfile;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.UserDetails;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.SecurePassword;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Login implements Initializable {
    public TextField email_f;
    public PasswordField password_f;
    public Button login_button;
    public ProgressIndicator progressBar;
    private Main main;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    public static int currentlyLogin_Id = 0;
    public static int currentRole_Id = 0;
    public static String currentRoleName,username;
    private Properties propRead;
    public Text headerText;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       Platform.runLater(() -> marqueeText());
        main = new Main();
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        PropertiesLoader propLoader = new PropertiesLoader();
        propRead = propLoader.getReadProp();
        method.hideElement(progressBar);

    }

    @FXML
    public void forget_password_bn(ActionEvent event) {
        customDialog.showFxmlDialog("auth/forgotPassword.fxml", "Forgot Password");
    }

    public void login_bn(ActionEvent event) {
        startLogin();
    }

    private Map<String, Object> getProfileDetails(ResultSet rs, PreparedStatement ps) throws SQLException {
        return openDashboard(rs, ps);
        // return  getLicenseData(rs , ps);
    }

    private Map<String, Object> openDashboard(ResultSet rs, PreparedStatement ps) throws SQLException {

        Map<String, Object> map = new HashMap<>();
        int userID = rs.getInt("user_id");
        int userStatus = rs.getInt("account_status");
        if (userStatus == 0) {
            map.put("message", "Your Account Has Been Inactive Please Contact Administrator");
            map.put("is_success", false);
        } else {
            currentlyLogin_Id = userID;
            UserDetails userDetails = new GetUserProfile().getUser(userID);
            if (null == userDetails) {

                map.put("message", "User not found");
                map.put("is_success", false);
            } else {
                currentRole_Id = userDetails.getRole_id();
                currentRoleName = userDetails.getRole();
                username = userDetails.getUsername();
                Main.primaryStage.setUserData(userDetails);
                map.put("isLicenceActive", true);
                map.put("is_success", true);
            }
        }

        return map;
    }

    private Map<String, Object> getLicenseData(ResultSet rsProfile, PreparedStatement psProfile) {
        Map<String, Object> map = new HashMap<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String query = "select expires_on from tbl_license";
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();

            if (rs.next()) {

                String pattern = "dd-MM-yyyy";
                SimpleDateFormat sdformat = new SimpleDateFormat(pattern);


                Date currentDate = sdformat.parse(sdformat.format(new Date()));
                Date expiresDate = sdformat.parse(rs.getString("expires_on"));

                int checkExpireDate = currentDate.compareTo(expiresDate);

                if (checkExpireDate > 0) {
                    map.put("message", "Licence Expired. Please renew licence");
                    map.put("is_success", true);
                    map.put("isLicenceActive", false);
                } else {
                    map = openDashboard(rsProfile, psProfile);

                }
            } else {
                map.put("message", "License not activated please activate license");
                map.put("is_success", true);
                map.put("isLicenceActive", false);
            }


        } catch (SQLException | ParseException e) {
            map.put("message", "There was an error fetching license details");
            map.put("is_success", false);
            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return map;
    }

    public void enterPress(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            startLogin();
        }
    }

    private void startLogin() {

        String inputValue = email_f.getText();
        String password = password_f.getText();

        if (inputValue.isEmpty()) {
            method.show_popup("Please enter valid username", email_f);
            return;
        } else if (password.isEmpty()) {
            method.show_popup("Please enter password", password_f);
            return;
        }

        MyAsyncTask myAsyncTask = new MyAsyncTask(inputValue, password);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String headerText = "";
        private String inputValue;
        private String password;
        private Map<String, Object> map;

        public MyAsyncTask(String inputValue, String password) {
            this.inputValue = inputValue;
            this.password = password;
        }

        @Override
        public void onPreExecute() {
            //Background Thread will start
            headerText = "";
            method.hideElement(login_button);
            progressBar.setVisible(true);
        }

        @Override
        public Boolean doInBackground(String... params) {
            /* Background Thread is running */

            Map<String, Object> status = login(inputValue, SecurePassword.secure(password));
            boolean isSuccess = (boolean) status.get("is_success");

            headerText = (String) status.get("message");
            map = status;
            return isSuccess;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            login_button.setVisible(true);
            if (success) {
                boolean isLicenceActive = (boolean) map.get("isLicenceActive");
                if (isLicenceActive) {
                    Main.primaryStage.setMaximized(true);


                    main.changeScene("dashboard.fxml", "DASHBOARD");

                } else {
                    customDialog.showFxmlDialog2("license/licenseMain.fxml", "");
                }

            } else {
                customDialog.showAlertBox("Login Failed", headerText);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private Map<String, Object> login(String inputValue, String password) {
        Map<String, Object> map = new HashMap<>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();
            // Email Login
            long phoneNum = 0;
            try {
                phoneNum = Long.parseLong(inputValue);
            } catch (NumberFormatException e) {
                // e.printStackTrace();
            }


            String qry = """
                    SELECT * FROM TBL_USERS WHERE (USERNAME = ? or phone = ? or email = ?) AND PASSWORD = ?
                    
                    """;

            ps = connection.prepareStatement(qry);
            ps.setString(1, inputValue);
            ps.setLong(2, phoneNum);
            ps.setString(3, inputValue);
            ps.setString(4, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                map = getProfileDetails(rs, ps);
            }else {
                map.put("message", "Invalid email or password or phone !");
                map.put("is_success", false);
            }
        } catch (Exception e) {
            map.put("message", "There was an error while login");
            map.put("is_success", false);
        } finally {

            DBConnection.closeConnection(connection, ps, rs);

        }
        return map;
    }

    private void marqueeText(){

        String appName = """ 
                Techwhizer Medico ERP · Techwhizer Medico ERP · Techwhizer Medico ERP · Techwhizer Medico ERP""";

        headerText.setText(appName);
        headerText.setTextOrigin(VPos.TOP);
        headerText.setFont(Font.font(18));
        headerText.setStyle("-fx-fill: #006666;-fx-font-weight: bold");

        double sceneWidth = email_f.getScene().getWidth();
        double msgWidth = headerText.getLayoutBounds().getWidth();

        KeyValue initKeyValue = new KeyValue(headerText.translateXProperty(), sceneWidth);
        KeyFrame initFrame = new KeyFrame(Duration.ZERO, initKeyValue);

        KeyValue endKeyValue = new KeyValue(headerText.translateXProperty(), -1.0 * msgWidth);
        KeyFrame endFrame = new KeyFrame(Duration.seconds(10), endKeyValue);

        Timeline timeline = new Timeline(initFrame, endFrame);

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }




}
