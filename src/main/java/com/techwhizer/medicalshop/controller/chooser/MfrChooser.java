package com.techwhizer.medicalshop.controller.chooser;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.ManufacturerModal;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MfrChooser implements Initializable {
    private int rowsPerPage = 100;
    public TextField searchTf;
    public TableColumn<ManufacturerModal, Integer> colSrNo;
    public TableColumn<ManufacturerModal, String> colName;
    public TableColumn<ManufacturerModal, String> colAction;
    public TableView<ManufacturerModal> tableView;
    public Pagination pagination;
    private Method method;
    private CustomDialog customDialog;
    private ObservableList<ManufacturerModal> list = FXCollections.observableArrayList();
    private FilteredList<ManufacturerModal> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        tableView.setFixedCellSize(27);

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
            //Background Thread will start
           tableView.setPlaceholder(method.getProgressBarRed(30,30));
            msg = "";
        }

        @Override
        public Boolean doInBackground(String... params) {

            getItems();

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setPlaceholder(new Label("Something went wrong."));
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getItems() {
        list.clear();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "SELECT * FROM tbl_manufacturer_list order by mfr_id desc";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("mfr_id");
                String manufacturer_name = rs.getString("manufacturer_name");
                String created_date = rs.getString("created_date");
                list.add(new ManufacturerModal(id,manufacturer_name,created_date));
            }

            if (!list.isEmpty()) {
                pagination.setVisible(true);
                search_Item();
            }

        } catch (SQLException e) {
           tableView.setPlaceholder(new Label("Something went wrong"));
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void search_Item() {

        filteredData = new FilteredList<>(list, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(products -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (products.getManufacturer_name().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }else return String.valueOf(products.getCreatedDate()).toLowerCase().contains(lowerCaseFilter);
            });

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colName.setCellValueFactory(new PropertyValueFactory<>("manufacturer_name"));

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, list.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<ManufacturerModal> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<ManufacturerModal, String>, TableCell<ManufacturerModal, String>>
                cellFactory = (TableColumn<ManufacturerModal, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button selectBn = new Button();

                    ImageView iv = new ImageView(new ImageLoader().load("img/icon/rightArrow_ic_white.png"));
                    iv.setFitHeight(12);
                    iv.setFitWidth(12);

                    selectBn.setGraphic(iv);
                    selectBn.setStyle("-fx-cursor: hand ;-fx-padding: 1 7 1 7; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");

                    selectBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        ManufacturerModal icm = tableView.getSelectionModel().getSelectedItem();

                        if (null != icm) {
                            Main.primaryStage.setUserData(icm);
                            Stage stage = (Stage) searchTf.getScene().getWindow();
                            if (null != stage && stage.isShowing()) {
                                stage.close();
                            }
                        }
                    });

                    HBox managebtn = new HBox(selectBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(selectBn, new Insets(0, 0, 0, 0));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellFactory);
    }

    public void add(MouseEvent actionEvent) {
        customDialog.showFxmlDialog("product/manufactureMain.fxml", "Add new manufacture");
        searchTf.setText("");
        callThread();

    }
}
