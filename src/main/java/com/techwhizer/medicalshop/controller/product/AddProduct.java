package com.techwhizer.medicalshop.controller.product;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.common.model.DepartmentModel;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
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
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddProduct implements Initializable {
    public Button submitButton;
    public TextField productNameTf;
    public ComboBox<GstModel> hsnCom;
    public ComboBox<String> typeCom;
    public ComboBox<String> narcoticCom;
    public ComboBox<String> itemTypeCom;
    public ComboBox<DiscountModel> discountCom;
    public TextField stripTabTf;
    public ComboBox<String> unitCom;
    public ProgressIndicator progressBar;
    public Label mrL;
    public Label mfrL;
    public Label companyNameL;
    public TextField compositionTf;
    public TextField productTag;
    public TextField medicineDoseTf;
    public HBox radioGroupHB;
    public HBox mrpContainerHB;
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
    boolean isStockableItem;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        staticData = new StaticData();
        dbConnection = new DBConnection();
        method.hideElement(progressBar);
        stripTabContainer.setDisable(true);
        callInitializeThread();
        selectionChooser();
        setRadioGroup();

        Platform.runLater(() -> {
            Stage stage = (Stage) stripTabLabel.getScene().getWindow();
            stage.setMaximized(true);
        });

    }

    private void setRadioGroup() {

        ToggleGroup group = new ToggleGroup();
        RadioButton yesRB = new RadioButton("YES");
        RadioButton noRb = new RadioButton("NO");

        yesRB.setToggleGroup(group);
        noRb.setToggleGroup(group);

        radioGroupHB.getChildren().addAll(yesRB, noRb);

        group.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            isStockableItem = yesRB.isSelected();

            if (isStockableItem) {
                stockableContaier.setVisible(true);
                method.hideElement(mrpContainerHB);
            } else {
                mrpContainerHB.setVisible(true);
                method.hideElement(stockableContaier);
            }

        });
        isStockableItem = true;
        yesRB.setSelected(true);
    }

    private void selectionChooser() {
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

    private class InitializeDataSetTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
            //Background Thread will start
        }

        @Override
        public Boolean doInBackground(String... params) {
            departmentCom.setItems(CommonUtil.getDepartmentsList());
            setData();
            getGst();

            getDiscount();
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            if (success) {
                comboBoxConfig();
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void comboBoxConfig() {
        unitCom.valueProperty().addListener((observableValue, s, newValue) -> {
            stripTabTf.setText("");
            if (newValue.equalsIgnoreCase("STRIP")) {
                stripTabLabel.setText("TAB PER STRIP :");
                stripTabContainer.setDisable(false);
            } else {
                stripTabLabel.setText("");
                stripTabContainer.setDisable(true);
            }
        });
    }

    private class SubmitDataTask extends AsyncTask<String, Integer, Boolean> {
        private ItemsModel itemsModel;

        public SubmitDataTask(ItemsModel itemsModel) {
            this.itemsModel = itemsModel;
        }

        @Override
        public void onPreExecute() {
            progressBar.setVisible(true);
            method.hideElement(submitButton);
        }

        @Override
        public Boolean doInBackground(String... params) {
            uploadData(itemsModel);
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void uploadData(ItemsModel im) {
        Map<String, Object> map = new HashMap<>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            String qry = """
                    INSERT INTO TBL_ITEMS_MASTER(ITEMS_NAME, UNIT, PACKING, COMPANY_ID, mfr_id, DISCOUNT_ID, mr_id, GST_ID,
                                                 TYPE, NARCOTIC, ITEM_TYPE, STATUS,created_by,STRIP_TAB,composition,tag,dose,
                                                 is_stockable,department_code)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""";

            ps = connection.prepareStatement(qry, new String[]{"item_id"});

            ps.setString(1, im.getProductName());
            ps.setString(2, im.getUnit() == null || im.getUnit().isEmpty()?"PCS":im.getUnit() );

            if (im.getPacking() == null || im.getPacking().isEmpty()){
                ps.setString(3, "1x1");
            }else {
                ps.setString(3, im.getPacking());
            }


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
            ps.setInt(12, 1);
            ps.setInt(13, Login.currentlyLogin_Id);
            ps.setLong(14, im.getTabPerStrip());
            ps.setString(15, im.getProductComposition());
            ps.setString(16, im.getProductTag());
            ps.setString(17, im.getMedicineDose());
            ps.setBoolean(18, isStockableItem);
            ps.setString(19, im.getDepartmentCode());

            int res = ps.executeUpdate();
            if (res > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int itemId = rs.getInt(1);

                    if (isStockableItem){
                        Connection finalConnection = connection;
                        PreparedStatement finalPs = ps;
                        ResultSet finalRs = rs;
                        Platform.runLater(()->{
                            try {
                                onSuccess(finalConnection, finalPs, finalRs);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }else {
                        addItemToStock(itemId,im.getMrp(),connection,ps,rs);

                    }


                }else {

                    DBConnection.rollBack(connection);
                    customDialog.showAlertBox("Error", "Something went wrong. Please try again");
                }

            }

        } catch (SQLException e) {
            System.out.println("item master-"+e.getMessage());
            Platform.runLater(() -> {
                submitButton.setVisible(true);
                method.hideElement(progressBar);
            });
            throw new RuntimeException(e);
        }
    }

    private void addItemToStock(int itemId, double mrp, Connection connection, PreparedStatement ps, ResultSet rs) {

        String billNum = new GenerateBillNumber().generatePurchaseBillNum();

        try {
            String purMainQry = "INSERT INTO TBL_PURCHASE_MAIN(DEALER_ID, BILL_NUM, DEALER_BILL_NUM, BILL_DATE)VALUES (?,?,?,?)";
            ps = connection.prepareStatement(purMainQry, new String[]{"purchase_main_id"});
            ps.setNull(1, 0);
            ps.setString(2, billNum);
            ps.setNull(3, Types.NULL);
            ps.setString(4, method.getCurrentDate());
            int res = ps.executeUpdate();

            if (res > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int purMainId = rs.getInt(1);
                    ps = null;
                    rs = null;
                    res = 0;

                    String purItemsQry = "INSERT INTO TBL_PURCHASE_ITEMS(purchase_main_id, item_id, batch, expiry_date, lot_number, quantity, quantity_unit,purchase_rate,mrp,sale_price) VALUES (?,?,?,?,?,?,?,?,?,?)";

                    ps = connection.prepareStatement(purItemsQry, new String[]{"purchase_items_id"});
                    ps.setInt(1, purMainId);
                    ps.setInt(2, itemId);
                    ps.setNull(3, Types.NULL);
                    ps.setString(4, "01/2050");
                    ps.setNull(5, Types.NULL);
                    ps.setInt(6, 0);
                    ps.setString(7, "PCS");
                    ps.setDouble(8, mrp);
                    ps.setDouble(9, mrp);
                    ps.setDouble(10, mrp);
                    res = ps.executeUpdate();

                    if (res > 0) {
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {

                            int purchaseItemId = rs.getInt(1);
                            ps = null;
                            rs = null;
                            res = 0;

                            String stockQryInsertQry = "INSERT INTO TBL_STOCK(ITEM_ID, PURCHASE_MAIN_ID, PURCHASE_ITEMS_ID, QUANTITY," + " QUANTITY_UNIT,UPDATE_DATE)VALUES(?,?,?,?,?,?)";
                            ps = connection.prepareStatement(stockQryInsertQry);
                            ps.setInt(1, itemId);
                            ps.setInt(2, purMainId);
                            ps.setInt(3, purchaseItemId);
                            ps.setInt(4, 1);
                            ps.setString(5, "PCS");
                            ps.setString(6, method.getCurrentDate());

                            res = ps.executeUpdate();

                            if (res > 0) {

                                PreparedStatement finalPs = ps;
                                ResultSet finalRs = rs;
                                Platform.runLater(()->{
                                    try {
                                        onSuccess(connection, finalPs, finalRs);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                });

                            } else {
                                DBConnection.rollBack(connection);
                                customDialog.showAlertBox("Error", "Something went wrong. Please try again");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("item master-"+e.getMessage()); System.out.println("item master-"+e.getMessage());
            Platform.runLater(() -> {
                submitButton.setVisible(true);
                method.hideElement(progressBar);
            });
            DBConnection.rollBack(connection);
            customDialog.showAlertBox("Error", "Something went wrong. Please try again");
        }


    }

    private void onSuccess(Connection connection,PreparedStatement ps, ResultSet rs) throws SQLException {

        connection.commit();

        submitButton.setVisible(true);
        productNameTf.setText("");
        stripTabTf.setText("");
        compositionTf.setText("");
        productTag.setText("");
        mrpTf.setText("");
        discountCom.getSelectionModel().clearSelection();
        departmentCom.getSelectionModel().clearSelection();
        companyModel = null;
        manufacturerModal = null;
        mrModel = null;
        companyNameL.setText("Click to choose company");
        mfrL.setText("Click to choose mfr");
        mrL.setText("Click to choose mr");
        method.hideElement(progressBar);

        Platform.runLater(() -> {
            customDialog.showAlertBox("", "Item successfully created.");
        });

        DBConnection.closeConnection(connection, ps, rs);
    }

    private void setData() {
        Platform.runLater(() -> {
            unitCom.setItems(staticData.getUnit());
            typeCom.setItems(typeList);
            typeCom.getSelectionModel().selectFirst();
            narcoticCom.setItems(narcoticList);
            narcoticCom.getSelectionModel().selectFirst();
            itemTypeCom.setItems(itemTypeList);
            itemTypeCom.getSelectionModel().selectFirst();
        });
    }

    public void submit_bn(ActionEvent actionEvent) {
        submit();
    }

    public void closeBn(ActionEvent actionEvent) {
        Stage stage = (Stage) submitButton.getScene().getWindow();

        if (null != stage && stage.isShowing()) {
            stage.close();
        }

    }

    public void addGst(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("product/gst/addGst.fxml", "Add Gst");
        getGst();
    }

    private void getGst() {
        hsnCom.setItems(new GetTax().getGst());
    }

    public void addDiscount(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("product/discount/discount.fxml", "Create new discount");
        getDiscount();
    }

    private void getDiscount() {

        discountCom.setItems((ObservableList<DiscountModel>) method.getDiscount().get("data"));
    }

    public void enterKeyPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER) {
            submit();
        }
    }

    private void submit() {
        String productName = productNameTf.getText();

        String stripTab = stripTabTf.getText();
        String composition = compositionTf.getText();
        String tag = productTag.getText();
        String medicineDose = medicineDoseTf.getText();
        String mrp = mrpTf.getText();



        long stripTabL = 0;
        double mrpD = 0;

        if (productName.isEmpty()) {
            method.show_popup("Please enter product name", productNameTf);
            return;
        } else if (tag.isEmpty()) {
            method.show_popup("Please enter product tag", productTag);
            return;
        } else if (departmentCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select item department", departmentCom);
            return;
        }

        if (isStockableItem) {
            if (composition.isEmpty()) {
                method.show_popup("Please enter product composition.", compositionTf);
                return;
            } else if (medicineDose.isEmpty()) {
                method.show_popup("Please enter medicine dose", medicineDoseTf);
                return;
            }  else if (unitCom.getSelectionModel().isEmpty()) {
                method.show_popup("Please select unit", unitCom);
                return;
            } else if (unitCom.getSelectionModel().getSelectedItem().equals("STRIP")) {
                if (stripTab.isEmpty()) {
                    method.show_popup("Please enter tab per strip", stripTabTf);
                    return;
                }
                try {
                    stripTabL = Long.parseLong(stripTab);
                } catch (NumberFormatException e) {
                    method.show_popup("Please enter number only", stripTabTf);
                    return;
                }
            }
        } else {

            if (mrp.isEmpty()) {
                method.show_popup("Please enter item mrp", mrpTf);
                return;
            }else {

                try {
                    mrpD = Double.parseDouble(mrp);
                } catch (NumberFormatException e) {
                    method.show_popup("Please enter valid mrp", mrpTf);
                    return;
                }

            }
        }


        if (hsnCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select hsn code", hsnCom);
            return;
        }

        String type = typeCom.getSelectionModel().getSelectedItem();
        String narcotic = narcoticCom.getSelectionModel().getSelectedItem();
        String itemType = itemTypeCom.getSelectionModel().getSelectedItem();
        String departmentCode = departmentCom.getSelectionModel().getSelectedItem().getDepartmentCode();
        int status = 0;

        String unit = unitCom.getSelectionModel().getSelectedItem();
        int gstId = hsnCom.getSelectionModel().getSelectedItem().getTaxID();

        int discountId = 0;
        if (!discountCom.getSelectionModel().isEmpty()) {
            discountId = discountCom.getSelectionModel().getSelectedItem().getDiscount_id();
        }

        ItemsModel itemsModel = new ItemsModel(productName, unit, null, discountId, gstId, mrpD, mrpD,
                mrpD, type, narcotic, itemType, status, stripTabL, composition, tag, medicineDose, isStockableItem,departmentCode);

        SubmitDataTask task = new SubmitDataTask(itemsModel);
        task.setDaemon(false);
        task.execute();
    }
}
