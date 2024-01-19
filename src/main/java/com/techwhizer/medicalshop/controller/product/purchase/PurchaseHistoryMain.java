package com.techwhizer.medicalshop.controller.product.purchase;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.controller.product.purchase.model.PurchaseHistoryMainModel;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.DealerModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class PurchaseHistoryMain implements Initializable {

    int rowsPerPage = 70;

    public ComboBox<DealerModel> comDealerList;
    public DatePicker dpFromDate;
    public DatePicker dpToDate;
    public Button filterBn;
    public TextField searchTf;
    public Button refresh_bn;

    public TableView<PurchaseHistoryMainModel> tableview;

    public TableColumn<PurchaseHistoryMainModel,Integer> colSrNum;
    public TableColumn<PurchaseHistoryMainModel,String> colDealerName;
    public TableColumn<PurchaseHistoryMainModel,String>  colDealerPhone;
    public TableColumn<PurchaseHistoryMainModel,String>  colDealerAddress;
    public TableColumn<PurchaseHistoryMainModel,String>  colInvoiceNum;
    public TableColumn<PurchaseHistoryMainModel,String>  colBillDate;
    public TableColumn<PurchaseHistoryMainModel,String>  colAction;

    public Pagination pagination;

    private Method method;
    private CustomDialog customDialog;

    private ObservableList<PurchaseHistoryMainModel> itemList = FXCollections.observableArrayList();


    private ObservableList<DealerModel> dealerList =
            FXCollections.observableArrayList();
    private FilteredList<PurchaseHistoryMainModel> filteredData;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        method = new Method();
        customDialog = new CustomDialog();
        tableview.setFixedCellSize(28.0);

        setDefaultValue();



    }

    public void filterBnClick(ActionEvent actionEvent) {

        int dealerId = comDealerList.getSelectionModel().isEmpty()?0:comDealerList.getSelectionModel().getSelectedItem().getDealerId();

        Map<String,Object> data = new HashMap<>();

        data.put("dealer_id",dealerId);
        data.put("from_date",dpFromDate.getEditor().getText());
        data.put("to_date",dpToDate.getEditor().getText());
        searchTf.setText("");
        startThread(data);
    }

    public void refreshBnClick(ActionEvent actionEvent) {

        setDefaultValue();

    }

    private void startThread(Map<String,Object> data) {
        new MyAsyncTask(data).execute();

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Map<String,Object> data;

        public MyAsyncTask(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public void onPreExecute() {
            tableview.setPlaceholder(method.getProgressBarRed(40,40));
        }

        @Override
        public Boolean doInBackground(String... params) {

            if ( null != data.get("type") && Objects.equals((String)
                    data.get("type"), "start")){
                getDealer();
            }

            getPurchaseMainItems(data);
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            tableview.setPlaceholder(new Label("Item Not Found"));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void setDefaultValue(){
        method.convertDateFormat(dpFromDate,dpToDate);
        LocalDate currentDate = LocalDate.now();
        LocalDate prevDate = currentDate.minusYears(1);
        dpFromDate.setValue(prevDate);
        dpToDate.setValue(currentDate);

        Map<String,Object> data = new HashMap<>();

        data.put("dealer_id",0);
        data.put("from_date",dpFromDate.getEditor().getText());
        data.put("to_date",dpToDate.getEditor().getText());
        data.put("type", "start");
        searchTf.setText("");
        comDealerList.getSelectionModel().selectFirst();
        startThread(data);
    }

    private void getPurchaseMainItems(Map<String,Object> data) {

        itemList.clear();
        itemList.removeAll();

        int dealerId =(Integer) data.get("dealer_id") ;
        String fromDate =(String) data.get("from_date") ;
        String toDate =(String) data.get("to_date");

        LocalDate currentDate = LocalDate.now();
        LocalDate prevDate = currentDate.minusYears(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


        if (fromDate.isEmpty()){
            fromDate = prevDate.format(formatter);
        }

        if (toDate.isEmpty()){
            toDate = currentDate.format(formatter);

        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();

            String whereQry = " where tpm.dealer_id = case when "+dealerId+" > 0 then "+dealerId+" else tpm.dealer_id end";

            String qry = """
                   
                    select tpm.purchase_main_id,
                                                td.dealer_id,
                                                td.dealer_name,
                                                td.dealer_phone,
                                                td.address,
                                                tpm.dealer_bill_num as invoice_number,
                                                tpm.bill_date
                                         from tbl_purchase_main tpm
                                                  left join tbl_dealer td on tpm.dealer_id = td.dealer_id
                                          """+whereQry+"""   
                                          and TO_DATE(tpm.bill_date, 'DD-MM-YYYY') between TO_DATE(?, 'DD-MM-YYYY') and  TO_DATE(?, 'DD-MM-YYYY') order by tpm.purchase_main_id desc
                                      
                    """;

            ps = connection.prepareStatement(qry);
            ps.setString(1,fromDate);
            ps.setString(2,toDate);

            rs = ps.executeQuery();

            while (rs.next()){
                int pmid = rs.getInt("purchase_main_id");
                int dealer_Id = rs.getInt("dealer_id");
                String dealerName = rs.getString("dealer_name");
                String dealerPhone = rs.getString("dealer_phone");
                String address = rs.getString("address");
                String invoiceNumber = rs.getString("invoice_number");
                String billDate = rs.getString("bill_date");


                itemList.add(new PurchaseHistoryMainModel(pmid,dealer_Id,dealerName,dealerPhone,address,invoiceNumber,billDate));
            }


            if (!itemList.isEmpty()) {
                pagination.setVisible(true);
                search_Item();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBConnection.closeConnection(connection,ps,rs);
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

                if (products.getDealerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (products.getDealerPhone().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (products.getDealerAddress().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return products.getInvoiceNumber().toLowerCase().contains(lowerCaseFilter);
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

        colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));
        colDealerName.setCellValueFactory(new PropertyValueFactory<>("dealerName"));
        colDealerAddress.setCellValueFactory(new PropertyValueFactory<>("dealerAddress"));
        colDealerPhone.setCellValueFactory(new PropertyValueFactory<>("dealerPhone"));
        colBillDate.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        colInvoiceNum.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));


        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, itemList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<PurchaseHistoryMainModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<PurchaseHistoryMainModel, String>, TableCell<PurchaseHistoryMainModel, String>>
                cellViewItems = (TableColumn<PurchaseHistoryMainModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    PurchaseHistoryMainModel phm = tableview.getItems().get(getIndex());

                    Hyperlink hl = new Hyperlink("View Items");
                    hl.setMinWidth(80);
                    hl.setStyle("-fx-font-weight: bold");

                    hl.setOnAction(actionEvent -> {

                        Map<String,Object> data = new HashMap<>();
                        data.put("dealer_name",phm.getDealerName());
                        data.put("dealer_address",phm.getDealerAddress());
                        data.put("purchase_main_id",phm.getPurchaseMainId());

                        System.out.println(phm.getDealerName());
                        System.out.println(phm.getDealerAddress());
                        System.out.println(phm.getPurchaseMainId());

                        Main.primaryStage.setUserData(data);
                        customDialog.showFxmlFullDialog("product/purchase/purchaseHistoryItems.fxml","");
                    });


                    HBox managebtn = new HBox(hl);
                    managebtn.setStyle("-fx-alignment:CENTER-LEFT");
                    HBox.setMargin(hl, new Insets(0, 0, 0, 5));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };
        colAction.setCellFactory(cellViewItems);
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
