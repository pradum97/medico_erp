package com.techwhizer.medicalshop.controller.product;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
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
    public Label batchNameTf;
    private StaticData staticData;
    private enum Type{
        GET,SAVE_UPDATE
    }

    private Method method;
    private int itemId, purchaseItemId, stockId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        staticData = new StaticData();
        comboBoxConfig();
        itemNameL.setFocusTraversable(true);

        if (Main.primaryStage.getUserData() instanceof Integer stock_id) {
            stockId = stock_id;
            callThread(stock_id, Type.GET, null);
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


        String tabPerStripStr = stripTabTf.getText();
        String stripTfValueStr = stripTf.getText();
        String tabTfValueStr = tabPcsTf.getText();

        String purchaseRateStr = purchaseRateTf.getText();
        String mrpStr = mrpTf.getText();
        String saleRateStr = saleRateTf.getText();

        if (tabPerStripStr.isEmpty()) {
            method.show_popup("Enter tab Per Strip", stripTabTf, Side.RIGHT);
            return;
        } else if (stripTfValueStr.isEmpty() && tabTfValueStr.isEmpty()) {
            method.show_popup("Enter strip or tab.", stripTf, Side.RIGHT);
            return;
        } else if (purchaseRateStr.isEmpty()) {
            method.show_popup("Enter purchase rate", purchaseRateTf, Side.RIGHT);
            return;
        } else if (mrpStr.isEmpty()) {
            method.show_popup("Enter mrp", mrpTf, Side.RIGHT);
            return;
        } else if (saleRateStr.isEmpty()) {
            method.show_popup("Enter sale rate", saleRateTf, Side.RIGHT);
            return;
        }


        int tabPerStrip = 0, stripTfValue = 0, tabTfValue = 0;
        double purchaseRate = 0, mrp = 0, saleRate = 0;

        try {
            tabPerStrip = Integer.parseInt(tabPerStripStr);
        } catch (NumberFormatException e) {
            method.show_popup("Enter valid value.", stripTabTf, Side.RIGHT);
            return;
        }

        try {
            stripTfValue = Integer.parseInt(stripTfValueStr);
        } catch (NumberFormatException e) {
            method.show_popup("Enter valid value", stripTf, Side.RIGHT);
            return;
        }

        try {
            tabTfValue = Integer.parseInt(tabTfValueStr);
        } catch (NumberFormatException e) {
            method.show_popup("Enter valid value.", tabPcsTf, Side.RIGHT);
            return;
        }


        try {
            purchaseRate = Double.parseDouble(purchaseRateStr);
        } catch (NumberFormatException e) {
            method.show_popup("Enter valid purchase rate", purchaseRateTf, Side.RIGHT);
            return;
        }

        try {
            mrp = Double.parseDouble(mrpStr);
        } catch (NumberFormatException e) {
            method.show_popup("Enter valid mrp", mrpTf, Side.RIGHT);
            return;
        }

        try {
            saleRate = Double.parseDouble(saleRateStr);
        } catch (NumberFormatException e) {
            method.show_popup("Enter valid sale rate", saleRateTf, Side.RIGHT);
            return;
        }

        String itemUnit = unitCom.getSelectionModel().getSelectedItem();

        StockUpdateData sub = new StockUpdateData(tabPerStrip, itemUnit, purchaseRate, mrp, saleRate, stripTfValue, tabTfValue);
        callThread(0, Type.SAVE_UPDATE, sub);
    }

    private void callThread(int stockId, Type type, StockUpdateData sub) {
        new MyAsyncTask(stockId, type, sub).execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int stockId;
        Type type;
        StockUpdateData sub;

        public MyAsyncTask(int stockId, Type type, StockUpdateData sub) {
            this.stockId = stockId;
            this.type = type;
            this.sub = sub;
        }

        @Override
        public void onPreExecute() {

            if (type == Type.SAVE_UPDATE) {
                updateBn.setDisable(true);
                closeBn.setDisable(true);
            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type){
                case GET -> getStockDetail(stockId);
                case SAVE_UPDATE -> updateData(sub);
            }

            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            if (type == Type.SAVE_UPDATE) {
                updateBn.setDisable(false);
                closeBn.setDisable(false);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void updateData(StockUpdateData sub) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();
            connection.setAutoCommit(false);
            String itemMasterQry = "UPDATE tbl_items_master set strip_tab = ? where item_id = ?";
            ps = connection.prepareStatement(itemMasterQry);
            ps.setInt(1, sub.stripPerTab);
            ps.setInt(2, itemId);

            int res = ps.executeUpdate();
            if (res > 0) {
                ps = null;
                res = 0;

                String purchaseItemQry = "UPDATE tbl_purchase_items SET purchase_rate = ?, mrp = ?, sale_price = ?,updated_by=?,updated_date=? where purchase_items_id = ?";
                ps = connection.prepareStatement(purchaseItemQry);

                ps.setDouble(1, sub.purchaseRate);
                ps.setDouble(2, sub.mrp);
                ps.setDouble(3, sub.saleRate);
                ps.setInt(4, Login.currentlyLogin_Id);
                ps.setTimestamp(5,Method.getCurrenSqlTimeStamp());
                ps.setInt(6, purchaseItemId);

                res = ps.executeUpdate();

                if (res > 0) {
                    ps = null;
                    res = 0;

                    String stockQry = "UPDATE tbl_stock SET quantity = ?, quantity_unit = ?,updated_by=?,updated_date=? WHERE stock_id = ?";
                    ps = connection.prepareStatement(stockQry);
                    ps.setInt(1, sub.getTotalUnit());
                    ps.setString(2, sub.getStockUnit());
                    ps.setInt(3, Login.currentlyLogin_Id);
                    ps.setTimestamp(4,Method.getCurrenSqlTimeStamp());
                    ps.setInt(5, stockId);

                    res = ps.executeUpdate();
                    if (res > 0) {
                        connection.commit();
                        new CustomDialog().showAlertBox("Success", "Stock successfully updated.");
                        Main.primaryStage.setUserData(true);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                method.closeStage(saleRateTf);

                            }
                        });
                    }
                }
            }

        } catch (SQLException e) {
            DBConnection.rollBack(connection);
            throw new RuntimeException(e);
        } finally {
            updateBn.setDisable(false);
            closeBn.setDisable(false);
            DBConnection.closeConnection(connection, ps, null);
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

                itemId = rs.getInt("item_id");
                purchaseItemId = rs.getInt("purchase_items_id");

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
                    stripTf.setText(strip.isEmpty()?"0":strip);
                    tabPcsTf.setText(pcs.isEmpty()?"0":pcs);

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
                    stripTf.setDisable(Objects.equals(unit, "PCS"));

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

            String qry = "SELECT purchase_rate,mrp,sale_price,batch FROM tbl_purchase_items WHERE purchase_items_id = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1,purchaseItemId);
            rs = ps.executeQuery();

            if (rs.next()){

                double purchaseMrp = rs.getDouble("purchase_rate");
                double mrp = rs.getDouble("mrp");
                double saleRate = rs.getDouble("sale_price");

                String batch = rs.getString("batch");


               Platform.runLater(() -> {
                   purchaseRateTf.setText(String.valueOf(purchaseMrp));
                   mrpTf.setText(String.valueOf(mrp));
                   saleRateTf.setText(String.valueOf(saleRate));
                   batchNameTf.setText(batch);
               });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }

    }

    private static class StockUpdateData {

        private int stripPerTab;
        private String itemUnit;
        private double purchaseRate, mrp, saleRate;
        private int stripTab, pcsTab;
        private int totalUnit;
        private String stockUnit;

        public StockUpdateData(int stripPerTab, String itemUnit, double purchaseRate, double mrp, double saleRate,
                               int stripTab, int pcsTab) {
            this.stripPerTab = stripPerTab;
            this.itemUnit = itemUnit;
            this.purchaseRate = purchaseRate;
            this.mrp = mrp;
            this.saleRate = saleRate;
            this.stripTab = stripTab;
            this.pcsTab = pcsTab;

            int totalUnit = pcsTab;

            boolean checkUnit = itemUnit.equalsIgnoreCase("STRIP");
            String stockUnit = checkUnit ? "TAB" : "PCS";

            if (checkUnit && stripTab > 0) {
                totalUnit += stripTab * stripPerTab;
                stockUnit = "TAB";
            }
            this.totalUnit = totalUnit;
            this.stockUnit = stockUnit;
        }

        public int getStripPerTab() {
            return stripPerTab;
        }

        public void setStripPerTab(int stripPerTab) {
            this.stripPerTab = stripPerTab;
        }

        public String getItemUnit() {
            return itemUnit;
        }

        public void setItemUnit(String itemUnit) {
            this.itemUnit = itemUnit;
        }

        public double getPurchaseRate() {
            return purchaseRate;
        }

        public void setPurchaseRate(double purchaseRate) {
            this.purchaseRate = purchaseRate;
        }

        public double getMrp() {
            return mrp;
        }

        public void setMrp(double mrp) {
            this.mrp = mrp;
        }

        public double getSaleRate() {
            return saleRate;
        }

        public void setSaleRate(double saleRate) {
            this.saleRate = saleRate;
        }

        public int getStripTab() {
            return stripTab;
        }

        public void setStripTab(int stripTab) {
            this.stripTab = stripTab;
        }

        public int getPcsTab() {
            return pcsTab;
        }

        public void setPcsTab(int pcsTab) {
            this.pcsTab = pcsTab;
        }

        public int getTotalUnit() {
            return totalUnit;
        }

        public void setTotalUnit(int totalUnit) {
            this.totalUnit = totalUnit;
        }

        public String getStockUnit() {
            return stockUnit;
        }

        public void setStockUnit(String stockUnit) {
            this.stockUnit = stockUnit;
        }
    }


}
