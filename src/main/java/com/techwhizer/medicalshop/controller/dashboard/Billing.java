package com.techwhizer.medicalshop.controller.dashboard;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.GenerateInvoice;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.model.DoctorModel;
import com.techwhizer.medicalshop.model.PatientModel;
import com.techwhizer.medicalshop.model.PriceTypeModel;
import com.techwhizer.medicalshop.model.SaleEntryModel;
import com.techwhizer.medicalshop.model.chooserModel.BatchChooserModel;
import com.techwhizer.medicalshop.model.chooserModel.StockItemChooserModel;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.type.DoctorType;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class Billing implements Initializable {
    public Label patientNameL;
    public TableView<SaleEntryModel> tableView;
    public TableColumn<SaleEntryModel, Integer> colSrNo;
    public TableColumn<SaleEntryModel, String> colProductName;
    public TableColumn<SaleEntryModel, String> colMrp;
    public TableColumn<SaleEntryModel, String> colPack;
    public TableColumn<SaleEntryModel, String> colStrip;
    public TableColumn<SaleEntryModel, String> colPcs;
    public TableColumn<SaleEntryModel, String> colExpiryDate;
    public TableColumn<SaleEntryModel, String> colDiscount;
    public TableColumn<SaleEntryModel, String> colGst;
    public TableColumn<SaleEntryModel, String> colAmount;

    public TableColumn<SaleEntryModel, String> colAction;
    public Label totAmountL;
    public Label totDisAmountL;
    public Label totGstAmountL;
    public Label invoiceValueTf;
    public VBox paymentModeContainer;
    public ComboBox<String> paymentModeC;
    public ComboBox<String> billingTypeC;
    public Label taxTitleL;
    public TextField addDiscTF;
    public VBox amountContainer;
    public Button checkOutButton;
    public HBox progressBar;
    public Label patientAgeL;
    public Label patientAddressL;
    public Label itemNameL;
    public TextField stripTf;
    public TextField pcsTf;
    public TextField saleRateTf;
    public Button addItemToList;
    public Label mrpL;
    public Label avlQuantity;
    public Label tabPerStripL;
    public TextField referenceNumTf;
    public TextField remarksTf;
    public TextField searchNameTf;
    public TableView<PatientModel> tableViewPatient;
    public TableColumn<PatientModel, Integer> colPatientSr;
    public TableColumn<PatientModel, String> colAdmNum;
    public TableColumn<PatientModel, String> colPatientName;
    public Pagination pagination;
    public Label genderL;
    public TextField receivedAmountTf;
    public ImageView applyDiscountBn;
    public HBox priceRadioContainerHB;
    private CustomDialog customDialog;
    private Method method;
    private DBConnection dbConnection;
    private StaticData staticData;
    private ObservableList<SaleEntryModel> itemList = FXCollections.observableArrayList();
    private double totGstAmount = 0, netTotalAmount = 0, totalDiscount = 0, totalAmtAsPerMrp = 0;
    private PatientModel patientModel;
    public ComboBox<DoctorModel> referByCom;
    public ComboBox<DoctorModel> consultNameCom;
    private ObservableList<PatientModel> patientList = FXCollections.observableArrayList();
    private FilteredList<PatientModel> filteredData;
    static private int rowsPerPage = 20;
    private BatchChooserModel bcm;
    private double addDiscountPercentage = 0.0;
    PriceTypeModel ptm = null;
    private static ObservableList<StockItemChooserModel> popItemList = FXCollections.observableArrayList();

    enum Type {
        GET_PATIENT, SAVE, GET_CART_DATA, GET_POPUP_ITEM, GET_POPUP_ITEM_CART_DATA
    }

    RadioButton saleRB = new RadioButton("Sale Rate");
    RadioButton purchaseRB = new RadioButton("Purchase Rate");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new Method();
        staticData = new StaticData();
        dbConnection = new DBConnection();
        method.hideElement(progressBar);
        tableViewPatient.setFixedCellSize(26);

        setRadioGroup();

        Platform.runLater(() -> {
            Method.setGlobalZoomout(genderL.getScene());
            Stage stage = (Stage) genderL.getScene().getWindow();
            stage.setMaximized(true);
            setData();
            callThread(Type.GET_POPUP_ITEM_CART_DATA);
            callThread(Type.GET_PATIENT);

            discountConfig();
        });
    }

    public void refreshPatientBnClick(ActionEvent actionEvent) {
        callThread(Type.GET_PATIENT);
    }

    private void setRadioGroup() {

        ToggleGroup group = new ToggleGroup();

        saleRB.setToggleGroup(group);
        purchaseRB.setToggleGroup(group);

        saleRB.setStyle("-fx-font-size: 9px;-fx-font-weight: bold;");
        purchaseRB.setStyle("-fx-font-size: 9px;-fx-font-weight: bold");

        priceRadioContainerHB.getChildren().addAll(saleRB, purchaseRB);

        saleRB.setGraphicTextGap(100);
        group.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (bcm != null && ptm != null) {
                if (purchaseRB.isSelected()) {
                    saleRateTf.setText(String.valueOf(ptm.getPurchaseRate()));
                } else {
                    saleRateTf.setText(String.valueOf(ptm.getSaleRate()));
                }
            } else {
                saleRateTf.setText("0");
            }

        });
        saleRB.setSelected(true);
    }

    public void applyDiscountBnClick(MouseEvent mouseEvent) {
        String addDisc = addDiscTF.getText();
        if (!addDisc.isEmpty()) {
            itemList.clear();
            itemList.removeAll();
            tableView.setItems(null);
            applyDiscountBn.setVisible(false);
            callThread(Type.GET_CART_DATA);
        }
    }

    public void keyPress(KeyEvent keyEvent) {

//        if (keyEvent.getCode() == KeyCode.ENTER) {
//
//        }

    }

    private void discountConfig() {

        addDiscTF.textProperty().addListener((observableValue, s, val1) -> {
            double mainValue = netTotalAmount;
            String val = addDiscTF.getText();

            if (!val.isEmpty()) {

                try {
                    double addDisPercentage = Double.parseDouble(val);
                    if (addDisPercentage > 100) {
                        method.show_popup("You cannot discount more than 100 percent", addDiscTF, Side.RIGHT);
                        addDiscTF.setText(String.valueOf(0));
                        return;
                    }
                    applyDiscountBn.setVisible(addDiscountPercentage != addDisPercentage);
                    addDiscountPercentage = addDisPercentage;
                    double discountAmount = mainValue * addDisPercentage / 100;

                    if (discountAmount > mainValue) {
                        method.show_popup("Please enter amount less then invoice value", addDiscTF, Side.RIGHT);
                        addDiscTF.setText(String.valueOf(0));
                        return;
                    }
                } catch (NumberFormatException e) {
                    method.show_popup("Please enter amount less then invoice value", addDiscTF, Side.RIGHT);
                    addDiscTF.setText(String.valueOf(0));

                }
            }

        });

    }

    private void setInvoiceAmount(double amount) {

        Platform.runLater(() -> invoiceValueTf.setText(String.valueOf(Math.round(amount))));
    }

    private void setData() {
        paymentModeC.setItems(staticData.getPaymentMode());
        billingTypeC.setItems(staticData.getBillingType());
        paymentModeC.getSelectionModel().select("CASH");
        billingTypeC.getSelectionModel().select("REGULAR");
    }

    private void setTableDate() {
        tableView.setItems(itemList);

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(cellData.getValue()) + 1));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        // colPack.setCellValueFactory(new PropertyValueFactory<>("pack"));
        colStrip.setCellValueFactory(new PropertyValueFactory<>("strip"));
        colPcs.setCellValueFactory(new PropertyValueFactory<>("pcs"));
        colMrp.setCellValueFactory(new PropertyValueFactory<>("saleRate"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colGst.setCellValueFactory(new PropertyValueFactory<>("totalGst"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        setOptionalCell();

        Platform.runLater(() -> {
            totAmountL.setText(method.decimalFormatter(totalAmtAsPerMrp));
            totGstAmountL.setText(method.decimalFormatter(totGstAmount));
            totDisAmountL.setText(method.decimalFormatter(totalDiscount));

            setInvoiceAmount(netTotalAmount);

        });
    }

    private void setOptionalCell() {

        Callback<TableColumn<SaleEntryModel, String>, TableCell<SaleEntryModel, String>>
                cellFactory = (TableColumn<SaleEntryModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Button deleteBn = new Button();
                    ImageView iv = new ImageView(new ImageLoader().load("img/icon/delete_ic_white.png"));
                    iv.setFitHeight(12);
                    iv.setFitWidth(12);

                    deleteBn.setGraphic(iv);
                    deleteBn.setStyle("-fx-cursor: hand ; -fx-background-color: #c1061c ; -fx-background-radius: 3;-fx-padding: 2 4 2 4 ");

                    deleteBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        SaleEntryModel sem = tableView.getSelectionModel().getSelectedItem();
                        removeItem(sem.getCartId());

                    });

                    HBox managebtn = new HBox(deleteBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(deleteBn, new Insets(0, 0, 0, 0));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellFactory);
    }

    private void getCartData() {
        itemList.clear();
        itemList = FXCollections.observableArrayList();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        totGstAmount = 0;
        netTotalAmount = 0;
        totalDiscount = 0;
        totalAmtAsPerMrp = 0;

        try {
            connection = dbConnection.getConnection();
            String qry = """
                                select tim.items_name,tim.item_id , tc.stock_id , tc,cart_id,t.batch,tim.mfr_id ,tpt.hsn_sac as hsn,tpt.igst,tpt.sgst,tpt.cgst, tim.packing , tc.mrp  as sale_rate , tc.strip,tc.pcs,
                                       (select expiry_date from tbl_purchase_items tpi where ts.purchase_items_id = tpi.purchase_items_id),t.purchase_rate,t.mrp,
                                  
                                       case when ? > 0 then ? else (coalesce(td.discount,0)) end as discount
                                      
                    ,td.discount_id,tpt.tax_id,tim.is_stockable,
                                       coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)+coalesce(tpt.igst,0)),0)as total_gst,
                                       ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0)) totaltab,
                                
                                       ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                                       case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end as amountAsPerMrp,
                                       ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                                       case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0)
                                           end-(( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                                                  case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else
                                                      coalesce(tc.mrp,0) end )*(case when ? > 0 then ? else (coalesce(td.discount,0)) end)/100) as netAmount,
                                
                                       ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                                         case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else
                                             coalesce(tc.mrp,0) end )*(
                                             case when ? > 0 then ? else (coalesce(td.discount,0)) end)/100 as discountAmount,
                                
                                       ( coalesce( ( ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )-
                                                     ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )*
                                                     (case when ? > 0 then ? else (coalesce(td.discount,0)) end)/100),0)*100)/
                                       (100+(coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)+coalesce(tpt.igst,0)),0))) as taxableAmount,
                                
                                       ( coalesce( ( ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )-
                                                     ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )*
                                                     (case when ? > 0 then ? else (coalesce(td.discount,0)) end)/100),0)*100)/
                                       (100+(coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)+coalesce(tpt.igst,0)),0)))*coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)
                        +coalesce(tpt.igst,0)),0)/100 as gstAmount,
                         ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0)) as totalRequestQuantity,
                         tde.department_id,tde.department_name,tde.department_code
                                from tbl_cart tc
                                         left join tbl_stock ts on tc.stock_id = ts.stock_id
                                         left join tbl_items_master tim on tim.item_id = ts.item_id
                                         left join tbl_discount td on tim.discount_id = td.discount_id
                                         left join tbl_product_tax tpt on tpt.tax_id = tim.gst_id
                                         left outer join tbl_purchase_items t on ts.purchase_items_id = t.purchase_items_id
                                         left join  tbl_departments tde on tim.department_code = tde.department_code
                      
                      where tc.created_by = """ + Login.currentlyLogin_Id;
            ps = connection.prepareStatement(qry);

            for (int i = 0; i < 10; i++) {
                ps.setDouble(i + 1, addDiscountPercentage);
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                String productName = rs.getString("items_name");
                double saleRate = rs.getDouble("sale_rate");
                String pack = rs.getString("packing");
                int strip = rs.getInt("strip");
                int pcs = rs.getInt("pcs");
                String expiryDate = rs.getString("expiry_date");
                int discountId = rs.getInt("discount_id");
                double discount = rs.getDouble("discount");
                int gstId = rs.getInt("tax_id");
                double totalGst = rs.getDouble("total_gst");
                double netAmount = rs.getDouble("netAmount");
                double amountAsPerMrp = rs.getDouble("amountAsPerMrp");
                double taxableAmt = rs.getDouble("taxableAmount");
                double discountAmt = rs.getDouble("discountAmount");
                double gstAmount = rs.getDouble("gstAmount");
                long hsn = rs.getLong("hsn");
                int iGst = rs.getInt("igst");
                int cGst = rs.getInt("cgst");
                int sGst = rs.getInt("sgst");
                double purchaseRate = rs.getDouble("purchase_rate");
                double mrp = rs.getDouble("mrp");
                String batch = rs.getString("batch");
                int mfrId = rs.getInt("mfr_id");
                int cartId = rs.getInt("cart_id");
                int stockId = rs.getInt("stock_id");
                double totalRequestQuantity = rs.getDouble("totalRequestQuantity");
                boolean isStockable = rs.getBoolean("is_stockable");

                int departmentId = rs.getInt("department_id");
                String departmentName = rs.getString("department_name");
                String departmentCode = rs.getString("department_code");

                SaleEntryModel sem = new SaleEntryModel(itemId, cartId, stockId, productName, saleRate, pack, strip, pcs, isStockable ? expiryDate : "-",
                        discountId, discount, gstId, totalGst, Method.removeDecimal(netAmount), hsn, iGst, cGst, sGst, Method.removeDecimal(gstAmount),
                        purchaseRate, mrp, batch, mfrId,  Method.removeDecimal(amountAsPerMrp), totalRequestQuantity, isStockable,departmentId,departmentName,departmentCode);


                if (!itemList.contains(sem)) {
                    itemList.add(sem);
                }

                totGstAmount += gstAmount;
                totalDiscount += discountAmt;
                netTotalAmount += netAmount;
                totalAmtAsPerMrp += amountAsPerMrp;
            }
            setTableDate();
            Platform.runLater(() -> tableView.setPlaceholder(new Label("Item Not Available.")));
        } catch (SQLException e) {
            Platform.runLater(() -> tableView.setPlaceholder(new Label("Something went wrong")));
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }



    public void closeBn(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage.isShowing()) {
            stage.close();
        }
    }

    public void clearAllBn(ActionEvent event) {
        Platform.runLater(() -> {
            ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
            image.setFitWidth(45);
            image.setFitHeight(45);
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setAlertType(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning ");
            alert.setGraphic(image);
            alert.setHeaderText("Are you sure you want to clearAllBn all items");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(Main.primaryStage);
            Optional<ButtonType> result = alert.showAndWait();
            ButtonType button = result.orElse(ButtonType.CANCEL);
            if (button == ButtonType.OK) {
                clearAll();
            } else {
                alert.close();
            }
        });
    }

    private void clearAll() {
        Platform.runLater(() -> {
            Connection con = null;
            PreparedStatement ps = null;
            String query = "DELETE FROM tbl_cart WHERE created_by = ?";

            try {
                con = dbConnection.getConnection();
                ps = con.prepareStatement(query);
                ps.setInt(1, Login.currentlyLogin_Id);
                int res = ps.executeUpdate();

                if (res >= 0) {
                    if (null != itemList) {
                        itemList.clear();
                    }
                    addDiscTF.setText("0");
                    remarksTf.setText("");
                    referenceNumTf.setText("");
                    callThread(Type.GET_POPUP_ITEM_CART_DATA);
                }
            } catch (SQLException e) {
                customDialog.showAlertBox("ERROR", "Failed to Clear Cart !");
                e.printStackTrace();
            } finally {
                DBConnection.closeConnection(con, ps, null);
                //callThread(Type.GET_CART_DATA);
            }
        });
    }

    public void removeItem(int cartId) {
        Connection con = null;
        PreparedStatement ps = null;
        String query = "DELETE FROM tbl_cart WHERE cart_id = ? and created_by = ?";

        try {
            con = dbConnection.getConnection();
            ps = con.prepareStatement(query);
            ps.setInt(1, cartId);
            ps.setInt(2, Login.currentlyLogin_Id);
            int res = ps.executeUpdate();
            if (res >= 0) {
                getCartData();
            }

        } catch (SQLException e) {
            customDialog.showAlertBox("ERROR", "Failed to Clear Cart !");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(con, ps, null);
            callThread(Type.GET_CART_DATA);
        }
    }

    private void callThread(Type type) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(type);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void addPatient(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("patient/patient_registration_form.fxml", "Add New Patient");
        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isSuccess = (Boolean) Main.primaryStage.getUserData();

            if (isSuccess) {
                callThread(Type.GET_PATIENT);
            }
        }
    }

    public void addItemToListClick(ActionEvent actionEvent) {
        submit(null);
    }

    public void chooseItem(MouseEvent mouseEvent) {

       // Main.primaryStage.setUserData(popItemList);
        customDialog.showFxmlDialog2("chooser/StockItemChooser.fxml", "SELECT ITEM");

        if (Main.primaryStage.getUserData() instanceof StockItemChooserModel sicm) {

            if (method.isItemAvailableInStock(sicm.getStockId())) {

                    Connection connection = null;
                    PreparedStatement ps = null;
                    ResultSet rs = null;

                    try {
                        connection = new DBConnection().getConnection();

                        String qry = """
                                select stock_id,
                                       tpi.purchase_items_id,
                                       tim.items_name,
                                       tim.strip_tab,
                                       tpi.batch,
                                       tpi.expiry_date,
                                       ts.quantity,
                                       ts.quantity_unit,
                                       tab_to_strip(ts.quantity,tim.strip_tab,ts.quantity_unit) as tab_to_strip
                                from tbl_stock ts
                                         left join tbl_purchase_items tpi on tpi.purchase_items_id = ts.purchase_items_id
                                         left join tbl_items_master tim on tim.item_id = ts.item_id
                                where ts.stock_id =?
                                  and ts.quantity > 0
                                order by expiry_date asc
                                                                
                                """;
                        ps = connection.prepareStatement(qry);
                        ps.setInt(1, sicm.getStockId());
                        rs = ps.executeQuery();

                        if (rs.next()) {

                            int stockId = rs.getInt("stock_id");
                            String itemName = rs.getString("items_name");
                            String batch = rs.getString("batch");
                            String expiryDate = rs.getString("expiry_date");
                            int quantity = rs.getInt("quantity");
                            int strip_tab = rs.getInt("strip_tab");
                            int purchase_items_id = rs.getInt("purchase_items_id");
                            String quantityUnit = rs.getString("quantity_unit");
                            String qty = rs.getString("tab_to_strip");

                            bcm = new BatchChooserModel(stockId, itemName, batch,
                                    expiryDate, quantity, quantityUnit, qty, strip_tab, purchase_items_id);
                        }

                        if (sicm.isStockable()) {
                            avlQuantity.setText(bcm.getFullQty());
                            tabPerStripL.setText(String.valueOf(bcm.getStripTab()));
                            stripTf.setDisable(false);
                            pcsTf.setDisable(false);
                        } else {
                            avlQuantity.setText("∞");
                            tabPerStripL.setText("");

                            stripTf.setText("0");
                            pcsTf.setText("1");
                            stripTf.setDisable(true);
                            pcsTf.setDisable(true);
                        }

                        saleRateTf.setFocusTraversable(false);

                        itemNameL.setText(bcm.getItemName());
                        ptm = method.getStockPrice(bcm.getPurchaseItemId());

                        mrpL.setText(String.valueOf(method.removeZeroAfterDecimal(ptm.getMrp())));
                        saleRB.setSelected(true);

                        if (ptm.getSaleRate() < 1) {
                            saleRateTf.setText(method.removeZeroAfterDecimal(ptm.getMrp()));
                        } else {
                            saleRateTf.setText(method.removeZeroAfterDecimal(ptm.getSaleRate()));
                        }


                        if (method.isItemAvlInCart(bcm.getStockId())) {

                            ps = null;
                            rs = null;

                            String cartQry = "select * from tbl_cart where stock_id = ? and created_by = ?";
                            ps = connection.prepareStatement(cartQry);
                            ps.setInt(1, bcm.getStockId());
                            ps.setInt(2, Login.currentlyLogin_Id);
                            rs = ps.executeQuery();
                            if (rs.next()) {
                                double mrp = rs.getDouble("mrp");
                                int strip = rs.getInt("strip");
                                int pcs = rs.getInt("pcs");

                                mrpL.setText(method.removeZeroAfterDecimal(mrp));
                                stripTf.setText(method.removeZeroAfterDecimal(strip));
                                pcsTf.setText(method.removeZeroAfterDecimal(pcs));
                                customDialog.showAlertBox("", "Item Already Added");
                            }

                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        DBConnection.closeConnection(connection, ps, rs);
                    }


            } else {
                customDialog.showAlertBox("", "Item stock not available. Please add purchase item");
            }
        }
    }

    public void enterKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            submit(null);
        }
    }

    public void submit(ActionEvent event) {
        String saleRate = saleRateTf.getText();
        String strip = stripTf.getText();
        String pcs = pcsTf.getText();
        boolean isStripAVl = false, isPcsAvl = false;
        int stripI = 0, pcsI = 0;
        double saleRateD = 0;

        if (null == bcm) {
            method.show_popup("Please select item", itemNameL, Side.RIGHT);
            return;
        } else if (strip.isEmpty() && pcs.isEmpty()) {
            method.show_popup("Please enter strip or pcs", pcsTf, Side.RIGHT);
            return;
        } else if (!strip.isEmpty()) {
            try {
                stripI = Integer.parseInt(strip);
            } catch (NumberFormatException e) {
                method.show_popup("Special characters are not allowed here", stripTf, Side.RIGHT);
                return;
            }
            if (pcs.isEmpty()) {
                if (stripI < 1) {
                    method.show_popup("Please enter valid strip", stripTf, Side.RIGHT);
                    return;
                }
            }
        }
        if (!pcs.isEmpty()) {
            try {
                pcsI = Integer.parseInt(pcs);
            } catch (NumberFormatException e) {
                method.show_popup("Special characters are not allowed here", pcsTf, Side.RIGHT);
                return;
            }
            if (strip.isEmpty()) {
                if (pcsI < 1) {
                    method.show_popup("Please enter valid pcs or tab", pcsTf, Side.RIGHT);
                    return;
                }
            }
        }

        if (stripI < 1 && pcsI < 1) {
            method.show_popup("Please enter strip or pcs", pcsTf, Side.RIGHT);
            return;
        } else if (saleRate.isEmpty()) {
            method.show_popup("Please sale rate", saleRateTf, Side.RIGHT);
            return;
        } else {
            try {
                saleRateD = Double.parseDouble(saleRate);
            } catch (NumberFormatException e) {
                method.show_popup("Please enter valid sale rate", saleRateTf, Side.RIGHT);
                return;
            }

            if (saleRateD < 1) {
                method.show_popup("Please enter valid sale rate", saleRateTf, Side.RIGHT);
                return;
            }
        }

        if (!strip.isEmpty() && stripI > 0) {
            String stockUnit = method.getStockUnitStockWise(bcm.getStockId());
            if (!Objects.equals(stockUnit, "TAB")) {
                customDialog.showAlertBox("", "Strip not available in stock. Only available in pcs");
                return;
            }
            isStripAVl = true;
        }
        if (!pcs.isEmpty() && pcsI > 0) {
            isPcsAvl = true;
        }
        int totalTab = 0;
        int stockQty = method.getQuantity(bcm.getStockId());
        if (isStripAVl && isPcsAvl) {
            totalTab = (stripI * bcm.getStripTab()) + pcsI;
            if (totalTab > stockQty) {
                customDialog.showAlertBox("", "Quantity not available");
                return;
            }

        } else if (isStripAVl) {
            totalTab = (stripI * bcm.getStripTab());
            if (totalTab > stockQty) {
                customDialog.showAlertBox("", "Strip not available");
                return;
            }

        } else {
            totalTab = pcsI;
            if (totalTab > stockQty) {
                customDialog.showAlertBox("", "PCS not available");
                return;
            }
        }

        if (totalTab < 1) {
            customDialog.showAlertBox("", "Please enter strip or pcs");
            return;
        }

        String qry = "";
        if (method.isItemAvlInCart(bcm.getStockId())) {
            qry = "UPDATE tbl_cart SET stock_id=?, MRP=?,  STRIP=?, PCS  = ? WHERE stock_id = " + bcm.getStockId() + " and created_by = ?";
        } else {
            qry = "INSERT INTO TBL_CART(stock_id, MRP,  STRIP, PCS,created_by) VALUES (?,?,?,?,?)";
        }
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(qry);
            ps.setInt(1, bcm.getStockId());
            ps.setDouble(2, saleRateD);
            ps.setInt(3, stripI);
            ps.setInt(4, pcsI);
            ps.setInt(5, Login.currentlyLogin_Id);

            int res = ps.executeUpdate();
            ptm = null;
            saleRB.setSelected(true);

            if (res > 0) {
                resetChooseItemField();
                callThread(Type.GET_CART_DATA);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Type type;

        public MyAsyncTask(Type type) {
            this.type = type;
        }

        @Override
        public void onPreExecute() {
            switch (type) {
                case GET_CART_DATA, GET_POPUP_ITEM_CART_DATA -> {
                    if (null != tableView) {
                        tableView.setItems(null);
                    }
                    if (null != itemList) {
                        itemList.clear();
                    }
                    assert tableView != null;
                    tableView.setPlaceholder(method.getProgressBarRed(40, 40));
                }
                case GET_PATIENT -> {
                    tableViewPatient.setPlaceholder(method.getProgressBarRed(40, 40));
                }
            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type) {
                case GET_CART_DATA -> getCartData();
                case GET_PATIENT -> {
                    ObservableList<DoctorModel> referredDoctorList = CommonUtil.getDoctor(DoctorType.OUT_SIDE);
                    Platform.runLater(() -> referByCom.setItems(referredDoctorList));
                    for (int i = 0; i < referredDoctorList.size(); i++) {
                        if (referredDoctorList.get(i).getDrName().equals("SELF")) {
                            int finalI = i;
                            Platform.runLater(() -> referByCom.getSelectionModel().select(finalI));
                        }
                    }
                    consultNameCom.setItems(CommonUtil.getDoctor(DoctorType.IN_HOUSE));
                    getPatient();
                }
                case GET_POPUP_ITEM_CART_DATA -> {
                   // getItems();
                    getCartData();
                }
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            switch (type) {
                case GET_PATIENT -> tableViewPatient.setPlaceholder(new Label("Patient Not Available."));
            }

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void resetChooseItemField() {
        avlQuantity.setText("");
        tabPerStripL.setText("");
        itemNameL.setText("SELECT ITEM");
        saleRateTf.setText("");
        mrpL.setText("");
        stripTf.setText("");
        pcsTf.setText("");
        stripTf.setDisable(false);
        pcsTf.setDisable(false);
    }

    private void getPatient() {
        patientList.clear();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = new DBConnection().getConnection();

            String qry = """
                            select * from patient_v
                    order by patient_id  desc
                     """;

            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();


            while (rs.next()) {

                int patient_id = rs.getInt("PATIENT_ID");
                int salutation_id = rs.getInt("salutation_id");
                int created_by = rs.getInt("created_by");
                int last_update_by = rs.getInt("last_update_by");

                String salutation_name = rs.getString("salutation_name");
                String first_name = rs.getString("first_name");
                String middle_name = rs.getString("middle_name");
                String last_name = rs.getString("last_name");

                String fullName = rs.getString("fullName");

                String gender = rs.getString("gender");
                String age = rs.getString("age");
                String address = rs.getString("address");
                String dob = rs.getString("dob");
                String phone = rs.getString("phone");

                String idType = rs.getString("id_type");
                String idNum = rs.getString("id_number");
                String guardianName = rs.getString("guardian_name");

                String weight = rs.getString("weight");
                String bp = rs.getString("bp");
                String pulse = rs.getString("pulse");
                String sugar = rs.getString("sugar");
                String spo2 = rs.getString("SPO2");
                String temp = rs.getString("temp");
                String cvs = rs.getString("cvs");
                String cns = rs.getString("cns");
                String chest = rs.getString("chest");
                String creationDate = rs.getString("creation_date");
                String lastUpdate = rs.getString("last_update");
                String patient_number = rs.getString("patient_number");
                String uhidNum = rs.getString("uhid_no");

                PatientModel pm = new PatientModel(patient_id, salutation_id, created_by, last_update_by, salutation_name, first_name,
                        middle_name, last_name, fullName, gender, age, address, dob, phone, idType, idNum, guardianName, weight, bp, pulse,
                        sugar, spo2, temp, cvs, cns, chest, creationDate, lastUpdate, patient_number, uhidNum);
                patientList.add(pm);
            }
            if (null != patientList) {
                if (!patientList.isEmpty()) {
                    pagination.setVisible(true);
                    search_Item();
                }
            }

            Platform.runLater(() -> {

                if (!patientList.isEmpty()) {
                    tableViewPatient.setPlaceholder(new Label(""));
                } else {
                    tableViewPatient.setPlaceholder(new Label("Patient not available"));
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {

                tableViewPatient.setPlaceholder(new Label("An error occurred while fetching the item"));
            });

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(patientList, p -> true);

        searchNameTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(patient -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(patient.getPhone()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return patient.getFullName().toLowerCase().contains(lowerCaseFilter);
            });

            changeTableViewPatient(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        Platform.runLater(() -> pagination.setCurrentPageIndex(0));
        changeTableViewPatient(0, rowsPerPage);
        Platform.runLater(() -> {
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        changeTableViewPatient(newValue1.intValue(), rowsPerPage);
                    });
        });

    }

    private void changeTableViewPatient(int index, int limit) {
        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));
        colPatientSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableViewPatient.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        setOptionalCellPatient();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, patientList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<PatientModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableViewPatient.comparatorProperty());

        tableViewPatient.setItems(sortedData);
    }

    private void setOptionalCellPatient() {

        Callback<TableColumn<PatientModel, String>, TableCell<PatientModel, String>>
                cellEdit = (TableColumn<PatientModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink patientNumHl = new Hyperlink(tableViewPatient.getItems().get(getIndex()).getPatientNumber());

                    patientNumHl.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 10px;-fx-alignment: center-left");

                    patientNumHl.setMinWidth(130);

                    patientNumHl.setOnAction(actionEvent -> {
                        tableViewPatient.getSelectionModel().select(getIndex());
                        patientModel = tableViewPatient.getSelectionModel().getSelectedItem();
                        patientNameL.setText(patientModel.getFullName());
                        genderL.setText(patientModel.getGender());
                        patientAgeL.setText(patientModel.getAge());
                        patientAddressL.setText(patientModel.getAddress());
                    });
                    HBox managebtn = new HBox(patientNumHl);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colAdmNum.setCellFactory(cellEdit);
    }

    public void checkOutBn(ActionEvent event) {

        String receivedAmountStr = receivedAmountTf.getText();
        double receivedAmountDouble = 0;

        if (itemList.isEmpty()) {
            customDialog.showAlertBox("Not available", "Item not available.please add at least one item");
            return;
        } else if (null == patientModel) {
            method.show_popup("Please select patient", patientNameL, Side.TOP);
            return;
        } else if (!method.isShopDetailsAvailable()) {
            customDialog.showAlertBox("Shop Details Not Available",
                    "Please update shop details");
            return;
        }

        boolean isCosultAvl = false;
        for (SaleEntryModel model : tableView.getItems()) {
            if (Objects.equals(model.getDepartmentCode(), "Consultation")) {
                isCosultAvl = true;
                break;
            }
        }
        if (isCosultAvl && consultNameCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please Select Doctor", consultNameCom, Side.RIGHT);
            return;
        }
        if (receivedAmountStr.isEmpty()) {
            method.show_popup("Please enter received Amount", receivedAmountTf, Side.TOP);
            return;
        }

        try {
            receivedAmountDouble = Double.parseDouble(receivedAmountStr);

        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid received Amount", receivedAmountTf, Side.TOP);
            return;
        }
        double  invoiceValue = Double.parseDouble(invoiceValueTf.getText());

        if (receivedAmountDouble > invoiceValue){
            method.show_popup("Received amount cannot more then invoice amount.", receivedAmountTf, Side.TOP);
            return;
        }

        String billType = billingTypeC.getSelectionModel().getSelectedItem();

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure you want to sell ?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            // Bill Type -> "REGULAR", "GST", "KITTY PARTY"
            SaleTask saleTask = new SaleTask(billType, patientModel.getPatientId(), receivedAmountDouble);
            saleTask.setDaemon(false);
            saleTask.execute();

        } else {
            alert.close();
        }

    }

    private class SaleTask extends AsyncTask<String, Integer, Boolean> {

        private String billType;
        private int patientId;
        private double receivedAmountDouble;

        public SaleTask(String billType, int patientId, double receivedAmountDouble) {
            this.billType = billType;
            this.patientId = patientId;
            this.receivedAmountDouble = receivedAmountDouble;
        }


        @Override
        public void onPreExecute() {
            method.hideElement(checkOutButton);
            progressBar.setVisible(true);
        }

        @Override
        public Boolean doInBackground(String... params) {

            boolean isAvailable = checkAllItemStock();
            if (isAvailable) {
                addSaleItem(patientId, billType, receivedAmountDouble);
            }
            return true;
        }

        private boolean checkAllItemStock() {

            ObservableList<SaleEntryModel> items = tableView.getItems();

            boolean isAvailable = true;

            Connection connection = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            String qry = "select concat(quantity,'-',quantity_unit) as quantity from tbl_stock where  stock_id = ? and quantity >= ?";

            try {
                connection = new DBConnection().getConnection();
                ps = connection.prepareStatement(qry);

                for (SaleEntryModel sem : items) {
                    ps.setInt(1, sem.getStockId());
                    ps.setDouble(2, sem.getTotalRequestQuantity());
                    rs = ps.executeQuery();

                    if (!rs.next()) {
                        isAvailable = false;

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Quantity Not Available.");
                            alert.setHeaderText("First delete this ( " + sem.getProductName() + ") item and then add it again.");
                            alert.initOwner(Main.primaryStage);
                            alert.show();
                        });

                        break;
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                DBConnection.closeConnection(connection, ps, rs);
            }

            return isAvailable;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            checkOutButton.setVisible(true);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void addSaleItem(int patientId, String billingType, double receivedAmountDouble) {

        String paytmModeS = paymentModeC.getSelectionModel().getSelectedItem();

        double totalTaxAmtD = totGstAmount;
        double invoiceValue = 0;

        invoiceValue = Double.parseDouble(invoiceValueTf.getText());
        Connection connection = null;
        PreparedStatement ps = null, psUpdateQty = null;
        ResultSet rs = null;
        ObservableList<SaleEntryModel> items = tableView.getItems();
        try {

            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            double addDiscountPercentage = 0, totalDiscountAmount = 0;

            if (!addDiscTF.getText().isEmpty()) {
                try {
                    addDiscountPercentage = addDiscTF.getText().isEmpty() ? 0 :
                            Double.parseDouble(addDiscTF.getText());

                } catch (NumberFormatException ignored) {
                    customDialog.showAlertBox("", "Please enter valid additional discount");
                    return;
                }
            }

            if (!totDisAmountL.getText().isEmpty()) {
                try {
                    double totDisAmount = Double.parseDouble(totDisAmountL.getText());
                    if (totDisAmount > 0) {
                        totalDiscountAmount = totDisAmount;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            String consultInvoiceNum = new GenerateBillNumber()
                    .generatorConsultReceiptNumber("OPD");
            String invoiceNumber = new GenerateBillNumber().getSaleBillNum();

            String saleMainQuery = "INSERT INTO TBL_SALE_MAIN(PATIENT_ID, SELLER_ID, additional_discount_amount," +
                    " PAYMENT_MODE, TOT_TAX_AMOUNT, NET_AMOUNT, INVOICE_NUMBER, BILL_TYPE,referred_by," +
                    "payment_reference_num,remarks,created_by,additional_discount_percentage,total_discount_amount,received_amount,consultation_doctor_id) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)\n";

            ps = connection.prepareStatement(saleMainQuery, new String[]{"sale_main_id"});
            ps.setInt(1, patientId);
            ps.setInt(2, Login.currentlyLogin_Id);
            ps.setDouble(3, 0);
            ps.setString(4, paytmModeS);
            ps.setDouble(5, totalTaxAmtD);
            ps.setDouble(6, invoiceValue);
            ps.setString(7, invoiceNumber);
            ps.setString(8, billingType);

            if (referByCom.getSelectionModel().isEmpty()) {
                ps.setNull(9, Types.NULL);
            } else {
                ps.setInt(9, referByCom.getSelectionModel().getSelectedItem().getDoctorId());
            }

            ps.setString(10, referenceNumTf.getText());
            ps.setString(11, remarksTf.getText());
            ps.setInt(12, Login.currentlyLogin_Id);
            ps.setDouble(13, addDiscountPercentage);
            ps.setDouble(14, totalDiscountAmount);
            ps.setDouble(15, receivedAmountDouble);

            if (consultNameCom.getSelectionModel().isEmpty()) {
                ps.setNull(16, Types.NULL);
            } else {
                ps.setInt(16, consultNameCom.getSelectionModel().getSelectedItem().getDoctorId());
            }

            int resMain = ps.executeUpdate();

            if (resMain > 0) {
                rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int sale_main_id = rs.getInt(1);


                    double duesAmount = invoiceValue - receivedAmountDouble;
                    boolean isDues = duesAmount > 0;

                    boolean isDuesInserted = false;

                    if (isDues) {
                        ps = null;
                        rs = null;

                        String duesQry = """
                                INSERT INTO TBL_DUES(SOURCE_ID, DUES_TYPE, DUES_AMOUNT, CREATED_BY) VALUES (?,?,?,?)                         
                                 """;
                        ps = connection.prepareStatement(duesQry);
                        ps.setInt(1, sale_main_id);
                        ps.setString(2, "BILLING");
                        ps.setDouble(3, duesAmount);
                        ps.setInt(4, Login.currentlyLogin_Id);
                        isDuesInserted = ps.executeUpdate() > 0;
                    }

                    ps = null;
                    rs = null;
                    int resItem = 0;

                    String query = "INSERT INTO TBL_SALE_ITEMS(SALE_MAIN_ID, ITEM_ID, ITEM_NAME, " +
                            "sale_rate, STRIP, PCS, DISCOUNT, HSN_SAC, igst, sgst, cgst, NET_AMOUNT, TAX_AMOUNT," +
                            "strip_tab,purchase_rate,mrp, PACK ,MFR_ID,BATCH ,EXPIRY_DATE,stock_id,additional_discount_percentage,is_stockable)" +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                    ps = connection.prepareStatement(query);

                    for (SaleEntryModel model : items) {

                        ps.setInt(1, sale_main_id);
                        ps.setInt(2, model.getItemId());
                        ps.setString(3, model.getProductName());
                        ps.setDouble(4, model.getSaleRate());
                        ps.setInt(5, model.getStrip());
                        ps.setInt(6, model.getPcs());
                        ps.setDouble(7, (model.getDiscount()));
                        ps.setLong(8, model.getHsn());
                        ps.setInt(9, model.getiGst());
                        ps.setInt(10, model.getsGst());
                        ps.setInt(11, model.getcGst());
                        ps.setDouble(12, model.getAmount());
                        ps.setDouble(13, model.getGstAmount());
                        ps.setInt(14, method.getTbPerStrip(model.getItemId()));
                        ps.setDouble(15, model.getPurchaseRate());
                        ps.setDouble(16, model.getMrp());
                        ps.setString(17, model.getPack());
                        ps.setInt(18, model.getMfrId());
                        ps.setString(19, model.getBatch());
                        ps.setString(20, model.getExpiryDate());
                        ps.setInt(21, model.getStockId());
                        ps.setDouble(22, addDiscountPercentage);
                        ps.setBoolean(23, model.isStockable());

                        resItem = ps.executeUpdate();

                        if (model.isStockable() && resItem > 0) {
                            int pcs = model.getPcs();
                            if (model.getStrip() > 0) {
                                int tab = model.getStrip() * method.getTbPerStrip(model.getItemId());
                                pcs += tab;
                            }
                            resItem = 0;
                            String qry = "UPDATE tbl_stock SET quantity = quantity-? WHERE  stock_id= ?";
                            psUpdateQty = connection.prepareStatement(qry);
                            psUpdateQty.setInt(1, pcs);
                            psUpdateQty.setInt(2, model.getStockId());
                            resItem = psUpdateQty.executeUpdate();

                        }



                        System.out.println(model.getDepartmentCode());

                        if (!consultNameCom.getSelectionModel().isEmpty() &&
                                Objects.equals(model.getDepartmentCode(), "Consultation")){

                            resItem = 0;

                            String insertConsultQuery = """
                                        INSERT INTO patient_consultation(PATIENT_ID, REFERRED_BY_DOCTOR_ID, REFERRED_BY_NAME, CONSULTATION_DOCTOR_ID,
                                                                         CREATED_BY,receipt_type,receipt_num,remarks,sale_main_id)
                                                                         VALUES (?,?,?,?,?,?,?,?,?);
                                        """;

                            PreparedStatement psConsult = connection.prepareStatement(insertConsultQuery);

                            psConsult.setInt(1, patientId);
                            psConsult.setInt(2, referByCom.getSelectionModel().getSelectedItem().getDoctorId());
                            psConsult.setString(3, referByCom.getSelectionModel().getSelectedItem().getDrName());
                            psConsult.setInt(4, consultNameCom.getSelectionModel().getSelectedItem().getDoctorId());
                            psConsult.setInt(5, Login.currentlyLogin_Id);
                            psConsult.setString(6, "OPD");
                            psConsult.setString(7, consultInvoiceNum);
                            psConsult.setString(8, remarksTf.getText());
                            psConsult.setInt(9,sale_main_id);
                            resItem = psConsult.executeUpdate();

                            if (resItem > 0){
                                psConsult.close();
                            }
                        }
                    }

                    if (isDues && !isDuesInserted) {
                        resItem = 0;
                    }

                    if (resItem > 0) {
                        connection.commit();
                        addDiscTF.setText(String.valueOf(0));
                        receivedAmountTf.setText("");
                        patientModel = null;
                        Platform.runLater(() -> patientNameL.setText("SELECT PATIENT"));
                        clearAll();
                        new GenerateInvoice().billingInvoice(sale_main_id, false, null, new Label());

                        if (psUpdateQty != null) {
                            psUpdateQty.close();
                        }
                    }

                }
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }
}
