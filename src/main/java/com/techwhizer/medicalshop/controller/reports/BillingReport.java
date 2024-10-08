package com.techwhizer.medicalshop.controller.reports;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.FileLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.SaleMainModel;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BillingReport implements Initializable {
    public HBox refresh_bn;
    int rowsPerPage = 60;
    public TableColumn<SaleMainModel, Integer> col_sno;
    public TableColumn<SaleMainModel, String> colCheck;
    public TableColumn<SaleMainModel, String> colPatientName;
    public TableColumn<SaleMainModel, String> colPhone;
    public TableColumn<SaleMainModel, String> colAddress;
    public TableColumn<SaleMainModel, String> colNetAmount;
    public TableColumn<SaleMainModel, String> colReceivedAmount;
    public TableColumn<SaleMainModel, String> colSellerName;
    public TableColumn<SaleMainModel, String> colBillType;
    public TableColumn<SaleMainModel, String> colDate;
    public TableColumn<SaleMainModel, String> colAddiDisc;
    public TableColumn<SaleMainModel, String> colTotTax;
    public TableColumn<SaleMainModel, String> colPaymentMode;

    public TextField searchTf;
    public TableView<SaleMainModel> tableView;
    public TableColumn<SaleMainModel, String> colInvoiceNumber;
    public Pagination pagination;
    private Method method;
    private CustomDialog customDialog;
    public DatePicker fromDateP;
    public DatePicker toDateP;
    public Button searchReportBn;
    public Label totalNetAmountL;
    private ObservableList<SaleMainModel> reportList = FXCollections.observableArrayList();

    private FilteredList<SaleMainModel> filteredData;
    LocalDate currentDate;
    LocalDate prevDate_15;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        customDialog = new CustomDialog();

        currentDate = LocalDate.now();
        prevDate_15 = currentDate.minusDays(15);
        initAllBillingReport();
    }

    private void initAllBillingReport(){

        tableView.setFixedCellSize(28.0);
        fromDateP.setValue(prevDate_15);
        toDateP.setValue(currentDate);
        convertDateFormat(fromDateP, toDateP);
        callThread(true);
    }

    private void callThread(boolean isDateFilter) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(isDateFilter);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String msg = "";
        private boolean isDateFilter;

        public MyAsyncTask(boolean isDateFilter) {
            this.isDateFilter = isDateFilter;
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

            getSaleItems(isDateFilter);
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            refresh_bn.setDisable(false);
            tableView.setPlaceholder(new Label("Item not available."));
            if (reportList.size() > 0) {
                pagination.setVisible(true);
                search_Item();
            } else {
                changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
            }

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getSaleItems(boolean isDateFilter) {
        reportList.clear();
        if (null != tableView) {
            tableView.setItems(null);
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Thread.sleep(100);
            connection = new DBConnection().getConnection();

            String query = "select * from billing_report_v";

            if (isDateFilter) {
                String q = query.concat(" where to_char(sale_date,'YYYY-MM-DD') between ? and ? order by sale_main_id desc  ");
                ps = connection.prepareStatement(q);
                ps.setString(1, String.valueOf(fromDateP.getValue()));
                ps.setString(2, String.valueOf(toDateP.getValue()));

            } else {
                query = query.concat("  order by sale_main_id desc");
                ps = connection.prepareStatement(query);
            }

            System.out.println(ps);

            rs = ps.executeQuery();
            double totalNetAmount = 0;
            while (rs.next()) {

                int saleMainId = rs.getInt("sale_main_id");
                int patientId = rs.getInt("patient_id");
                int sellerId = rs.getInt("seller_id");
                double additionalDisc = rs.getDouble("additional_discount");
                double totTaxAmt = rs.getDouble("tot_tax_amount");
                double netAmount = rs.getDouble("net_amount");
                String paymentMode = rs.getString("payment_mode");
                String invoiceNumber = rs.getString("invoice_number");
                String billType = rs.getString("bill_type");
                String sale_date = rs.getString("saleDate");
                String patientName = rs.getString("name");
                String patientPhone = rs.getString("phone");
                String patientAddress = rs.getString("address");
                String sellerName = rs.getString("first_name") + " " + rs.getString("last_name");
                double totNetAmount = rs.getDouble("netAmount");
                double receivedAmount = rs.getDouble("received_amount");

                reportList.add(new SaleMainModel(saleMainId, patientId, sellerId, patientName, patientPhone, patientAddress, additionalDisc,
                        Double.parseDouble(method.decimalFormatter(totTaxAmt)), Double.parseDouble(method.decimalFormatter(netAmount)),
                        paymentMode, billType, invoiceNumber, sellerName, sale_date,receivedAmount));

                totalNetAmount = totalNetAmount + totNetAmount;
            }

            double finalTotalNetAmount = totalNetAmount;
            Platform.runLater(() -> totalNetAmountL.setText(String.valueOf(Double.parseDouble(method.decimalFormatter(finalTotalNetAmount)))));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            refresh_bn.setDisable(false);
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void customColumn(TableColumn<SaleMainModel, String> columnName) {

        columnName.setCellFactory(tc -> {
            TableCell<SaleMainModel, String> cell = new TableCell<>();
            Text text = new Text();
            text.setStyle("-fx-font-size: 12");
            cell.setGraphic(text);
            text.setStyle("-fx-padding: 5");
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(columnName.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    public void bnRefresh(MouseEvent event) {

        callThread(false);
        changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        setOptional();

        if (null != fromDateP) {
            fromDateP.setValue(null);
        }

        if (null != toDateP) {
            toDateP.setValue(null);
        }
        searchTf.setText("");
    }

    private void search_Item() {
        filteredData = new FilteredList<>(reportList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(saleMain -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (saleMain.getPatientName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;

                } else if (String.valueOf(saleMain.getPatientPhone()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return saleMain.getInvoiceNumber().toLowerCase().contains(lowerCaseFilter);
            });
            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> changeTableView(newValue1.intValue(), rowsPerPage));
    }

    private void changeTableView(int index, int limit) {

        if (null == filteredData) {
            return;
        }
        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        col_sno.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("patientPhone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("patientAddress"));
        colAddiDisc.setCellValueFactory(new PropertyValueFactory<>("additionalDisc"));
        colTotTax.setCellValueFactory(new PropertyValueFactory<>("totalTaxAmount"));
        colNetAmount.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        colReceivedAmount.setCellValueFactory(new PropertyValueFactory<>("receivedAmount"));
        colInvoiceNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colPaymentMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
        colBillType.setCellValueFactory(new PropertyValueFactory<>("billType"));
        colSellerName.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("sellingDate"));

        setOptional();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, reportList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<SaleMainModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setOptional() {
        Callback<TableColumn<SaleMainModel, String>, TableCell<SaleMainModel, String>>
                checkItems = (TableColumn<SaleMainModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    Label bnChecItem = new Label("ITEMS");

                    bnChecItem.getStyleClass().add("checkItem");
                    bnChecItem.setMinWidth(80);


                    bnChecItem.setOnMouseClicked(event -> {
                        SaleMainModel saleMain = tableView.getSelectionModel().getSelectedItem();
                        if (null == tableView) {
                            return;
                        }

                        Main.primaryStage.setUserData(saleMain.getSale_main_id());
                        customDialog.showFxmlFullDialog("dashboard/viewBillingItems.fxml", "All Items - " + saleMain.getPatientName() + " / " + saleMain.getSellingDate() + " / Invoice- " +
                                saleMain.getInvoiceNumber());
                    });

                    HBox container = new HBox(bnChecItem);
                    setGraphic(container);

                    setText(null);
                }
            }

        };


        colCheck.setCellFactory(checkItems);
        customColumn(colPatientName);
        customColumn(colAddress);
        customColumn(colDate);
    }

    public static void convertDateFormat(DatePicker... date) {

        for (DatePicker datePicker : date) {

            datePicker.setConverter(new StringConverter<>() {

                private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                @Override
                public String toString(LocalDate localDate) {
                    if (localDate == null)
                        return "";
                    return dateTimeFormatter.format(localDate);
                }

                @Override
                public LocalDate fromString(String dateString) {
                    if (dateString == null || dateString.trim().isEmpty()) {
                        return null;
                    }
                    return LocalDate.parse(dateString, dateTimeFormatter);
                }
            });
        }


    }

    public void searchReportBn(ActionEvent event) {

        if (null == fromDateP.getValue()) {
            method.show_popup("SELECT START DATE", fromDateP, Side.RIGHT);
            return;
        } else if (null == toDateP.getValue()) {
            method.show_popup("SELECT END DATE", toDateP, Side.RIGHT);
            return;
        }


        callThread(true);

    }
}
