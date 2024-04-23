package com.techwhizer.medicalshop.controller.master;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.master.model.WardFacilityMasterModel;
import com.techwhizer.medicalshop.controller.master.model.WardFacilityMasterModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class WardFacilityMaster implements Initializable {
    public TableView<WardFacilityMasterModel> tableview;
    public TableColumn<WardFacilityMasterModel,Integer> colSrNum;
    public TableColumn<WardFacilityMasterModel,String> colFacilityCode;
    public TableColumn<WardFacilityMasterModel,String> colFacilityName;
    public TableColumn<WardFacilityMasterModel,String> colCreatedDate;
    public TextField facilityCodeTf;
    public TextField facilityNameTf;
    public Button saveUpdateBn;
    public Button clearBn;
    private Method method;

    private ObjectProperty<Integer> facilityMasterId;
    private ObservableList<WardFacilityMasterModel> facilityList = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        clearBn.setVisible(false);
        facilityMasterId = new SimpleObjectProperty<>(0);

        facilityMasterId.addListener((observableValue, integer, t1) -> {

            if (t1 > 0) {
                saveUpdateBn.setText("UPDATE");
                clearBn.setVisible(true);
            } else {
                saveUpdateBn.setText("SAVE");
                clearBn.setVisible(false);
            }

        });

        new MyAsyncTask().execute();
    }

    public void clearBnClick(ActionEvent actionEvent) {
        clear();
    }

    public void saveUpdateButtonClick(ActionEvent actionEvent) {

        String facilityCode = facilityCodeTf.getText();
        String facilityName = facilityNameTf.getText();

        if (facilityCode.isEmpty()) {
            method.show_popup("Please enter facility code", facilityCodeTf, Side.RIGHT);
            return;
        } else if (facilityName.isEmpty()) {
            method.show_popup("Please enter facility name", facilityNameTf, Side.RIGHT);
            return;
        }


        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            if (facilityMasterId.getValue() > 0) {

                String qry = "UPDATE tbl_ward_facility SET facility_code=?, facility_name=?,last_updated_by=?,last_updated_date=?  WHERE ward_facility_id = ?";
                ps = connection.prepareStatement(qry);

                ps.setString(1, facilityCode);
                ps.setString(2, facilityName);
                ps.setInt(3, Login.currentlyLogin_Id);
                ps.setTimestamp(4, Method.getCurrenSqlTimeStamp());
                ps.setInt(5, facilityMasterId.get());

            } else {
                String qry = "INSERT INTO  tbl_ward_facility(facility_code, facility_name, created_by) VALUES (?,?,?)";
                ps = connection.prepareStatement(qry);

                ps.setString(1, facilityCode);
                ps.setString(2, facilityName);
                ps.setInt(3, Login.currentlyLogin_Id);
            }

            int res = ps.executeUpdate();
            if (res > 0) {
                clear();
                new MyAsyncTask().execute();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }
    private void clear() {
        facilityCodeTf.setText("");
        facilityNameTf.setText("");
        facilityMasterId.setValue(0);
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
            tableview.setPlaceholder(method.getProgressBarRed(40, 40));
        }

        @Override
        public Boolean doInBackground(String... params) {
            getFacilities();
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            tableview.setPlaceholder(new Label("Not Available."));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getFacilities() {

        facilityList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "select ward_facility_id,facility_code,facility_name,to_char(created_date,'DD-MM-YYYY HH12:MI AM') as created_date2 from tbl_ward_facility";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {

                int facilityId = rs.getInt("ward_facility_id");
                String facilityCode = rs.getString("facility_code");
                String facilityName = rs.getString("facility_name");
                String createdDate = rs.getString("created_date2");

                WardFacilityMasterModel bmm = new WardFacilityMasterModel(facilityId,facilityCode.toUpperCase(),facilityName,createdDate);
                facilityList.add(bmm);
            }

            colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colFacilityName.setCellValueFactory(new PropertyValueFactory<>("facilityName"));
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
            setOptionalCell();
            tableview.setItems(facilityList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void setOptionalCell() {

        Callback<TableColumn<WardFacilityMasterModel, String>, TableCell<WardFacilityMasterModel, String>>
                cellAdmissionNum = (TableColumn<WardFacilityMasterModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink facilityCodeHL = new Hyperlink(tableview.getItems().get(getIndex()).getFacilityCode());

                    facilityCodeHL.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 10;-fx-alignment: center-left");

                    facilityCodeHL.setMinWidth(125);

                    facilityCodeHL.setOnAction(actionEvent -> {
                        tableview.getSelectionModel().select(getIndex());
                        WardFacilityMasterModel pm = tableview.getSelectionModel().getSelectedItem();
                        facilityMasterId.setValue(pm.getWardFacilityId());
                        facilityCodeTf.setText(pm.getFacilityCode());
                        facilityNameTf.setText(pm.getFacilityName());
                    });
                    HBox managebtn = new HBox(facilityCodeHL);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };
        colFacilityCode.setCellFactory(cellAdmissionNum);

    }

}
