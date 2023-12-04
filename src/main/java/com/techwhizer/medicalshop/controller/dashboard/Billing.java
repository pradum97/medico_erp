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
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
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
import java.util.*;

public class Billing implements Initializable {
    public Label patientNameL;
    public Label addItem;
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
    public Button saleButton;
    public Label totAmountL;
    public Label totDisAmountL;
    public Label totGstAmountL;
    public Label invoiceValueTf;
    public VBox paymentModeContainer;
    public ComboBox<String> paymentModeC;
    public ComboBox<String> billingTypeC;
    public VBox gstContainer;
    public Label taxTitleL;
    public TextField addDiscTF;
    public VBox amountContainer;
    public Button checkOutButton;
    public HBox progressBar;
    public Label doctorNameL;
    public Label patientAgeL;
    public Label patientAddressL;
    public Label itemNameL;
    public TextField stripTf;
    public TextField pcsTf;
    public Label saleRateLabel;
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
    public Label guardianNameL;
    private CustomDialog customDialog;
    private Method method;
    private DBConnection dbConnection;
    private StaticData staticData;
    private ObservableList<SaleEntryModel> itemList = FXCollections.observableArrayList();
    private double totGstAmount = 0, netTotalAmount = 0, totalDiscount = 0, totalAmtAsPerMrp = 0;
    private PatientModel patientModel;
    private DoctorModel doctorModel;
    private ObservableList<PatientModel> patientList = FXCollections.observableArrayList();
    private FilteredList<PatientModel> filteredData;
    static private int rowsPerPage = 20;
    private  BatchChooserModel bcm ;


    enum Type{
        GET_PATIENT,SAVE,GET_CART_DATA
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new Method();
        staticData = new StaticData();
        dbConnection = new DBConnection();
        method.hideElement(progressBar);
        tableViewPatient.setFixedCellSize(30);
        setData();
        callThread(Type.GET_PATIENT);
        callThread(Type.GET_CART_DATA);
    }

    private void setData() {
        paymentModeC.setItems(staticData.getPaymentMode());
        billingTypeC.setItems(staticData.getBillingType());
        paymentModeC.getSelectionModel().select("CASH");
        billingTypeC.getSelectionModel().select("REGULAR");
    }

    private void setTableDate() {
        tableView.refresh();
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

            invoiceValueTf.setText(method.removeZeroAfterDecimal(Math.round((netTotalAmount))));
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

                    Button selectBn = new Button();
                    ImageView iv = new ImageView(new ImageLoader().load("img/icon/delete_ic_white.png"));
                    iv.setFitHeight(17);
                    iv.setFitWidth(17);

                    selectBn.setGraphic(iv);
                    selectBn.setStyle("-fx-cursor: hand ; -fx-background-color: #c1061c ; -fx-background-radius: 3 ");

                    selectBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        SaleEntryModel sem = tableView.getSelectionModel().getSelectedItem();
                        removeItem(sem.getCartId());

                    });

                    HBox managebtn = new HBox(selectBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(selectBn, new Insets(0, 0, 0, 0));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellFactory);
    }

    private void getCartData() {

        if (null != itemList) {
            itemList.clear();
        }
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
                          coalesce(td.discount,0) as discount,td.discount_id,tpt.tax_id,
                          coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)+coalesce(tpt.igst,0)),0)as total_gst,
                          ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0)) totaltab,
                   
                          ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                          case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end as amountAsPerMrp,
                          ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                          case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0)
                              end-(( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                               case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else
                                coalesce(tc.mrp,0) end )*coalesce(td.discount,0)/100) as netAmount,
                   
                          ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*
                            case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else
                                coalesce(tc.mrp,0) end )*coalesce(td.discount,0)/100 as discountAmount,
                   
                          ( coalesce( ( ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )-
                                        ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )*coalesce(td.discount,0)/100),0)*100)/
                          (100+(coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)+coalesce(tpt.igst,0)),0))) as taxableAmount,
                   
                          ( coalesce( ( ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )-
                                        ( ((coalesce(tc.strip,0)*coalesce(tim.strip_tab,0))+coalesce(tc.pcs,0))*case when ts.quantity_unit = 'TAB' then (coalesce(tc.mrp,0)/coalesce(tim.strip_tab,0)) else coalesce(tc.mrp,0) end )*coalesce(td.discount,0)/100),0)*100)/
                          (100+(coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)+coalesce(tpt.igst,0)),0)))*coalesce((coalesce(tpt.sgst,0)+coalesce(tpt.cgst,0)
                                                                                                                             +coalesce(tpt.igst,0)),0)/100 as gstAmount
                   from tbl_cart tc
                            left join tbl_stock ts on tc.stock_id = ts.stock_id
                            left join tbl_items_master tim on tim.item_id = ts.item_id
                            left join tbl_discount td on tim.discount_id = td.discount_id
                            left join tbl_product_tax tpt on tpt.tax_id = tim.gst_id
                            left outer join tbl_purchase_items t on ts.purchase_items_id = t.purchase_items_id
                   """;
            ps = connection.prepareStatement(qry);
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

                itemList.add(new SaleEntryModel(itemId,cartId , stockId, productName, saleRate, pack, strip, pcs, expiryDate, discountId,
                        discount, gstId, totalGst, netAmount, hsn, iGst, cGst, sGst, gstAmount, purchaseRate, mrp,batch,mfrId,amountAsPerMrp));
                totGstAmount += gstAmount;
                totalDiscount += discountAmt;
                netTotalAmount += netAmount;
                totalAmtAsPerMrp += amountAsPerMrp;
            }

            setTableDate();
            Platform.runLater(()->{
                addDiscTF.textProperty().addListener((observableValue, s, val1) -> {
                    double mainValue = Math.round((netTotalAmount));

                    String val = addDiscTF.getText();
                    if (!val.isEmpty()){
                        try {
                            double addDis = Double.parseDouble(val);
                            if (addDis>mainValue){
                                method.show_popup("Please enter amount less then invoice value",addDiscTF);
                                addDiscTF.setText("");
                                return;
                            }else {
                                double invVal = Math.round(mainValue-addDis);
                                Platform.runLater(()->invoiceValueTf.setText(String.valueOf(invVal)));
                            }
                        } catch (NumberFormatException e) {
                            invoiceValueTf.setText(String.valueOf(mainValue));
                            method.show_popup("Please enter amount less then invoice value",addDiscTF);
                            addDiscTF.setText("");
                        }
                    }else {
                        invoiceValueTf.setText(String.valueOf(mainValue));
                    }

                });
            });

        } catch (SQLException e) {
            tableView.setPlaceholder(new Label("Something went wrong"));
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }


    public void selectDoctor(MouseEvent mouseEvent) {
        customDialog.showFxmlDialog2("chooser/doctorChooser.fxml", "SELECT DOCTOR");
        if (Main.primaryStage.getUserData() instanceof DoctorModel dm) {
            this.doctorModel = dm;
            doctorNameL.setText(dm.getDrName());
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
            String query = "TRUNCATE TABLE tbl_cart RESTART IDENTITY";

            try {
                con = dbConnection.getConnection();
                ps = con.prepareStatement(query);

                int res = ps.executeUpdate();

                if (res >= 0) {
                    if (null != itemList) {
                        itemList.clear();
                    }
                    callThread(Type.GET_CART_DATA);
                }

            } catch (SQLException e) {
                customDialog.showAlertBox("ERROR", "Failed to Clear Cart !");
                e.printStackTrace();
            } finally {
                DBConnection.closeConnection(con, ps, null);
                callThread(Type.GET_CART_DATA);
            }
        });
    }

    public void removeItem(int cartId) {
        Connection con = null;
        PreparedStatement ps = null;
        String query = "DELETE FROM tbl_cart WHERE cart_id = ?";

        try {
            con = dbConnection.getConnection();
            ps = con.prepareStatement(query);
            ps.setInt(1, cartId);
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

   private void callThread(Type type){
       MyAsyncTask myAsyncTask = new MyAsyncTask(type);
       myAsyncTask.setDaemon(false);
       myAsyncTask.execute();
    }

    public void addPatient(ActionEvent actionEvent) {
    }

    public void addItemToListClick(ActionEvent actionEvent) {
        submit(null);
    }

    public void chooseItem(MouseEvent mouseEvent) {
        Main.primaryStage.setUserData(null);
        customDialog.showFxmlDialog2("chooser/itemChooser.fxml", "SELECT ITEM");

        if (Main.primaryStage.getUserData() instanceof ItemChooserModel icm) {

            if (method.isItemAvailableInStock(icm.getItemId())) {

                if (method.isMultipleItemInStock(icm.getItemId())){

                    Map<String, Object> map = new HashMap<>();
                    map.put("item_id", icm.getItemId());
                    map.put("item_name", icm.getItemName());
                    Main.primaryStage.setUserData(map);
                    customDialog.showFxmlDialog2("chooser/batchChooser.fxml", "SELECT BATCH");

                    if (Main.primaryStage.getUserData() instanceof BatchChooserModel bcm) {
                        this.bcm = bcm;
                    }

                } else {

                    Connection connection = null;
                    PreparedStatement ps = null;
                    ResultSet rs = null;

                    try {
                        connection = new DBConnection().getConnection();

                        String qry = """
                                 select  stock_id,tpi.purchase_items_id ,tim.items_name,tim.strip_tab ,tpi.batch , tpi.expiry_date , ts.quantity , ts.quantity_unit  from tbl_stock ts
                                                    left join tbl_purchase_items tpi on tpi.purchase_items_id = ts.purchase_items_id
                                                    left join tbl_items_master tim on tim.item_id = ts.item_id
                                                    where tpi.item_id =?  and ts.quantity>0 order by expiry_date asc
                                """;
                        ps = connection.prepareStatement(qry);
                        ps.setInt(1,icm.getItemId());
                        rs = ps.executeQuery();

                        if (rs.next()){

                            int stockId = rs.getInt("stock_id");
                            String itemName = rs.getString("items_name");
                            String batch = rs.getString("batch");
                            String expiryDate = rs.getString("expiry_date");
                            int quantity = rs.getInt("quantity");
                            int strip_tab = rs.getInt("strip_tab");
                            int purchase_items_id = rs.getInt("purchase_items_id");
                            String quantityUnit = rs.getString("quantity_unit");
                            String qty = method.tabToStrip(quantity, strip_tab, quantityUnit);

                            bcm = new BatchChooserModel(stockId, itemName, batch,
                                    expiryDate, quantity, quantityUnit, qty,strip_tab,purchase_items_id);
                        }



                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }finally {
                        DBConnection.closeConnection(connection,ps,rs);
                    }

                }

                if (null == bcm){
                    return;
                }

                avlQuantity.setText(bcm.getFullQty());
                tabPerStripL.setText(String.valueOf(bcm.getStripTab()));
                itemNameL.setText(bcm.getItemName());
                PriceTypeModel ptm = method.getStockPrice(bcm.getPurchaseItemId());

                mrpL.setText(String.valueOf(method.removeZeroAfterDecimal(ptm.getMrp())));

                if (ptm.getSaleRate() < 1) {
                    saleRateTf.setText(method.removeZeroAfterDecimal(ptm.getMrp()));
                }else {
                    saleRateTf.setText(method.removeZeroAfterDecimal(ptm.getSaleRate()));
                }

                if (method.isItemAvlInCart(bcm.getStockId())) {
                    Connection connection = null;
                    PreparedStatement ps = null;
                    ResultSet rs = null;

                    try {
                        connection = new DBConnection().getConnection();
                        String qry = "select * from tbl_cart where stock_id = ?";
                        ps = connection.prepareStatement(qry);
                        ps.setInt(1, bcm.getStockId());
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            double mrp = rs.getDouble("mrp");
                            int strip = rs.getInt("strip");
                            int pcs = rs.getInt("pcs");

                            mrpL.setText(method.removeZeroAfterDecimal(mrp));
                            stripTf.setText(method.removeZeroAfterDecimal(strip));
                            pcsTf.setText(method.removeZeroAfterDecimal(pcs));
                            customDialog.showAlertBox("","Item Already Added");
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        DBConnection.closeConnection(connection, ps, rs);
                    }
                } else {
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
            method.show_popup("Please select item", itemNameL);
            return;
        } else if (strip.isEmpty() && pcs.isEmpty()) {
            method.show_popup("Please enter strip or pcs", pcsTf);
            return;
        } else if (!strip.isEmpty()) {
            try {
                stripI = Integer.parseInt(strip);
            } catch (NumberFormatException e) {
                method.show_popup("Special characters are not allowed here", stripTf);
                return;
            }
            if (pcs.isEmpty()) {
                if (stripI < 1) {
                    method.show_popup("Please enter valid strip", stripTf);
                    return;
                }
            }
        }
        if (!pcs.isEmpty()) {
            try {
                pcsI = Integer.parseInt(pcs);
            } catch (NumberFormatException e) {
                method.show_popup("Special characters are not allowed here", pcsTf);
                return;
            }
            if (strip.isEmpty()) {
                if (pcsI < 1) {
                    method.show_popup("Please enter valid pcs or tab", pcsTf);
                    return;
                }
            }
        }

        if (stripI < 1 && pcsI < 1) {
            method.show_popup("Please enter strip or pcs", pcsTf);
            return;
        } else if (saleRate.isEmpty()) {
            method.show_popup("Please sale rate", saleRateTf);
            return;
        } else if (!saleRate.isEmpty()) {
            try {
                saleRateD = Double.parseDouble(saleRate);
            } catch (NumberFormatException e) {
                method.show_popup("Please enter valid sale rate", saleRateTf);
                return;
            }

            if (saleRateD < 1) {
                method.show_popup("Please enter valid sale rate", saleRateTf);
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
            totalTab = (stripI *  bcm.getStripTab());
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
            qry = "UPDATE tbl_cart SET stock_id=?, MRP=?,  STRIP=?, PCS  = ? WHERE stock_id = " + bcm.getStockId();
        } else {
            qry = "INSERT INTO TBL_CART(stock_id, MRP,  STRIP, PCS) VALUES (?,?,?,?)";
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

            int res = ps.executeUpdate();

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

            switch (type){

                case GET_CART_DATA -> {
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

            switch (type){

            case GET_CART_DATA -> {
                getCartData();
            }

            case GET_PATIENT -> {
                getPatient();
            }
        }

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {

            switch (type){

                case GET_CART_DATA -> {
                    tableView.setPlaceholder(new Label("Not Available."));
                }

                case GET_PATIENT -> {
                    tableViewPatient.setPlaceholder(new Label("Patient Not Available."));
                }
            }


        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void resetChooseItemField(){

        avlQuantity.setText("");
        tabPerStripL.setText("");
        itemNameL.setText("SELECT ITEM");
        saleRateTf.setText("");
        mrpL.setText("");
        stripTf.setText("");
        pcsTf.setText("");

    }

    private void getPatient() {

        if (null != patientList) {
            patientList.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = new DBConnection().getConnection();

            String qry = """
                            select (TO_CHAR(tp.creation_date, 'DD-MM-YYYY')) as creation_date,
                           (TO_CHAR(tp.last_update, 'DD-MM-YYYY')) as last_update,
                         concat ( COALESCE(ts.name,''),' ',COALESCE(tp.first_name,''),' ',
                        COALESCE(tp.middle_name,''),' ',COALESCE(tp.last_name,'')) as fullName,
                        ts.name as salutation_name , * from tbl_patient tp
                    left join tbl_salutation ts on tp.salutation_id = ts.salutation_id
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
                String admission_number = rs.getString("admission_number");
                String uhidNum = rs.getString("uhid_no");

                PatientModel pm = new PatientModel(patient_id, salutation_id, created_by, last_update_by, salutation_name, first_name,
                        middle_name, last_name, fullName, gender, age, address, dob, phone, idType, idNum, guardianName, weight, bp, pulse,
                        sugar, spo2, temp, cvs, cns, chest, creationDate, lastUpdate, admission_number,uhidNum);
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

        pagination.setCurrentPageIndex(0);
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

                    Hyperlink admNumHl = new Hyperlink(tableViewPatient.getItems().get(getIndex()).getAdmissionNumber());

                    admNumHl.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 12;-fx-alignment: center-left");

                    admNumHl.setMinWidth(130);

                    admNumHl.setOnAction(actionEvent -> {
                        tableViewPatient.getSelectionModel().select(getIndex());
                        patientModel = tableViewPatient.getSelectionModel().getSelectedItem();
                        patientNameL.setText(patientModel.getFullName());
                        genderL.setText(patientModel.getGender());
                        patientAgeL.setText(patientModel.getAge());
                        patientAddressL.setText(patientModel.getAddress());
                        guardianNameL.setText(patientModel.getGuardianName());
                                           });
                    HBox managebtn = new HBox(admNumHl);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colAdmNum.setCellFactory(cellEdit);
    }

    public void checkOutBn(ActionEvent event) {

        if (itemList.size() < 1) {
            customDialog.showAlertBox("Not available", "Item not available.please add at least one item");
            return;
        } else if (null == patientModel) {
            method.show_popup("Please select patient", patientNameL);
            return;
        } else if (!method.isShopDetailsAvailable()) {
            customDialog.showAlertBox("Shop Details Not Available",
                    "Please update shop details");
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
            SaleTask saleTask = new SaleTask(billType, patientModel.getPatientId());
            saleTask.setDaemon(false);
            saleTask.execute();

        } else {
            alert.close();
        }

    }

    private class SaleTask extends AsyncTask<String, Integer, Boolean> {

        private String billType;
        private int patientId;

        public SaleTask(String billType, int patientId) {
            this.billType = billType;
            this.patientId = patientId;
        }


        @Override
        public void onPreExecute() {
            method.hideElement(checkOutButton);
            progressBar.setVisible(true);
        }

        @Override
        public Boolean doInBackground(String... params) {
            addSaleItem(patientId, billType);
            return true;
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

    private void addSaleItem(int patientId, String billingType) {

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
            double addiDisc = 0;

            if (!addDiscTF.getText().isEmpty()){
                try {
                    addiDisc = Double.parseDouble(addDiscTF.getText());
                } catch (NumberFormatException ignored) {
                    customDialog.showAlertBox("", "Please enter valid additional discount");
                    return;
                }
            }


            String invoiceNumber = new GenerateBillNumber().getSaleBillNum();

            String saleMainQuery = "INSERT INTO TBL_SALE_MAIN(PATIENT_ID, SELLER_ID, ADDITIONAL_DISCOUNT," +
                    " PAYMENT_MODE, TOT_TAX_AMOUNT, NET_AMOUNT, INVOICE_NUMBER, BILL_TYPE,doctor_id,payment_reference_num,remarks,created_by) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)\n";

            ps = connection.prepareStatement(saleMainQuery, new String[]{"sale_main_id"});
            ps.setInt(1, patientId);
            ps.setInt(2, Login.currentlyLogin_Id);
            ps.setDouble(3, addiDisc);
            ps.setString(4, paytmModeS);
            ps.setDouble(5, totalTaxAmtD);
            ps.setDouble(6, invoiceValue);
            ps.setString(7, invoiceNumber);
            ps.setString(8, billingType);

            if (null == doctorModel){
                ps.setNull(9, Types.NULL);
            }else {
                ps.setInt(9, doctorModel.getDoctorId());
            }

            ps.setString(10, referenceNumTf.getText());
            ps.setString(11, remarksTf.getText());
            ps.setInt(12, Login.currentlyLogin_Id);

            int resMain = ps.executeUpdate();

            if (resMain > 0) {
                rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int sale_main_id = rs.getInt(1);
                    ps = null;
                    rs = null;
                    int resItem = 0;
                    String query = "INSERT INTO TBL_SALE_ITEMS(SALE_MAIN_ID, ITEM_ID, ITEM_NAME, " +
                            "sale_rate, STRIP, PCS, DISCOUNT, HSN_SAC, igst, sgst, cgst, NET_AMOUNT, TAX_AMOUNT," +
                            "strip_tab,purchase_rate,mrp, PACK ,MFR_ID,BATCH ,EXPIRY_DATE,stock_id)" +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    ps = connection.prepareStatement(query);

                    for (SaleEntryModel model : items) {

                        ps.setInt(1, sale_main_id);
                        ps.setInt(2, model.getItemId());
                        ps.setString(3, model.getProductName());
                        ps.setDouble(4, model.getSaleRate());
                        ps.setInt(5, model.getStrip());
                        ps.setInt(6, model.getPcs());
                        ps.setDouble(7, model.getDiscount());
                        ps.setLong(8, model.getHsn());
                        ps.setInt(9, model.getiGst());
                        ps.setInt(10, model.getsGst());
                        ps.setInt(11, model.getcGst());
                        ps.setDouble(12, model.getAmount());
                        ps.setDouble(13, model.getGstAmount());
                        ps.setInt(14, method.getTbPerStrip(model.getItemId()));
                        ps.setDouble(15, model.getPurchaseRate());
                        ps.setDouble(16, model.getMrp());
                        ps.setString(17,model.getPack());
                        ps.setInt(18,model.getMfrId());
                        ps.setString(19,model.getBatch());
                        ps.setString(20,model.getExpiryDate());
                        ps.setInt(21,model.getStockId());

                        resItem = ps.executeUpdate();

                        if (resItem > 0) {
                            int pcs = model.getPcs();
                            if (model.getStrip() > 0) {
                                int tab = model.getStrip() * method.getTbPerStrip(model.getItemId());
                                pcs += tab;
                            }
                            String qry = "UPDATE tbl_stock SET quantity = quantity-? WHERE  stock_id= ?";
                            psUpdateQty = connection.prepareStatement(qry);
                            psUpdateQty.setInt(1, pcs);
                            psUpdateQty.setInt(2, model.getStockId());
                            psUpdateQty.executeUpdate();

                        }

                        System.out.println("count");
                    }
                    if (resItem > 0) {
                        psUpdateQty.close();
                        connection.commit();
                        addDiscTF.setText(String.valueOf(0));
                        patientModel = null;
                        Platform.runLater(() -> patientNameL.setText("SELECT PATIENT"));
                        clearAll();
                        switch (billingType){
                            case "REGULAR" -> {
                                new GenerateInvoice().regularInvoice(sale_main_id, false, null, new Label());
                            }
                            case "GST" -> {
                                new GenerateInvoice().gstInvoice(sale_main_id, false, null, new Label());
                            }
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
