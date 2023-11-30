package com.techwhizer.medicalshop.controller.patient;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.MedicalHistoryItemsModel;
import com.techwhizer.medicalshop.model.MedicalHistoryItemsModel;
import com.techwhizer.medicalshop.model.MedicalHistoryMainModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MedicalHistoryItems implements Initializable {
    public TableView<MedicalHistoryItemsModel> tableview;
    public TableColumn<MedicalHistoryItemsModel, Integer> colSr;
    public TableColumn<MedicalHistoryItemsModel, String> colItemName;
    public TableColumn<MedicalHistoryItemsModel, String> colDose;
    public TableColumn<MedicalHistoryItemsModel, String> colQty;
    public TableColumn<MedicalHistoryItemsModel, String> colDuration;
    public TableColumn<MedicalHistoryItemsModel, String> colTime;
    public TableColumn<MedicalHistoryItemsModel, String> colFrequency;
    public TableColumn<MedicalHistoryItemsModel, String> colRemark;

    private final ObservableList<MedicalHistoryItemsModel> historyItemList = FXCollections.observableArrayList();

    private int historyMainId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (Main.primaryStage.getUserData() != null &&
                Main.primaryStage.getUserData() instanceof Integer) {

            historyMainId = (int) Main.primaryStage.getUserData();

            if (historyMainId > 0) {


                getHistoryItems();
            }

        }
    }

    private void getHistoryItems() {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = """

               select * from patient_medical_history_item where medical_main_id = ?
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, historyMainId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int historyItemId = rs.getInt("MEDICAL_ITEM_ID");
                int itemId = rs.getInt("ITEM_ID");
                int historyMainId = rs.getInt("MEDICAL_MAIN_ID");
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
                String status = rs.getString("STATUS");
                String creationDate = rs.getString("CREATION_DATE");

                MedicalHistoryItemsModel it = new MedicalHistoryItemsModel(historyItemId,itemId,historyMainId,itemName,
                        isItemExistsInStock,composition,tag,quantity,time,dose,frequency,duration,status,remark,creationDate);

                historyItemList.add(it);

            }

            tableview.setItems(historyItemList);
            colSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colDose.setCellValueFactory(new PropertyValueFactory<>("dose"));
            colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
            colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
            colFrequency.setCellValueFactory(new PropertyValueFactory<>("frequency"));
            colRemark.setCellValueFactory(new PropertyValueFactory<>("remarks"));

            setOptionCell();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }


    private void setOptionCell() {

        Callback<TableColumn<MedicalHistoryItemsModel, String>, TableCell<MedicalHistoryItemsModel, String>>
                cellAction = (TableColumn<MedicalHistoryItemsModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    MedicalHistoryItemsModel hi = tableview.getItems().get(getIndex());

                    Label itemName = new Label(hi.getItemName());
                    Label composition = new Label(hi.getComposition());



                    itemName.setStyle("-fx-background-color: transparent; -fx-text-fill: black;-fx-border-color: transparent");
                    composition.setStyle("-fx-background-color: transparent; -fx-text-fill: grey;-fx-border-color: transparent");



                    VBox managebtn = new VBox(itemName,composition);
                  //  managebtn.setStyle("-fx-alignment:left");


                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colItemName.setCellFactory(cellAction);
    }

    public void closeButton(ActionEvent actionEvent) {

        new Method().closeStage(tableview);
    }
}
