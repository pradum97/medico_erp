package com.techwhizer.medicalshop.controller.doctor;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.DoctorModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ViewDoctor implements Initializable {

    private int rowsPerPage = 12;
    public TextField searchTf;
    public Pagination pagination;
    public TableColumn<DoctorModel, Integer> colSrNo;
    public TableColumn<DoctorModel, String> colName;
    public TableColumn<DoctorModel, String> colReg;
    public TableView<DoctorModel> tableView;
    public TableColumn<DoctorModel, String> colPhone;
    public TableColumn<DoctorModel, String> colSpec;
    public TableColumn<DoctorModel, String> colQly;
    public TableColumn<DoctorModel, String> colAddress;
    public TableColumn<DoctorModel, String> colDate;
    public TableColumn<DoctorModel, String> colDocType;
    public TableColumn<DoctorModel, String> colAction;
    private Method method;
    private FilteredList<DoctorModel> filteredData;
    private ObservableList<DoctorModel> doctorList = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        tableView.setFixedCellSize(28.0);
        callThread();
    }

    public void addDr(ActionEvent event) {
        new CustomDialog().showFxmlDialog2("doctor/add_doctor.fxml", "Add doctor");
        callThread();
    }

    private void callThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String msg;

        @Override
        public void onPreExecute() {
            msg = "";

            if (null != tableView) {
                tableView.setItems(null);
            }
            assert tableView != null;
            tableView.setPlaceholder(method.getProgressBarRed(40, 40));

        }

        @Override
        public Boolean doInBackground(String... params) {
            getDoctor();
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setPlaceholder(new Label("Doctor not available"));
            if (null != doctorList) {
                if (doctorList.size() > 0) {
                    pagination.setVisible(true);
                    search_Item();
                }
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getDoctor() {
        if (null != doctorList){
            doctorList.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            connection = new DBConnection().getConnection();

            String query = """
                    SELECT doctor_id,dr_name,coalesce(dr_phone,'-')as dr_phone,coalesce(dr_address,'-') as dr_address,
                    coalesce(speciality,'-') as speciality ,doctor_type,
                    coalesce(dr_reg_num,'-') as dr_reg_num,coalesce(qualification,'-') as qualification 
                    ,(TO_CHAR(created_date, 'DD-MM-YYYY')) as created_date FROM tbl_doctor order by doctor_id desc
                    """;
            ps = connection.prepareStatement(query);

            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("DOCTOR_ID");
                String name = "Dr. "+rs.getString("DR_NAME");
                String phone = rs.getString("DR_PHONE");
                String address = rs.getString("DR_ADDRESS");
                String spec = rs.getString("SPECIALITY");
                String reg = rs.getString("DR_REG_NUM");
                String qly = rs.getString("QUALIFICATION");
                String date = rs.getString("CREATED_DATE");
                String docType = rs.getString("doctor_type");
                doctorList.add(new DoctorModel(id, name, phone,
                        address, reg,spec, qly, date, docType));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(doctorList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(patient -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(patient.getDrName()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(patient.getDrPhone()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return patient.getDrRegNo().toLowerCase().contains(lowerCaseFilter);
            });

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        Platform.runLater(() -> {
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        changeTableView(newValue1.intValue(), rowsPerPage);
                    });
        });

    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colName.setCellValueFactory(new PropertyValueFactory<>("drName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("drPhone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("drAddress"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("drSpeciality"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colQly.setCellValueFactory(new PropertyValueFactory<>("qualification"));
        colReg.setCellValueFactory(new PropertyValueFactory<>("drRegNo"));
        colDocType.setCellValueFactory(new PropertyValueFactory<>("doctorType"));

        setOptionalCell();

        tableView.setItems(doctorList);
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, doctorList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<DoctorModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

  private void   setOptionalCell(){

      Callback<TableColumn<DoctorModel, String>, TableCell<DoctorModel, String>>
              cellEdit = (TableColumn<DoctorModel, String> param) -> new TableCell<>() {
          @Override
          public void updateItem(String item, boolean empty) {
              super.updateItem(item, empty);
              if (empty) {
                  setGraphic(null);
                  setText(null);

              } else {

                  Label bnUpdate = new Label();
                  bnUpdate.setMinWidth(40);
                  bnUpdate.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                  bnUpdate.setStyle("-fx-background-color:  #006666; -fx-background-radius: 3 ;-fx-font-weight: bold ;-fx-font-family: Arial; " +
                          "-fx-padding: 2 5 2 5 ; -fx-text-fill: white; -fx-alignment: center;-fx-cursor: hand");

                  ImageView add_iv = new ImageView();

                  String path = "img/icon/";

                  add_iv.setFitHeight(12);
                  add_iv.setFitWidth(12);

                  ImageLoader loader = new ImageLoader();
                  add_iv.setImage(loader.load(path.concat("update_ic.png")));
                  bnUpdate.setGraphic(add_iv);

                  bnUpdate.setOnMouseClicked(mouseEvent -> {
                      DoctorModel dm = tableView.getSelectionModel().getSelectedItem();
                      Main.primaryStage.setUserData(dm);
                      new CustomDialog().showFxmlDialog2("doctor/add_doctor.fxml", "Update Doctor Details");
                      Main.primaryStage.setUserData(null);
                      callThread();
                  });

                  HBox managebtn = new HBox(bnUpdate);
                  managebtn.setStyle("-fx-alignment:center");
                  HBox.setMargin(bnUpdate, new Insets(0, 0, 0, 15));

                  setGraphic(managebtn);
                  setText(null);
              }
          }

      };

      colAction.setCellFactory(cellEdit);

    }
}
