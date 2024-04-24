package com.techwhizer.medicalshop.controller.master;

import com.techwhizer.medicalshop.FileLoader;
import com.techwhizer.medicalshop.controller.dashboard.Billing;
import com.techwhizer.medicalshop.method.Method;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Master implements Initializable {
    public TabPane containerTabPane;
    public Tab bedMasterTab;
    public Tab roomMasterReportTab;
    public Tab floorMasterTab;
    public Tab buildingMasterTab;
    public Tab discountMasterTab;
    public Tab gstMasterTab;
    public Tab pharmaCompanyMasterTab;
    public Tab manufacturerMasterTab;
    public Tab mrMasterTab;
    public Tab wardFacilityTab;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Platform.runLater(() -> {
            Stage stage = (Stage) containerTabPane.getScene().getWindow();
            stage.setMaximized(true);

            buildingMasterTab.setContent(FileLoader.loadFxmlFile("master/buildingMaster.fxml"));
            floorMasterTab.setContent(FileLoader.loadFxmlFile("master/floorMaster.fxml"));
            wardFacilityTab.setContent(FileLoader.loadFxmlFile("master/ward/wardFacilityMaster.fxml"));
            roomMasterReportTab.setContent(FileLoader.loadFxmlFile("master/ward/wardMaster.fxml"));

            mrMasterTab.setContent(FileLoader.loadFxmlFile("product/mr/mrMain.fxml"));
            manufacturerMasterTab.setContent(FileLoader.loadFxmlFile("product/manufactureMain.fxml"));
            pharmaCompanyMasterTab.setContent(FileLoader.loadFxmlFile("product/viewCompany.fxml"));
            gstMasterTab.setContent(FileLoader.loadFxmlFile("product/gst/gstConfig.fxml"));
            discountMasterTab.setContent(FileLoader.loadFxmlFile("product/discount/discount.fxml"));

        });

    }
}
