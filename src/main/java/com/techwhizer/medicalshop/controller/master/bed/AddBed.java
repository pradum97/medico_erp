package com.techwhizer.medicalshop.controller.master.bed;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.master.model.BedModel;
import com.techwhizer.medicalshop.controller.master.model.BuildingMasterModel;
import com.techwhizer.medicalshop.controller.master.model.FloorMasterModel;
import com.techwhizer.medicalshop.controller.master.model.WardModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddBed implements Initializable {
    public Button clearBn;
    public Button saveUpdateBn;
    public TextField bedNameTf;
    public TextField bedNumberTf;
    public TextField rowNumTf;
    public TextField columnNumberTf;
    public ComboBox<String> bedTypeCom;
    public ComboBox<WardModel> wardCom;
    public ComboBox<String> bedStatusCom;
    public ComboBox<String> bedForCom;
    public ComboBox<BuildingMasterModel> buildingCom;
    public ComboBox<FloorMasterModel> floorCom;

    private Method method;

    private BedModel bedModel;

    private enum Type {
        INIT, SAVE_UPDATE
    }

    private ObservableList<BuildingMasterModel> buildingList = FXCollections.observableArrayList();
    private ObservableList<FloorMasterModel> floorList = FXCollections.observableArrayList();
    private ObservableList<WardModel> wardList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();

        if (Main.primaryStage.getUserData() instanceof BedModel bedModel1) {
            bedModel = bedModel1;

            saveUpdateBn.setText("UPDATE");

        }

        init();
    }

    private void init() {
        bedTypeCom.setItems(CommonUtil.getBedType());
        bedStatusCom.setItems(CommonUtil.getBedStatus());
        bedForCom.setItems(CommonUtil.getBedFor());
        bedStatusCom.getSelectionModel().select(0);
        callThread(Type.INIT, null);
        buildingCom.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, buildingMasterModel, t1) -> {
                    if (t1 == null) {
                        getFloorList(0);
                    } else {
                        getFloorList(t1.getBuildingMasterId());

                    }

                });

        floorCom.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, floorMasterModel, t1) -> {

                    if (!buildingCom.getSelectionModel().isEmpty()) {
                        if (t1 == null) {
                            getWard(0, 0);
                        } else {
                            getWard(buildingCom.getSelectionModel().getSelectedItem().getBuildingMasterId(), t1.getFloorMasterId());
                        }
                    } else {
                        new CustomDialog().showAlertBox("Failed", "Please Select Building");
                    }

                });
    }

    public void clearBnClick(ActionEvent actionEvent) {
        clearAll();
    }

    private void clearAll() {
        buildingCom.getSelectionModel().clearSelection();
        floorCom.getSelectionModel().clearSelection();
        wardCom.getSelectionModel().clearSelection();
        bedForCom.getSelectionModel().clearSelection();
        bedStatusCom.getSelectionModel().clearSelection();
        bedTypeCom.getSelectionModel().clearSelection();
        bedNameTf.setText("");
        bedNumberTf.setText("");
        rowNumTf.setText("");
        columnNumberTf.setText("");
    }

    private void clear() {
        bedNameTf.setText("");
        bedNumberTf.setText("");
        rowNumTf.setText("");
        columnNumberTf.setText("");
    }

    public void saveUpdateButtonClick(ActionEvent actionEvent) {

        String bedName = bedNameTf.getText();
        String bedNumber = bedNumberTf.getText();
        String rowNumberStr = rowNumTf.getText();
        String columnNumberStr = columnNumberTf.getText();

        if (buildingCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select building", buildingCom, Side.RIGHT);
            return;
        } else if (floorCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select floor", floorCom, Side.RIGHT);
            return;
        } else if (wardCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select ward.room", wardCom, Side.RIGHT);
            return;
        } else if (bedName.isEmpty()) {
            method.show_popup("Please enter bed name", bedNameTf, Side.RIGHT);
            return;
        } else if (bedNumber.isEmpty()) {
            method.show_popup("Please enter bed number", bedNumberTf, Side.RIGHT);
            return;
        } else if (bedTypeCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select bed type", bedTypeCom, Side.RIGHT);
            return;
        } else if (rowNumberStr.isEmpty()) {
            method.show_popup("Please enter row number", rowNumTf, Side.RIGHT);
            return;
        } else if (columnNumberStr.isEmpty()) {
            method.show_popup("Please enter column number", columnNumberTf, Side.RIGHT);
            return;
        } else if (bedStatusCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select bed status", bedStatusCom, Side.RIGHT);
            return;
        } else if (bedForCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select bed for", bedForCom, Side.RIGHT);
            return;
        }

        int rowNum = 0, columnNum,bedNumberI = 0;
        try {
            rowNum = Integer.parseInt(rowNumberStr);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid row number", rowNumTf, Side.RIGHT);
            return;
        }

        try {
            columnNum = Integer.parseInt(columnNumberStr);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid row column", columnNumberTf, Side.RIGHT);
            return;
        }

        try {
            bedNumberI = Integer.parseInt(bedNumber);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid bed number", columnNumberTf, Side.RIGHT);
            return;
        }

        int wardId = wardCom.getSelectionModel().getSelectedItem().getWardId();
        String bedType = bedTypeCom.getSelectionModel().getSelectedItem();
        String bedStatus = bedStatusCom.getSelectionModel().getSelectedItem();
        String bedFor = bedForCom.getSelectionModel().getSelectedItem();

        FormData wfd = new FormData(wardId, bedName, bedNumber, bedType, rowNum, columnNum, bedStatus, bedFor);
        callThread(Type.SAVE_UPDATE, wfd);
    }

    private void callThread(Type type, FormData wardFormData) {

        new MyAsyncTask(type, wardFormData).execute();
    }

    private static class FormData {
        private int wardId;
        private String bedName;
        private String bedNumber;
        private String bedType;
        private int rowNumber;
        private int columnNumber;
        private String bedStatus;
        private String bedFor;

        public FormData(int wardId, String bedName, String bedNumber, String bedType,
                        int rowNumber, int columnNumber, String bedStatus, String bedFor) {
            this.wardId = wardId;
            this.bedName = bedName;
            this.bedNumber = bedNumber;
            this.bedType = bedType;
            this.rowNumber = rowNumber;
            this.columnNumber = columnNumber;
            this.bedStatus = bedStatus;
            this.bedFor = bedFor;
        }

        public int getWardId() {
            return wardId;
        }

        public void setWardId(int wardId) {
            this.wardId = wardId;
        }

        public String getBedName() {
            return bedName;
        }

        public void setBedName(String bedName) {
            this.bedName = bedName;
        }

        public String getBedNumber() {
            return bedNumber;
        }

        public void setBedNumber(String bedNumber) {
            this.bedNumber = bedNumber;
        }

        public String getBedType() {
            return bedType;
        }

        public void setBedType(String bedType) {
            this.bedType = bedType;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public void setColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
        }

        public String getBedStatus() {
            return bedStatus;
        }

        public void setBedStatus(String bedStatus) {
            this.bedStatus = bedStatus;
        }

        public String getBedFor() {
            return bedFor;
        }

        public void setBedFor(String bedFor) {
            this.bedFor = bedFor;
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Type type;
        FormData wardFormData;

        public MyAsyncTask(Type type, FormData wardFormData) {
            this.type = type;
            this.wardFormData = wardFormData;
        }

        @Override
        public void onPreExecute() {
            if (type == Type.SAVE_UPDATE) {
                saveUpdateBn.setDisable(true);
                clearBn.setDisable(true);

            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type) {
                case INIT -> {
                    getBuildings();
                    if (bedModel != null) {
                        Platform.runLater(AddBed.this::setData);
                    }
                }
                case SAVE_UPDATE -> saveData(wardFormData);
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            if (type == Type.SAVE_UPDATE) {
                saveUpdateBn.setDisable(false);
                clearBn.setDisable(false);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void setData() {

        for (int i = 0; i < buildingList.size(); i++) {
            if (buildingList.get(i).getBuildingMasterId() == bedModel.getBuildingId()) {
                buildingCom.getSelectionModel().select(i);
            }
        }

        for (int i = 0; i < floorList.size(); i++) {
            if (floorList.get(i).getFloorMasterId() == bedModel.getFloorId()) {
                floorCom.getSelectionModel().select(i);
            }
        }

        for (int i = 0; i < wardList.size(); i++) {
            if (wardList.get(i).getWardId() == bedModel.getWardId()) {
                wardCom.getSelectionModel().select(i);
            }
        }

        bedForCom.getSelectionModel().select(bedModel.getBedFor());
        bedStatusCom.getSelectionModel().select(bedModel.getBedStatus());
        bedTypeCom.getSelectionModel().select(bedModel.getBedType());
        bedNameTf.setText(bedModel.getBedName());
        bedNumberTf.setText(bedModel.getBedNumber());
        rowNumTf.setText(String.valueOf(bedModel.getRowNumber()));
        columnNumberTf.setText(String.valueOf(bedModel.getColumnNumber()));
    }

    private void saveData(FormData wfd) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();
            connection.setAutoCommit(false);

            if (bedModel != null && bedModel.getBedId() > 0) {


                String qry = """
                        UPDATE tbl_beds SET ward_id = ?, row_number = ?,column_number = ?,bed_number= ?,bed_name= ?,
                        bed_type= ?,bed_status= ?,bed_for= ?,last_updated_by = ?,last_updated_date= ? WHERE bed_id = ?
                        """;
                ps = connection.prepareStatement(qry);

                ps.setInt(1, wfd.getWardId());
                ps.setInt(2, wfd.getRowNumber());
                ps.setInt(3, wfd.getColumnNumber());
                ps.setString(4, wfd.getBedNumber());
                ps.setString(5, wfd.getBedName());
                ps.setString(6, wfd.getBedType());
                ps.setString(7, wfd.getBedStatus());
                ps.setString(8, wfd.getBedFor());
                ps.setInt(9,Login.currentlyLogin_Id);
                ps.setTimestamp(10, Method.getCurrenSqlTimeStamp());
                ps.setInt(11,bedModel.getBedId());

            } else {
                String qry = """
                        INSERT INTO tbl_beds(ward_id, row_number, column_number, bed_number,
                        bed_name, bed_type, bed_status, bed_for,created_by)VALUES (?,?,?,?,?,?,?,?,?)
                        """;
                ps = connection.prepareStatement(qry);

                ps.setInt(1, wfd.getWardId());
                ps.setInt(2, wfd.getRowNumber());
                ps.setInt(3, wfd.getColumnNumber());
                ps.setString(4, wfd.getBedNumber());
                ps.setString(5, wfd.getBedName());
                ps.setString(6, wfd.getBedType());
                ps.setString(7, wfd.getBedStatus());
                ps.setString(8, wfd.getBedFor());
                ps.setInt(9,Login.currentlyLogin_Id);
            }

            int res = ps.executeUpdate();
            if (res > 0) {
                connection.commit();
                Main.primaryStage.setUserData(true);

                if (bedModel != null) {
                    new CustomDialog().showAlertBox("Success", "Bed successfully updated.");
                    Platform.runLater(() -> method.closeStage(bedNameTf));
                } else {
                    Platform.runLater(() -> {
                        clear();
                        bedModel = null;
                    });
                    new CustomDialog().showAlertBox("Success", "Bed successfully created.");

                }

            } else {
                new CustomDialog().showAlertBox("Failed", "Please try again.");
            }

        } catch (SQLException e) {
            new CustomDialog().showAlertBox("Error", "Something went wrong.");
            throw new RuntimeException(e);
        } finally {
            saveUpdateBn.setDisable(false);
            clearBn.setDisable(false);
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    private void getBuildings() {
        buildingList.clear();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "select *,to_char(created_date,'DD-MM-YYYY HH:mm PM') as created_date from tbl_building";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {

                int buildingId = rs.getInt("building_id");
                String buildingNumber = rs.getString("building_number");
                String buildingName = rs.getString("building_name");
                String buildingAddress = rs.getString("address");
                String buildingCreatedDate = rs.getString("created_date");

                BuildingMasterModel bmm = new BuildingMasterModel(buildingId, buildingNumber, buildingName, buildingAddress, buildingCreatedDate);
                buildingList.add(bmm);
            }
            buildingCom.setItems(buildingList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void getFloorList(int buildingMasterId) {

        floorList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = """
                                                         
                    select *,to_char(created_date,'DD-MM-YYYY HH12:MI AM') as created_date2,(select building_name from tbl_building
                                        where tbl_floor.building_id =tbl_building.building_id ) as building_name from tbl_floor
                                        where building_id = case when ? > 0 then ? else building_id end order by building_name, floor_number
                                                           """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, buildingMasterId);
            ps.setInt(2, buildingMasterId);
            rs = ps.executeQuery();


            while (rs.next()) {

                int buildingId = rs.getInt("building_id");
                int floorMasterId = rs.getInt("floor_id");
                String floorNumber = rs.getString("floor_number");
                String floorName = rs.getString("floor_name");
                String createdDate = rs.getString("created_date2");
                String buildingName = rs.getString("building_name");

                FloorMasterModel bmm = new FloorMasterModel(floorMasterId, floorNumber, floorName, buildingId, createdDate, buildingName);
                floorList.add(bmm);
            }

            floorCom.setItems(floorList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void getWard(int buildingId, int floorId) {
        wardList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = """
                    select td.ward_id,tb.building_name,tf.floor_name,twf.facility_name,td.number_of_beds,td.ward_name,
                    td.ward_facility_id,
                    to_char(td.created_date,'DD-MM-YYYY HH12:MI AM') as created_date2 from tbl_wards td
                    left  join tbl_building tb on tb.building_id = td.building_id
                    left  join tbl_floor tf on tf.floor_id = td.floor_id
                    left  join tbl_ward_facility twf on td.ward_facility_id = twf.ward_facility_id
                    where td.building_id = ? and td.floor_id = ? order by ward_id
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, buildingId);
            ps.setInt(2, floorId);
            rs = ps.executeQuery();

            while (rs.next()) {

                int wardId = rs.getInt("ward_id");
                int facilityId = rs.getInt("ward_facility_id");
                int numberOfBeds = rs.getInt("number_of_beds");
                String buildingName = rs.getString("building_name");
                String floorName = rs.getString("floor_name");
                String facilityName = rs.getString("facility_name");
                String wardName = rs.getString("ward_name");
                String buildingCreatedDate = rs.getString("created_date2");

                WardModel wd = new WardModel(wardId, buildingId, floorId, facilityId,
                        buildingName, floorName, facilityName, wardName, numberOfBeds, buildingCreatedDate);

                wardList.add(wd);
            }

            wardCom.setItems(wardList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

}
