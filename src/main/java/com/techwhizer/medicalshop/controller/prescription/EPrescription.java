package com.techwhizer.medicalshop.controller.prescription;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.DateTimePicker;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.investigation.model.InvestigationModel;
import com.techwhizer.medicalshop.controller.prescription.model.PrescriptionMasterModel;
import com.techwhizer.medicalshop.method.GenerateBillNumber;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.PrintPrescription;
import com.techwhizer.medicalshop.model.ConsultantModel;
import com.techwhizer.medicalshop.model.PrescriptionMedicationModel;
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.ItemChooserType;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.poi.ss.formula.FormulaType;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EPrescription implements Initializable {


    public TextArea remarksTa;
    public Button saveAsDraftBn;
    public Button printBn;
    public TableView<PrescriptionMasterModel> tableViewPrescription;
    public TableColumn<PrescriptionMasterModel, Integer> colPrescriptionSrNum;
    public TableColumn<PrescriptionMasterModel, String> colPrescriptionNum;
    public TableColumn<PrescriptionMasterModel, String> colPrescriptionDate;
    public SplitMenuButton getLastInfoMenuButton;

    enum Type {
        PRINT, SAVE, UPDATE, INIT, UPDATE_DELETE, VIEW_MEDICINE, GET_LAST, RE_BIND
    }

    enum LastPrescriptionType{
        INVESTIGATION,MEDICATION,BOTH
    }

    enum FromType{
        LAST_INFO_MENU,OTHER
    }


    public Pagination pagination;
    public Label genderL;
    public Label ageL;
    public Label addressL;
    public Button clearList;
    public Label receiptNumL;
    public TableView<InvestigationModel> investigationTableView;
    public TableColumn<InvestigationModel, Integer> colInveSr;
    public TableColumn<InvestigationModel, String> colInveItemName;
    public TableColumn<InvestigationModel, String> colInvePrescribedDate;
    public TableColumn<InvestigationModel, String> colInvestAction;
    public TableColumn<InvestigationModel, String> colInveResultDate;
    public TableColumn<InvestigationModel, String> colInveResultValue;
    private ObservableList<ConsultantModel> consultList = FXCollections.observableArrayList();
    public Label patientNameL;
    public TableView<PrescriptionMedicationModel> tableview;
    public TableColumn<PrescriptionMedicationModel, Integer> colSr;
    public TableColumn<PrescriptionMedicationModel, String> colMedicineName;
    public TableColumn<PrescriptionMedicationModel, String> colDuration;
    public TableColumn<PrescriptionMedicationModel, String> colFrequency;
    public TableColumn<PrescriptionMedicationModel, String> colTimes;
    public TableColumn<PrescriptionMedicationModel, String> colQty;
    public TableColumn<PrescriptionMedicationModel, String> colAction;
    public TableColumn<PrescriptionMedicationModel, String> colComposition;
    public TableColumn<PrescriptionMedicationModel, String> colRemarks;
    public TableColumn<PrescriptionMedicationModel, String> colDose;
    public Button emptyPrint;

    private ObservableList<PrescriptionMedicationModel> medicineList = FXCollections.observableArrayList();
    private ObservableList<InvestigationModel> investigationList = FXCollections.observableArrayList();
    private Method method;
    private CustomDialog customDialog;
    private ConsultantModel cm;
    private ObservableList<PrescriptionMasterModel> prescriptionList = FXCollections.observableArrayList();

    private ObjectProperty<Integer> prescriptionMasterId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();

        Platform.runLater(() -> {
            Stage stage = (Stage) genderL.getScene().getWindow();
            stage.setMaximized(true);
        });
        tableview.setFixedCellSize(30.0);
        tableViewPrescription.setFixedCellSize(29);

        investigationTableView.setFixedCellSize(30.0);



        if (Main.primaryStage.getUserData() instanceof ConsultantModel cm2) {
            this.cm = cm2;
            patientNameL.setText(cm2.getPatient_name());
            addressL.setText(cm2.getAddress());
            genderL.setText(cm2.getGender());
            ageL.setText(cm2.getAge());
        }

        callThread(Type.INIT, new HashMap<>());

        setToolTip(colMedicineName);
        setToolTip(colComposition);
        setToolTip(colRemarks);
        setToolTip(colFrequency);
        setToolTip(colTimes);

        setupTable();

        startup();
        printBn.setVisible(false);
    }

    private void startup() {
        prescriptionMasterId = new SimpleObjectProperty<>(0);
        prescriptionMasterId.addListener((observableValue, integer, newVal) -> printBn.setVisible(newVal>0));
    }

    private void setupTable() {
        investigationSetOptionalCell();
        setupMedicineTable();
        setupPrescriptionTable();
    }

    private void setupPrescriptionTable() {
        tableViewPrescription.setItems(prescriptionList);
        colPrescriptionSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(tableViewPrescription.getItems().indexOf(cellData.getValue()) + 1));
        colPrescriptionDate.setCellValueFactory(new PropertyValueFactory<>("prescriptionDate"));
        setOptionalCellPrescription();
    }

    public void addInvestigationClick(ActionEvent actionEvent) {
        investigationList.add(getInvestigationDefaultItem());
        double totalHeight = investigationTableView.getFixedCellSize() * investigationList.size() + 40;
        investigationTableView.setMinHeight(totalHeight);
    }

    private InvestigationModel getInvestigationDefaultItem() {
        return new InvestigationModel(0, "", "", null, null, null, 0);
    }

    private void investigationSetOptionalCell() {
        investigationTableView.setItems(investigationList);
        colInveSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(investigationTableView.getItems().indexOf(cellData.getValue()) + 1));
        colInvePrescribedDate.setCellValueFactory(new PropertyValueFactory<>("prescribedDateFor"));

        Callback<TableColumn<InvestigationModel, String>, TableCell<InvestigationModel, String>> cellAction =
                (TableColumn<InvestigationModel, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);

                        } else {
                            InvestigationModel icm = investigationTableView.getItems().get(getIndex());

                            ImageView ivDelete = new ImageView(new ImageLoader().load("img/icon/delete_ic.png"));
                            ivDelete.setFitHeight(12);
                            ivDelete.setFitWidth(12);

                            ivDelete.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3 ");

                            ivDelete.setVisible(icm.getResultValue() == null || icm.getResultValue().isEmpty());
                            ivDelete.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    new Method().selectTable(getIndex(), investigationTableView);
                                    InvestigationModel icm = investigationTableView.getSelectionModel().getSelectedItem();
                                    investigationList.remove(icm);
                                    investigationTableView.getItems().remove(icm);

                                    double totalHeight = investigationTableView.getFixedCellSize() * investigationList.size() - 40; // 30 is for header
                                    investigationTableView.setMinHeight(totalHeight);
                                }
                            });

                            HBox managebtn = new HBox(ivDelete);
                            managebtn.setStyle("-fx-alignment:center");
                            HBox.setMargin(ivDelete, new Insets(2, 2, 0, 2));

                            setGraphic(managebtn);
                            setText(null);

                        }
                    }
                };

        colInvestAction.setCellFactory(cellAction);

        Callback<TableColumn<InvestigationModel, String>, TableCell<InvestigationModel, String>> cellItemName = (TableColumn<InvestigationModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    InvestigationModel im = investigationTableView.getItems().get(getIndex());
                    Button inveItemSelect = new Button((null == im.getItemDetails() || im.getItemDetails().getItemName().isEmpty()) ? "SELECT ITEM" : im.getItemDetails().getItemName());
                    inveItemSelect.setMinWidth(210);
                    inveItemSelect.setStyle("-fx-border-color: grey;-fx-border-radius: 3;-fx-padding: 2 10 2 10");
                    inveItemSelect.setOnAction(actionEvent -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("item_chooser_type", ItemChooserType.INVESTIGATION);
                        Main.primaryStage.setUserData(data);
                        customDialog.showFxmlDialog2("chooser/itemChooser.fxml", "");

                        if (Main.primaryStage.getUserData() instanceof ItemChooserModel icm) {
                            im.setItemDetails(icm);
                            inveItemSelect.setText(icm.getItemName());
                        }
                    });

                    setGraphic(inveItemSelect);
                    setText(null);
                }
            }

        };


        Callback<TableColumn<InvestigationModel, String>, TableCell<InvestigationModel, String>> cellItemResultValue = (TableColumn<InvestigationModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    InvestigationModel im = investigationTableView.getItems().get(getIndex());
                    String resultValueStr = im.getResultValue();
                    TextField resultValue = new TextField(resultValueStr == null || resultValueStr.isEmpty() ? "" : resultValueStr);
                    resultValue.setPromptText("Enter result value");

                    resultValue.setMinWidth(190);
                    resultValue.setStyle("-fx-border-color: grey;-fx-border-radius: 3;");

                    resultValue.textProperty().addListener((observableValue, s, result) -> im.setResultValue(result));

                    HBox managebtn = new HBox(resultValue);
                    managebtn.setStyle("-fx-alignment:center-left;");

                    HBox.setMargin(resultValue, new Insets(2, 2, 0, 2));
                    setGraphic(resultValue);
                    setText(null);

                }
            }

        };

        Callback<TableColumn<InvestigationModel, String>, TableCell<InvestigationModel, String>> cellItemResultDateAndTime = (TableColumn<InvestigationModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    InvestigationModel im = investigationTableView.getItems().get(getIndex());
                    DateTimePicker dateTimePicker = new DateTimePicker(im.getResultDate());
                    dateTimePicker.dateTimeProperty().addListener((observableValue, localDateTime, dateTimeString) ->
                            im.setResultDate(dateTimeString));


                    setGraphic(dateTimePicker);
                    setText(null);

                }
            }

        };

        colInveResultDate.setCellFactory(cellItemResultDateAndTime);
        colInveResultValue.setCellFactory(cellItemResultValue);
        colInveItemName.setCellFactory(cellItemName);
    }
    public void clearListClick(ActionEvent actionEvent) {
        clearAll();
    }
    private void clearAll() {
        medicineList.clear();
        tableview.refresh();
        investigationList.clear();
        investigationTableView.refresh();
        prescriptionMasterId.setValue(0);
        receiptNumL.setText("");
        remarksTa.setText("");
    }


    private void init() {
        getPrescriptionList();
    }

    public void addMedicationClick(ActionEvent actionEvent) {
        customDialog.showFxmlDialog2("prescription/addPrescriptionMedicine.fxml", "");
        if (Main.primaryStage.getUserData() instanceof PrescriptionMedicationModel pmm) {
            medicineList.add(pmm);
            double totalHeight = tableview.getFixedCellSize() * medicineList.size() + 45;
            tableview.setMinHeight(totalHeight);
        }
    }

    private void setupMedicineTable() {
        tableview.setItems(medicineList);
        colSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(tableview.getItems().indexOf(cellData.getValue()) + 1));
        colMedicineName.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colTimes.setCellValueFactory(new PropertyValueFactory<>("times"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colFrequency.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        colComposition.setCellValueFactory(new PropertyValueFactory<>("composition"));
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remark"));
        colDose.setCellValueFactory(new PropertyValueFactory<>("dose"));
        setOptionalCell();
    }

    public void saveAsDraftBnClick(ActionEvent actionEvent) {
        saveBnClick(false, saveAsDraftBn);
    }

    public void printBnClick(ActionEvent actionEvent) {
        if(prescriptionMasterId.get()>0){
            new PrintPrescription().print(0,prescriptionMasterId.get(),printBn,false);
        }

    }

    private void saveBnClick(boolean isFinal, Button bn) {

        if (medicineList.isEmpty() && investigationList.isEmpty()) {
            new CustomDialog().showAlertBox("", "Please Fill At-least One Box Investigation or Medication");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("button", bn);
        data.put("is_final", isFinal);
        callThread(Type.SAVE, data);
    }

    private void setOptionalCell() {

        Callback<TableColumn<PrescriptionMedicationModel, String>, TableCell<PrescriptionMedicationModel, String>> cellAction =
                (TableColumn<PrescriptionMedicationModel, String> param) -> new TableCell<>() {
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
                            PrescriptionMedicationModel icm = tableview.getSelectionModel().getSelectedItem();
                            medicineList.remove(icm);
                            tableview.getItems().remove(icm);
                            double totalHeight = tableview.getFixedCellSize() * medicineList.size() - 45;
                            tableview.setMinHeight(totalHeight);
                        }
                    });

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

    private void setToolTip(TableColumn<PrescriptionMedicationModel, String> tc) {

        Callback<TableColumn<PrescriptionMedicationModel, String>, TableCell<PrescriptionMedicationModel, String>> cellFactoryDelete = (TableColumn<PrescriptionMedicationModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                tooltipProperty().bind(Bindings.when(Bindings.or(emptyProperty(),
                        itemProperty().isNull())).then((Tooltip) null).otherwise(new Tooltip(item)));
            }
        };
        tc.setCellFactory(cellFactoryDelete);
    }
    private void getPrescriptionList() {
        prescriptionList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = "select *,TO_CHAR(creation_date,'DD-MM-YYYY hh:mm PM') AS prescription_date from " +
                    "tbl_prescription_master where consultation_id = ? order by prescription_num asc";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, cm.getConsultation_id());
            rs = ps.executeQuery();
            while (rs.next()) {
                int prescriptionMasterId = rs.getInt("prescription_master_id");
                int consultationId = rs.getInt("consultation_id");
                int patientId = rs.getInt("patient_id");
                int createdBy = rs.getInt("created_by");
                int updatedBy = rs.getInt("update_by");
                String prescriptionDate = rs.getString("prescription_date");
                String prescriptionNum = rs.getString("prescription_num");
                String remark = rs.getString("prescription_num");
                boolean isFinal = rs.getBoolean("is_final");

                PrescriptionMasterModel pmm = new PrescriptionMasterModel(prescriptionMasterId, consultationId,
                        patientId, createdBy, updatedBy, isFinal, prescriptionDate, prescriptionNum, remark);
                prescriptionList.add(pmm);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
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

                    case PRINT, SAVE -> {
                        if (data.get("button") != null && data.get("button") instanceof Button bn) {
                            bn.setGraphic(method.getProgressBarWhite(20, 20));
                            bn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            bn.setDisable(true);
                        }
                    }


                }

            }

        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type) {

                case PRINT -> {


                }

                case SAVE -> {
                    save(data);
                }

                case INIT -> {
                    init();
                }
                case RE_BIND -> getPrescriptionDetailsById(((Integer) data.get("prescription_master_id")),LastPrescriptionType.BOTH,FromType.OTHER);
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
                        if (data.get("button") != null && data.get("button") instanceof Button bn) {
                            bn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                            bn.setDisable(false);
                        }

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

    private void setOptionalCellPrescription() {
        Callback<TableColumn<PrescriptionMasterModel, String>, TableCell<PrescriptionMasterModel, String>> cellEdit =
                (TableColumn<PrescriptionMasterModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Hyperlink prescriptionNumHl = new Hyperlink(tableViewPrescription.getItems().get(getIndex()).getPrescriptionNum());

                    prescriptionNumHl.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;" + "-fx-border-color: transparent;-fx-font-size: 11;-fx-alignment: center-left");
                    prescriptionNumHl.setMinWidth(80);

                    prescriptionNumHl.setOnAction(actionEvent -> {
                        tableViewPrescription.getSelectionModel().select(getIndex());
                        Map<String, Object> map = new HashMap<>();
                        map.put("prescription_master_id", tableViewPrescription.getItems().get(getIndex()).getPrescriptionMasterId());
                        clearAll();
                        callThread(Type.RE_BIND, map);
                    });
                    HBox managebtn = new HBox(prescriptionNumHl);
                    managebtn.setStyle("-fx-alignment: center-left");
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colPrescriptionNum.setCellFactory(cellEdit);
    }


    public void onlyInvestigationMenuClick(ActionEvent actionEvent) {
        getPrescriptionDetailsById(getLastPrescriptionMasterId(),LastPrescriptionType.INVESTIGATION,FromType.LAST_INFO_MENU);
    }

    public void onlyMedicationMenuClick(ActionEvent actionEvent) {
        getPrescriptionDetailsById(getLastPrescriptionMasterId(),LastPrescriptionType.MEDICATION,FromType.LAST_INFO_MENU);
    }

    public void bothMenuClick(ActionEvent actionEvent) {
        getPrescriptionDetailsById(getLastPrescriptionMasterId(),LastPrescriptionType.BOTH,FromType.LAST_INFO_MENU);
    }

    private int getLastPrescriptionMasterId(){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = """
                    select prescription_master_id from tbl_prescription_master where consultation_id = ? order by prescription_master_id desc
                                        
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1,cm.getConsultation_id());
            rs = ps.executeQuery();

            if (rs.next()){
                return rs.getInt("prescription_master_id");
            }else {
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void getPrescriptionDetailsById(int prescMasterId,LastPrescriptionType lastPrescriptionType,FromType fromType) {
        investigationList.clear();
        medicineList.clear();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = """
                    SELECT * from tbl_prescription_master where prescription_master_id = ?
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, prescMasterId);
            rs = ps.executeQuery();

            if (rs.next()) {
                prescriptionMasterId.setValue(rs.getInt("prescription_master_id"));
                String prescriptionNum = rs.getString("prescription_num");
                String remarks = rs.getString("remarks");

               switch (lastPrescriptionType){

                   case INVESTIGATION -> investigationList.addAll( getInvestigation(prescriptionMasterId.get()));
                   case MEDICATION -> medicineList.addAll(getMedication(prescriptionMasterId.get())) ;
                   default -> {
                       investigationList.addAll( getInvestigation(prescriptionMasterId.get()));
                       medicineList.addAll(getMedication(prescriptionMasterId.get())) ;
                   }
               }
                Platform.runLater(() -> {
                    remarksTa.setText(remarks);
                    if (fromType == FromType.OTHER){
                        receiptNumL.setText(prescriptionNum);
                    }else {
                        receiptNumL.setText("");
                    }

                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    public static ObservableList<InvestigationModel> getInvestigation(int presMasterId) {
        ObservableList<InvestigationModel> investigationTempList = FXCollections.observableArrayList();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = """
                    SELECT tpi.investigation_id,tim.item_id,tim.items_name,
                     tpi.prescription_master_id,tpi.prescribed_date,tpi.result_date,tpi.result_value from tbl_prescription_investigation tpi
                     left join public.tbl_items_master tim on tim.item_id = tpi.item_id
                     where prescription_master_id = ?
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, presMasterId);
            rs = ps.executeQuery();

            while (rs.next()) {

                int investigationId = rs.getInt("investigation_id");
                int itemId = rs.getInt("item_id");
                String itemName = rs.getString("items_name");
                int prescMasterId = rs.getInt("prescription_master_id");
                Timestamp prescribedDate = rs.getTimestamp("prescribed_date");
                Timestamp resultDate = rs.getTimestamp("result_date");

                String resultValue = rs.getString("result_value");

                ItemChooserModel itemChooserModel = new ItemChooserModel(itemId, itemName);

                LocalDate prescribeLocalDate = prescribedDate == null ? null : prescribedDate.toLocalDateTime().toLocalDate();

                LocalDateTime resultLocalDateTime = resultDate == null ? null : resultDate.toLocalDateTime();

                InvestigationModel im = new InvestigationModel(investigationId, itemName, resultValue,
                        prescribeLocalDate, resultLocalDateTime, itemChooserModel, prescMasterId);
                investigationTempList.add(im);

            }



          return  investigationTempList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    public static ObservableList<PrescriptionMedicationModel> getMedication(int presMasterId) {

        ObservableList<PrescriptionMedicationModel> medicationTempList = FXCollections.observableArrayList();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            String qry = """
                    SELECT * from tbl_prescription_medications where prescription_master_id = ?
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, presMasterId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int medicationId = rs.getInt("medication_id");
                int itemId = rs.getInt("item_id");
                String itemName = rs.getString("item_name");
                int prescriptionMasterId = rs.getInt("prescription_master_id");
                boolean itemExistsInStock = rs.getBoolean("is_item_exists_in_stock");
                String composition = rs.getString("composition");
                String tag = rs.getString("tag");
                String remarks = rs.getString("remark");
                String quantity = rs.getString("quantity");
                String time = rs.getString("time");
                String dose = rs.getString("dose");
                String frequency = rs.getString("frequency");
                String duration = rs.getString("duration");

                PrescriptionMedicationModel pmm = new PrescriptionMedicationModel(medicationId, itemName, tag, quantity, frequency,
                        duration, time, composition, dose, remarks, itemId, itemExistsInStock, prescriptionMasterId);
                medicationTempList.add(pmm);
            }

            return  medicationTempList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void save(Map<String, Object> data) {
        String remarks = remarksTa.getText();
        boolean isFinal = (boolean) data.get("is_final");
        Button bn = (Button) data.get("button");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {
            connection = new DBConnection().getConnection();
            connection.setAutoCommit(false);

            if (prescriptionMasterId.get() > 0) {
                String qryMains = "UPDATE TBL_PRESCRIPTION_MASTER SET IS_FINAL= ? ,REMARKS = ?,update_by = ?, last_update_date = ? where prescription_master_id = ?";
                ps = connection.prepareStatement(qryMains);
                ps.setBoolean(1, isFinal);
                ps.setString(2, remarks);
                ps.setInt(3, Login.currentlyLogin_Id);
                ps.setTimestamp(4, Method.getCurrenSqlTimeStamp());
                ps.setInt(5, prescriptionMasterId.get());

            } else {
                String qryMains = "INSERT INTO TBL_PRESCRIPTION_MASTER(CONSULTATION_ID, PATIENT_ID, PRESCRIPTION_NUM,IS_FINAL,REMARKS,CREATED_BY) VALUES (?,?,?,?,?,?)";
                ps = connection.prepareStatement(qryMains, new String[]{"prescription_master_id"});
                ps.setInt(1, cm.getConsultation_id());
                ps.setInt(2, cm.getPatient_id());
                ps.setString(3, new GenerateBillNumber().getPrescriptionBillNumber());
                ps.setBoolean(4, isFinal);
                ps.setString(5, remarks);
                ps.setInt(6, Login.currentlyLogin_Id);
            }

            int count = ps.executeUpdate();
            if (count > 0 && prescriptionMasterId.get() < 1) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    prescriptionMasterId.setValue(rs.getInt(1));
                }
            }

            if (count > 0) {
                ps = null;
                rs = null;

                String prescriptionItemDeleteQry = "DELETE FROM TBL_PRESCRIPTION_MEDICATIONS where prescription_master_id = ?  ";
                ps = connection.prepareStatement(prescriptionItemDeleteQry);
                ps.setInt(1, prescriptionMasterId.get());
                ps.executeUpdate();

                ps = null;

                String qryItems = """
                        INSERT INTO TBL_PRESCRIPTION_MEDICATIONS(ITEM_NAME, ITEM_ID, PRESCRIPTION_MASTER_ID,
                                                                   IS_ITEM_EXISTS_IN_STOCK, COMPOSITION, TAG, REMARK, QUANTITY,
                                                        TIME, DOSE, FREQUENCY, DURATION,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);
                                                        
                                    """;
                ps = connection.prepareStatement(qryItems);

                for (PrescriptionMedicationModel pmm : medicineList) {
                    count = 0;
                    ps.setString(1, pmm.getMedicineName());
                    ps.setInt(2, pmm.getItemId());
                    ps.setInt(3, prescriptionMasterId.get());
                    ps.setBoolean(4, pmm.isItemExists());
                    ps.setString(5, pmm.getComposition());
                    ps.setString(6, pmm.getMedicineTag());
                    ps.setString(7, pmm.getRemark());
                    ps.setString(8, pmm.getQuantity());
                    ps.setString(9, pmm.getTimes());
                    ps.setString(10, pmm.getDose());
                    ps.setString(11, pmm.getFrequency());
                    ps.setString(12, pmm.getDuration());
                    ps.setInt(13, Login.currentlyLogin_Id);
                    count = ps.executeUpdate();
                }
                ps = null;

                String investigationItemDeleteQry = "DELETE FROM TBL_PRESCRIPTION_INVESTIGATION where prescription_master_id = ?  ";
                ps = connection.prepareStatement(investigationItemDeleteQry);
                ps.setInt(1, prescriptionMasterId.get());
                ps.executeUpdate();
                ps = null;

                if (count > 0) {

                    for (InvestigationModel tm : investigationTableView.getItems()) {

                        if (tm.getItemDetails() != null) {
                            count = 0;
                            String investigationQry = """
                                    INSERT INTO TBL_PRESCRIPTION_INVESTIGATION(ITEM_ID, PRESCRIPTION_MASTER_ID, PRESCRIBED_DATE,
                                                                               RESULT_DATE, RESULT_VALUE, CREATED_BY) VALUES (?,?,?,?,?,?)
                                    """;
                            ps = connection.prepareStatement(investigationQry);
                            ps.setInt(1, tm.getItemDetails().getItemId());
                            ps.setInt(2, prescriptionMasterId.get());

                            ps.setTimestamp(3, tm.getPrescribeDate() == null ? Method.getCurrenSqlTimeStamp() : Method.getCurrenSqlTimeStampFromLocalDate(tm.getPrescribeDate()));


                            ps.setTimestamp(4, tm.getResultDate() == null ? null : Method.getCurrenSqlTimeStampFromLocalDateTime(tm.getResultDate()));
                            ps.setString(5, tm.getResultValue());
                            ps.setInt(6, Login.currentlyLogin_Id);
                            count = ps.executeUpdate();
                        }
                    }
                }
                if (count > 0) {
                    connection.commit();
                    new CustomDialog().showAlertBox("Success", "Prescription Successfully Saved.");
                    callThread(Type.INIT, new HashMap<>());
                    getPrescriptionDetailsById(prescriptionMasterId.get(),LastPrescriptionType.BOTH,FromType.OTHER);
                }
            }
        } catch (SQLException | NullPointerException e) {
            DBConnection.rollBack(connection);
            new CustomDialog().showAlertBox("Failed", "Something went wrong. Please try again.");
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }
}
