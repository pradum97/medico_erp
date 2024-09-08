package com.techwhizer.medicalshop.controller.master.ward;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.master.model.BuildingMasterModel;
import com.techwhizer.medicalshop.controller.master.model.FloorMasterModel;
import com.techwhizer.medicalshop.controller.master.model.WardModel;
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

public class WardMaster implements Initializable {
    public TableView<WardModel> tableview;
    public TableColumn<WardModel,Integer> colSrNum;
    public TableColumn<WardModel,String> colWardName;
    public TableColumn<WardModel,String> colFacilityName;
    public TableColumn<WardModel,String> colNoBeds;
    public TableColumn<WardModel,String> colCreatedDate;
    public TableColumn<WardModel,String> colAction;
    public ComboBox<BuildingMasterModel> buildingCom;
    public ComboBox<FloorMasterModel> floorCom;
    public Button searchBn;
    public Button addWardBn;


    private enum Type{
        INIT,SEARCH
    }

    private ObservableList<BuildingMasterModel> buildingList = FXCollections.observableArrayList();
    private ObservableList<FloorMasterModel> floorList = FXCollections.observableArrayList();
    private ObservableList<WardModel> wardList = FXCollections.observableArrayList();

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
        }

        search();
    }

    private void search(){

        if ((!buildingCom.getSelectionModel().isEmpty()) &&
                (!floorCom.getSelectionModel().isEmpty())){

            int buildingId = buildingCom.getSelectionModel().getSelectedItem().getBuildingMasterId();
            int floorId = floorCom.getSelectionModel().getSelectedItem().getFloorMasterId();

            Map<String,Object> data = new HashMap<>();
            data.put("building_id",buildingId);
            data.put("floor_id",floorId);
            callThread(Type.SEARCH,data);
        }

    }

    public void addWardBnClick(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("master/ward/addWard.fxml","");
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            buildingCom.getSelectionModel().select(0);
                            floorCom.getSelectionModel().select(0);
                            search();
                        }
                    });
                }
                case SEARCH -> getWard(data);
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

    private void getWard(Map<String, Object> data) {
        wardList.clear();

        int buildingId = (Integer) data.get("building_id");
        int floorId = (Integer) data.get("floor_id");

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
            ps.setInt(1,buildingId);
            ps.setInt(2,floorId);
            rs = ps.executeQuery();

            while (rs.next()) {

                int wardId= rs.getInt("ward_id");
                int facilityId= rs.getInt("ward_facility_id");
                int numberOfBeds= rs.getInt("number_of_beds");
                String buildingName = rs.getString("building_name");
                String floorName = rs.getString("floor_name");
                String facilityName = rs.getString("facility_name");
                String wardName = rs.getString("ward_name");
                String buildingCreatedDate = rs.getString("created_date2");

                WardModel wd = new WardModel(wardId,buildingId,floorId,facilityId,
                        buildingName,floorName,facilityName,wardName,numberOfBeds,buildingCreatedDate);

                wardList.add(wd);
            }

            colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colWardName.setCellValueFactory(new PropertyValueFactory<>("wardName"));
            colFacilityName.setCellValueFactory(new PropertyValueFactory<>("facilityName"));
            colNoBeds.setCellValueFactory(new PropertyValueFactory<>("noOfBeds"));
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
            tableview.setItems(wardList);
            setOptionalCell();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void setOptionalCell() {

        Callback<TableColumn<WardModel, String>, TableCell<WardModel, String>>
                cellFactoryAction = (TableColumn<WardModel, String> param) -> new TableCell<>() {
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
                        WardModel wm = tableview.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(wm);
                        customDialog.showFxmlDialog2("master/ward/addWard.fxml","");

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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildingList.clear();
            }
        });

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
}
