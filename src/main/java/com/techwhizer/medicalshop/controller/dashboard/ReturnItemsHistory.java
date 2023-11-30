package com.techwhizer.medicalshop.controller.dashboard;

import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.model.ReturnItemModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReturnItemsHistory implements Initializable {
    public TableView<ReturnItemModel> tableview;
    public TableColumn<ReturnItemModel, Integer> colSrNo;
    public TableColumn<ReturnItemModel, String> colProductName;
    public TableColumn<ReturnItemModel, String> colQty;

    private ObservableList<ReturnItemModel> itemsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableview.setFixedCellSize(28.0);
        int returnMainId = (Integer) Main.primaryStage.getUserData();
        getItems(returnMainId);
    }

    private void getItems(int returnMainId) {
        if (null != itemsList) {
            itemsList.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String query = """
                    select tsi.item_name ,tri.sale_item_id, concat(tri.quantity,'-',tri.quantity_unit) as quantity from tbl_return_items tri
                    left join tbl_sale_items tsi on tri.sale_item_id = tsi.sale_item_id where return_main_id = ?
                    """;
            ps = connection.prepareStatement(query);
            ps.setInt(1, returnMainId);

            rs = ps.executeQuery();

            while (rs.next()) {
                int sale_item_id = rs.getInt("sale_item_id");
                String itemName = rs.getString("item_name");
                String quantity = rs.getString("quantity");
                ReturnItemModel rmi = new ReturnItemModel(sale_item_id, itemName, quantity);
                itemsList.add(rmi);
            }

            tableview.setItems(itemsList);

            colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }
    }
}
