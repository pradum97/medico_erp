package com.techwhizer.medicalshop.controller.chooser;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.Constant;
import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.dashboard.StockReport;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.GstModel;
import com.techwhizer.medicalshop.model.chooserModel.ItemChooserModel;
import com.techwhizer.medicalshop.util.DBConnection;
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
import javafx.scene.input.MouseEvent;
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

public class CommonItemChooser implements Initializable {
    public ProgressIndicator progressBar;
    public TextField searchTf;
    public TableColumn<ItemChooserModel, Integer> colSrNo;
    public TableColumn<ItemChooserModel, String> colProductName;
    public TableColumn<ItemChooserModel, String> colAvlQty;
    public TableColumn<ItemChooserModel, String> colAction;
    public TableView<ItemChooserModel> tableView;
    public Pagination pagination;
    private Method method;
    private CustomDialog customDialog;
    private DBConnection dbConnection;
    private ObservableList<ItemChooserModel> itemList = FXCollections.observableArrayList();
    private FilteredList<ItemChooserModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        dbConnection = new DBConnection();
        tableView.setFixedCellSize(27);
        var data =( Map<String,Object>) Main.primaryStage.getUserData();

        if (null != data && data instanceof Map<String,Object>){
            callThread(data);
        }

    }

    private void callThread(Map<String, Object> data) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(data);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String msg;
        Map<String, Object> data;
        public MyAsyncTask(Map<String, Object> data) {
            this.data = data;
        }
        @Override
        public void onPreExecute() {
            method.hideElement(tableView);
            msg = "";
        }

        @Override
        public Boolean doInBackground(String... params) {
            msg = getItems(data);
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setVisible(true);
            method.hideElement(progressBar);
            tableView.setPlaceholder(new Label(msg));
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private String getItems(Map<String, Object> data) {

        if (null != itemList) {
            itemList.clear();
        }

        boolean isStockable = (Boolean) data.get("is_stockable");

       String msg ;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();

            String qry = """
                    select * from available_quantity_v where is_stockable = ?
                    """;
            ps = connection.prepareStatement(qry);
            ps.setBoolean(1,isStockable);
            rs = ps.executeQuery();

            int count = 0;

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
                int status = rs.getInt("status");
                String gstName = rs.getString("gstName");
                String type = rs.getString("type");
                String unit = rs.getString("unit");
                String composition = rs.getString("composition");
                String tag = rs.getString("tag");
                String medicineDose = rs.getString("dose");
                String avlQty = rs.getString("avl_qty_strip");
                count++;

                GstModel gm = new GstModel(gstId, hsn, sGst, cGst, iGst, gstName, null);
                int departmentId = rs.getInt("department_id");
                String departmentName = rs.getString("department_name");

                if (status == 1){
                    if (Constant.ITEM_TYPE_PROHIBIT.equalsIgnoreCase(type)) {
                        if (Login.currentRoleName.equalsIgnoreCase("admin")) {
                            itemList.add(new ItemChooserModel(itemId, itemName, packing, gm, unit, tabPerStrip,composition,tag,
                                    medicineDose,avlQty,isStockable,departmentId,departmentName));
                        }
                    } else {
                        itemList.add(new ItemChooserModel(itemId, itemName, packing, gm, unit, tabPerStrip,composition,tag,medicineDose,
                                avlQty,isStockable,departmentId,departmentName));
                    }
                }else {
                    msg = "All items are disabled.";
                }
            }

            if (!itemList.isEmpty()) {
                pagination.setVisible(true);
                search_Item();
            }

            if (count > 0) {

                msg = "";
            } else {

                msg = "Item not available";
            }


        } catch (SQLException e) {
            msg = "Something went wrong ";
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
        return msg;
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

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, itemList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<ItemChooserModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<ItemChooserModel, String>, TableCell<ItemChooserModel, String>>
                cellQty = (TableColumn<ItemChooserModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    String qtyStr = tableView.getItems().get(getIndex()).getAvailableQuantity();
                    Label qtyLabel = new Label(qtyStr);
                    HBox managebtn = new HBox(qtyLabel);
                    managebtn.setStyle("-fx-alignment:CENTER-LEFT");
                    HBox.setMargin(qtyLabel, new Insets(0, 0, 0, 5));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };

        Callback<TableColumn<ItemChooserModel, String>, TableCell<ItemChooserModel, String>>
                cellFactory = (TableColumn<ItemChooserModel, String> param) -> new TableCell<>() {
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
                        ItemChooserModel icm = tableView.getSelectionModel().getSelectedItem();

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
colAvlQty.setCellFactory(cellQty);
        colAction.setCellFactory(cellFactory);
    }

    public void addProductBnClick(MouseEvent actionEvent) {

        customDialog.showFxmlDialog2("product/addProduct.fxml", "Add new product");
        searchTf.setText("");
        //callThread();

    }
}
