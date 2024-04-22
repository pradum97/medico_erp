package com.techwhizer.medicalshop;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GetUserProfile;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.UserDetails;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.RoleKey;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

public class Dashboard implements Initializable {
    @FXML
    public BorderPane main_container;
    public StackPane contentArea;
    public Label fullName;
    @FXML
    public Button hardRefreshBn;
    public ImageView userImage;
    public Label userRole;
    public Hyperlink homeBn;
    public Hyperlink myProductBn;
    public Hyperlink saleReportBn;
    public Hyperlink returnProductBn;
    public Hyperlink stockH;
    public Separator topLine;
    public Hyperlink patientView;
    public Label patientViewTop;
    public Label billingBnTop;
    public Hyperlink consultListBn;
    private DBConnection dbConnection;
    private CustomDialog customDialog;
    private Main main;
    public static Stage stage;
    private Properties propRead;
    public MenuButton settingMenuButton;
    @FXML
    ImageView hideIv, showIv;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        hideElement(showIv,patientViewTop);
        hideMenu(null);
        setToolTip(patientView,homeBn,myProductBn,
                stockH,saleReportBn,returnProductBn,consultListBn);

        main_container.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/setting.css")).toExternalForm());
        dbConnection = new DBConnection();
        PropertiesLoader propLoader = new PropertiesLoader();
        propRead = propLoader.getReadProp();
        customDialog = new CustomDialog();
        main = new Main();

        if (Objects.equals(Login.currentRoleName, RoleKey.STAFF)){
            myProductBnClick(null);
        } else {
            homeBnClick(null);
        }
        addButtonMenu();
        setUserData();

        setting();
    }

    void unselectedBg(Node... nodes) {
        for (Node node : nodes) {
            node.setStyle("-fx-background-color: transparent");
            node.setDisable(false);
        }
    }

    void selectedBg(Node node) {
        //node.setDisable(true);
        node.setStyle("""
                -fx-background-color: #0881ea;
                -fx-border-color: transparent;
                -fx-font-weight: bold;
                -fx-background-radius: 4;
                -fx-border-radius: 4px;
                """);
    }


    public void hideMenu(MouseEvent mouseEvent) {
        homeBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        myProductBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        returnProductBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        saleReportBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        stockH.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        patientView.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        consultListBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        showIv.setVisible(true);
       hideElement(userImage,fullName,userRole, fullName,hideIv,topLine);
    }

    public void hideElement(Node... node) {
        for (Node n : node){
            n.setVisible(false);
            n.managedProperty().bind(n.visibleProperty());
        }
    }

    public void showMenu(MouseEvent mouseEvent) {
        homeBn.setContentDisplay(ContentDisplay.LEFT);
        myProductBn.setContentDisplay(ContentDisplay.LEFT);
        returnProductBn.setContentDisplay(ContentDisplay.LEFT);
        saleReportBn.setContentDisplay(ContentDisplay.LEFT);
        stockH.setContentDisplay(ContentDisplay.LEFT);
        patientView.setContentDisplay(ContentDisplay.LEFT);
        consultListBn.setContentDisplay(ContentDisplay.LEFT);

        userImage.setVisible(true);
        fullName.setVisible(true);
        userRole.setVisible(true);
        topLine.setVisible(true);

        hideIv.setVisible(true);
        hideElement(showIv);
    }
    private void setting() {

        setVisible(homeBn, Objects.equals(Login.currentRoleName, RoleKey.ADMIN));
        setVisible(saleReportBn, Objects.equals(Login.currentRoleName, RoleKey.ADMIN));
        setVisible(returnProductBn, (Objects.equals(Login.currentRoleName, RoleKey.ADMIN) ||
                Objects.equals(Login.currentRoleName, RoleKey.STAFF)));

        // top
        setVisible(billingBnTop, Objects.equals(Login.currentRoleName, RoleKey.ADMIN) ||
                Objects.equals(Login.currentRoleName, RoleKey.STAFF));
    }

    private void setVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.managedProperty().bind(node.visibleProperty());
    }

    private void addButtonMenu() {

        // product -- start
        MenuItem masterMenu = new MenuItem("MASTER");

        MenuItem buildingMasterItem = new MenuItem("BUILDING MASTER");
        MenuItem floorMasterItem = new MenuItem("FLOOR MASTER");
        MenuItem roomMasterItem = new MenuItem("ROOM MASTER");
        MenuItem bedMasterItem = new MenuItem("BED MASTER");


        MenuItem discount = new MenuItem("DISCOUNT MASTER");
        MenuItem gst = new MenuItem("GST MASTER");
        MenuItem company = new MenuItem("MEDICINE COMPANY");
        MenuItem manufacture = new MenuItem("MEDICINE MANUFACTURE");
        MenuItem mr = new MenuItem("MEDICAL REPRESENTATIVE");


//        masterMenu.getItems().addAll(discount,gst,company,manufacture,mr,
//                buildingMasterItem,floorMasterItem,roomMasterItem);

        // general -- end
        Menu prescription = new Menu("PRESCRIPTION");
        MenuItem dealer = new MenuItem("DEALER");
        MenuItem shopData = new MenuItem("SHOP DETAILS");
        MenuItem profile = new MenuItem("PROFILE");
        MenuItem users = new MenuItem("USERS");
        MenuItem doctor = new MenuItem("DOCTOR");
        MenuItem returnHistory = new MenuItem("RETURN HISTORY");
        MenuItem purchaseHistory = new MenuItem("PURCHASE HISTORY");
        MenuItem dues = new MenuItem("DUES");
        MenuItem backup = new MenuItem("BACKUP");
        MenuItem logout = new MenuItem("LOGOUT");

        // prescription
        MenuItem frequency = new MenuItem("FREQUENCY");
        MenuItem timing = new MenuItem("TIMING");
        prescription.getItems().addAll(frequency,timing);

        // role control

        // top
     shopData.setVisible(Objects.equals(Login.currentRoleName, RoleKey.ADMIN));
     doctor.setVisible(Objects.equals(Login.currentRoleName, RoleKey.ADMIN));
        returnHistory.setVisible(Objects.equals(Login.currentRoleName, RoleKey.ADMIN) ||
                        Objects.equals(Login.currentRoleName, RoleKey.STAFF)
                );


        users.setVisible(Objects.equals(Login.currentRoleName, RoleKey.ADMIN));

        settingMenuButton.getItems().addAll(masterMenu, prescription, profile, users, shopData, doctor,
                returnHistory,purchaseHistory, dues,dealer, backup, logout);

        onClickAction(gst, shopData, profile, users, dealer, backup, company, discount,
                manufacture, mr, doctor, returnHistory,purchaseHistory,dues, frequency, timing, logout);

        onClickMasterMenuItem(masterMenu);
    }

    private void onClickMasterMenuItem(MenuItem masterMenu) {

        masterMenu.setOnAction(event -> customDialog.showFxmlFullDialog("master/master.fxml", "Master"));

    }

    private void onClickAction(MenuItem gst, MenuItem shopData,
                               MenuItem profile, MenuItem users, MenuItem dealer ,
                               MenuItem backup, MenuItem company, MenuItem discount,
                               MenuItem manufacture, MenuItem mr, MenuItem doctor,
                               MenuItem returnHistory,MenuItem purchaseHistory ,MenuItem dues, MenuItem frequency,
                               MenuItem timing, MenuItem logout) {

        gst.setOnAction(event -> {
            customDialog.showFxmlDialog2("product/gst/gstConfig.fxml", "GST");
            if (Objects.equals(Login.currentRoleName.toLowerCase(), "admin".toLowerCase())) {
                refreshPage();
            }
        });
        backup.setOnAction(event -> customDialog.showFxmlDialog2("db_backup.fxml", "BACKUP"));

        frequency.setOnAction(event -> customDialog.showFxmlDialog2("prescription/frequency.fxml", ""));
        timing.setOnAction(event -> customDialog.showFxmlDialog2("prescription/medicineTime.fxml", ""));

        returnHistory.setOnAction(event -> customDialog.showFxmlFullDialog("dashboard/returnHistory.fxml", "RETURN HISTORY"));
        purchaseHistory.setOnAction(event ->
                customDialog.showFxmlFullDialog("product/purchase/purchaseHistoryMain.fxml", "PURCHASE HISTORY"));

        dues.setOnAction(event ->
                customDialog.showFxmlFullDialog("dues/duesMain.fxml", "DUES"));


        doctor.setOnAction(event -> customDialog.showFxmlFullDialog("doctor/view_doctor.fxml", "DOCTORS"));


        shopData.setOnAction(event -> {
            if (new Method().isShopDetailsAvailable()){
                customDialog.showFxmlDialog2("shopDetails.fxml", "SHOP DETAILS");
            }else {
                customDialog.showFxmlDialog2("update/shopDetailsUpdate.fxml", "Shop details not available.Please submit shop details");
            }

        });
        company.setOnAction(event -> customDialog.showFxmlDialog2("product/viewCompany.fxml","Companies lists"));
        discount.setOnAction(event -> customDialog.showFxmlDialog2("product/discount/discount.fxml","Discounts"));
        users.setOnAction(event -> {
            customDialog.showFxmlFullDialog("user/users.fxml", "ALL USERS");
            if (Objects.equals(Login.currentRoleName.toLowerCase(), "admin".toLowerCase())) {
                refreshPage();
            }
        });
        dealer.setOnAction(event -> customDialog.showFxmlFullDialog("product/dealer/allDealer.fxml", "DEALERS"));
        profile.setOnAction(event -> {

            Main.primaryStage.setUserData(Login.currentlyLogin_Id);
            customDialog.showFxmlDialog2("user/userprofile.fxml", "MY PROFILE");
            if (Objects.equals(Login.currentRoleName.toLowerCase(), "admin".toLowerCase())) {
                refreshPage();
            }
        });

        manufacture.setOnAction(event -> customDialog.showFxmlDialog2("product/manufactureMain.fxml","Manufactures"));
        mr.setOnAction(event -> customDialog.showFxmlFullDialog("product/mr/mrMain.fxml","Medical Representatives"));
        logout.setOnAction(event -> logout());



    }

    private void refreshPage() {
        setUserData();
    }
    private void setUserData() {

        UserDetails userDetails = new GetUserProfile().getUser(Login.currentlyLogin_Id);

        if (null != userDetails) {
            fullName.setText((userDetails.getFirstName() + " " + userDetails.getLastName()).toUpperCase());
            userRole.setText(userDetails.getRole().toUpperCase());
//            String imgPath = "img/Avatar/" + userDetails.getUserImage();
//            userImage.setImage(new ImageLoader().load(imgPath));
        }
    }


    private void replaceScene(String fxml_file_name) {
        Parent parent =FileLoader.loadFxmlFile(fxml_file_name);
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(parent);
    }

    public void logout() {

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("ARE YOU SURE YOU WANT TO LOGOUT ?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            main.changeScene("auth/login.fxml", "LOGIN HERE");
            Login.currentlyLogin_Id = 0;
            Login.currentRoleName = "";
            Login.currentRole_Id = 0;
        } else {
            alert.close();
        }
    }

    public void homeBnClick(ActionEvent actionEvent) {
        selectedBg(homeBn);
        unselectedBg(patientView,consultListBn,myProductBn,stockH,
                        saleReportBn,returnProductBn);
        replaceScene("dashboard/home.fxml");
    }

    public void myProductBnClick(ActionEvent actionEvent) {
        selectedBg(myProductBn);
        unselectedBg(patientView,consultListBn,homeBn,stockH,
                saleReportBn,returnProductBn);
        replaceScene("dashboard/itemMaster.fxml");
    }
    public void stockReport(ActionEvent event) {
        selectedBg( stockH);
        unselectedBg(patientView,consultListBn,homeBn,myProductBn,
                saleReportBn,returnProductBn);
        replaceScene("dashboard/stockReport.fxml");
    }


    public void saleProductBnClick(MouseEvent actionEvent) {

        customDialog.showFxmlFullDialog("dashboard/billing.fxml","SALE ENTRY");
    }

    public void saleReportBnClick(ActionEvent actionEvent) {
        selectedBg(  saleReportBn);
        unselectedBg(patientView,consultListBn,homeBn,myProductBn,
                stockH,returnProductBn);
        replaceScene("reports/reports.fxml");
    }

    public void returnProductBnClick(ActionEvent actionEvent) {
        selectedBg(returnProductBn  );
        unselectedBg(patientView,consultListBn,homeBn,myProductBn,
                stockH,saleReportBn);
        replaceScene("dashboard/returnMedicine.fxml");
    }


    public void patientMain(MouseEvent mouseEvent) {

        customDialog.showFxmlFullDialog("patient/patientMain.fxml", "ALL PATIENTS");
    }

    public void patientViewClick(ActionEvent actionEvent) {
        selectedBg(   patientView);
        unselectedBg(consultListBn,homeBn,myProductBn,
                stockH,saleReportBn,returnProductBn);
        patientView.setFocusTraversable(true);
        replaceScene("patient/patientMain.fxml");
    }


    public void consultListBnClick(ActionEvent actionEvent) {
        selectedBg( consultListBn);
        unselectedBg(patientView,homeBn,myProductBn,
                stockH,saleReportBn,returnProductBn);
        replaceScene("consultant/consultant_list.fxml");

    }


    private void setToolTip(Hyperlink... hyperlinks){

        for(Hyperlink hl:hyperlinks){
            Tooltip tooltip  = new Tooltip(hl.getText());
            hl.setTooltip(tooltip);

        }

    }
}
