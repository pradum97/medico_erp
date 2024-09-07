package com.techwhizer.medicalshop.controller.master;

import com.techwhizer.medicalshop.FileLoader;
import com.techwhizer.medicalshop.controller.dashboard.Billing;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.setting.ProjectType;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static com.techwhizer.medicalshop.setting.Setting.PROJECT_TYPE;

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
            loadFile();
        });
    }

    private void loadFile(){

if (ProjectType.IPD.name().equals(PROJECT_TYPE)) {

    buildingMasterTab.setContent(FileLoader.loadFxmlFile("master/buildingMaster.fxml"));
    floorMasterTab.setContent(FileLoader.loadFxmlFile("master/floorMaster.fxml"));
    roomMasterReportTab.setContent(FileLoader.loadFxmlFile("master/ward/wardMaster.fxml"));
    bedMasterTab.setContent(FileLoader.loadFxmlFile("master/bed/bedMaster.fxml"));
    wardFacilityTab.setContent(FileLoader.loadFxmlFile("master/ward/wardFacilityMaster.fxml"));
} else if (ProjectType.OPD.name().equals(PROJECT_TYPE)) {
    containerTabPane.getTabs().removeAll(roomMasterReportTab,floorMasterTab,wardFacilityTab,buildingMasterTab,bedMasterTab);
}

        mrMasterTab.setContent(FileLoader.loadFxmlFile("product/mr/mrMain.fxml"));
        manufacturerMasterTab.setContent(FileLoader.loadFxmlFile("product/manufactureMain.fxml"));
        pharmaCompanyMasterTab.setContent(FileLoader.loadFxmlFile("product/viewCompany.fxml"));
        gstMasterTab.setContent(FileLoader.loadFxmlFile("product/gst/gstConfig.fxml"));
        discountMasterTab.setContent(FileLoader.loadFxmlFile("product/discount/discount.fxml"));

    }
}
