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
    public TableColumn<ReturnProductModel, String> colReturnQuantity;
    public TableColumn<ReturnProductModel, String> colRefundAmount;
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
    private double totalRefundAmount, totalDiscountAmount;

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

                    int count = 0, totalItems = 0;

                    ObservableList<ReturnProductModel> returnProductModelsList = tableview.getItems();

                    for (ReturnProductModel rm : returnProductModelsList) {
                        if (rm.isReturn()) {
                            totalItems += 1;
                        }
                    }

                    for (ReturnProductModel rm : returnProductModelsList) {

                        if (rm.isReturn()) {

                            String returnItemQuery = """
                                            INSERT INTO TBL_RETURN_ITEMS(STOCK_ID, SALE_ITEM_ID, QUANTITY,
                                             QUANTITY_UNIT,return_main_id,discount_amount,amount,net_amount) VALUES (?,?,?,?,?,?,?,?);
                            """;
                            ps = connection.prepareStatement(returnItemQuery);

                            int quantity = Integer.parseInt(rm.getReturnQuantity());
                            ps.setInt(1, rm.getStockId());
                            ps.setInt(2, rm.getSaleItemID());
                            ps.setInt(3, quantity);
                            ps.setString(4, rm.getDisplayUnit());
                            ps.setInt(5, returnMainId);
                            ps.setDouble(6, rm.getReturnDiscountAmount());
                            ps.setDouble(7, rm.getAmount());
                            ps.setDouble(8, rm.getReturnNetAmount());


                            if ( ps.executeUpdate() > 0) {

                                if (rm.isStockable()){
                                    ps = null;

                                    String stockUpdateQuery = """
                                update tbl_stock set quantity = quantity+? where stock_id = ?
                                """;
                                    ps = connection.prepareStatement(stockUpdateQuery);
                                    ps.setInt(1, quantity);
                                    ps.setInt(2, rm.getStockId());
                                    if (ps.executeUpdate() > 0) {
                                        count += 1;
                                    }

                                }else {
                                    count += 1;
                                }

                            }
                        }
                    }


                    if (totalItems == count) {
                        connection.commit();
                        customDialog.showAlertBox("success", "Successfully Returned.");
                        totalDiscountAmount = 0;
                        totalRefundAmount = 0;
                        saleInvoiceNumber = "";
                        refundAmountL.setText("0");
                        search(null);

                    } else {
                        customDialog.showAlertBox("success", "Something went wrong. Please try again");
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
                           concat(tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end+tsi.pcs,'-',
                                  (case when tsi.strip > 0 then 'TAB' ELSE 'PCS' END)) as quantity , tsi.discount as discount_Percentage,tsi.is_stockable,
                    
                           (((( tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)+tsi.pcs)*(tsi.sale_rate/case when
                                  case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end > 0 then case when tsi.strip_tab > 0 then tsi.strip_tab else 1
                                   end else 1 end))* cast((greatest(cast(greatest(tsi.discount,0) as numeric)))as numeric)/100) as discount_Amount,
                           (tsi.sale_rate/greatest(case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end,1)) as mrp_Per_Tab,tsi.sale_rate,tsi.stock_id,
                           case when tsi.strip > 0 then 'TAB' ELSE 'PCS' END as display_unit,
                           ((( tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)+tsi.pcs)*(tsi.sale_rate/case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)-
                            (((( tsi.strip*case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end)+
                               tsi.pcs)*(tsi.sale_rate/case when tsi.strip_tab > 0 then tsi.strip_tab else 1 end))*tsi.discount/100)) as net_Amount
                    
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
                String displayUnit = rs.getString("display_unit");
                double netAmount = rs.getDouble("net_Amount");
                double mrp = rs.getDouble("sale_rate");

                double discountPercentage = rs.getDouble("discount_percentage");
                double discountAmount = rs.getDouble("discount_Amount");
                double mrpPerTab = rs.getDouble("mrp_Per_Tab");

                int returnedQty = Integer.parseInt(getReturnableQuantity(saleItemId).split("-")[0]);
                int totalQuantity = Integer.parseInt(quantity.split("-")[0]);
                String returnable = ((totalQuantity - returnedQty) + "-" + displayUnit);


                boolean isStockable = rs.getBoolean("is_stockable");


                String displayMrp = mrpPerTab + " / " + displayUnit;

                ReturnProductModel returnProductModel = new ReturnProductModel(saleItemId, productName,
                        Double.parseDouble(method.decimalFormatter(netAmount)), mrp,
                        quantity, saleDate, Double.parseDouble(method.decimalFormatter(discountAmount)),
                        "0", false, stockId, mrpPerTab, discountPercentage, returnable, displayMrp,
                        displayUnit, 0, 0, 0,isStockable);
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
            colMrp.setCellValueFactory(new PropertyValueFactory<>("displayMrp"));
            colReturnQuantity.setCellValueFactory(new PropertyValueFactory<>("returnQuantity"));
            colReturnableQty.setCellValueFactory(new PropertyValueFactory<>("returnableQuantity"));
            colRefundAmount.setCellValueFactory(new PropertyValueFactory<>("returnNetAmount"));
            colReturnQuantity.setCellFactory(TextFieldTableCell.forTableColumn());
            colReturnQuantity.setOnEditCommit(e -> {

                ReturnProductModel rmp = e.getTableView().getItems().get(e.getTablePosition()
                        .getRow());
                int avlQuantity = Integer.parseInt(e.getTableView().getItems().get(e.getTablePosition()
                        .getRow()).getReturnableQuantity().split("-")[0].replaceAll("[^0-9.]", ""));
                int inputQuantity = 0;
                try {
                    inputQuantity = Integer.parseInt(e.getNewValue().replaceAll("[^0-9.]", ""));
                    String qtyUnit = e.getTableView().getItems().get(e.getTablePosition()
                            .getRow()).getDisplayUnit();
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
                            double rate = rmp.getMrpPerTab();


                            double disPer = rmp.getDiscountPercentage();
                            double disAmt = (((rate * inputQuantity) * disPer) / 100);
                            double reAmt = (rate * inputQuantity);

                            rmp.setAmount(reAmt);
                            rmp.setReturnDiscountAmount(disAmt);

                            double netAmount = reAmt - disAmt;
                            e.getTableView().getItems().get(e.getTablePosition().getRow()).setReturnNetAmount(netAmount);
                            rmp.setReturnNetAmount(netAmount);
                            calculate();
                           tableview.refresh();
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
                totalRefundAmount += rp.getAmount();
                totalDiscountAmount += rp.getReturnDiscountAmount();
            }
        }
        double refundAmount = ((totalRefundAmount - totalDiscountAmount));
        refundAmountL.setText(String.valueOf(Math.round(refundAmount)));
    }

    public void keyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            search(null);
        }
    }
}
