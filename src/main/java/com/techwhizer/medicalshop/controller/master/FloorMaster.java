package com.techwhizer.medicalshop.controller.master;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.master.model.BuildingMasterModel;
import com.techwhizer.medicalshop.controller.master.model.FloorMasterModel;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

public class FloorMaster implements Initializable {
    public TableView<FloorMasterModel> tableview;
    public TableColumn<FloorMasterModel,Integer> colSrNum;
    public TableColumn<FloorMasterModel,String> colBuildingName;
    public TableColumn<FloorMasterModel,String> colFloorNum;
    public TableColumn<FloorMasterModel,String> colFloorName;
    public TableColumn<FloorMasterModel,String> colCreatedDate;
    public ComboBox<BuildingMasterModel> buildingNameCom;
    public ComboBox<BuildingMasterModel> buildingNameFilterCom;

    public TextField floorNumberTf;
    public TextField floorNameTf;
    public Button saveUpdateBn;
    public Button clearBn;
    private Method method;

    private ObjectProperty<Integer> floorMasterId;
    private ObservableList<BuildingMasterModel> buildingList = FXCollections.observableArrayList();
    private ObservableList<FloorMasterModel> floorList = FXCollections.observableArrayList();

    private enum Type{
        GET,INIT
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        clearBn.setVisible(false);
        floorMasterId = new SimpleObjectProperty<>(0);


        floorMasterId.addListener((observableValue, integer, t1) -> {

            if (t1 > 0) {
                saveUpdateBn.setText("UPDATE");
                clearBn.setVisible(true);
            } else {
                saveUpdateBn.setText("SAVE");
                clearBn.setVisible(false);
            }

        });

        new MyAsyncTask(0,Type.INIT).execute();

        init();
    }

    private void init(){

        buildingNameFilterCom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BuildingMasterModel>() {
            @Override
            public void changed(ObservableValue<? extends BuildingMasterModel> observableValue,
                                BuildingMasterModel buildingMasterModel, BuildingMasterModel t1) {

                new MyAsyncTask(t1.getBuildingMasterId(),Type.GET).execute();

            }
        });
    }

    public void saveUpdateButtonClick(ActionEvent actionEvent) {

        String floorNumber = floorNumberTf.getText();
        String floorName = floorNameTf.getText();

       if (buildingNameCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select building", buildingNameCom, Side.RIGHT);
            return;
        } else if (floorNumber.isEmpty()) {
            method.show_popup("Please enter floor number.", floorNumberTf, Side.RIGHT);
            return;
        } else if (floorName.isEmpty()) {
            method.show_popup("Please enter floor name.", floorNameTf, Side.RIGHT);
            return;
        }

        int floorNumberInt = 0;
        try {
            floorNumberInt = Integer.parseInt(floorNumber);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid floor number.", floorNumberTf, Side.RIGHT);
            return;
        }

        int buildingId = buildingNameCom.getSelectionModel().getSelectedItem().getBuildingMasterId();
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            if (floorMasterId.get() > 0) {

                String qry = "UPDATE  tbl_floor SET  floor_number = ?, floor_name = ?,building_id = ? , last_updated_by = ?,last_updated_date = ? WHERE floor_id = ?";
                ps = connection.prepareStatement(qry);

                ps.setInt(1, floorNumberInt);
                ps.setString(2, floorName);
                ps.setInt(3, buildingId);
                ps.setInt(4, Login.currentlyLogin_Id);
                ps.setTimestamp(5, Method.getCurrenSqlTimeStamp());
                ps.setInt(6, floorMasterId.get());
            } else {
                String qry = "INSERT INTO tbl_floor(floor_number, floor_name, building_id, created_by) VALUES (?,?,?,?)";
                ps = connection.prepareStatement(qry);

                ps.setInt(1, floorNumberInt);
                ps.setString(2, floorName);
                ps.setInt(3, buildingId);
                ps.setInt(4, Login.currentlyLogin_Id);
            }

            int res = ps.executeUpdate();
            if (res > 0) {
                clearAll();

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    public void clearBnClick(ActionEvent actionEvent) {
        clearAll();
    }

    private void clearAll(){
        buildingNameCom.getSelectionModel().clearSelection();
        floorNameTf.setText("");
        floorNumberTf.setText("");
        floorMasterId.setValue(0);
        new MyAsyncTask(0,Type.INIT).execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int buildingId;
        Type type;

        public MyAsyncTask(int buildingId, Type type) {
            this.buildingId = buildingId;
            this.type = type;

        }

        @Override
        public void onPreExecute() {
            switch (type){
                case GET -> tableview.setPlaceholder(method.getProgressBarRed(40, 40));
            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type){
                case GET -> getFloorList(buildingId);
                case INIT -> {
                    getFloorList(buildingId);
                    getBuildings();
                }
            }

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

    private void getFloorList(int buildingMasterId) {

        floorList.clear();
        tableview.setItems(null);
        tableview.refresh();

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

            colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colBuildingName.setCellValueFactory(new PropertyValueFactory<>("buildingName"));
            colFloorNum.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
            setOptionalCell();

           tableview.setItems(floorList);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void setOptionalCell() {

        Callback<TableColumn<FloorMasterModel, String>, TableCell<FloorMasterModel, String>>
                cellFloorName = (TableColumn<FloorMasterModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink buildingNum = new Hyperlink(tableview.getItems().get(getIndex()).getFloorName());

                    buildingNum.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 10;-fx-alignment: center-left");

                    buildingNum.setMinWidth(125);

                    buildingNum.setOnAction(actionEvent -> {
                        tableview.getSelectionModel().select(getIndex());
                        FloorMasterModel pm = tableview.getSelectionModel().getSelectedItem();
                        floorMasterId.setValue(pm.getFloorMasterId());
                        floorNumberTf.setText(pm.getFloorNumber());
                        floorNameTf.setText(pm.getFloorName());

                        ObservableList<BuildingMasterModel> bmmL = buildingNameCom.getItems();

                        for (int i = 0; i < bmmL.size(); i++) {
                            if (bmmL.get(i).getBuildingName().equals(pm.getBuildingName())){
                                int finalI = i;
                                Platform.runLater(() -> buildingNameCom.getSelectionModel().select(finalI));
                            }
                        }

                    });
                    HBox managebtn = new HBox(buildingNum);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };
        colFloorName.setCellFactory(cellFloorName);

    }

    private void getBuildings() {
        buildingList.clear();
        buildingNameFilterCom.getSelectionModel().select(0);

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
            buildingNameFilterCom.setItems(buildingList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }
}
