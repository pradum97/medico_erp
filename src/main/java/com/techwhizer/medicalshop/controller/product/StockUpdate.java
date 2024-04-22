package com.techwhizer.medicalshop.controller.product;

import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class StockUpdate implements Initializable {
    public Label itemNameL;
    public ComboBox<String> unitCom;
    public VBox stripTabContainer;
    public TextField stripTabTf;
    public TextField stripTf;
    public TextField tabPcsTf;
    public TextField purchaseRateTf;
    public TextField mrpTf;
    public TextField saleRateTf;
    public Button closeBn;
    public Button updateBn;
    private StaticData staticData;
    private enum Type{
        GET,SAVE_UPDATE
    }

    private Method method;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        staticData = new StaticData();
        comboBoxConfig();

        if (Main.primaryStage.getUserData() instanceof Integer stockId){
            callThread(stockId,Type.GET);
        }

    }

    private void comboBoxConfig() {
        unitCom.setItems(staticData.getUnit());
        unitCom.valueProperty().addListener((observableValue, s, newValue) -> {
            stripTabTf.setText("");
            stripTabContainer.setDisable(!newValue.equalsIgnoreCase("STRIP"));
        });
    }

    public void closeBnClick(ActionEvent actionEvent) {
     method.closeStage(saleRateTf);
    }

    public void updateBnClick(ActionEvent actionEvent) {


    }
    private void callThread(int stockId, Type type){
        new MyAsyncTask(stockId,type).execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int stockId;
        Type type;

        public MyAsyncTask(int stockId, Type type) {
            this.stockId = stockId;
            this.type = type;
        }

        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type){
                case GET -> getStockDetail(stockId);
            }

            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getStockDetail(int stockId){

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try{
            connection = new DBConnection().getConnection();

            String qry = """
                    select item_id,purchase_items_id,full_quantity  from stock_v where stock_id = ?""";
            ps = connection.prepareStatement(qry);
            ps.setInt(1,stockId);
            rs = ps.executeQuery();

            if (rs.next()) {

                int itemId = rs.getInt("item_id");
                int purchaseItemId= rs.getInt("purchase_items_id");

                String fullQuantity= rs.getString("full_quantity");

                if (!fullQuantity.isBlank() ||! fullQuantity.isEmpty()){

                    String[] str = fullQuantity.split(",");
                    String strip = "";
                    String pcs = "";
                    if (str.length>1){
                         strip = str[0].split("-")[0];
                         pcs = str[1].split("-")[0];

                    }else {
                        String[] strAA = fullQuantity.split("-");
                        if (Objects.equals(strAA[1], "PCS") || Objects.equals(strAA[1], "TAB")){
                            pcs = strAA[0];
                        }else {
                            strip = strAA[0];
                        }

                    }
                    stripTf.setText(strip);
                    tabPcsTf.setText(pcs);

                }

                getItemDetailsByItemId(itemId);
                getPurchaseDetail(purchaseItemId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }

    }
    private void getItemDetailsByItemId(int itemId){

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try{
            connection = new DBConnection().getConnection();

            String qry = "SELECT unit,strip_tab,items_name as  item_name FROM tbl_items_master WHERE item_id = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1,itemId);
            rs = ps.executeQuery();

            if (rs.next()){
                String unit = rs.getString("unit");
                String itemName = rs.getString("item_name");
                int stripTab = rs.getInt("strip_tab");

                Platform.runLater(() -> {
                    stripTabContainer.setDisable(Objects.equals(unit, "PCS"));

                    unitCom.getSelectionModel().select(unit);
                    stripTabTf.setText(String.valueOf(stripTab));
                    itemNameL.setText(itemName);
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }

    }

    private void getPurchaseDetail(int purchaseItemId){

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try{
            connection = new DBConnection().getConnection();

            String qry = "SELECT purchase_rate,mrp,sale_price FROM tbl_purchase_items WHERE purchase_items_id = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1,purchaseItemId);
            rs = ps.executeQuery();

            if (rs.next()){

                double purchaseMrp = rs.getDouble("purchase_rate");
                double mrp = rs.getDouble("mrp");
                double saleRate = rs.getDouble("sale_price");

               Platform.runLater(() -> {
                   purchaseRateTf.setText(String.valueOf(purchaseMrp));
                   mrpTf.setText(String.valueOf(mrp));
                   saleRateTf.setText(String.valueOf(saleRate));
               });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }

    }
}
