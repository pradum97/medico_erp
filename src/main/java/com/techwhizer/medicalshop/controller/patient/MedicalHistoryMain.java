package com.techwhizer.medicalshop.controller.patient;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.method.PrintPrescription;
import com.techwhizer.medicalshop.model.MedicalHistoryMainModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MedicalHistoryMain implements Initializable {
    public TableView<MedicalHistoryMainModel> tableview;
    public TableColumn<MedicalHistoryMainModel, Integer> colSr;
    public TableColumn<MedicalHistoryMainModel, String> colConsultDate;
    public TableColumn<MedicalHistoryMainModel, String> colTotalItems;
    public TableColumn<MedicalHistoryMainModel, String> colAction;

    private final ObservableList<MedicalHistoryMainModel> historyMainModelsList = FXCollections.observableArrayList();
    private int patientId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (Main.primaryStage.getUserData() != null &&
                Main.primaryStage.getUserData() instanceof Integer) {

            patientId = (int) Main.primaryStage.getUserData();

            if (patientId > 0) {


                getHistory();
            }

        }
    }

    private void getHistory() {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = """

                 
                    select TO_CHAR(consultation_date,'MM/DD/YYYY hh12:mi AM')as consultation_date,consultation_date, (select count(*) from patient_medical_history_item hi
                    where hi.medical_main_id = pmh.medical_main_id) as totalItems,medical_main_id
                    from patient_medical_history_main pmh where pmh.patient_id =?
                    group by medical_main_id,TO_CHAR(consultation_date,'dd/MM/yyyy') order by pmh.consultation_date desc 
                    fetch first 10 rows only
                    """;
            ps = connection.prepareStatement(qry);
            ps.setInt(1, patientId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int mainId = rs.getInt("MEDICAL_MAIN_ID");
                String consultDate = rs.getString("consultation_date");
                int totalItems = rs.getInt("totalItems");

                historyMainModelsList.add(new MedicalHistoryMainModel(mainId, consultDate, totalItems));

            }

            tableview.setItems(historyMainModelsList);
            colSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableview.getItems().indexOf(cellData.getValue()) + 1));
            colConsultDate.setCellValueFactory(new PropertyValueFactory<>("consultDate"));
            colTotalItems.setCellValueFactory(new PropertyValueFactory<>("totalItems"));
            setOptionCell();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }
    }

  private  void  setOptionCell(){

        Callback<TableColumn<MedicalHistoryMainModel, String>, TableCell<MedicalHistoryMainModel, String>>
                cellAction = (TableColumn<MedicalHistoryMainModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Hyperlink printBn = new Hyperlink("Print");
                    Hyperlink viewHistory = new Hyperlink("View Items");

                    printBn.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;-fx-border-color: transparent");
                    viewHistory.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;-fx-border-color: transparent");

                    printBn.setOnAction(new EventHandler<>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            tableview.getSelectionModel().select(getIndex());
                            MedicalHistoryMainModel pm = tableview.getSelectionModel().getSelectedItem();
                           // new PrintPrescription().print(patientId,false,printBn, pm.getHistoryMainId());
                        }
                    });

                    viewHistory.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            tableview.getSelectionModel().select(getIndex());
                            MedicalHistoryMainModel pm = tableview.getSelectionModel().getSelectedItem();
                            Main.primaryStage.setUserData(pm.getHistoryMainId());
                            new CustomDialog().showFxmlFullDialog("patient/medicalHistoryItems.fxml", "");

                        }
                    });


                    HBox managebtn = new HBox(printBn,viewHistory );
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(printBn, new Insets(0, 0, 0, 5));
                    HBox.setMargin(viewHistory, new Insets(0, 0, 0, 5));
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colAction.setCellFactory(cellAction);
    }


    public void closeButton(ActionEvent actionEvent) {

        new Method().closeStage(tableview);
    }
}
