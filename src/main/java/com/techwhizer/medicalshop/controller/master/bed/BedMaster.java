package com.techwhizer.medicalshop.controller.master.bed;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.master.model.*;
import com.techwhizer.medicalshop.controller.master.model.BedModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BedMaster implements Initializable {
    public TableView<BedModel> tableview;
    public TableColumn<BedModel,Integer> colSrNum;
    public TableColumn<BedModel,String> colBedNumber;
    public TableColumn<BedModel,String> colBedName;
    public TableColumn<BedModel,String> colRowNumber;
    public TableColumn<BedModel,String> colColumnNumber;
    public TableColumn<BedModel,String> colBedType;
    public TableColumn<BedModel,String> colBedStatus;
    public TableColumn<BedModel,String> colBedFor;
    public TableColumn<BedModel,String> colCreatedDate;
    public TableColumn<BedModel,String> colAction;
    public ComboBox<BuildingMasterModel> buildingCom;
    public ComboBox<FloorMasterModel> floorCom;
    public ComboBox<WardModel> wardCom;
    public Button searchBn;

    private enum Type{
        INIT,SEARCH
    }

    private ObservableList<BuildingMasterModel> buildingList = FXCollections.observableArrayList();
    private ObservableList<FloorMasterModel> floorList = FXCollections.observableArrayList();
    private ObservableList<WardModel> wardList = FXCollections.observableArrayList();
    private ObservableList<BedModel> bedList = FXCollections.observableArrayList();
    private Method method;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();

        init();
    }

    private void init(){
        callThread(Type.INIT,new HashMap<>());

        buildingCom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BuildingMasterModel>() {
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

    public void refresh(ActionEvent actionEvent) {

        callThread(Type.INIT,new HashMap<>());
    }

    public void searchBnClick(ActionEvent actionEvent) {

        if (buildingCom.getSelectionModel().isEmpty()){
            method.show_popup("Please select building",buildingCom, Side.RIGHT);
            return;
        }else if (floorCom.getSelectionModel().isEmpty()){
            method.show_popup("Please select floor",floorCom, Side.RIGHT);
            return;
        }else if (wardCom.getSelectionModel().isEmpty()){
            method.show_popup("Please select room/ward",wardCom, Side.RIGHT);
            return;
        }

        search();
    }

    private void search(){

        if ((!buildingCom.getSelectionModel().isEmpty()) &&
                (!floorCom.getSelectionModel().isEmpty()) &&
                (!wardCom.getSelectionModel().isEmpty())){
            int wardId = wardCom.getSelectionModel().getSelectedItem().getWardId();

            Map<String,Object> data = new HashMap<>();
            data.put("ward_id",wardId);
            callThread(Type.SEARCH,data);
        }

    }

    public void addBnClick(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("master/bed/addBed.fxml","");
    }

    private void callThread(Type type, Map<String, Object> data){
        new MyAsyncTask(type,data).execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        Type type;
        Map<String, Object> data;

        public MyAsyncTask(Type type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public void onPreExecute() {
            if (type == Type.SEARCH){
                tableview.setPlaceholder(new Method().getProgressBarRed(40,40));
            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type){
                case INIT -> {
                  getBuildings();
                    Platform.runLater(() -> {
                        buildingCom.getSelectionModel().select(0);
                    });
                }
                case SEARCH -> getBeds(data);
            }

            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            if (type == Type.SEARCH){
                tableview.setPlaceholder(null);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }


    }

    private void getBeds(Map<String, Object> data) {
        Platform.runLater(() -> bedList.clear());
        int wardId = (Integer) data.get("ward_id");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = """
                    select
                        tbed.bed_id,tb.building_id,tf.floor_id,tw.ward_id,
                        tbed.row_number,tbed.column_number,tbed.bed_number,
                        tbed.bed_name,tbed.bed_type,tbed.bed_status,tbed.bed_for,
                        to_char(tbed.created_date, 'DD-MM-YYYY HH12:MI AM') as created_date2
                    from tbl_beds tbed
                             left join tbl_wards tw on tbed.ward_id = tw.ward_id
                             left join tbl_floor tf on tf.floor_id = tw.floor_id
                             left join tbl_building tb on tb.building_id = tf.building_id
                    where tbed.ward_id = ? order by bed_id asc
                    
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1,wardId);
            rs = ps.executeQuery();

            while (rs.next()) {

                int bedId= rs.getInt("bed_id");
                int buildingId= rs.getInt("building_id");
                int floorId= rs.getInt("floor_id");
                int rowNumber= rs.getInt("row_number");
                int columnNumber= rs.getInt("column_number");
                String bedNumber = rs.getString("bed_number");
                String bedName = rs.getString("bed_name");
                String bedType = rs.getString("bed_type");
                String bedStatus = rs.getString("bed_status");
                String bedFor = rs.getString("bed_for");
                String createdDate = rs.getString("created_date2");

                BedModel wd = new BedModel(bedId,buildingId,floorId,wardId,rowNumber,columnNumber,
                        bedNumber,bedName,bedType,bedStatus,bedFor,createdDate);
                bedList.add(wd);
            }

            colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colBedNumber.setCellValueFactory(new PropertyValueFactory<>("bedNumber"));
            colBedName.setCellValueFactory(new PropertyValueFactory<>("bedName"));
            colRowNumber.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));
            colColumnNumber.setCellValueFactory(new PropertyValueFactory<>("columnNumber"));
            colBedType.setCellValueFactory(new PropertyValueFactory<>("bedType"));
            colBedStatus.setCellValueFactory(new PropertyValueFactory<>("bedStatus"));
            colBedFor.setCellValueFactory(new PropertyValueFactory<>("bedFor"));
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
            tableview.setItems(bedList);
            setOptionalCell();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void setOptionalCell() {

        Callback<TableColumn<BedModel, String>, TableCell<BedModel, String>>
                cellFactoryAction = (TableColumn<BedModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button editBn = new Button();

                    ImageView ivEdit = new ImageView(new ImageLoader().load("img/icon/update_ic.png"));
                    ivEdit.setFitHeight(12);
                    ivEdit.setFitWidth(12);


                    editBn.setGraphic(ivEdit);
                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ;-fx-padding: 2 4 2 4");

                    editBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        BedModel bm = tableview.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(bm);
                        customDialog.showFxmlDialog2("master/bed/addBed.fxml","");

                        if(Main.primaryStage.getUserData() instanceof Boolean isUpdated){
                            if(isUpdated){
                                search();
                            }
                        }
                    });


                    HBox managebtn = new HBox(editBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(editBn, new Insets(0, 8, 0, 8));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        };


        colAction.setCellFactory(cellFactoryAction);
    }

    private void getBuildings() {
        Platform.runLater(() -> buildingList.clear());

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
