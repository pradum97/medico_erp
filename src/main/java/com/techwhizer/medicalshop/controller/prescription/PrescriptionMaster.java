package com.techwhizer.medicalshop.controller.prescription;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.PrintPrescription;
import com.techwhizer.medicalshop.model.*;
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.model.chooserModel.PrescribeMedicineChooserModel;
import com.techwhizer.medicalshop.util.CommonUtil;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.type.DoctorType;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class PrescriptionMaster implements Initializable {

    public TextField itemNameTf;
    public TextField itemTagTf;
    public TextField quantityTf;
    public ComboBox<String> unitCom;
    public ComboBox<FrequencyModel> freqCom;
    public TextField durationTf;
    public ComboBox<String> durationType;
    public ComboBox<MedicineTimeModel> timingCom;
    public TextField compositionTf;
    public TextField remarkTf;

    public TextField doseTf;
    public TableView<ConsultantModel> tableViewPatient;
    public TableColumn<ConsultantModel, Integer> colPatientSr;
    public TableColumn<ConsultantModel, String> colReceiptNum;
    public TableColumn<ConsultantModel, String> colPatientName;
    public TableColumn<ConsultantModel, String> colConsultDate;
    public TableColumn<ConsultantModel, String> colActionConsult;
    public TextField searchTf;
    public Pagination pagination;
    public ComboBox<String> comStatus;
    public ComboBox<DoctorModel> comDoctor;
    public Button searchPatientBn;
    public Label gurdianNameL;
    public Label genderL;
    public Label ageL;
    public Label addressL;
    public Button print_save;
    public Button clearList;
    public Button addItemToList;
    public Label consultDoctorNameL;
    public Label receiptNumL;
    private ObservableList<ConsultantModel> consultList = FXCollections.observableArrayList();
    private ObservableList<String> durationTypeList = FXCollections.observableArrayList("Days");
    private ObservableList<String> unitList = FXCollections.observableArrayList("PCS", "STRIP");
    private ObservableList<FrequencyModel> frequencyList = FXCollections.observableArrayList();
    private ObservableList<MedicineTimeModel> timeList = FXCollections.observableArrayList();
    private ObservableList<String> consultStatusList = FXCollections.observableArrayList("All", "Pending", "Done");
    public Label patientNameL;
    public TableView<PrescribedMedicineModel> tableview;
    public TableColumn<PrescribedMedicineModel, Integer> colSr;
    public TableColumn<PrescribedMedicineModel, String> colMedicineName;
    public TableColumn<PrescribedMedicineModel, String> colDuration;
    public TableColumn<PrescribedMedicineModel, String> colFrequency;
    public TableColumn<PrescribedMedicineModel, String> colTimes;
    public TableColumn<PrescribedMedicineModel, String> colQty;
    public TableColumn<PrescribedMedicineModel, String> colAction;

    public TableColumn<PrescribedMedicineModel, String> colItemTag;
    public TableColumn<PrescribedMedicineModel, String> colComposition;
    public TableColumn<PrescribedMedicineModel, String> colRemarks;
    public TableColumn<PrescribedMedicineModel, String> colDose;
    public Button saveBn;
    public Button addMedicine;
    public Button emptyPrint;

    int rowsPerPage = 25;
    ConsultantModel selectedPatient;
    int selectedPrescribeMasterId;

    enum Type {
        PRINT, GET_PATIENT, SAVE, UPDATE, INIT,
        UPDATE_DELETE,
        VIEW_MEDICINE,
        GET_LAST
    }

    private ObservableList<PrescribedMedicineModel> medicineList = FXCollections.observableArrayList();
    private Method method;
    private ItemChooserModel icmGlobal;

    private Type oprationType;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        method.hideElement(clearList);

        tableViewPatient.setFixedCellSize(29);

        callThread(Type.INIT, new HashMap<>());
        setToolTip(colMedicineName);
        setToolTip(colComposition);
        setToolTip(colRemarks);
        setToolTip(colFrequency);
        setToolTip(colTimes);
    }

    public void clearListClick(ActionEvent actionEvent) {

        medicineList.clear();
        icmGlobal = null;
        getMedicine();
        tableview.refresh();
        saveBn.setVisible(true);
        method.hideElement(clearList);
        print_save.setText("SAVE & PRINT");
        saveBn.setText("SAVE");
        selectedPrescribeMasterId = 0;
        Platform.runLater(() -> {
            print_save.setGraphic(new ImageLoader().getPrintIcon());
        });
        print_save.setContentDisplay(ContentDisplay.LEFT);
        addItemToList.setDisable(false);
        oprationType = null;
        getMedicine();
    }

    public void searchPatientBnClick(ActionEvent actionEvent) {

        if (comDoctor.getSelectionModel().isEmpty() &&
                comStatus.getSelectionModel().isEmpty() &&
                searchTf.getText().isEmpty()) {

            new CustomDialog().showAlertBox("", "Empty filter not allow");
            return;
        }

        int doctorId = comDoctor.getSelectionModel().isEmpty() ? 0 : comDoctor.getSelectionModel().getSelectedItem().getDoctorId();
        Map<String, Object> data = new HashMap<>();
        data.put("status", comStatus.getSelectionModel().isEmpty() ? "All" : comStatus.getSelectionModel().getSelectedItem().toString());
        data.put("search_key", searchTf.getText().toString());
        data.put("doctor_id", doctorId);
        data.put("button", searchPatientBn);
        callThread(Type.GET_PATIENT, data);


    }

    private void config() {
        setComValue();
        getFrequency();
        getTimes();
        Map<String, Object> data = new HashMap<>();
        data.put("status", "Pending");
        data.put("search_key", "");
        data.put("doctor_id", 0);
        callThread(Type.GET_PATIENT, data);
        tableview.setFixedCellSize(30.0);
        comStatus.setItems(consultStatusList);
        Platform.runLater(() -> comStatus.getSelectionModel().select("Pending"));
        comDoctor.setItems(CommonUtil.getDoctor(DoctorType.IN_HOUSE));
        DoctorModel dm = new DoctorModel(0, "All");
        comDoctor.getItems().add(0, dm);
    }

    private void setComValue() {

        durationType.setItems(durationTypeList);
        unitCom.setItems(unitList);
        Platform.runLater(() -> durationType.getSelectionModel().selectFirst());
    }

    public void getTimes() {

        if (null != timeList) {
            timeList.clear();
        }


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = "select * from tbl_medicine_time";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();
            while (rs.next()) {

                String time = rs.getString("time");
                MedicineTimeModel fm = new MedicineTimeModel(time);
                timeList.add(fm);
            }

            timingCom.setItems(timeList);

        } catch (Exception e) {

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    public void getFrequency() {

        if (null != frequencyList) {
            frequencyList.clear();
        }


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = "select * from tbl_frequency";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();
            while (rs.next()) {

                String frequency = rs.getString("frequency");
                FrequencyModel fm = new FrequencyModel(frequency);
                frequencyList.add(fm);
            }

            freqCom.setItems(frequencyList);


        } catch (Exception e) {

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void editableEnable() {

        itemNameTf.setDisable(false);
        itemTagTf.setDisable(false);
        compositionTf.setDisable(false);
        doseTf.setDisable(false);
    }

    public void addItemClick(ActionEvent actionEvent) {

        new CustomDialog().showFxmlDialog2("chooser/itemChooser.fxml", "SELECT ITEM");
        if (Main.primaryStage.getUserData() instanceof ItemChooserModel icm) {
            icmGlobal = icm;
            itemNameTf.setText(icm.getItemName());
            itemTagTf.setText(icm.getProductTag());
            compositionTf.setText(icm.getComposition());
            doseTf.setText(icm.getMedicineDose());

            if (!icm.getItemName().isEmpty()) {
                itemNameTf.setDisable(true);
            }
            if (!icm.getProductTag().isEmpty()) {
                itemTagTf.setDisable(true);
            }
            if (!icm.getComposition().isEmpty()) {
                compositionTf.setDisable(true);
            }
            if (!icm.getMedicineDose().isEmpty()) {
                doseTf.setDisable(true);
            }

        } else {
            itemNameTf.setDisable(false);
            itemTagTf.setDisable(false);
            compositionTf.setDisable(false);
            doseTf.setDisable(false);
        }
    }

    public void addItemToListClick(ActionEvent actionEvent) {

        String medicineName = itemNameTf.getText();
        String medicineTag = itemTagTf.getText();
        String quantity = quantityTf.getText();
        String duration = durationTf.getText();
        String composition = compositionTf.getText();
        String dose = doseTf.getText();
        String remarks = remarkTf.getText();

        if (medicineName.isEmpty()) {
            method.show_popup("Please enter medicine name", itemNameTf);
            return;
        } else if (medicineTag.isEmpty()) {
            method.show_popup("Please enter medicine tag", itemTagTf);
            return;
        } else if (quantity.isEmpty()) {
            method.show_popup("Please enter quantity.", quantityTf);
            return;
        } else if (unitCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select quantity unit.", unitCom);
            return;
        } else if (freqCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select frequency.", freqCom);
            return;
        } else if (duration.isEmpty()) {
            method.show_popup("Please enter duration", durationTf);
            return;
        } else if (timingCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select time.", timingCom);
            return;
        } else if (composition.isEmpty()) {
            method.show_popup("Please enter medicine composition", compositionTf);
            return;
        } else if (dose.isEmpty()) {
            method.show_popup("Please enter medicine dose", doseTf);
            return;
        }

        try {
            Double.parseDouble(quantity);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid quantity.", quantityTf);
            return;
        }

        try {
            Double.parseDouble(duration);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter valid duration.", durationTf);
            return;
        }

        String freq = freqCom.getSelectionModel().getSelectedItem().getFrequency();
        String durationTypeStr = durationType.getSelectionModel().getSelectedItem();
        String times = timingCom.getSelectionModel().getSelectedItem().getTime();
        String qtyUnit = unitCom.getSelectionModel().getSelectedItem();

        PrescribedMedicineModel pmm = new PrescribedMedicineModel(medicineName,
                medicineTag, quantity + "-" + qtyUnit, freq,
                duration + "-" + durationTypeStr, times, composition, dose, remarks, icmGlobal == null ? 0 : icmGlobal.getItemId(), icmGlobal != null);

        medicineList.add(pmm);
        getMedicine();
        clearItem();
    }

    private void clearItem() {
        icmGlobal = null;
        itemNameTf.setText("");
        itemTagTf.setText("");
        quantityTf.setText("");
        durationTf.setText("");
        freqCom.getSelectionModel().clearSelection();
        timingCom.getSelectionModel().clearSelection();
        compositionTf.setText("");
        doseTf.setText("");
        remarkTf.setText("");
        editableEnable();
    }

    private void getMedicine() {
        tableview.setItems(medicineList);
        colSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));

        colMedicineName.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colTimes.setCellValueFactory(new PropertyValueFactory<>("times"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colFrequency.setCellValueFactory(new PropertyValueFactory<>("frequency"));

        colItemTag.setCellValueFactory(new PropertyValueFactory<>("medicineTag"));
        colComposition.setCellValueFactory(new PropertyValueFactory<>("composition"));
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remark"));
        colDose.setCellValueFactory(new PropertyValueFactory<>("dose"));
        setOptionalCell();
    }

    public void print_and_save(ActionEvent actionEvent) {

        if (selectedPatient == null) {
            new CustomDialog().showAlertBox("", "Please select patient.");
            return;

        }

        if (!medicineList.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("isPrintable", true);
            data.put("isOnlyPrint", print_save.getText().equalsIgnoreCase("print"));
            data.put("button", print_save);

            callThread(Type.PRINT, data);
        } else {

            new CustomDialog().showAlertBox("", "Please add at least one medicine.");

        }

    }

    public void saveMedicine(ActionEvent actionEvent) {

        if (selectedPatient == null) {
            new CustomDialog().showAlertBox("", "Please select patient.");
            return;

        }

        if (!medicineList.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("isPrintable", false);
            data.put("isOnlyPrint", print_save.getText().equalsIgnoreCase("print"));
            data.put("button", saveBn);

            callThread(Type.SAVE, data);

        } else {
            new CustomDialog().showAlertBox("", "Please add at least one medicine.");
        }

    }


    private void save(boolean isPrintable, boolean isOnlyPrint, Button button) {

        if (isOnlyPrint) {
            int lastPrescribeMasterId = getLastPrescribeMasterId(selectedPatient.getConsultation_id());

            new PrintPrescription().print(lastPrescribeMasterId, selectedPatient.getConsultation_id(), selectedPatient.getPatient_id(), button,
                    getConsultDoctorName(selectedPatient.getConsultation_id()), selectedPatient.getPatient_name(), false);

            return;
        }

        if (!medicineList.isEmpty()) {
            Connection connection = null;
            PreparedStatement ps = null;
            ResultSet rs = null;


            try {

                connection = new DBConnection().getConnection();
                connection.setAutoCommit(false);

                if (oprationType != null && oprationType == Type.UPDATE_DELETE && selectedPrescribeMasterId > 0) {
                    // Update

                    String qryDelete = "delete from prescribe_medicine_items  WHERE prescribe_master_medicine_id = ?";

                    ps = connection.prepareStatement(qryDelete);
                    ps.setInt(1, selectedPrescribeMasterId);

                    int res = ps.executeUpdate();

                    if (res > 0) {

                        System.out.println("rresss:" + res);
                        insertDate(isPrintable, isOnlyPrint, button, connection, ps, rs, true);
                    }


                } else {

                    insertDate(isPrintable, isOnlyPrint, button, connection, ps, rs, false);
                }

                // Create


            } catch (SQLException e) {
                new CustomDialog().showAlertBox("Failed", "Something went wrong. Please try again.");

            }


        } else {

            new CustomDialog().showAlertBox("", "Medicine not available.");
        }
    }

    private void insertDate(boolean isPrintable, boolean isOnlyPrint,
                            Button button, Connection connection, PreparedStatement ps, ResultSet rs,
                            boolean isUpdate) {

        try {

            int masterId;

            System.out.println("isUpdate:" + isUpdate);

            if (!isUpdate) {

                String qryMains = "INSERT INTO PRESCRIBE_MEDICINE_MASTER(CONSULTATION_ID, PATIENT_ID, INVOICE_NUM, CREATED_BY) VALUES (?,?,?,?)";
                ps = connection.prepareStatement(qryMains, new String[]{"prescribe_master_medicine_id"});
                ps.setInt(1, selectedPatient.getConsultation_id());
                ps.setInt(2, selectedPatient.getPatient_id());
                ps.setString(3, new GenerateBillNumber().getPrescriptionBillNumber());
                ps.setInt(4, Login.currentlyLogin_Id);
                int res = ps.executeUpdate();

                if (res > 0) {

                    rs = ps.getGeneratedKeys();

                    if (rs.next()) {
                        masterId = rs.getInt(1);
                        ps = null;
                        rs = null;
                    } else {
                        masterId = 0;
                    }
                } else {
                    masterId = 0;
                }
            } else {
                masterId = selectedPrescribeMasterId;
            }

            System.out.println("master id:" + masterId);

            if (masterId > 0) {


                int count = 0;

                for (PrescribedMedicineModel pmm : medicineList) {

                    String qryItems = """
                                    INSERT INTO prescribe_medicine_items(ITEM_NAME, ITEM_ID, PRESCRIBE_MASTER_MEDICINE_ID,
                                                                   IS_ITEM_EXISTS_IN_STOCK, COMPOSITION, TAG, REMARK, QUANTITY,
                                                                    TIME, DOSE, FREQUENCY, DURATION) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                                    """;
                    ps = connection.prepareStatement(qryItems);
                    ps.setString(1, pmm.getMedicineName());
                    ps.setInt(2, pmm.getItemId());
                    ps.setInt(3, masterId);
                    ps.setBoolean(4, pmm.isItemExists());
                    ps.setString(5, pmm.getComposition());
                    ps.setString(6, pmm.getMedicineTag());
                    ps.setString(7, pmm.getRemark());
                    ps.setString(8, pmm.getQuantity());
                    ps.setString(9, pmm.getTimes());
                    ps.setString(10, pmm.getDose());
                    ps.setString(11, pmm.getFrequency());
                    ps.setString(12, pmm.getDuration());

                    count += ps.executeUpdate();
                }

                if (count > 0) {

                    ps = null;
                    int res = 0;
                    if (!isUpdate) {
                        String updateConsulttatus = """
                                                                        
                                    update patient_consultation set consultant_status = 'Done' where consultation_id = ?
                                    """;
                        ps = connection.prepareStatement(updateConsulttatus);
                        ps.setInt(1, selectedPatient.getConsultation_id());

                        res = ps.executeUpdate();
                    }


                    if (res > 0 || isUpdate) {
                        connection.commit();

                        String status = comStatus.getSelectionModel().getSelectedItem().isEmpty() ?
                                "Pending" : comStatus.getSelectionModel().getSelectedItem();

                        String searchKey = searchTf.getText().isEmpty() ?
                                "" : searchTf.getText();

                        int doctorId = comDoctor.getSelectionModel().getSelectedItem() == null ?
                                0 : comDoctor.getSelectionModel().getSelectedItem().getDoctorId();

                        Map<String, Object> data = new HashMap<>();
                        data.put("status", status);
                        data.put("search_key", searchKey);
                        data.put("doctor_id", doctorId);


                        callThread(Type.GET_PATIENT, data);
                        new Method().hideElement(saveBn);

                        Platform.runLater(() -> {

                            print_save.setText("PRINT");
                            getMedicine();

                            if (isPrintable) {

                                new PrintPrescription().print(masterId, selectedPatient.getConsultation_id(),
                                        selectedPatient.getPatient_id(), button, selectedPatient.getConsult_name(), selectedPatient.getPatient_name(), false);

                            } else {
                                String msg = "Successfully Created.";
                                if (isUpdate) {
                                    msg = "Successfully Updated";
                                }

                                new CustomDialog().showAlertBox("Success", msg);
                            }

                            clearList.setVisible(true);
                            addItemToList.setDisable(true);

                        });

                    }

                }
            } else {

                connection.rollback();
                new CustomDialog().showAlertBox("Failed", "Something went wrong. Please try again.");
            }


        } catch (SQLException e) {
            new CustomDialog().showAlertBox("Failed", "Something went wrong. Please try again.");

        } finally {

            DBConnection.closeConnection(connection, ps, rs);
        }
    }


    private void setOptionalCell() {

        Callback<TableColumn<PrescribedMedicineModel, String>, TableCell<PrescribedMedicineModel, String>>
                cellAction = (TableColumn<PrescribedMedicineModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    ImageView ivDelete = new ImageView(new ImageLoader().load("img/icon/delete_ic.png"));
                    ivDelete.setFitHeight(12);
                    ivDelete.setFitWidth(12);

                    ivDelete.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3 ");


                    ivDelete.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            new Method().selectTable(getIndex(), tableview);
                            PrescribedMedicineModel icm = tableview.getSelectionModel().getSelectedItem();
                            tableview.getItems().remove(icm);
                        }
                    });

                    boolean isButtonVisible = true;

                    if (Objects.equals(print_save.getText(), "PRINT")) {
                        isButtonVisible = false;
                    } else if (oprationType != null && oprationType == Type.VIEW_MEDICINE) {
                        isButtonVisible = false;
                    }



                    ivDelete.setVisible(isButtonVisible);

                    HBox managebtn = new HBox(ivDelete);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(ivDelete, new Insets(2, 2, 0, 2));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellAction);
    }

    private void setToolTip(TableColumn<PrescribedMedicineModel, String> tc) {

        Callback<TableColumn<PrescribedMedicineModel, String>, TableCell<PrescribedMedicineModel, String>>
                cellFactoryDelete = (TableColumn<PrescribedMedicineModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(item);

                tooltipProperty().bind(Bindings.when(Bindings.or(emptyProperty(), itemProperty().isNull())).then((Tooltip) null).otherwise(new Tooltip(item)));

            }

        };

        tc.setCellFactory(cellFactoryDelete);
    }


    public void emptyPrint(ActionEvent actionEvent) {

        if (selectedPatient == null) {

            new CustomDialog().showAlertBox("", "Please select patient");
        } else {

            new PrintPrescription().print(0, selectedPatient.getConsultation_id(), selectedPatient.getPatient_id(), emptyPrint,
                    getConsultDoctorName(selectedPatient.getConsultation_id()), selectedPatient.getPatient_name(), true);
        }


    }

    private void callThread(Type type, Map<String, Object> data) {

        SaveMedicineTask task = new SaveMedicineTask(type, data);
        task.setDaemon(false);
        task.execute();

    }

    private class SaveMedicineTask extends AsyncTask<String, Integer, Boolean> {
        Type type;
        Map<String, Object> data;

        public SaveMedicineTask(Type type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public void onPreExecute() {
            if (null != data) {

                switch (type) {

                    case PRINT -> {
                        if (data.get("button") != null && data.get("button") instanceof Button) {
                            Button bn = (Button) data.get("button");
                            bn.setGraphic(method.getProgressBarWhite(20, 20));
                            bn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        }
                    }

                    case SAVE -> {
                        saveBn.setGraphic(method.getProgressBarWhite(20, 20));
                        saveBn.setDisable(true);
                    }

                    case GET_PATIENT -> {
                        if (data.get("button") != null && data.get("button") instanceof Button) {
                            Button bn = (Button) data.get("button");
                            bn.setGraphic(method.getProgressBarWhite(20, 20));
                            bn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        }
                        tableViewPatient.setPlaceholder(method.getProgressBarRed(35, 35));
                    }


                    case GET_LAST, UPDATE_DELETE, VIEW_MEDICINE -> {

                        tableview.setPlaceholder(method.getProgressBarRed(35, 35));
                    }

                }

            }

        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type) {

                case PRINT, SAVE -> {
                    boolean isPrintable = (Boolean) data.get("isPrintable");
                    boolean isOnlyPrint = (Boolean) data.get("isOnlyPrint");
                    save(isPrintable, isOnlyPrint, (Button) data.get("button"));
                }
                case GET_PATIENT -> {

                    getConsultPatient(data);

                }

                case INIT -> {
                    config();
                }

                case GET_LAST, UPDATE_DELETE, VIEW_MEDICINE -> {

                    getBilledItem(type, data);
                }

            }


            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            if (null != data) {

                switch (type) {

                    case PRINT -> {
                        //printBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }

                    case SAVE -> {
                        saveBn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                        saveBn.setDisable(false);
                    }
                    case GET_PATIENT -> {
                        if (data.get("button") != null && data.get("button") instanceof Button) {
                            Button bn = (Button) data.get("button");
                            bn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                        }
                        tableViewPatient.setPlaceholder(null);
                    }
                    case GET_LAST, UPDATE_DELETE, VIEW_MEDICINE -> {

                        tableview.setPlaceholder(null);
                    }
                }

            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getConsultPatient(Map<String, Object> data) {
        if (null != consultList) {
            consultList.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String status = ((String) data.get("status"));
            String searchKey = (String) data.get("search_key");
            int doctorId = (int) data.get("doctor_id");


            String qry = "select concat(coalesce(receipt_num,''),coalesce(patient_name,'')),* from consultant_history_v\n" +
                    "         where concat(coalesce(receipt_num,''),coalesce(patient_name,'')) ilike '%" + searchKey + "%'" +
                    "         and consultation_doctor_id = case when " + doctorId + "> 0 then " + doctorId + " else consultation_doctor_id end\n" +
                    "         and consultant_status = case when lower('" + status + "') =  'all' or lower('" + status + "') = ''  then consultant_status   else '" + status + "'  end\n" +
                    "         order by consultation_id desc\n";

            ps = connection.prepareStatement(qry);

            rs = ps.executeQuery();

            while (rs.next()) {
                int consultation_id = rs.getInt("consultation_id");
                int referred_by_doctor_id = rs.getInt("referred_by_doctor_id");
                int consultation_doctor_id = rs.getInt("consultation_doctor_id");
                int patient_id = rs.getInt("patient_id");

                String patient_name = rs.getString("patient_name");


                String consult_date = rs.getString("consult_date");
                String referred_by_name = rs.getString("referred_by_name");
                String consult_name = rs.getString("consult_name");
                String consultant_status = rs.getString("consultant_status");

                String receipt_num = rs.getString("receipt_num");
                String receipt_type = rs.getString("receipt_type");
                String remarks = rs.getString("remarks");
                String description = rs.getString("description");
                String guardian_name = rs.getString("guardian_name");
                String age = rs.getString("age");
                String gender = rs.getString("gender");
                String address = rs.getString("address");


                ConsultantModel cm = new ConsultantModel(consultation_id, referred_by_doctor_id, patient_id, consultation_doctor_id, patient_name,
                        consult_date, referred_by_name, consult_name, consultant_status, receipt_num, receipt_type, remarks, description,
                        guardian_name, gender, age, address);
                consultList.add(cm);
            }

            if (null != consultList) {
                if (!consultList.isEmpty()) {
                    pagination.setVisible(true);
                }

            }

            pagination.setCurrentPageIndex(0);
            changeTableViewPatient(0, rowsPerPage);
            Platform.runLater(() -> {
                pagination.currentPageIndexProperty().addListener(
                        (observable1, oldValue1, newValue1) -> {
                            changeTableViewPatient(newValue1.intValue(), rowsPerPage);
                        });
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void changeTableViewPatient(int index, int limit) {

        int totalPage = (int) (Math.ceil(consultList.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));
        colPatientSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableViewPatient.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patient_name"));
        colConsultDate.setCellValueFactory(new PropertyValueFactory<>("consult_date"));


        setOptionalCellPatientView();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, consultList.size());

        int minIndex = Math.min(toIndex, consultList.size());
        SortedList<ConsultantModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(consultList.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableViewPatient.comparatorProperty());

        tableViewPatient.setItems(sortedData);
    }

    private void setOptionalCellPatientView() {

        Callback<TableColumn<ConsultantModel, String>, TableCell<ConsultantModel, String>> cellAction = (TableColumn<ConsultantModel, String> param) -> {
            return new TableCell<>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);

                    } else {

                        Button bnPrint = new Button();


                        ImageView ivPrint = new ImageView(new ImageLoader().load("img/icon/print_ic.png"));
                        ivPrint.setFitHeight(14);
                        ivPrint.setFitWidth(14);

                        bnPrint.setGraphic(ivPrint);

                        bnPrint.setStyle("-fx-cursor: hand ; -fx-background-color:  #04852f ; -fx-background-radius: 3 ;-fx-padding: 2 3 2 3");

                        ImageView ivMenu = new ImageView(new ImageLoader().
                                load("img/menu_icon/vertical_menu_icon.png"));
                        ivMenu.setFitHeight(15);
                        ivMenu.setFitWidth(15);
                        Button menuButton = new Button();
                        menuButton.setPrefWidth(25);
                        ivMenu.setSmooth(true);
                        ivMenu.setPreserveRatio(true);
                        menuButton.setGraphic(ivMenu);
                        menuButton.setStyle("-fx-cursor: hand ; -fx-background-color:  #04852f ; -fx-background-radius: 3 ;-fx-padding: 2 1 2 1");


                        menuButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                tableViewPatient.getSelectionModel().select(getIndex());
                                ConsultantModel cm = tableViewPatient.getSelectionModel().getSelectedItem();

                                ContextMenu contextMenu = new ContextMenu();
                                contextMenu.setPrefWidth(200);

                                MenuItem selectLast = new MenuItem("Select Last");
                                MenuItem itemEdit = new MenuItem("Edit/Delete");
                                MenuItem viewItem = new MenuItem("View");

                                menuClick(selectLast, itemEdit, viewItem, cm);


                                contextMenu.getItems().addAll(selectLast, itemEdit, viewItem);

                                contextMenu.setStyle("""
                                                                            
                                            -fx-background-color: #bfbfbf;
                                            -fx-border-color:  #04852f;
                                            -fx-min-width: 100;
                                            -fx-padding: 5;
                                            -fx-text-fill: white;
                                                                            
                                        """);
                                contextMenu.show(menuButton, Side.RIGHT, 10, 0);
                            }
                        });
                        bnPrint.setOnAction((event) -> {
                            tableViewPatient.getSelectionModel().select(getIndex());
                            ConsultantModel cm = tableViewPatient.getSelectionModel().getSelectedItem();

                            new PrintPrescription().print(0, cm.getConsultation_id(),
                                    cm.getPatient_id(), bnPrint, cm.getConsult_name(), cm.getPatient_name(), false);

                        });

                        HBox managebtn = new HBox(bnPrint, menuButton);
                        managebtn.setStyle("-fx-alignment:center");
                        HBox.setMargin(menuButton, new Insets(0, 8, 0, 8));

                        setGraphic(managebtn);

                        setText(null);
                    }
                }

            };
        };

        Callback<TableColumn<ConsultantModel, String>, TableCell<ConsultantModel, String>>
                cellEdit = (TableColumn<ConsultantModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink admNumHl = new Hyperlink(tableViewPatient.getItems().get(getIndex()).getReceiptNum());

                    admNumHl.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" +
                            "-fx-border-color: transparent;-fx-font-size: 11;-fx-alignment: center-left");

                    admNumHl.setMinWidth(80);

                    admNumHl.setOnAction(actionEvent -> {
                        tableViewPatient.getSelectionModel().select(getIndex());
                        selectedPatient = tableViewPatient.getSelectionModel().getSelectedItem();

                        selectPatient(selectedPatient);


                    });
                    HBox managebtn = new HBox(admNumHl);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };


        colActionConsult.setCellFactory(cellAction);
        colReceiptNum.setCellFactory(cellEdit);
    }

    private void selectPatient(ConsultantModel cm) {

        patientNameL.setText(cm.getPatient_name());
        gurdianNameL.setText(cm.getGuardianName());
        genderL.setText(cm.getGender());
        ageL.setText(cm.getAge());
        addressL.setText(cm.getAddress());
        consultDoctorNameL.setText(cm.getConsult_name());
        receiptNumL.setText(cm.getReceiptNum());
    }

    private void menuClick(MenuItem selectLast, MenuItem itemEdit,
                           MenuItem viewItem, ConsultantModel cm) {

        selectLast.setOnAction(actionEvent -> {
            clearListClick(null);
            selectedPatient = cm;
            selectPatient(cm);
            Map<String, Object> map = new HashMap<>();
            map.put("consult_id", cm.getConsultation_id());
            callThread(Type.GET_LAST, map);

        });

        viewItem.setOnAction(actionEvent -> {
            selectedPatient = cm;
            selectPatient(cm);
            Map<String, Object> map = new HashMap<>();
            map.put("consult_id", cm.getConsultation_id());
            callThread(Type.VIEW_MEDICINE, map);

        });

        itemEdit.setOnAction(actionEvent -> {
            selectedPatient = cm;
            selectPatient(cm);
            Map<String, Object> map = new HashMap<>();
            map.put("consult_id", cm.getConsultation_id());
            callThread(Type.UPDATE_DELETE, map);
        });
    }

    private void getBilledItem(Type type, Map<String, Object> map) {

        int consultId = (int) map.get("consult_id");


        switch (type) {
            case GET_LAST -> {
                oprationType = Type.GET_LAST;
                clearList.setVisible(false);
                addItemToList.setDisable(false);
                clearItem();
                getBilledItem(getLastPrescribeMasterId(consultId));
            }

            case VIEW_MEDICINE -> {

                int consultCount = new PrintPrescription().totalPrescribeMedicine(consultId);

                if (consultCount > 1) {


                    Map<String, Object> data = new HashMap<>();
                    data.put("consult_id", consultId);
                    data.put("patient_id", selectedPatient.getPatient_id());
                    data.put("doctorName", selectedPatient.getConsult_name());
                    data.put("isPrint", false);
                    Main.primaryStage.setUserData(data);
                    Platform.runLater(() -> {
                        new CustomDialog().showFxmlDialog2("chooser/prescribeMedicineChooser.fxml", selectedPatient.getPatient_name());
                        if (Main.primaryStage.getUserData() != null &&
                                Main.primaryStage.getUserData() instanceof
                                        PrescribeMedicineChooserModel) {

                            PrescribeMedicineChooserModel pmc = (PrescribeMedicineChooserModel)
                                    Main.primaryStage.getUserData();

                            viewItems(pmc.getPrescribe_master_medicine_id());


                        }
                    });

                } else if (consultCount == 1) {
                    int lastPrescribeMedicine = getLastPrescribeMasterId(consultId);

                    viewItems(lastPrescribeMedicine);
                } else {
                    return;
                }
            }

            case UPDATE_DELETE -> {

                int consultCount = new PrintPrescription().totalPrescribeMedicine(consultId);

                if (consultCount > 1) {


                    Map<String, Object> data = new HashMap<>();
                    data.put("consult_id", consultId);
                    data.put("patient_id", selectedPatient.getPatient_id());
                    data.put("doctorName", selectedPatient.getConsult_name());
                    data.put("isPrint", false);
                    Main.primaryStage.setUserData(data);
                    Platform.runLater(() -> {
                        new CustomDialog().showFxmlDialog2("chooser/prescribeMedicineChooser.fxml", selectedPatient.getPatient_name());
                        if (Main.primaryStage.getUserData() != null &&
                                Main.primaryStage.getUserData() instanceof
                                        PrescribeMedicineChooserModel) {

                            PrescribeMedicineChooserModel pmc = (PrescribeMedicineChooserModel)
                                    Main.primaryStage.getUserData();

                            editItems(pmc.getPrescribe_master_medicine_id());


                        }
                    });

                } else if (consultCount == 1) {
                    int lastPrescribeMedicine = getLastPrescribeMasterId(consultId);

                    editItems(lastPrescribeMedicine);
                } else {
                    return;
                }
            }
        }
    }

    private void editItems(int prescribeMasterMedicineId) {
        clearListClick(null);
        oprationType = Type.UPDATE_DELETE;
        selectedPrescribeMasterId = prescribeMasterMedicineId;
        Platform.runLater(() -> {
            print_save.setText("UPDATE & PRINT");
            saveBn.setText("UPDATE");
        });
        clearList.setVisible(true);

        getBilledItem(prescribeMasterMedicineId);
    }

    private void viewItems(int lastPrescribeMedicine) {
        clearListClick(null);
        oprationType = Type.VIEW_MEDICINE;
        clearList.setVisible(true);
        addItemToList.setDisable(true);
        getBilledItem(lastPrescribeMedicine);
    }

    private void getBilledItem(int prescribeMasterId) {

        medicineList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = """
                      select to_char(pmm.creation_date,'DD/MM/YYYY HH12:MI AM')as prescribeDate,* from prescribe_medicine_master pmm
                                        left join prescribe_medicine_items pmi on
                              pmm.prescribe_master_medicine_id = pmi.prescribe_master_medicine_id
                                        left outer join public.patient_consultation pc on pmm.consultation_id = pc.consultation_id
                      where pmm.prescribe_master_medicine_id = ? order by pmm.prescribe_master_medicine_id desc
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, prescribeMasterId);
            rs = ps.executeQuery();


            while (rs.next()) {
                int itemId = rs.getInt("ITEM_ID");
                boolean isItemExistsInStock = rs.getBoolean("IS_ITEM_EXISTS_IN_STOCK");
                String itemName = rs.getString("item_name");
                String composition = rs.getString("COMPOSITION");
                String tag = rs.getString("TAG");
                String remark = rs.getString("REMARK");
                String quantity = rs.getString("QUANTITY");
                String time = rs.getString("TIME");
                String dose = rs.getString("DOSE");
                String frequency = rs.getString("FREQUENCY");
                String duration = rs.getString("DURATION");


                PrescribedMedicineModel pmm = new PrescribedMedicineModel(itemName, tag, quantity,
                        frequency, duration, time, composition, dose, remark, itemId, isItemExistsInStock);

                medicineList.add(pmm);

            }

            if (!medicineList.isEmpty()) {
                getMedicine();
                clearItem();
            } else {

                new CustomDialog().showAlertBox("Failed", "Item not exists.");
            }


        } catch (Exception e) {

            new CustomDialog().showAlertBox("Failed", "Something went wrong.");

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }


    }


    public int getLastPrescribeMasterId(int consultId) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "select  prescribe_master_medicine_id as pmmi from" +
                    "  prescribe_medicine_master where consultation_id = ? order by 1 desc limit 1 ";

            ps = connection.prepareStatement(qry);
            ps.setInt(1, consultId);

            rs = ps.executeQuery();

            int id = 0;
            while (rs.next()) {
                id = rs.getInt("pmmi");
            }

            return id;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    public String getConsultDoctorName(int consultId) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = """

                    select concat('Dr. ',td.dr_name) as doctorName from patient_consultation pc
                    left join tbl_doctor td on td.doctor_id = pc.consultation_doctor_id where consultation_id = ?

                    """;

            ps = connection.prepareStatement(qry);
            ps.setInt(1, consultId);

            rs = ps.executeQuery();

            String doctorName = "";
            if (rs.next()) {
                doctorName = rs.getString("doctorName");
            }

            return doctorName;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }
}
