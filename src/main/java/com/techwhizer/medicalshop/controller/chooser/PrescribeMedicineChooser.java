package com.techwhizer.medicalshop.controller.chooser;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.PrintPrescription;
import com.techwhizer.medicalshop.model.chooserModel.PrescribeMedicineChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
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

public class PrescribeMedicineChooser implements Initializable {
    private int rowsPerPage = 20;
    public TableColumn<PrescribeMedicineChooserModel, Integer> colSrNo;
    public TableColumn<PrescribeMedicineChooserModel, String> colInvoiceNum;
    public TableColumn<PrescribeMedicineChooserModel, String> colConsultDate;
    public TableColumn<PrescribeMedicineChooserModel, String> colAction;
    public TableView<PrescribeMedicineChooserModel> tableView;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    private ObservableList<PrescribeMedicineChooserModel> list = FXCollections.observableArrayList();
    private FilteredList<PrescribeMedicineChooserModel> filteredData;

    private Map<String, Object> data;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        tableView.setFixedCellSize(28);

        if (Main.primaryStage.getUserData() != null &&
                Main.primaryStage.getUserData() instanceof Map) {
            data = (Map<String,Object>) Main.primaryStage.getUserData();
        }else {

            return;
        }

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
                    select prescribe_master_medicine_id as prescribe_master_id,invoice_num,consultation_id,
                    to_char(creation_date,'DD/MM/YYYY hh12:mi AM') as consult_date
                    from prescribe_medicine_master where consultation_id = ?
                    order by consultation_id  desc
                     """;

            ps = connection.prepareStatement(qry);
            ps.setInt(1,(int)data.get("consult_id"));

            rs = ps.executeQuery();

            int count = 0;

            while (rs.next()) {


                int prescribe_master_id = rs.getInt("prescribe_master_id");
                int consultation_id = rs.getInt("consultation_id");

                String invoice_num = rs.getString("invoice_num");
                String consult_date = rs.getString("consult_date");


                PrescribeMedicineChooserModel pm = new
                        PrescribeMedicineChooserModel(prescribe_master_id, consultation_id, invoice_num, consult_date);
                list.add(pm);
                count++;

            }

            tableView.setItems(list);
            colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableView.getItems().indexOf(cellData.getValue()) + 1));
            colInvoiceNum.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
            colConsultDate.setCellValueFactory(new PropertyValueFactory<>("consultDate"));

            setOptionalCell();

            if (count > 0) {
                map.put("is_success", true);
                map.put("message", "Many item find");
            } else {
                map.put("is_success", false);
                map.put("message", "Data not available");
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


    private void setOptionalCell() {

        Callback<TableColumn<PrescribeMedicineChooserModel, String>, TableCell<PrescribeMedicineChooserModel, String>>
                cellFactory = (TableColumn<PrescribeMedicineChooserModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    boolean isPrint = (Boolean) data.get("isPrint");
                    Button selectBn = new Button();

                    String imgPath = "img/icon/print_ic.png";

                    if (!isPrint){

                        imgPath = "img/icon/rightArrow_ic_white.png";
                    }


                    ImageView iv = new ImageView(new ImageLoader().load(imgPath));
                    iv.setFitHeight(12);
                    iv.setFitWidth(12);

                    selectBn.setGraphic(iv);
                    selectBn.setStyle("-fx-cursor: hand ;-fx-padding: 1 7 1 7; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");

                    selectBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        PrescribeMedicineChooserModel icm = tableView.getSelectionModel().getSelectedItem();

                        Main.primaryStage.setUserData(icm);
                        method.closeStage(tableView);

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

}
