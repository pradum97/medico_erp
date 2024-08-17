package com.techwhizer.medicalshop.controller.master;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.master.model.BuildingMasterModel;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
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

public class BuildingMaster implements Initializable {
    public TableView<BuildingMasterModel> tableview;
    public TableColumn<BuildingMasterModel, Integer> colSrNum;
    public TableColumn<BuildingMasterModel, String> colBuildingNum;
    public TableColumn<BuildingMasterModel, String> colBuildingName;
    public TableColumn<BuildingMasterModel, String> colBuildingAddress;
    public TableColumn<BuildingMasterModel, String> colCreatedDate;
    public TextField buildingNameTF;
    public Button saveUpdateBn;
    public Button clearBn;
    public TextField buildingAddressTF;
    private Method method;

    private ObjectProperty<Integer> buildingMasterId;


    private ObservableList<BuildingMasterModel> buildingList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        clearBn.setVisible(false);
        buildingMasterId = new SimpleObjectProperty<>(0);

        buildingNameTF.textProperty().addListener((observableValue, s, t1) -> {

            clearBn.setVisible(!t1.isBlank() && !t1.isEmpty());

        });

        buildingMasterId.addListener((observableValue, integer, t1) -> {

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

    public void saveUpdateButtonClick(ActionEvent actionEvent) {

        String buildingName = buildingNameTF.getText();
        String buildingAddress = buildingAddressTF.getText();

        if (buildingName.isEmpty()) {
            method.show_popup("Please enter building name.", saveUpdateBn, Side.RIGHT);
            return;
        } else if (buildingAddress.isEmpty()) {
            method.show_popup("Please enter building address.", saveUpdateBn, Side.RIGHT);
            return;
        }

        Connection connection = null;
        PreparedStatement ps = null;

        String buildingNumber = new GenerateBillNumber().getBuildingNumber();

        try {
            connection = new DBConnection().getConnection();

            if (buildingMasterId.getValue() > 0) {

                String qry = "UPDATE  tbl_building SET  building_name = ?, address = ? , last_updated_by = ?,last_updated_date = ? WHERE building_id = ?";
                ps = connection.prepareStatement(qry);

                ps.setString(1, buildingName);
                ps.setString(2, buildingAddress);
                ps.setInt(3, Login.currentlyLogin_Id);
                ps.setTimestamp(4, Method.getCurrenSqlTimeStamp());
                ps.setInt(5, buildingMasterId.get());

            } else {
                String qry = "INSERT INTO tbl_building(building_number, building_name, address, created_by) VALUES (?,?,?,?)";
                ps = connection.prepareStatement(qry);

                ps.setString(1, buildingNumber);
                ps.setString(2, buildingName);
                ps.setString(3, buildingAddress);
                ps.setInt(4, Login.currentlyLogin_Id);
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
        buildingNameTF.setText("");
        buildingAddressTF.setText("");
        buildingMasterId.setValue(0);
    }

    public void clearBnClick(ActionEvent actionEvent) {
        clear();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
            tableview.setPlaceholder(method.getProgressBarRed(40, 40));
        }

        @Override
        public Boolean doInBackground(String... params) {
            getBuildings();
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

    private void getBuildings() {
        buildingList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "select *,to_char(created_date,'DD-MM-YYYY HH12:MI AM') as created_date2 from tbl_building";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {

                int buildingId = rs.getInt("building_id");
                String buildingNumber = rs.getString("building_number");
                String buildingName = rs.getString("building_name");
                String buildingAddress = rs.getString("address");
                String buildingCreatedDate = rs.getString("created_date2");

                BuildingMasterModel bmm = new BuildingMasterModel(buildingId, buildingNumber, buildingName, buildingAddress, buildingCreatedDate);
                buildingList.add(bmm);
            }

            colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colBuildingName.setCellValueFactory(new PropertyValueFactory<>("buildingName"));
            colBuildingAddress.setCellValueFactory(new PropertyValueFactory<>("buildingAddress"));
            colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("buildingCreatedDate"));
            setOptionalCell();
            tableview.setItems(buildingList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void setOptionalCell() {

        Callback<TableColumn<BuildingMasterModel, String>, TableCell<BuildingMasterModel, String>>
                cellAdmissionNum = (TableColumn<BuildingMasterModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink buildingNum = new Hyperlink(tableview.getItems().get(getIndex()).getBuildingNumber());

                    buildingNum.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 10;-fx-alignment: center-left");

                    buildingNum.setMinWidth(125);

                    buildingNum.setOnAction(actionEvent -> {
                        tableview.getSelectionModel().select(getIndex());
                        BuildingMasterModel pm = tableview.getSelectionModel().getSelectedItem();
                        buildingMasterId.setValue(pm.getBuildingMasterId());
                        buildingNameTF.setText(pm.getBuildingName());
                        buildingAddressTF.setText(pm.getBuildingAddress());
                    });
                    HBox managebtn = new HBox(buildingNum);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };
        colBuildingNum.setCellFactory(cellAdmissionNum);

    }
}
