package com.techwhizer.medicalshop.method;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.model.PrescribedMedicineModel;
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
    public void print(int prescribe_master_id, int consultId, int patientId,
                      Object button, String consultDoctorName, String patientName, boolean isEmptyPrint) {



        if (prescribe_master_id > 0) {

            callThread(prescribe_master_id, patientId, button, consultDoctorName, null, consultId, isEmptyPrint);
        } else {

            if (isEmptyPrint) {
                callThread(getPrescribeMasterId(consultId), patientId, button, consultDoctorName, null, consultId, isEmptyPrint);
            } else  if (consultId > 0) {
                if (totalPrescribeMedicine(consultId) > 1) {


                    Map<String, Object> data = new HashMap<>();
                    data.put("consult_id", consultId);
                    data.put("patient_id", patientId);
                    data.put("doctorName", consultDoctorName);
                    data.put("isPrint", true);
                    Main.primaryStage.setUserData(data);
                    new CustomDialog().showFxmlDialog2("chooser/prescribeMedicineChooser.fxml", patientName);

                } else if (totalPrescribeMedicine(consultId) < 1) {
                    return;
                } else {
                    callThread(getPrescribeMasterId(consultId), patientId, button, consultDoctorName, null, consultId, isEmptyPrint);
                }

            } else {

                callThread(0, patientId, button, consultDoctorName, null, consultId, isEmptyPrint);

            }


        }
    }

    public void callThread(int prescribe_master_id, int patientId,
                           Object button, String consultDoctorName, String buttonType, int consultId, boolean isEmptyPrint) {

        PrintTask myAsyncTask = new PrintTask(prescribe_master_id, patientId, button, consultDoctorName, buttonType, consultId, isEmptyPrint);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();

    }

    private class PrintTask extends AsyncTask<String, Integer, Boolean> {

        int prescribe_master_id, patientId;
        Object button;

        String consultDoctorName;
        String buttonType;
        int consultId;
        boolean isEmptyPrint;

        public PrintTask(int prescribe_master_id, int patientId, Object button,
                         String consultDoctorName, String buttonType, int consultId, boolean isEmptyPrint) {
            this.prescribe_master_id = prescribe_master_id;
            this.patientId = patientId;
            this.button = button;
            this.consultDoctorName = consultDoctorName;
            this.buttonType = buttonType;
            this.consultId = consultId;
            this.isEmptyPrint = isEmptyPrint;
        }

        @Override
        public void onPreExecute() {

            ProgressIndicator pi = new Method().getProgressBarWhite(20, 20);
            if (null != button) {

                if (button instanceof Button) {
                    ((Button) button).setGraphic(pi);

                } else if (button instanceof Hyperlink) {
                    ((Hyperlink) button).setGraphic(new Method().getProgressBarRed(20, 20));
                }
            }
        }
        @Override
        public Boolean doInBackground(String... params) {
            getDataPrint(prescribe_master_id, patientId, consultDoctorName, consultId, isEmptyPrint);
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {

            if (null != button) {
                ImageLoader loader = new ImageLoader();

                if (button != null && button instanceof Button && Objects.equals(buttonType, "icon")) {
                    ((Button) button).setGraphic(loader.getPrintIcon());
                }

                if (button instanceof Button) {
                    Button bn = (Button) button;
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

    private void getDataPrint(int prescribe_master_id, int patientId, String consultDoctorName, int consultId, boolean isEmptyPrint) {

        ObservableList<PrescribedMedicineModel> medicineList = FXCollections.observableArrayList();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            if (isEmptyPrint) {
                String qryConsultDetails = "select to_char(creation_date, 'DD/MM/YYYY HH12:MI AM') as consultDate,receipt_num from patient_consultation where consultation_id = ?";
                ps = connection.prepareStatement(qryConsultDetails);

                ps.setInt(1, consultId);
                rs = ps.executeQuery();

                if (rs.next()) {

                    String prescribeDate = rs.getString("consultDate");
                    String invoiceNum = rs.getString("receipt_num");

                    new GenerateInvoice().prescriptionInvoice(patientId, consultDoctorName, medicineList, invoiceNum, prescribeDate);
                }


                return;
            }


            String qry = """
                      select to_char(pmm.creation_date,'DD/MM/YYYY HH12:MI AM')as prescribeDate,* from prescribe_medicine_master pmm
                                        left join prescribe_medicine_items pmi on
                              pmm.prescribe_master_medicine_id = pmi.prescribe_master_medicine_id
                                        left outer join public.patient_consultation pc on pmm.consultation_id = pc.consultation_id
                      where pmm.prescribe_master_medicine_id = ? order by pmm.prescribe_master_medicine_id desc
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, prescribe_master_id);
            rs = ps.executeQuery();

            String invoiceNum = "";
            String prescribeDate = "";

            while (rs.next()) {
                int itemId = rs.getInt("ITEM_ID");
                boolean isItemExistsInStock = rs.getBoolean("IS_ITEM_EXISTS_IN_STOCK");
                String itemName = rs.getString("item_name");
                String composition = rs.getString("COMPOSITION");
                String tag = rs.getString("TAG");
                String remark = rs.getString("REMARK");
                String quantity = rs.getString("QUANTITY");
                String time = rs.getString("TIME");
                String dose = rs.getString("DOSE");
                String frequency = rs.getString("FREQUENCY");
                String duration = rs.getString("DURATION");
                prescribeDate = rs.getString("prescribeDate");
                invoiceNum = rs.getString("invoice_num") + " / " + rs.getString("receipt_num");

                PrescribedMedicineModel pmm = new PrescribedMedicineModel(itemName, tag, quantity,
                        frequency, duration, time, composition, dose, remark, itemId, isItemExistsInStock);

                medicineList.add(pmm);

            }


            new GenerateInvoice().prescriptionInvoice(patientId, consultDoctorName, medicineList, invoiceNum, prescribeDate);



        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }


    }




    public int totalPrescribeMedicine(int consultId) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "select count(*) as totalPrescribeMedicine from prescribe_medicine_master where consultation_id = ?";

            ps = connection.prepareStatement(qry);
            ps.setInt(1, consultId);

            rs = ps.executeQuery();

            rs.next();
            return rs.getInt("totalPrescribeMedicine");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    public int getPrescribeMasterId(int consultId) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "select prescribe_master_medicine_id as pmmi from prescribe_medicine_master where consultation_id = ?";

            ps = connection.prepareStatement(qry);
            ps.setInt(1, consultId);


            rs = ps.executeQuery();

            int id = 0;
            while (rs.next()) {
                id = rs.getInt("pmmi");
            }


            return id;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }
}
