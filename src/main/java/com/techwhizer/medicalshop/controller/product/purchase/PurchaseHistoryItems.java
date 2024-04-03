package com.techwhizer.medicalshop.controller.product.purchase;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.product.purchase.model.PurchaseHistoryItemModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PurchaseHistoryItems implements Initializable {
    public Label dealerNameL;
    public Label dealerAddressL;

    public TableView<PurchaseHistoryItemModel> tableview;
    public TableColumn<PurchaseHistoryItemModel,Integer> colSrNum;
    public TableColumn<PurchaseHistoryItemModel,String> colItemName;
    public TableColumn<PurchaseHistoryItemModel,String> colQuantity;
    public TableColumn<PurchaseHistoryItemModel,String> colPurchaseRate;
    public TableColumn<PurchaseHistoryItemModel,String> colMrp;
    public TableColumn<PurchaseHistoryItemModel,String> colSaleRate;
    public TableColumn<PurchaseHistoryItemModel,String> colBatch;
    public TableColumn<PurchaseHistoryItemModel,String> colExpiryDate;
    public TableColumn<PurchaseHistoryItemModel,String> colLotNum;

    private Method method;
    private CustomDialog customDialog;

    private ObservableList<PurchaseHistoryItemModel> itemList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        method = new Method();
        customDialog = new CustomDialog();
        tableview.setFixedCellSize(28.0);

        if (null != Main.primaryStage.getUserData() &&
                Main.primaryStage.getUserData() instanceof Map){

            Map<String,Object> data =( Map<String,Object> )  Main.primaryStage.getUserData();

            String dealerName =(String) data.get("dealer_name");
            String dealerAddress =(String)  data.get("dealer_address");
            int purchaseMainId = (Integer) data.get("purchase_main_id");

            dealerNameL.setText(dealerName);
            dealerAddressL.setText(dealerAddress);

            startThread(purchaseMainId);
//            Platform.runLater(()->{
//                Stage stage = (Stage) dealerAddressL.getScene().getWindow();
//                stage.setMaximized(true);
//            });
        }

    }

    private void startThread(int purchaseMainId) {
       new MyAsyncTask(purchaseMainId).execute();

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private int purchaseMainId;

        public MyAsyncTask(int purchaseMainId) {
            this.purchaseMainId = purchaseMainId;
        }

        @Override
        public void onPreExecute() {
            tableview.setPlaceholder(method.getProgressBarRed(40,40));

        }

        @Override
        public Boolean doInBackground(String... params) {

getPurchaseItems(purchaseMainId);
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            tableview.setPlaceholder(new Label("Item Not Found"));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getPurchaseItems(int purchaseMainId) {

        itemList.clear();
        itemList.removeAll();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = """
                    select tpi.purchase_items_id,purchase_main_id,tim.item_id,tim.items_name,
                           tab_to_strip(tpi.quantity,tim.strip_tab,tpi.quantity_unit) as quantity
                            ,tpi.batch,tpi.expiry_date,tpi.lot_number,
                          tpi.purchase_rate,tpi.mrp,tpi.sale_price
                                        
                    from tbl_purchase_items tpi
                             left join public.tbl_items_master tim on tim.item_id = tpi.item_id
                    where tpi.purchase_main_id = ? order by 1 desc
                    """;

            ps = connection.prepareStatement(qry);
            ps.setInt(1,purchaseMainId);
            rs = ps.executeQuery();

            while (rs.next()){
                int purchaseItemsId = rs.getInt("purchase_items_id");
                int purchaseMainIdResult = rs.getInt("purchase_main_id");
                int itemId = rs.getInt("item_id");
                String itemName = rs.getString("items_name");
                String quantity = rs.getString("quantity");
                String batch = rs.getString("batch");
                String expiryDate = rs.getString("expiry_date");
                String lotNumber = rs.getString("lot_number");
                double purchaseRate = rs.getDouble("purchase_rate");
                double mrp = rs.getDouble("mrp");
                double salePrice = rs.getDouble("sale_price");

                itemList.add(new PurchaseHistoryItemModel(purchaseItemsId,purchaseMainIdResult,itemId,itemName,
                        quantity,batch,expiryDate,lotNumber,purchaseRate,mrp,salePrice));
            }

            colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));
            colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
            colLotNum.setCellValueFactory(new PropertyValueFactory<>("lotNumber"));
            colPurchaseRate.setCellValueFactory(new PropertyValueFactory<>("purchaseRate"));
            colMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
            colSaleRate.setCellValueFactory(new PropertyValueFactory<>("salePrice"));

            tableview.setItems(itemList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }


    }



}
