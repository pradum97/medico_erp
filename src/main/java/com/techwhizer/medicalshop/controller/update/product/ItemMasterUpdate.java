package com.techwhizer.medicalshop.controller.update.product;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.common.model.DepartmentModel;
import com.techwhizer.medicalshop.method.GetTax;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.model.*;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ItemMasterUpdate implements Initializable {
    public Button updateButton;
    public TextField productNameTf;
    public ComboBox<GstModel> hsnCom;
    public ComboBox<String> typeCom;
    public ComboBox<String> narcoticCom;
    public ComboBox<String> itemTypeCom;
    public ComboBox<String> statusCom;
    public ComboBox<DiscountModel> discountCom;
    public TextField  stripTabTf;
    public ComboBox<String> unitCom;
    public ProgressIndicator progressBar;
    public Label mrL;
    public Label mfrL;
    public Label companyNameL;
    public ProgressIndicator progressBarMain;
    public VBox mainContainer;
    public TextField compositionTf;
    public TextField productTag;
    public TextField medicineDoseTf;
    public HBox mrpContainer;
    public TextField mrpTf;
    public VBox stockableContaier;
    public ComboBox<DepartmentModel> departmentCom;
    private Method method;
    public Label stripTabLabel;
    public VBox stripTabContainer;
    private CustomDialog customDialog;
    private StaticData staticData;
    private DBConnection dbConnection;
    private ObservableList<DealerModel> dealerList = FXCollections.observableArrayList();
    private ObservableList<String> typeList = FXCollections.observableArrayList("NORMAL", "PROHIBIT");
    private ObservableList<String> narcoticList = FXCollections.observableArrayList("NO", "YES");
    private ObservableList<String> itemTypeList = FXCollections.observableArrayList("NORMAL", "COSTLY ITEMS", "8° STORAGE", "24° STORAGE");
    private ObservableList<String> statusList = FXCollections.observableArrayList("CONTINUE", "CLOSE");
    private CompanyModel companyModel;
    private MrModel mrModel;
    private ManufacturerModal manufacturerModal;
    private ItemsModel icm;
    private int purchaseItemId = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        staticData = new StaticData();
        dbConnection = new DBConnection();
        method.hideElement(progressBar);
        stripTabContainer.setDisable(true);
        mainContainer.setDisable(true);



        if (Main.primaryStage.getUserData() instanceof ItemsModel itemsModel) {
            icm = itemsModel;
            if (icm.isStockable()){
                stockableContaier.setVisible(true);
                method.hideElement(mrpContainer);
            }else {
                mrpContainer.setVisible(true);
                method.hideElement(stockableContaier);
                getMrp(icm.getItemId());
            }
            callInitializeThread();
        } else {
            customDialog.showAlertBox("", "Something went wrong...");
            return;
        }

    }

    private void callInitializeThread() {

        InitializeDataSetTask task = new InitializeDataSetTask();
        task.setDaemon(false);
        task.execute();
    }

    public void chooseMr(MouseEvent mouseEvent) {
        customDialog.showFxmlDialog2("chooser/mrChooser.fxml", "SELECT MR");
        if (Main.primaryStage.getUserData() instanceof MrModel mm) {
            this.mrModel = mm;
            mrL.setText(mm.getName());
        }
    }

    public void chooseMfr(MouseEvent mouseEvent) {
        customDialog.showFxmlDialog2("chooser/mfrChooser.fxml", "SELECT MANUFACTURE");
        if (Main.primaryStage.getUserData() instanceof ManufacturerModal mm) {
            this.manufacturerModal = mm;
            mfrL.setText(mm.getManufacturer_name());
        }
    }

    public void chooseCompany(MouseEvent mouseEvent) {
        customDialog.showFxmlDialog2("chooser/companyChooser.fxml", "SELECT COMPANY");
        if (Main.primaryStage.getUserData() instanceof CompanyModel cm) {
            this.companyModel = cm;
            companyNameL.setText(cm.getCompanyName());
        }
    }

    public void clearDiscount(ActionEvent event) {
        discountCom.getSelectionModel().clearSelection();
    }

    public void clearMfr(ActionEvent event) {
        if (null != manufacturerModal) {
            manufacturerModal = null;
            mfrL.setText("Click to select mfr");
        }
    }

    public void clearCompany(ActionEvent event) {
        if (null != companyModel) {
            companyModel = null;
            companyNameL.setText("Click to select company");
        }
    }

    public void clearMr(ActionEvent event) {
        if (null != mrModel) {
            mrModel = null;
            mrL.setText("Click to select mr");
        }
    }

    private class InitializeDataSetTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
           if (icm.isStockable()){
               comboBoxConfig();
           }
        }

        @Override
        public Boolean doInBackground(String... params) {
            if (!icm.isStockable()){
                getMrp(icm.getItemId());
            }

            getGst();
            Platform.runLater(ItemMasterUpdate.this::setComboBoxData);
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            mainContainer.setDisable(false);
            method.hideElement(progressBarMain);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void setComboBoxData() {

        hsnCom.getItems().add(method.getSpecificGst(icm.getGstId()));
        hsnCom.getSelectionModel().selectFirst();

        if (icm.getDiscount_id() > 0) {
                discountCom.getItems().add(method.getSpecificDiscount(icm.getDiscount_id()));
            discountCom.getSelectionModel().selectFirst();
        }else {
            discountCom.setItems((ObservableList<DiscountModel>) method.getDiscount().get("data"));
        }

        DepartmentModel departmentModel = CommonUtil.getDepartment(icm.getDepartmentCode());

        departmentCom.getItems().add(departmentModel);
        departmentCom.getSelectionModel().selectFirst();
       departmentCom.getItems().addAll(CommonUtil.getDepartmentsList());

        setData(icm);
        if (icm.getMr_id() > 0) {
            mrModel = method.getSpecificMr(icm.getMr_id());
            mrL.setText(mrModel.getName());
        }
        if (icm.getMfr_id() > 0) {
            manufacturerModal = method.getManufacture(icm.getMfr_id());
            mfrL.setText(manufacturerModal.getManufacturer_name());
        }
        if (icm.getCompany_id() > 0) {
            companyModel = method.getSpecificCompany(icm.getCompany_id());
            companyNameL.setText(companyModel.getCompanyName());
        }
        discountCom.setOnMouseClicked(mouseEvent ->
                discountCom.setItems((ObservableList<DiscountModel>) method.getDiscount().get("data")));

        hsnCom.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                hsnCom.setItems(new GetTax().getGst());
            }
        });
    }

    private void comboBoxConfig() {
        unitCom.valueProperty().addListener((observableValue, s, newValue) -> {
            stripTabTf.setText("");
            if (newValue.equalsIgnoreCase("STRIP")) {
                stripTabLabel.setText("TAB PER STRIP :");
                stripTabContainer.setDisable(false);
                stripTabTf.setText(String.valueOf(icm.getTabPerStrip()));
            } else {
                stripTabLabel.setText("");
                stripTabContainer.setDisable(true);
            }

        });
    }

    private class UpdateDataTask extends AsyncTask<String, Integer, Boolean> {
        private ItemsModel itemsModel;

        public UpdateDataTask(ItemsModel itemsModel) {
            this.itemsModel = itemsModel;
        }

        @Override
        public void onPreExecute() {
            progressBar.setVisible(true);
            method.hideElement(updateButton);
        }

        @Override
        public Boolean doInBackground(String... params) {
            uploadData(itemsModel);
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            updateButton.setVisible(true);
            method.hideElement(progressBar);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void uploadData(ItemsModel im) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            String qry = "UPDATE TBL_ITEMS_MASTER SET ITEMS_NAME = ?, UNIT= ?, PACKING= ?, COMPANY_ID= ?, mfr_id= ?, DISCOUNT_ID= ?, mr_id= ?, GST_ID= ?,\n" +
                    "                             TYPE= ?, NARCOTIC= ?, ITEM_TYPE= ?, STATUS= ? ,STRIP_TAB= ?," +
                    "composition=?,tag=?,dose = ?,department_code = ? WHERE item_id = ?";

            ps = connection.prepareStatement(qry);

            ps.setString(1, im.getProductName());
            ps.setString(2, im.getUnit());
            ps.setString(3, "1X1");

            if (null == companyModel) {
                ps.setNull(4, Types.NULL);
            } else {
                ps.setInt(4, companyModel.getCompany_id());
            }

            if (null == manufacturerModal) {
                ps.setNull(5, Types.NULL);
            } else {
                ps.setInt(5, manufacturerModal.getManufacturer_id());
            }

            if (discountCom.getSelectionModel().isEmpty()) {
                ps.setNull(6, Types.NULL);
            } else {
                ps.setInt(6, im.getDiscount_id());
            }

            if (null == mrModel) {
                ps.setNull(7, Types.NULL);
            } else {
                ps.setInt(7, mrModel.getMr_id());
            }

            ps.setInt(8, im.getGstId());
            ps.setString(9, im.getType());
            ps.setString(10, im.getNarcotic());
            ps.setString(11, im.getItemType());
            ps.setInt(12, im.getStatus());
            ps.setLong(13, im.getTabPerStrip());
            ps.setString(14, im.getProductComposition());
            ps.setString(15, im.getProductTag());
            ps.setString(16, im.getMedicineDose());
            ps.setString(17, im.getDepartmentCode());
            ps.setInt(18, icm.getItemId());

            int res = ps.executeUpdate();
            if (res > 0) {

                if (icm.isStockable()){
                    connection.commit();
                    success();
                }else {
                    res = 0;
                    ps = null;

                    String mrpUpdateQry = "update tbl_purchase_items set purchase_rate = ? , mrp = ?, sale_price = ?,updated_by=?  where purchase_items_id = ?";
                    ps = connection.prepareStatement(mrpUpdateQry);

                    ps.setDouble(1,im.getPurchaseMrp());
                    ps.setDouble(2,im.getMrp());
                    ps.setDouble(3,im.getSaleRate());
                    ps.setInt(4, Login.currentlyLogin_Id);
                    ps.setInt(5,purchaseItemId);
                    res = ps.executeUpdate();

                    if (res> 0){
                        connection.commit();
                        success();
                    }else {
                        customDialog.showAlertBox("Failed","Item not updated. Please try again.");
                    }
                }

            } else {
                customDialog.showAlertBox("Failed","Item not updated. Please try again.");
            }
        } catch (SQLException e) {
            customDialog.showAlertBox("Error","An error occurred while creating the product");

            updateButton.setVisible(true);
            method.hideElement(progressBar);
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    private void success() {
       Platform.runLater(()->{
           customDialog.showAlertBox("Success","Item successfully updated.");
           Stage stage = (Stage) updateButton.getScene().getWindow();
           if (null != stage && stage.isShowing()) {
               Main.primaryStage.setUserData(true);
               stage.close();
           }
       });
    }

    private void setData(ItemsModel icm) {

        productNameTf.setText(icm.getProductName());

        if (icm.isStockable()){
            compositionTf.setText(icm.getProductComposition());
            medicineDoseTf.setText(icm.getMedicineDose());
        }

        productTag.setText(icm.getProductTag());
        typeCom.setItems(typeList);
        typeCom.getSelectionModel().select(icm.getType());
        narcoticCom.setItems(narcoticList);
        narcoticCom.getSelectionModel().select(icm.getNarcotic());
        itemTypeCom.setItems(itemTypeList);
        itemTypeCom.getSelectionModel().select(icm.getItemType());
        statusCom.setItems(statusList);

        String status = "";

        switch (icm.getStatus()) {
            case 1 -> status = "CONTINUE";
            case 0 -> status = "CLOSE";

        }
        statusCom.getSelectionModel().select(status);
        if (method.isItemAvailableInStock(icm.getItemId())) {
            String stockUnit = method.getStockUnit(icm.getItemId());

            if (stockUnit.equalsIgnoreCase("TAB") || stockUnit.equalsIgnoreCase("STRIP")) {
                unitCom.setItems(staticData.tabUnit);
                unitCom.getSelectionModel().select(icm.getUnit());
            } else {
                unitCom.setItems(staticData.pcsUnit);
                unitCom.getSelectionModel().select("PCS");
            }


        } else {
            unitCom.setItems(staticData.getUnit());
            unitCom.getSelectionModel().select(icm.getUnit());
        }


    }

    public void updateBn(ActionEvent actionEvent) {
        update();
    }

    public void closeBn(ActionEvent actionEvent) {
        Stage stage = (Stage) updateButton.getScene().getWindow();

        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }
    private void getGst() {

    }

    public void enterKeyPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER) {
            update();
        }
    }

    private void update() {

        String productName = productNameTf.getText();
        String stripTab = stripTabTf.getText();
        String composition = compositionTf.getText();
        String tag = productTag.getText();
        String medicineDose = medicineDoseTf.getText();
        String mrp = mrpTf.getText();


        long stripTabL = 0;
        double mrpD = 0;

        if (productName.isEmpty()) {
            method.show_popup("Please enter product name", productNameTf, Side.RIGHT);
            return;
        } else if (tag.isEmpty()) {
            method.show_popup("Please enter product tag", productTag, Side.RIGHT);
            return;
        } else if (departmentCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select item department", departmentCom, Side.RIGHT);
            return;
        }


        if (icm.isStockable()){

            if (composition.isEmpty()) {
                method.show_popup("Please enter product composition.", compositionTf, Side.RIGHT);
                return;
            }  else if (medicineDose.isEmpty()) {
                method.show_popup("Please enter medicine dose", medicineDoseTf, Side.RIGHT);
                return;
            }  else if (unitCom.getSelectionModel().isEmpty()) {
                method.show_popup("Please select unit", unitCom, Side.RIGHT);
                return;
            } else if (unitCom.getSelectionModel().getSelectedItem().equals("STRIP")) {
                if (stripTab.isEmpty()) {
                    method.show_popup("Please enter tab per strip", stripTabTf, Side.RIGHT);
                    return;
                }
                try {
                    stripTabL = Long.parseLong(stripTab);
                } catch (NumberFormatException e) {
                    method.show_popup("Please enter number only", stripTabTf, Side.RIGHT);
                    return;
                }
            }
        } else {

            if (mrp.isEmpty()) {
                method.show_popup("Please enter item mrp", mrpTf, Side.RIGHT);
                return;
            }else {

                try {
                    mrpD = Double.parseDouble(mrp);
                } catch (NumberFormatException e) {
                    method.show_popup("Please enter valid mrp", mrpTf, Side.RIGHT);
                    return;
                }

            }
        }


        if (hsnCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select hsn code", hsnCom, Side.RIGHT);
            return;
        }

        String type = typeCom.getSelectionModel().getSelectedItem();
        String narcotic = narcoticCom.getSelectionModel().getSelectedItem();
        String itemType = itemTypeCom.getSelectionModel().getSelectedItem();
        String departmentCode = departmentCom.getSelectionModel().getSelectedItem().getDepartmentCode();

        int status = 0;
        switch (statusCom.getSelectionModel().getSelectedItem()) {
            case "CONTINUE" -> status = 1;
            case "CLOSE" -> status = 0;
        }

        String unit = unitCom.getSelectionModel().getSelectedItem();
        int gstId = hsnCom.getSelectionModel().getSelectedItem().getTaxID();

        int discountId = 0;
        if (!discountCom.getSelectionModel().isEmpty()) {
            discountId = discountCom.getSelectionModel().getSelectedItem().getDiscount_id();
        }


        System.out.println("departmentCode-"+departmentCode);

        ItemsModel itemsModel = new ItemsModel(productName, unit, null, discountId, gstId,
                mrpD, mrpD, mrpD, type, narcotic, itemType, status, stripTabL,composition,tag,medicineDose,icm.isStockable(),departmentCode);

        UpdateDataTask task = new UpdateDataTask(itemsModel);
        task.setDaemon(false);
        task.execute();
    }

    private void getMrp (int itemId){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = "select mrp,purchase_items_id from stock_v where item_id = ? order by 1 desc limit 1";
            ps = connection.prepareStatement(qry);
            ps.setInt(1,itemId);
            rs = ps.executeQuery();

            if (rs.next()){

                double mrp = rs.getDouble("mrp");
                purchaseItemId = rs.getInt("purchase_items_id");
                mrpTf.setText(String.valueOf(mrp));
            }

        }catch (SQLException ignored){

        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }

    }
}
