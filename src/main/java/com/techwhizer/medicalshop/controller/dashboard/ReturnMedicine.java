package com.techwhizer.medicalshop.controller.dashboard;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.ReturnProductModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReturnMedicine implements Initializable {
    public TableView<ReturnProductModel> tableview;
    public TableColumn<ReturnProductModel, Integer> colSrNo;
    public TableColumn<ReturnProductModel, String> colProductName;
    public TableColumn<ReturnProductModel, String> colQty;
    public TableColumn<ReturnProductModel, String> colMrp;
    public TableColumn<ReturnProductModel, String> colDiscountAmount;
    public TableColumn<ReturnProductModel, String> colNetAmount;
    public TableColumn<ReturnProductModel, String> colPurchaseDate;
    public TableColumn<ReturnProductModel, String> colReturnQuantity;
    public TableColumn<ReturnProductModel, String> colReturnableQty;
    public Label invoicePrefixL;
    public TextField invoiceNumberTf;
    public Button searchBn;
    public Label refundAmountL;
    public Button submitBn;
    public TextField remarkTf;
    private Method method;
    private CustomDialog customDialog;
    private ObservableList<ReturnProductModel> itemList = FXCollections.observableArrayList();
    private String saleInvoiceNumber;
    private double additionalDiscount, totalRefundAmount, totalDiscountAmount;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        submitBn.setDisable(true);
        customDialog = new CustomDialog();
    }

    public void submitBn(ActionEvent event) {

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure you want to Return This Item");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            returnProduct();
        } else {
            alert.close();
        }
    }

    private void returnProduct() {
        String remark = remarkTf.getText();
        String invoiceNumber = new GenerateBillNumber().getReturnBillNumb();
        int returnBy = Login.currentlyLogin_Id;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            connection.setAutoCommit(false);
            String returnMainQuery = """
                    INSERT INTO TBL_RETURN_MAIN(INVOICE_NUMBER, RETURN_BY_ID, REFUND_AMOUNT,REMARK) VALUES (?,?,?,?)
                    """;
            ps = connection.prepareStatement(returnMainQuery, new String[]{"return_main_id"});
            ps.setString(1, invoiceNumber);
            ps.setInt(2, returnBy);
            ps.setDouble(3, totalRefundAmount);
            ps.setString(4, remark);

            if (ps.executeUpdate() > 0) {
                rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int returnMainId = rs.getInt(1);
                    ps = null;
                    rs = null;

                    String returnItemQuery = """
                            INSERT INTO TBL_RETURN_ITEMS(STOCK_ID, SALE_ITEM_ID, QUANTITY, QUANTITY_UNIT,return_main_id) VALUES (?,?,?,?,?);
                            """;
                    ps = connection.prepareStatement(returnItemQuery);
                    PreparedStatement psStockUpdate = null;

                    ObservableList<ReturnProductModel> returnProductModelsList = tableview.getItems();
                    for (ReturnProductModel rm : returnProductModelsList) {

                        if (rm.isReturn()){
                            int quantity = Integer.parseInt(rm.getReturnQuantity());

                            ps.setInt(1, rm.getStockId());
                            ps.setInt(2, rm.getSaleItemID());
                            ps.setInt(3, quantity);
                            ps.setString(4, "TAB");
                            ps.setInt(5, returnMainId);
                            ps.executeUpdate();

                            String saleMainUpdate = """
                                update tbl_sale_main set additional_discount = 0 where invoice_number = ?
                                """;
                            psStockUpdate = connection.prepareStatement(saleMainUpdate);
                            psStockUpdate.setString(1, saleInvoiceNumber);
                            psStockUpdate.executeUpdate();

                            String stockUpdateQuery = """
                                update tbl_stock set quantity = quantity+? where stock_id = ?
                                """;
                            psStockUpdate = connection.prepareStatement(stockUpdateQuery);
                            psStockUpdate.setInt(1, quantity);
                            psStockUpdate.setInt(2, rm.getStockId());
                            psStockUpdate.executeUpdate();

                            if (null != psStockUpdate) {
                                psStockUpdate.close();
                            }
                        }
                    }

                    connection.commit();
                    customDialog.showAlertBox("success", "Successfully Returned.");
                    invoiceNumberTf.setText("");
                    if (!itemList.isEmpty()) {
                        itemList.clear();
                        tableview.setItems(itemList);
                        totalDiscountAmount = 0;
                        totalRefundAmount = 0;
                        additionalDiscount = 0;
                        saleInvoiceNumber = "";
                        refundAmountL.setText("0");
                    }
                }
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    public void search(ActionEvent event) {
        itemList.clear();
        String invoiceNum = invoiceNumberTf.getText();

        if (invoiceNum.isEmpty()) {

            ContextMenu form_Validator = new ContextMenu();
            form_Validator.setAutoHide(true);
            form_Validator.getItems().add(new MenuItem("Please enter invoice number"));
            form_Validator.show(invoicePrefixL, Side.LEFT, 10, 0);
            return;
        }
        invoiceNum = invoicePrefixL.getText().concat(invoiceNum);
        saleInvoiceNumber = invoiceNum;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String query = """
                             select tsi.sale_item_id ,(TO_CHAR(tsm.sale_date, 'dd-MM-yyyy')) as sale_date,tsi.item_name,
                                    concat(tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end+tsi.pcs,'-','TAB') as quantity , tsi.discount as discountPercentage,
                                    (tsi.sale_rate/case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end) as mrpPerTab,
                                    tsi.sale_rate,tsi.stock_id,tsm.additional_discount,
                                    ((( tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)+tsi.pcs)*(tsi.sale_rate/case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)-
                                     (((( tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)+tsi.pcs)*(tsi.sale_rate/case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end))*tsi.discount/100)) as netAmount,
                                    (((( tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)+tsi.pcs)*(tsi.sale_rate/case when case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end > 0 then case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end else 1 end))*tsi.discount/100) as discountAmount
                             from tbl_sale_main tsm
                                      left join tbl_sale_items tsi on tsm.sale_main_id = tsi.sale_main_id
                             where invoice_number = ?
                                        
                    """;
            ps = connection.prepareStatement(query);
            ps.setString(1, invoiceNum);
            rs = ps.executeQuery();
            int count = 0;

            while (rs.next()) {
                count++;

                int saleItemId = rs.getInt("sale_item_id");
                int stockId = rs.getInt("stock_id");
                String productName = rs.getString("item_name");

                System.out.println(productName);
                String saleDate = rs.getString("sale_date");
                String quantity = rs.getString("quantity");
                double netAmount = rs.getDouble("netAmount");
                double mrp = rs.getDouble("sale_rate");

                double discountPercentage = rs.getDouble("discountPercentage");
                double mrpPerTab = rs.getDouble("mrpPerTab");

                additionalDiscount = rs.getDouble("additional_discount");
                double discountAmount = rs.getDouble("discountAmount");

                int returnedQty = Integer.parseInt(getReturnableQuantity(saleItemId).split("-")[0]);
                int totalQuantity = Integer.parseInt(quantity.split("-")[0]);

                String returnable = ((totalQuantity-returnedQty)+"-TAB");

                ReturnProductModel returnProductModel = new ReturnProductModel(saleItemId, productName, netAmount, mrp,
                        quantity, saleDate, discountAmount, "0", false, stockId, mrpPerTab, discountPercentage,returnable);
                itemList.add(returnProductModel);
            }

            if (count < 1) {
                customDialog.showAlertBox("", "Product not found. Please try again!");
                return;
            }

            tableview.setItems(itemList);

            colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));

            colProductName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
            colDiscountAmount.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
            colNetAmount.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
            colPurchaseDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
            colReturnQuantity.setCellValueFactory(new PropertyValueFactory<>("returnQuantity"));
            colReturnableQty.setCellValueFactory(new PropertyValueFactory<>("returnableQuantity"));
            colReturnQuantity.setCellFactory(TextFieldTableCell.forTableColumn());
            colReturnQuantity.setOnEditCommit(e -> {
                String qtyUnit = "TAB";

                int avlQuantity = Integer.parseInt(e.getTableView().getItems().get(e.getTablePosition()
                        .getRow()).getReturnableQuantity().split("-")[0].replaceAll("[^0-9.]", ""));
                int inputQuantity = 0;
                try {
                    inputQuantity = Integer.parseInt(e.getNewValue().replaceAll("[^0-9.]", ""));
                    if (inputQuantity>=0){
                        if (inputQuantity > avlQuantity) {
                            String msg = "Available Quantity:  " + avlQuantity + " -" + qtyUnit
                                    + "\n\nEnter Quantity :  " + inputQuantity + " -" + qtyUnit;

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Quantity not available.");
                            alert.setHeaderText(msg);
                            alert.setContentText("Please Enter Less Then :  " + avlQuantity + " -" + qtyUnit);
                            alert.initOwner(Main.primaryStage);
                            alert.showAndWait();
                            tableview.refresh();
                        } else {
                            e.getTableView().getItems().get(e.getTablePosition().getRow()).setReturn(inputQuantity > 0);
                            e.getTableView().getItems().get(e.getTablePosition().getRow()).setReturnQuantity(String.valueOf(inputQuantity));
                            calculate();
                        }
                    }else {
                        customDialog.showAlertBox("","Enter quantity more then 0");
                    }
                } catch (NumberFormatException ex) {
                   customDialog.showAlertBox("","Please enter valid quantity.");
                    e.getTableView().getItems().get(e.getTableView().getSelectionModel().getSelectedIndex()).setReturnQuantity(String.valueOf(0));
                   tableview.refresh();
                }

            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private String getReturnableQuantity(int saleItemId) {

        int qty = 0;

         Connection connection = null;
         PreparedStatement ps= null;
         ResultSet rs = null;

         try {
             connection = new DBConnection().getConnection();

             String query = "select COALESCE(sum(quantity),0) as totalQuantity from tbl_return_items where sale_item_id = ?";
             ps = connection.prepareStatement(query);
             ps.setInt(1,saleItemId);
             rs = ps.executeQuery();
             if (rs.next()){
                qty= rs.getInt("totalQuantity");
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }finally {
             DBConnection.closeConnection(connection,ps,rs);
         }
         return qty+"-TAB";
    }

    private void calculate() {

        totalRefundAmount = 0;
        totalDiscountAmount = 0;

        submitBn.setDisable(true);
        for (ReturnProductModel rp : tableview.getItems()) {
            if (rp.isReturn()) {
                submitBn.setDisable(false);
                int returnQuantity = 0;
                try {
                    returnQuantity = Integer.parseInt(rp.getReturnQuantity().replaceAll("[^0-9.]", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                double rate = rp.getMrpPerTab();
                double disAmt = ((rate * rp.getDiscountPercentage()) / 100) * returnQuantity;
                double reAmt = (rate * returnQuantity);

                totalRefundAmount += reAmt;
                totalDiscountAmount += disAmt;
            }
        }
        double refundAmount = ((totalRefundAmount - totalDiscountAmount) - additionalDiscount);
        refundAmountL.setText(String.valueOf(refundAmount));
    }

    public void keyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            search(null);
        }
    }
}
