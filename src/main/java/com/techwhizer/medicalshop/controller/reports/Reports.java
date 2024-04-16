package com.techwhizer.medicalshop.controller.reports;

import com.techwhizer.medicalshop.FileLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class Reports implements Initializable {
    public TabPane containerTabPane;
    public Tab billingReportTab;
    public Tab invoiceReportTab;
    public Tab departmentWiseReportTab;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        billingReportTab.setContent(FileLoader.loadFxmlFile("reports/billingReport.fxml"));
        invoiceReportTab.setContent(FileLoader.loadFxmlFile("reports/invoiceReport.fxml"));
        departmentWiseReportTab.setContent(FileLoader.loadFxmlFile("reports/departmentWiseReport.fxml"));

//        tabPaneContainer.getSelectionModel().selectedItemProperty()
//                .addListener((observable, oldTab, newTab) -> {
//            if (newTab != null) {
//                if (newTab == tabBillingReport) {
//                    initAllBillingReport();
//                } else if (newTab == tabDeptBillingReport) {
//                    System.out.println("Data loaded dynamically for Tab 2");
//                }
//            }
//        });
    }
}
