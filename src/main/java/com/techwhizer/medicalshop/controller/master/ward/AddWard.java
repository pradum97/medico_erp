package com.techwhizer.medicalshop.controller.master.ward;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.master.BuildingMaster;
import com.techwhizer.medicalshop.controller.master.model.BuildingMasterModel;
import com.techwhizer.medicalshop.controller.master.model.FacilityModel;
import com.techwhizer.medicalshop.controller.master.model.FloorMasterModel;
import com.techwhizer.medicalshop.controller.master.model.WardModel;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

public class AddWard  implements Initializable {
    public ComboBox<BuildingMasterModel> buildingNameCom;
    public ComboBox<FloorMasterModel> floorCom;
    public ComboBox<FacilityModel> facilityCom;
    public TextField wardNameTf;
    public TextField numOfBedsTf;
    public Button clearBn;
    public Button saveUpdateBn;

    private Method method;

    private WardModel wardModel;

    private enum Type {
        INIT,SAVE_UPDATE
    }

    private ObservableList<BuildingMasterModel> buildingList = FXCollections.observableArrayList();
    private ObservableList<FloorMasterModel> floorList = FXCollections.observableArrayList();
    private ObservableList<FacilityModel> facilityList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();

        if (  Main.primaryStage.getUserData() instanceof  WardModel wardModel1){
            wardModel = wardModel1;

            saveUpdateBn.setText("UPDATE");

        }

        init();
    }

    private void init(){
        callThread(Type.INIT,null);
        buildingNameCom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BuildingMasterModel>() {
            @Override
            public void changed(ObservableValue<? extends BuildingMasterModel> observableValue,
                                BuildingMasterModel buildingMasterModel, BuildingMasterModel t1) {
                if (t1 == null) {
                    getFloorList(0);
                }else {
                    getFloorList(t1.getBuildingMasterId());

                }

            }
        });
    }

    public void clearBnClick(ActionEvent actionEvent) {
        clearAll();
    }

    private void clearAll (){
        buildingNameCom.getSelectionModel().clearSelection();
        floorCom.getSelectionModel().clearSelection();
        facilityCom.getSelectionModel().clearSelection();
        wardNameTf.setText("");
        numOfBedsTf.setText("");
    }

    public void saveUpdateButtonClick(ActionEvent actionEvent) {

        String wardName = wardNameTf.getText();
        String noOfBeds = numOfBedsTf.getText();

        if (buildingNameCom.getSelectionModel().isEmpty()){
            method.show_popup("Please select building",buildingNameCom, Side.RIGHT);
            return;
        }else if (floorCom.getSelectionModel().isEmpty()){
            method.show_popup("Please select floor",floorCom, Side.RIGHT);
            return;
        }else if (facilityCom.getSelectionModel().isEmpty()){
            method.show_popup("Please select facility",facilityCom, Side.RIGHT);
            return;
        }else if (wardName.isEmpty()){
            method.show_popup("Please enter ward name",wardNameTf, Side.RIGHT);
            return;
        }else if (noOfBeds.isEmpty()){
            method.show_popup("Please enter no of beds",numOfBedsTf, Side.RIGHT);
            return;
        }

        int numberOfBeds = 0;
        try {
            numberOfBeds = Integer.parseInt(noOfBeds);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid no of beds",numOfBedsTf, Side.RIGHT);
            return;
        }

        int buildingId = buildingNameCom.getSelectionModel().getSelectedItem().getBuildingMasterId();
        int floorId = floorCom.getSelectionModel().getSelectedItem().getFloorMasterId();
        int facilityId = facilityCom.getSelectionModel().getSelectedItem().getFacilityId();

        WardFormData wfd = new WardFormData(buildingId,floorId,facilityId,wardName,numberOfBeds);
        callThread(Type.SAVE_UPDATE,wfd);
    }

    private void callThread(Type type,WardFormData wardFormData ){

        new MyAsyncTask(type,wardFormData).execute();
    }

    private static class WardFormData{
        private int buildingId,floorId,facilityId;
        private String wardName;
        private int noOfBeds;

        public WardFormData(int buildingId, int floorId, int facilityId, String wardName, int noOfBeds) {
            this.buildingId = buildingId;
            this.floorId = floorId;
            this.facilityId = facilityId;
            this.wardName = wardName;
            this.noOfBeds = noOfBeds;
        }

        public int getBuildingId() {
            return buildingId;
        }

        public void setBuildingId(int buildingId) {
            this.buildingId = buildingId;
        }

        public int getFloorId() {
            return floorId;
        }

        public void setFloorId(int floorId) {
            this.floorId = floorId;
        }

        public int getFacilityId() {
            return facilityId;
        }

        public void setFacilityId(int facilityId) {
            this.facilityId = facilityId;
        }

        public String getWardName() {
            return wardName;
        }

        public void setWardName(String wardName) {
            this.wardName = wardName;
        }

        public int getNoOfBeds() {
            return noOfBeds;
        }

        public void setNoOfBeds(int noOfBeds) {
            this.noOfBeds = noOfBeds;
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Type type;
        WardFormData wardFormData;

        public MyAsyncTask(Type type, WardFormData wardFormData) {
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

            switch (type){
                case INIT -> {
                    getBuildings();
                    getFacilities();

                    if (wardModel != null){
                       Platform.runLater(AddWard.this::setData);
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
            if (buildingList.get(i).getBuildingMasterId() == wardModel.getBuildingId()){
                buildingNameCom.getSelectionModel().select(i);
            }
        }

        for (int i = 0; i < floorList.size(); i++) {
            if (floorList.get(i).getFloorMasterId() == wardModel.getFloorId()){
                floorCom.getSelectionModel().select(i);
            }
        }

        for (int i = 0; i < facilityList.size(); i++) {
            if (facilityList.get(i).getFacilityId() == wardModel.getFacilityId()){
                facilityCom.getSelectionModel().select(i);
            }
        }

        wardNameTf.setText(wardModel.getWardName());
        numOfBedsTf.setText(String.valueOf(wardModel.getNoOfBeds()));
    }

    private void saveData(WardFormData wfd) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();
            connection.setAutoCommit(false);

            if (wardModel != null && wardModel.getWardId() > 0) {


                String qry = """
                        UPDATE tbl_wards SET building_id = ?, floor_id = ?, ward_facility_id = ?,ward_name = ?,
                                             number_of_beds = ?,last_updated_by=?,last_updated_date=? where ward_id = ?

                        """;
                ps = connection.prepareStatement(qry);

                ps.setInt(1, wfd.getBuildingId());
                ps.setInt(2, wfd.getFloorId());
                ps.setInt(3, wfd.getFacilityId());
                ps.setString(4, wfd.getWardName());
                ps.setInt(5, wfd.getNoOfBeds());
                ps.setInt(6, Login.currentlyLogin_Id);
                ps.setTimestamp(7, Method.getCurrenSqlTimeStamp());
                ps.setInt(8, wardModel.getWardId());

            } else {
                String qry = "INSERT INTO tbl_wards(building_id, floor_id, ward_facility_id, ward_name , number_of_beds, created_by) values (?,?,?,?,?,?)";
                ps = connection.prepareStatement(qry);

                ps.setInt(1, wfd.getBuildingId());
                ps.setInt(2, wfd.getFloorId());
                ps.setInt(3, wfd.getFacilityId());
                ps.setString(4, wfd.getWardName());
                ps.setInt(5, wfd.getNoOfBeds());
                ps.setInt(6, Login.currentlyLogin_Id);
            }

            int res = ps.executeUpdate();
            if (res > 0) {
                connection.commit();
                if (wardModel != null){
                    new CustomDialog().showAlertBox("Success","Ward successfully updated.");
                }else {
                    new CustomDialog().showAlertBox("Success","Ward successfully created.");

                }
                Main.primaryStage.setUserData(true);
                Platform.runLater(() -> method.closeStage(wardNameTf));
            }else {
                new CustomDialog().showAlertBox("Failed","Please try again.");
            }

        } catch (SQLException e) {
            new CustomDialog().showAlertBox("Error","Something went wrong.");
            throw new RuntimeException(e);
        } finally {
            saveUpdateBn.setDisable(false);
            clearBn.setDisable(false);
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    private void getBuildings() {
        buildingList.clear();
        System.out.println("ttttttt7777");

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
            buildingNameCom.setItems(buildingList);
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
            ps.setInt(1,buildingMasterId);
            ps.setInt(2,buildingMasterId);
            rs = ps.executeQuery();


            while (rs.next()) {

                int buildingId = rs.getInt("building_id");
                int floorMasterId = rs.getInt("floor_id");
                String floorNumber = rs.getString("floor_number");
                String floorName = rs.getString("floor_name");
                String createdDate = rs.getString("created_date2");
                String buildingName = rs.getString("building_name");

                FloorMasterModel bmm = new FloorMasterModel(floorMasterId,floorNumber,floorName,buildingId,createdDate,buildingName);
                floorList.add(bmm);
            }

            floorCom.setItems(floorList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void getFacilities() {
        facilityList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "select *,to_char(created_date,'DD-MM-YYYY HH:mm PM') as created_date from tbl_ward_facility";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {

                int facilityId = rs.getInt("ward_facility_id");
                String facilityCode = rs.getString("facility_code");
                String facilityName = rs.getString("facility_name");

                String buildingCreatedDate = rs.getString("created_date");

                FacilityModel bmm = new FacilityModel(facilityId,facilityCode, facilityName,buildingCreatedDate);
                facilityList.add(bmm);
            }
            facilityCom.setItems(facilityList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

}
