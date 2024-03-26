package com.techwhizer.medicalshop.controller.product.purchase;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.StaticData;
import com.techwhizer.medicalshop.model.PriceTypeModel;
import com.techwhizer.medicalshop.model.PurchaseItemsTemp;
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddPurchaseItems implements Initializable {
    public Label productNameL;
    public VBox expireDateContainer;
    public ComboBox<String> monthCom;
    public ComboBox<Integer> yearCom;
    public TextField batchTf;
    public TextField lotNumTf;
    public TextField packingTf;
    public TextField quantityTf;
    public ComboBox<String> quantityUnitCom;
    public TextField purchaseRateTf;
    public TextField mrpTf;
    public TextField saleRateTf;
    public ComboBox<String> unitCom;
    public VBox stripTabContainer;
    public Label stripTabLabel;
    public TextField stripTabTf;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    private StaticData staticData;
    private ItemChooserModel icm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        staticData = new StaticData();
        stripTabContainer.setDisable(true);
        setData();

    }


    private void setData() {

        monthCom.setItems(staticData.getMonth());
        yearCom.setItems(staticData.getYear());
        unitCom.valueProperty().addListener((observableValue, s, t1) -> {

            if (t1.equalsIgnoreCase("STRIP")){
                quantityUnitCom.setItems(staticData.tabUnit);
                stripTabLabel.setText("Tab per strip :");
                stripTabContainer.setDisable(false);
               stripTabTf.setText(String.valueOf(icm.getTabPerStrip()));
            }else if (t1.equalsIgnoreCase("TAB")){
                quantityUnitCom.setItems(FXCollections.observableArrayList("TAB"));
                quantityUnitCom.getSelectionModel().select("TAB");
                stripTabLabel.setText("");
                stripTabContainer.setDisable(true);
                stripTabTf.setText("");

            } else {
                quantityUnitCom.setItems(staticData.getQuantityUnit());
                quantityUnitCom.getSelectionModel().select("PCS");
                stripTabLabel.setText("");
                stripTabContainer.setDisable(true);
                stripTabTf.setText("");
            }
        });
    }
    public void cancelClick(ActionEvent actionEvent) {
        Stage stage = ((Stage) batchTf.getScene().getWindow());
        if (null != stage && stage.isShowing()){
            stage.close();
        }
    }

    public void submitClick(ActionEvent actionEvent) {
        uploadData();
    }

    public void enterKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            uploadData();
        }
    }

    private void uploadData() {

        String batchNumber = batchTf.getText();
        String lotNum = lotNumTf.getText();
        String packing = packingTf.getText();
        String quantity = quantityTf.getText();

        String purchaseRate = purchaseRateTf.getText();
        String mrp = mrpTf.getText();
        String saleRate = saleRateTf.getText();
        String stripTab = stripTabTf.getText();
        int stripTabInt = 0,quantityD = 0;
        double saleRateD = 0.0, purchasePriceD = 0.0,mrpD = 0.0;

        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(batchNumber);
        boolean found = matcher.find();

        if (null == icm) {
            method.show_popup("Please select product", productNameL, Side.RIGHT);
            return;
        } else if (batchNumber.isEmpty()) {
            method.show_popup("Please enter batch", batchTf, Side.RIGHT);
            return;
        }else if (found){
            method.show_popup("Please remove space from batch", batchTf, Side.RIGHT);
            return;
        }else if (monthCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select expiry month", monthCom, Side.RIGHT);
            return;
        } else if (yearCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select expiry year", yearCom, Side.RIGHT);
            return;
        } else if (unitCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select unit", unitCom, Side.RIGHT);
            return;
        } else if (unitCom.getSelectionModel().getSelectedItem().equals("STRIP")) {
            try {
                 stripTabInt  = Integer.parseInt(stripTab);
            } catch (NumberFormatException e) {
                method.show_popup("Please enter number only", stripTabTf, Side.RIGHT);
                return;
            }

            if (stripTab.isEmpty() || stripTabInt < 1) {
                method.show_popup("Please enter tab per strip", stripTabTf, Side.RIGHT);
                return;
            }
        }else if (packing.isEmpty()){
            method.show_popup("Please enter packing", packingTf, Side.RIGHT);
            return;
        }
        if (quantity.isEmpty()) {
            method.show_popup("Please enter quantity", quantityTf, Side.RIGHT);
            return;
        }
        try {
            quantityD = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter a valid quantity without decimals", quantityTf, Side.RIGHT);
            return;
        }
        if (quantityD < 1 ){
            method.show_popup("Enter quantity more then 0", quantityTf, Side.RIGHT);
            return;
        }else if (quantityUnitCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select quantity unit", quantityUnitCom, Side.RIGHT);
            return;
        } else if (purchaseRate.isEmpty()) {
            method.show_popup("Please enter purchase price", purchaseRateTf, Side.RIGHT);
            return;
        }

        try {
        purchasePriceD =  Double.parseDouble(purchaseRate);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid purchase price", purchaseRateTf, Side.RIGHT);
            return;
        }
        if (purchasePriceD <1){
            method.show_popup("Enter valid rate", purchaseRateTf, Side.RIGHT);
            return;
        }else if (mrp.isEmpty()) {
            method.show_popup("Please enter mrp", mrpTf, Side.RIGHT);
            return;
        }
        try {
         mrpD = Double.parseDouble(mrp);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid mrp", mrpTf, Side.RIGHT);
            return;
        }

        if (mrpD <0){
            method.show_popup("Enter valid rate", mrpTf, Side.RIGHT);
            return;
        }else if (!saleRate.isEmpty()){
            try {
                saleRateD =  Double.parseDouble(saleRate);
            } catch (NumberFormatException e) {
                method.show_popup("Please enter valid sale rate", saleRateTf, Side.RIGHT);
                return;
            }

            if (saleRateD <0){
                method.show_popup("Enter valid rate", saleRateTf, Side.RIGHT);
                return;
            }
        }

        String expiryMonth = monthCom.getSelectionModel().getSelectedItem();
        int expiryYear = yearCom.getSelectionModel().getSelectedItem();
        String quantityUnit = quantityUnitCom.getSelectionModel().getSelectedItem();
        String unit = unitCom.getSelectionModel().getSelectedItem();

        String expiryDate = expiryMonth+"/"+expiryYear;

        PurchaseItemsTemp pit = new PurchaseItemsTemp(icm.getItemId(), icm.getItemName(), batchNumber,expiryDate,unit,stripTabInt,
                  packing,lotNum,quantityD,quantityUnit,Double.parseDouble(purchaseRate),
                Double.parseDouble(mrp),saleRateD);

       if (null != Main.primaryStage.getUserData()){
           Main.primaryStage.setUserData(null);
       }

       Main.primaryStage.setUserData(pit);

        Stage stage =(Stage) productNameL.getScene().getWindow();

        if (null != stage && stage.isShowing()){
            stage.close();
        }
    }

    public void chooseProduct(MouseEvent actionEvent) {
        customDialog.showFxmlDialog2("chooser/itemChooser.fxml", "SELECT PRODUCT");

        if (Main.primaryStage.getUserData() instanceof ItemChooserModel icm) {
            this.icm = icm;
            productNameL.setText(icm.getItemName());
            packingTf.setText(icm.getPacking());

            if (method.isItemAvailableInStock(icm.getItemId())){

                String stockUnit = method.getStockUnit(icm.getItemId());

                if (stockUnit.equalsIgnoreCase("TAB") || stockUnit.equalsIgnoreCase("STRIP")){
                    unitCom.setItems(staticData.strip);
                    unitCom.getSelectionModel().select(icm.getUnit());
                    if (icm.getUnit().equalsIgnoreCase("strip")) {
                        stripTabTf.setText(String.valueOf(icm.getTabPerStrip()));
                    }
                }else {
                    unitCom.setItems(staticData.pcsUnit);
                    unitCom.getSelectionModel().select("PCS");
                }

                PriceTypeModel ptm = method.getLastPrice(icm.getItemId());
                purchaseRateTf.setText(method.removeZeroAfterDecimal(ptm.getPurchaseRate()));
                mrpTf.setText(method.removeZeroAfterDecimal(ptm.getMrp()));
                saleRateTf.setText(method.removeZeroAfterDecimal(ptm.getSaleRate()));

            }else {
                if (icm.getUnit().equalsIgnoreCase("strip")) {
                    stripTabTf.setText(String.valueOf(icm.getTabPerStrip()));
                }
                unitCom.setItems(staticData.getUnit());
                unitCom.getSelectionModel().select(icm.getUnit());
            }
        }
    }
}
