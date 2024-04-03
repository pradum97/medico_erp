package com.techwhizer.medicalshop.controller.chooser;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.GstModel;
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.model.chooserModel.StockItemChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.ItemChooserType;
import com.techwhizer.medicalshop.util.TableViewConfig;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;



public class StockItemChooser implements Initializable {
    public TextField searchTf;
    public TableColumn<StockItemChooserModel, Integer> colSrNo;
    public TableColumn<StockItemChooserModel, String> colProductName;
    public TableColumn<StockItemChooserModel, String> colBatch;
    public TableColumn<StockItemChooserModel, String> colExpiryDate;
    public TableColumn<StockItemChooserModel, String> colAvlQty;
    public TableColumn<StockItemChooserModel, String> colAction;
    public TableView<StockItemChooserModel> tableView;
    public Pagination pagination;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    private ObservableList<StockItemChooserModel> itemList = FXCollections.observableArrayList();
    private FilteredList<StockItemChooserModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        tableView.setFixedCellSize(27);
        callThread();
//        var data = Main.primaryStage.getUserData();
//        if (data instanceof ObservableList){
//            itemList = ( ObservableList<StockItemChooserModel> ) data;
//            pagination.setVisible(true);
//            search_Item();
//        }else {
//            callThread();
//        }
    }

    private void callThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
            method.hideElement(tableView);
        }

        @Override
        public Boolean doInBackground(String... params) {
            getItems();
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setVisible(true);
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getItems() {

        if (null != itemList) {
            itemList.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();

            String qry = "select * from stock_v where quantity > 0 and is_expired = false";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {

                int itemId = rs.getInt("ITEM_ID");
                String itemName = rs.getString("ITEMS_NAME");
                String packing = rs.getString("PACKING");
                int gstId = rs.getInt("gst_id");
                int cGst = rs.getInt("cgst");
                int iGst = rs.getInt("igst");
                int sGst = rs.getInt("sgst");
                int hsn = rs.getInt("hsn_sac");
                int tabPerStrip = rs.getInt("STRIP_TAB");
                String gstName = rs.getString("gstName");
                String unit = rs.getString("unit");
                String composition = rs.getString("composition");
                String tag = rs.getString("tag");
                String medicineDose = rs.getString("dose");
                String avlQty = rs.getString("full_quantity");

                GstModel gm = new GstModel(gstId, hsn, sGst, cGst, iGst, gstName, null);
                int departmentId = rs.getInt("department_id");
                String departmentName = rs.getString("department_name");
                String batch = rs.getString("batch");
                String expiryDate = rs.getString("expiry_date");
                boolean isExpired = rs.getBoolean("is_expired");
                boolean isStockable = rs.getBoolean("is_stockable");
                int stockId = rs.getInt("stock_id");

                itemList.add(new StockItemChooserModel(itemId, itemName, packing, gm, unit, tabPerStrip,composition,tag,medicineDose,
                        avlQty,isStockable,departmentId,departmentName,batch,expiryDate,isExpired,stockId));
            }

            if (!itemList.isEmpty()) {
                pagination.setVisible(true);
                search_Item();
            }

            Platform.runLater(() -> tableView.setPlaceholder(new Label("Item not available")));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(itemList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(products -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (products.getItemName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }  else if (products.getProductTag().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return String.valueOf(products.getPacking()).toLowerCase().contains(lowerCaseFilter);
            });

            changeTableView(pagination.getCurrentPageIndex(), TableViewConfig.POPUP_ROW_PER_PAGE);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, TableViewConfig.POPUP_ROW_PER_PAGE);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    changeTableView(newValue1.intValue(), TableViewConfig.POPUP_ROW_PER_PAGE);
                });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / TableViewConfig.POPUP_ROW_PER_PAGE));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colAvlQty.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));


        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, itemList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<StockItemChooserModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<StockItemChooserModel, String>, TableCell<StockItemChooserModel, String>>
                cellFactory = (TableColumn<StockItemChooserModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button selectBn = new Button();

                    ImageView iv = new ImageView(new ImageLoader().load("img/icon/rightArrow_ic_white.png"));
                    iv.setFitHeight(13);
                    iv.setFitWidth(13);

                    selectBn.setGraphic(iv);
                    selectBn.setStyle("-fx-cursor: hand ; -fx-padding: 1 7 1 7; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");

                    selectBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        StockItemChooserModel icm = tableView.getSelectionModel().getSelectedItem();

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

}
