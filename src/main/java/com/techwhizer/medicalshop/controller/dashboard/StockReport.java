package com.techwhizer.medicalshop.controller.dashboard;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.DealerModel;
import com.techwhizer.medicalshop.model.StockModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.techwhizer.medicalshop.util.ExcelExporter;
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
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StockReport implements Initializable {

    public Label createdDate;
    public Label statusL;
    public Button refresh_bn;
    public ComboBox<String> filterCom;
    public Button excelExportBn;
    public ComboBox<DealerModel> comDealerList;
    int rowsPerPage = 60;
   public static int lowQuantity = 15, expiryLeftDays = 60;

    public TextField searchTf;
    public TableView<StockModel> tableView;
    public TableColumn<StockModel, Integer> colSrNo;
    public TableColumn<StockModel, String> colProductName;
    public TableColumn<StockModel, String> colDealerName;
    public TableColumn<StockModel, String> colPack;
    public TableColumn<StockModel, String> colDealerAddress;
    public TableColumn<StockModel, String> colBatch;
    public TableColumn<StockModel, String> colExpiryDate;
    public TableColumn<StockModel, String> colPurchaseRate;
    public TableColumn<StockModel, String> colMrp;
    public TableColumn<StockModel, String> colSale;
    public TableColumn<StockModel, String> colQty;
    public Pagination pagination;

    private Method method;
    private CustomDialog customDialog;
    private ObservableList<StockModel> itemList = FXCollections.observableArrayList();
    private ObservableList<String> filterList =
            FXCollections.observableArrayList("All", "Out Of Stock", "Expired Medicine",
                    "Low Quantity", "Expiring Soon");

    private ObservableList<DealerModel> dealerList =
            FXCollections.observableArrayList();
    private FilteredList<StockModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
//        colAction.setVisible(false);
        tableView.setFixedCellSize(28.0);

        filterCom.setItems(filterList);
        filterCom.getSelectionModel().select(0);

        Map<String, Object> data = new HashMap<>();
        data.put("filter_type", "All");
        data.put("type", "start");
        data.put("dealer_id", 0);
        callThread(data);
    }

    private void callThread(Map<String, Object> data) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(data);
        myAsyncTask.execute();
    }

    public void refresh(ActionEvent event) {
        searchTf.setText("");
        Map<String, Object> data = new HashMap<>();
        data.put("filter_type", "All");
        data.put("type", "start");
        data.put("dealer_id", 0);
        callThread(data);
    }

    public void filterBn(ActionEvent actionEvent) {

        String filterType = filterCom.getSelectionModel().getSelectedItem();
        int dealerId =comDealerList.getSelectionModel().isEmpty()?0: comDealerList.getSelectionModel().getSelectedItem().getDealerId();
        Map<String, Object> data = new HashMap<>();
        data.put("filter_type", filterType);
        data.put("type", "filter");
        data.put("dealer_id", dealerId);
        callThread(data);

        colDealerAddress.setVisible(dealerId < 1);
        colDealerName.setVisible(dealerId < 1);
    }

    public <T> void excelExportBn(ActionEvent actionEvent) {


        if (!tableView.getItems().isEmpty()) {

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(Main.primaryStage);

            if (selectedDirectory != null) {
                String filterType = filterCom.getSelectionModel().getSelectedItem();
                String pattern = "dd_MM_yyyy hh_mm_ss";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(new Date());

                String path = selectedDirectory.getAbsolutePath() + "/StockReport " + date +"_"+filterType+".xlsx";

                List<T> hideColumnList = new ArrayList<>();
                hideColumnList.add((T) colSrNo);
                hideColumnList.add((T) colMrp);

                new ExcelExporter().exportToExcel(tableView, filterType, path, excelExportBn, hideColumnList);
            }
        } else {
            customDialog.showAlertBox("", "Item Not Available!");
        }


    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private Map<String, Object> data;

        public MyAsyncTask(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public void onPreExecute() {
            refresh_bn.setDisable(true);
            if (null != tableView) {
                tableView.setItems(null);
                tableView.refresh();
            }
            assert tableView != null;
            tableView.setPlaceholder(method.getProgressBarRed(40, 40));
        }

        @Override
        public Boolean doInBackground(String... params) {

            if ( null != data.get("type") && Objects.equals((String)
                    data.get("type"), "start")){
                getDealer();
            }

            getStock(data);
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            refresh_bn.setDisable(false);
            tableView.setPlaceholder(new Label("Not Available"));
            if (!itemList.isEmpty()) {
                pagination.setVisible(true);
                search_Item();
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getStock(Map<String, Object> data) {
        itemList.clear();
        String filterType = "All";

        if (data != null) {

            filterType = (String) data.get("filter_type");
        }

        int dealerId = (int) data.get("dealer_id");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String order_by = """
                                        
                     order by  expiry_date, quantity asc 
                                        
                    """;

            String qry = """
                    select * from stock_v
                    """;

            String whereCondition = "";

            if (Objects.equals(filterType, "Expired Medicine")) {
                whereCondition = """
                          where TO_DATE(concat(EXTRACT(DAY FROM (date_trunc('MONTH', concat(split_part(expiry_date, '/', 2), '-',
                                                                                          split_part(expiry_date, '/', 1), '-', '01')::date) +
                                                               INTERVAL '1 MONTH' - INTERVAL '1 day')),'/',expiry_date),'dd/MM/yyyy') <= now() 
                        """ ;
            } else if (Objects.equals(filterType, "Low Quantity")) {
                whereCondition = """
                          where cast(split_part(full_quantity,'-',1) as int) < ?
                        """;
            } else if (Objects.equals(filterType, "Expiring Soon")) {
                whereCondition = """
                          where expiry_days_left < ?
                        """;
            } else {

                whereCondition = """
                          where quantity = case when ? = 'Out Of Stock' then 0 else quantity end
                                           
                        """ ;

            }

            String finalQry = qry + " " + whereCondition+" and dealer_id = case when "+dealerId+" > 0 then "+dealerId+" else dealer_id  end"+ order_by;

            System.out.println(finalQry);

            ps = connection.prepareStatement(finalQry);

            if (Objects.equals(filterType, "Out Of Stock") || Objects.equals(filterType, "All")) {
                ps.setString(1, filterType);
            } else if (Objects.equals(filterType, "Low Quantity")) {
                ps.setInt(1, lowQuantity);
            } else if (Objects.equals(filterType, "Expiring Soon")) {
                ps.setInt(1, expiryLeftDays);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                int stockId = rs.getInt("stock_id");
                String productName = rs.getString("ITEMS_NAME");
                String packing = rs.getString("PACKING");
                int quantity = rs.getInt("quantity");
                String quantityUnit = rs.getString("quantity_unit");
                String batch = rs.getString("batch");
                String expiry = rs.getString("expiry_date");
                double purRate =rs.getDouble("purchase_rate");
                double mrp =rs.getDouble("mrp");
                double saleRate =rs.getDouble("sale_price");

                String qty = rs.getString("full_quantity");
                String composition = rs.getString("composition");
                String dose = rs.getString("dose");
                String fullExpiryDate = rs.getString("full_expiry_date");
                String dealerName = rs.getString("dealer_name");
                String dealerAddress = rs.getString("dealer_address");
                long expiry_days_left = rs.getLong("expiry_days_left");

                StockModel im = new StockModel(stockId,productName,packing,quantity,
                        quantityUnit, batch, expiry, purRate, mrp, saleRate, qty,
                        composition, dose, fullExpiryDate, expiry_days_left,dealerName,dealerAddress);
                itemList.add(im);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            refresh_bn.setDisable(false);
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
                } else if (products.getExpiry().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (products.getPacking().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return String.valueOf(products.getBatch()).toLowerCase().contains(lowerCaseFilter);
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
        colProductName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colPack.setCellValueFactory(new PropertyValueFactory<>("packing"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));
        colPurchaseRate.setCellValueFactory(new PropertyValueFactory<>("purchaseRate"));
        colMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        colSale.setCellValueFactory(new PropertyValueFactory<>("saleRate"));
        colDealerName.setCellValueFactory(new PropertyValueFactory<>("dealerName"));
        colDealerAddress.setCellValueFactory(new PropertyValueFactory<>("dealerAddress"));

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, itemList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<StockModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<StockModel, String>, TableCell<StockModel, String>>
                cellQty = (TableColumn<StockModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    String qtyStr = tableView.getItems().get(getIndex()).getFullQty();
                    int qty = qtyStr == null ? 0 : Integer.parseInt(qtyStr.split("-")[0]);

                    Label qtyLabel = new Label(qtyStr);

                    if (qty > 0 && qty < lowQuantity) {
                        qtyLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-background-color: #ff9933;" +
                                "-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px");
                    } else if (qty == 0) {
                        qtyLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-background-color: red;" +
                                "-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px");
                    } else {
                        qtyLabel.setStyle("-fx-text-fill: black;-fx-font-weight:bold;-fx-background-color: inherit");
                    }

                    HBox managebtn = new HBox(qtyLabel);
                    managebtn.setStyle("-fx-alignment:CENTER-LEFT");
                    HBox.setMargin(qtyLabel, new Insets(0, 0, 0, 5));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };

        Callback<TableColumn<StockModel, String>, TableCell<StockModel, String>>
                cellExpiryDate = (TableColumn<StockModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    String expiryDate = tableView.getItems().get(getIndex()).getExpiry();
                    long expiryDaysLeft = tableView.getItems().get(getIndex()).getExpiry_days_left();
                    Label label = getExpiryDateLabel(expiryDate, expiryDaysLeft);
                    HBox manage = new HBox(label);
                    manage.setStyle("-fx-alignment:CENTER-LEFT");
                    HBox.setMargin(label, new Insets(0, 0, 0, 5));
                    setGraphic(manage);
                    setText(null);

                }
            }

        };

        colExpiryDate.setCellFactory(cellExpiryDate);
        colQty.setCellFactory(cellQty);
    }

    private Label getExpiryDateLabel(String expiryDate, long expiryDaysLeft) {
        Label label = new Label(expiryDate);

        String style = "", tooltipText = expiryDaysLeft + " Days Left.";
        if (expiryDaysLeft < 1) {
            tooltipText = "Medicine Has Expired.";
            style = "-fx-cursor: hand;-fx-text-fill: white;-fx-font-weight: bold;-fx-background-color: red;-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px";
        } else if (expiryDaysLeft < expiryLeftDays) {
            style = "-fx-cursor: hand;-fx-text-fill: white;-fx-font-weight: bold;-fx-background-color: #ff9933;-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px";
        } else {
            style = "-fx-cursor: hand;-fx-text-fill: black;-fx-font-weight: bold;-fx-background-color: inherit;-fx-padding: 0px 3px 0px 3px;-fx-background-radius: 3px";
        }

        label.setTooltip(new Tooltip(tooltipText));
        label.setStyle(style);
        return label;
    }

    private void getDealer() {

          Platform.runLater(()->{  dealerList.clear();});


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            connection = new DBConnection().getConnection();

            String query = """
                    select 0 as dealer_id, 'All' as dealer_name
                    union
                    SELECT dealer_id, dealer_name FROM tbl_dealer order by dealer_name asc
                    """;
            ps = connection.prepareStatement(query);

            rs = ps.executeQuery();

            while (rs.next()) {

                int dealerId = rs.getInt("dealer_id");
                String dealerName = rs.getString("dealer_name");

                dealerList.add(new DealerModel(dealerId, dealerName));
            }

            comDealerList.setItems(dealerList);

           Platform.runLater(() -> comDealerList.getSelectionModel().selectFirst());


        } catch (SQLException ignored) {

        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

}
