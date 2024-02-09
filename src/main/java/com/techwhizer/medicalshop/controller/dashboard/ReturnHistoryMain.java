package com.techwhizer.medicalshop.controller.dashboard;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.ReturnMainModel;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReturnHistoryMain implements Initializable {
    private int rowsPerPage = 30;
    public TableView<ReturnMainModel> tableview;
    public TableColumn<ReturnMainModel,Integer>  colSrNo;
    public TableColumn<ReturnMainModel,String>  colPatientName;
    public TableColumn<ReturnMainModel,String>  colInvoiceNumber;
    public TableColumn<ReturnMainModel,String>  colReturnDate;
    public TableColumn<ReturnMainModel,String> colRefundAmount;
    public TableColumn<ReturnMainModel,String> colRemark;
    public TableColumn<ReturnMainModel,String>  colItems;
    public TextField searchTf;
    public Pagination pagination;
    private ObservableList<ReturnMainModel> returnList = FXCollections.observableArrayList();
    private FilteredList<ReturnMainModel> filteredData;
    private Method method;
    private CustomDialog customDialog;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();
        tableview.setFixedCellSize(28.0);
        callThread();

        Platform.runLater(()->{
            Stage stage = (Stage) tableview.getScene().getWindow();
            stage.setMaximized(true);
        });
    }

    private void callThread() {
        MyAsyncTask myAsyncTask=new MyAsyncTask();
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {
            if (null != tableview){
                tableview.setItems(FXCollections.observableArrayList());
            }
            assert tableview != null;
            tableview.setPlaceholder(method.getProgressBarRed(40,40));
        }

        @Override
        public Boolean doInBackground(String... params) {
            getReturnProduct();

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableview.setPlaceholder(new Label("Product not found"));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getReturnProduct() {
        if (null != returnList){
            returnList.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs= null;

        try {
            connection = new DBConnection().getConnection();
            String query = """
                  
                    select distinct regexp_replace(trim( concat(COALESCE(ts.name, ''), ' ',
                                                     COALESCE(tp.first_name, ''), ' ',
                                                     COALESCE(tp.middle_name, ''), ' ',
                                                     COALESCE(tp.last_name, '')) ),'  ',' ' ) as patientName , trm.invoice_number,trm.return_main_id,
                         to_char(trm.return_date,'dd-MM-yyyy') as returnDate,trm.refund_amount,coalesce(trm.remark,'-') as remark
                  from tbl_return_main trm
                           left join tbl_sale_main tsm on tsm.sale_main_id = trm.sale_main_id
                           left outer join tbl_patient tp on tp.patient_id = tsm.patient_id
                           left join tbl_salutation ts on ts.salutation_id = tp.salutation_id
                    """;
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()){
                int returnMainId = rs.getInt("return_main_id");
                String patientName = rs.getString("patientName");
                String invoiceNumber = rs.getString("invoice_number");
                String returnDate = rs.getString("returnDate");
                String remark = rs.getString("remark");
                double refundAmount = rs.getDouble("refund_amount");
                ReturnMainModel returnMainModel = new ReturnMainModel(returnMainId,patientName,returnDate,
                        invoiceNumber,refundAmount,null == remark ||remark.isEmpty()?"-":remark);
                returnList.add(returnMainModel);
            }

            if (!returnList.isEmpty()) {
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

        filteredData = new FilteredList<>(returnList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(products -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (products.getInvoiceNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (products.getPatientName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }else return String.valueOf(products.getReturnDate()).toLowerCase().contains(lowerCaseFilter);
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
                tableview.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colInvoiceNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        colRefundAmount.setCellValueFactory(new PropertyValueFactory<>("refundAmount"));
        colRemark.setCellValueFactory(new PropertyValueFactory<>("remark"));
        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, returnList.size());
        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<ReturnMainModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());
        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<ReturnMainModel, String>, TableCell<ReturnMainModel, String>>
                cellFactory = (TableColumn<ReturnMainModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Label viewItems = new Label("VIEW ITEMS");
                    viewItems.setStyle("-fx-background-color:#036d83 ;-fx-background-radius: 100;" +
                            ";-fx-padding: 2 10 2 10;-fx-text-fill: white;-fx-cursor: hand");
                    viewItems.setOnMouseClicked(event -> {

                        selectTable(getIndex());
                        int returnMainId = tableview.getSelectionModel().getSelectedItem().getReturnMainId();
                        Main.primaryStage.setUserData(returnMainId);
                        customDialog.showFxmlDialog2("dashboard/returnItems.fxml", "RETURN ITEMS");
                    });
                    HBox managebtn = new HBox(viewItems);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(viewItems, new Insets(2, 10, 2, 10));
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colItems.setCellFactory(cellFactory);
    }

   private void selectTable(int index){
        tableview.getSelectionModel().select(index);
   }
}
