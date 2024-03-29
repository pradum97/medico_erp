package com.techwhizer.medicalshop.controller.prescription;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.FrequencyModel;
import com.techwhizer.medicalshop.model.MedicineTimeModel;
import com.techwhizer.medicalshop.model.PrescriptionMedicationModel;
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddPrescriptionMedicine implements Initializable {
    public TextField itemNameTf;
    public TextField itemTagTf;
    public TextField quantityTf;
    public ComboBox<String> unitCom;
    public ComboBox<FrequencyModel> freqCom;
    public TextField durationTf;
    public ComboBox<String> durationType;
    public ComboBox<MedicineTimeModel> timingCom;
    public TextArea compositionTf;
    public TextArea remarkTf;

    public TextArea doseTf;

    private ObservableList<String> durationTypeList= FXCollections.observableArrayList("Days");
    private ObservableList<String> unitList= FXCollections.observableArrayList("PCS","STRIP");
    private ObservableList<FrequencyModel> frequencyList = FXCollections.observableArrayList();
    private ObservableList<MedicineTimeModel> timeList = FXCollections.observableArrayList();
    private Method method;
    private ItemChooserModel icmGlobal;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();

        setComValue();
        getFrequency();
        getTimes();
    }

    private void setComValue() {

        durationType.setItems(durationTypeList);
        unitCom.setItems(unitList);


        durationType.getSelectionModel().selectFirst();
    }

    public void addMedicineClick(ActionEvent actionEvent) {

        Map<String, Object> data = new HashMap<>();
        data.put("is_stockable", true);
        Main.primaryStage.setUserData(data);
        new CustomDialog().showFxmlDialog2("chooser/commonItemChooser.fxml", "SELECT ITEM");
        if (Main.primaryStage.getUserData() instanceof ItemChooserModel icm) {
            icmGlobal = icm;
           itemNameTf.setText(icm.getItemName());
           itemTagTf.setText(icm.getProductTag());
           compositionTf.setText(icm.getComposition());
           doseTf.setText(icm.getMedicineDose());

           if (!icm.getItemName().isEmpty()){
               itemNameTf.setDisable(true);
           }
            if (!icm.getProductTag().isEmpty()){
                itemTagTf.setDisable(true);
            }
            if (!icm.getComposition().isEmpty()){
                compositionTf.setDisable(true);
            }
            if (!icm.getMedicineDose().isEmpty()){
                doseTf.setDisable(true);
            }

        }else {
            itemNameTf.setDisable(false);
            itemTagTf.setDisable(false);
            compositionTf.setDisable(false);
            doseTf.setDisable(false);
        }
    }

    public void saveItemClick(MouseEvent mouseEvent) {

        String medicineName = itemNameTf.getText();
        String medicineTag = itemTagTf.getText();
        String quantity = quantityTf.getText();
        String duration = durationTf.getText();
        String composition = compositionTf.getText();
        String dose = doseTf.getText();
        String remarks = remarkTf.getText();

        if (medicineName.isEmpty()) {
            method.show_popup("Please enter medicine name", itemNameTf, Side.RIGHT);
            return;
        } else if (medicineTag.isEmpty()) {
            method.show_popup("Please enter medicine tag", itemTagTf, Side.RIGHT);
            return;
        } else if (quantity.isEmpty()) {
            method.show_popup("Please enter quantity.", quantityTf, Side.RIGHT);
            return;
        } else if (unitCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select quantity unit.", unitCom, Side.RIGHT);
            return;
        } else if (freqCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select frequency.", freqCom, Side.RIGHT);
            return;
        } else if (duration.isEmpty()) {
            method.show_popup("Please enter duration", durationTf, Side.RIGHT);
            return;
        } else if (timingCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select time.", timingCom, Side.RIGHT);
            return;
        } else if (composition.isEmpty()) {
            method.show_popup("Please enter medicine composition", compositionTf, Side.RIGHT);
            return;
        } else if (dose.isEmpty()) {
            method.show_popup("Please enter medicine dose", doseTf, Side.RIGHT);
            return;
        }

        String freq = freqCom.getSelectionModel().getSelectedItem().getFrequency();
        String durationTypeStr = durationType.getSelectionModel().getSelectedItem();
        String times = timingCom.getSelectionModel().getSelectedItem().getTime();
        String qtyUnit = unitCom.getSelectionModel().getSelectedItem();

        PrescriptionMedicationModel pmm = new PrescriptionMedicationModel(0,medicineName,
                medicineTag,quantity+"-"+qtyUnit,freq,
                duration+"-"+durationTypeStr,times,composition,dose,remarks, icmGlobal == null ?0:icmGlobal.getItemId(), icmGlobal  != null,0);

        Stage stage = ((Stage) itemNameTf.getScene().getWindow());

        if (null != stage && stage.isShowing()){
            Main.primaryStage.setUserData(pmm);
            stage.close();
        }
    }

    public void getTimes() {

        if(null != timeList){
            timeList.clear();
        }


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = "select * from tbl_medicine_time";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();
            while (rs.next()){

                String time = rs.getString("time");
                MedicineTimeModel fm = new MedicineTimeModel(time);
                timeList.add(fm);
            }

            timingCom.setItems(timeList);

        }catch (Exception e){

        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }


    }

    public void getFrequency() {

        if(null != frequencyList){
            frequencyList.clear();
        }


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = "select * from tbl_frequency";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();
            while (rs.next()){

                String frequency = rs.getString("frequency");
                FrequencyModel fm = new FrequencyModel(frequency);
                frequencyList.add(fm);
            }

         freqCom.setItems(frequencyList);


        }catch (Exception ignored){

        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }


    }

    public void closeButton(ActionEvent actionEvent) {
        new Method().closeStage(itemTagTf);
    }
}
