package com.techwhizer.medicalshop.method;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.model.PrescriptionMedicationModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PrintPrescription {
    public void print(int patientId, int prescriptionMasterId,
                      Object button, boolean isEmptyPrint) {

            callThread(patientId,prescriptionMasterId,button,isEmptyPrint);

        }

    private void callThread(int patientId, int prescriptionMasterId, Object button, boolean isEmptyPrint) {

        PrintTask myAsyncTask = new PrintTask(patientId,prescriptionMasterId,button,isEmptyPrint);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();

    }

    private class PrintTask extends AsyncTask<String, Integer, Boolean> {

        int patientId;
        int prescriptionMasterId;
        Object button;
        boolean isEmptyPrint;

        public PrintTask(int patientId, int prescriptionMasterId, Object button, boolean isEmptyPrint) {
            this.patientId = patientId;
            this.prescriptionMasterId = prescriptionMasterId;
            this.button = button;
            this.isEmptyPrint = isEmptyPrint;
        }

        @Override
        public void onPreExecute() {

            ProgressIndicator pi = new Method().getProgressBarWhite(20, 20);
            if (null != button) {

                if (button instanceof Button bn) {
                    bn.setGraphic(pi);
                } else if (button instanceof Hyperlink) {
                    ((Hyperlink) button).setGraphic(new Method().getProgressBarRed(20, 20));
                }
            }
        }
        @Override
        public Boolean doInBackground(String... params) {

            if (isEmptyPrint){
                new GenerateInvoice().emptyPrescriptionPrint(patientId);
            }else {
                new GenerateInvoice().prescriptionInvoice(prescriptionMasterId);
            }

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {

            if (null != button) {
                ImageLoader loader = new ImageLoader();

                if (button instanceof Button bn) {
                    bn.setGraphic(loader.getPrintIcon());
                    bn.setContentDisplay(ContentDisplay.LEFT);

                } else if (button instanceof Hyperlink) {
                    ((Hyperlink) button).setGraphic(null);
                }

            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

}
