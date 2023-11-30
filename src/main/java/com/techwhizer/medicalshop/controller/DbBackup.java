package com.techwhizer.medicalshop.controller;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.DatabaseBackup;
import com.techwhizer.medicalshop.method.Method;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DbBackup implements Initializable {
    public Label backupDateL;
    public Label lastBackupL;
    public Button backup_button;
    public ProgressIndicator progressBar;
    private DatabaseBackup backup;
    private CustomDialog customDialog;
    private Method method;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backup = new DatabaseBackup();
        customDialog = new CustomDialog();
        method = new Method();
        setLabelData();
        method.hideElement(progressBar);
    }


    private void setLabelData() {
        backupDateL.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

        final String FIRST_REGEX = "backup_";
        final String LAST_REGEX = ".backup";
        final String PATTERN = "dd_MM_yyyy";
        String path = System.getProperty("user.home") + "/DB_BACKUP/";
        File dir = new File(path);
        String[] list = dir.list((dir1, name) -> name.toLowerCase().endsWith(LAST_REGEX));

        List<String> strDateList = new ArrayList<>();

        if (null == list) {
            return;
        }
        for (String s : list) {
            strDateList.add(s.replaceAll(FIRST_REGEX, "").replaceAll(LAST_REGEX, ""));
        }
        List<LocalDate> dateList = new ArrayList<>();
        for (String ds : strDateList) {
            dateList.add(LocalDate.parse(ds, DateTimeFormatter.ofPattern(PATTERN)));
        }
        Collections.sort(dateList);

        String lastBackup = null;
        for (LocalDate localDate : dateList) {
            lastBackup = localDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        lastBackupL.setText(lastBackup);
    }

    public void backupNow(ActionEvent actionEvent) throws IOException {

        String msg = "Are you sure you want to backup ?";
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(msg);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {

            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.setDaemon(false);
            myAsyncTask.execute();

        } else {
            alert.close();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        public void onPreExecute() {
            method.hideElement(backup_button);
            progressBar.setVisible(true);
        }

        @Override
        public Boolean doInBackground(String... params) {
            startLocalBackup();
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            backup_button.setVisible(true);
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void startLocalBackup() {
        String path = backup.startBackup();
        if (!path.equals("failed")){
            Platform.runLater(()->{
                Stage stage = (Stage) backup_button.getScene().getWindow();
                if (null != stage && stage.isShowing()) {
                    stage.close();
                }
            });
            checkExtraFile();
        }

    }

    private void checkExtraFile() {

        int requiredBackupFile = 5;
        final String FIRST_REGEX = "backup_";
        final String LAST_REGEX = ".backup";
        final String PATTERN = "dd_MM_yyyy";
        String path = System.getProperty("user.home") + "/HOTEL_DB_BACKUP/";
        File dir = new File(path);
        String[] list = dir.list((dir1, name) -> name.toLowerCase().endsWith(LAST_REGEX));

        List<String> strDateList = new ArrayList<>();
        for (String s : list) {
            strDateList.add(s.replaceAll(FIRST_REGEX, "").replaceAll(LAST_REGEX, ""));
        }
        List<LocalDate> dateList = new ArrayList<>();
        for (String ds : strDateList) {
            dateList.add(LocalDate.parse(ds, DateTimeFormatter.ofPattern(PATTERN)));
        }
        Collections.sort(dateList);

        int fileLength = dateList.size();
        if (fileLength >= requiredBackupFile) {
            int extraFileLength = fileLength - requiredBackupFile;
            for (int j = 0; j < extraFileLength; j++) {
                String filaName = FIRST_REGEX.concat(dateList.get(j).format(DateTimeFormatter.ofPattern(PATTERN))).concat(LAST_REGEX);
                String dPath = path.concat(filaName);

                File dFile = new File(dPath);
                if (dFile.exists()) {
                    dFile.delete();
                }
            }
        }
    }
}
