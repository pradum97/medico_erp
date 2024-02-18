package com.techwhizer.medicalshop.controller.dashboard;

import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.GenerateInvoice;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.InvoiceModel;
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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class InvoiceReport implements Initializable {
    private final int rowsPerPage = 30;
    public DatePicker fromDateP;
    public DatePicker toDateP;
    public TextField searchTf;
    public ComboBox<String> searchTypeC;
    public TableView<InvoiceModel> tableView;
    public TableColumn<InvoiceModel, Integer> colSrNo;
    public TableColumn<InvoiceModel, String> colCusName;
    public TableColumn<InvoiceModel, String> colCusPhone;
    public TableColumn<InvoiceModel, String> colTotItems;
    public TableColumn<InvoiceModel, String> colBillType;
    public TableColumn<InvoiceModel, String> colDate;
    public TableColumn<InvoiceModel, String> colInvoice;
    public TableColumn<InvoiceModel, String> colAction;
    public Pagination pagination;
    public VBox contentContainer;
    public VBox progressBar;
    public HBox refresh_bn;
    private Method method;
    private DBConnection dbConnection;
    private ObservableList<InvoiceModel> invoiceList = FXCollections.observableArrayList();
    private FilteredList<InvoiceModel> filteredData ;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        method = new Method();
        dbConnection = new DBConnection();

        callThread(false);
        comboBoxConfig();
        convertDateFormat(fromDateP, toDateP);
        tableView.setFixedCellSize(28.0);

    }

    private void comboBoxConfig() {
        searchTypeC.setItems(FXCollections.observableArrayList("INV", "CUSTOM"));
        searchTypeC.getSelectionModel().selectFirst();

        searchTypeC.setButtonCell(new ListCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    Insets old = getPadding();
                    setPadding(new Insets(old.getTop(), 0, old.getBottom(), 0));
                }
            }
        });

        searchTypeC.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new ListCell<String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                            setAlignment(Pos.CENTER);
                            setPadding(new Insets(3, 3, 3, 0));
                        }
                    }
                };
            }
        });
        String searchType = searchTypeC.getSelectionModel().getSelectedItem();

        switch (searchType) {

            case "INV" -> {
                searchTf.setPromptText("Enter invoice number");
            }
            case "CUSTOM" -> {
                searchTf.setPromptText("Enter your value");
            }
        }

        searchTypeC.valueProperty().addListener((observableValue, s, sType) -> {

            switch (sType) {

                case "INV" -> searchTf.setPromptText("Enter invoice number");

                case "CUSTOM" -> searchTf.setPromptText("Enter your value");

            }
            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
            searchTf.setText("");


        });
    }

    private void callThread(boolean isDateFilter) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(isDateFilter,null);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private boolean isDateFilter;
        private Map<String, Object> map;
        private Label labelButton;

        public MyAsyncTask(boolean isDateFilter, Map<String, Object> map) {
            this.isDateFilter = isDateFilter;
            this.map = map;
            if (null != map){
                labelButton = (Label) map.get("button");
            }
        }

        @Override
        public void onPreExecute() {
            //Background Thread will start
            refresh_bn.setDisable(true);
            ProgressIndicator pi = new ProgressIndicator();
            pi.indeterminateProperty();
            pi.setPrefHeight(25);
            pi.setPrefWidth(25);
            pi.setStyle("-fx-progress-color: white;");
            if (null != invoiceList) {
                invoiceList.clear();
            }

            if (null != map) {
                labelButton.setGraphic(pi);
            }else {
                method.hideElement(contentContainer);
                progressBar.setVisible(true);
                if (null != tableView) {
                    tableView.setItems(null);
                    tableView.refresh();
                }
                assert tableView != null;
                tableView.setPlaceholder(method.getProgressBarRed(40, 40));
            }


        }

        @Override
        public Boolean doInBackground(String... params) {
            if (null != map){
                boolean isReportPrint = (boolean) map.get("isReportPrint");
                boolean isDownloadable =  (boolean) map.get("isDownloadable");
                int saleMainId = (int) map.get("saleMainId");
                String fullPath = (String) map.get("path");
                if (isDownloadable){
                    printReport(saleMainId, true, fullPath,labelButton);
                }else if(isReportPrint){
                    printReport(saleMainId, false, fullPath,labelButton);
                }
            }else {
                getSaleItems(isDateFilter);
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            refresh_bn.setDisable(false);
            contentContainer.setVisible(true);
            tableView.setPlaceholder(new Label("Product not found"));
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void printReport(int saleMainId, boolean isDownLoad, String downloadPath, Label labelButton){
        new GenerateInvoice().billingInvoice(saleMainId, false, downloadPath, labelButton);
    }

    private void getSaleItems(boolean isDateFilter) {
        invoiceList.clear();
        if (null != filteredData){
            filteredData.clear();
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            String query = """
                    select regexp_replace(trim( concat(COALESCE(ts.name, ''), ' ',
                    COALESCE(tp.first_name, ''), ' ',
                    COALESCE(tp.middle_name, ''), ' ',
                    COALESCE(tp.last_name, '')) ),'  ',' ' ) as name, coalesce(tp.phone, '-')as phone,  tsm.bill_type,
                           (TO_CHAR(tsm.sale_date, 'DD-MM-YYYY HH12:MI AM')) as sale_date,tsm.sale_main_id,tsm.invoice_number,
                           (select count(*) from tbl_sale_items tsi where tsm.sale_main_id = tsi.sale_main_id)as totalItem
                    from tbl_sale_main tsm 
                    left join tbl_patient tp on tsm.patient_id = tp.patient_id
                    left join tbl_salutation ts on ts.salutation_id = tp.salutation_id 

                    """;

            if (isDateFilter) {
                String q = query.concat(" where TO_CHAR(tsm.sale_date, 'DD-MM-YYYY') between ? and ? order by sale_main_id asc  ");
                ps = connection.prepareStatement(q);
                ps.setString(1, fromDateP.getEditor().getText().toString());
                ps.setString(2, toDateP.getEditor().getText().toString());

            } else {
                query = query.concat("  order by sale_main_id desc");
                ps = connection.prepareStatement(query);
            }


            rs = ps.executeQuery();
            while (rs.next()) {

                int saleMainId = rs.getInt("sale_main_id");
                String patientName = rs.getString("name");
                String phone = rs.getString("phone");
                String billType = rs.getString("bill_type");
                String saleDate = rs.getString("sale_date");
                String invoiceNum = rs.getString("invoice_number");

                int totalItem = rs.getInt("totalItem");

                invoiceList.add(new InvoiceModel(saleMainId, totalItem,
                        patientName, phone, billType, saleDate, invoiceNum));
            }
            if (invoiceList.size() > 0) {
                pagination.setVisible(true);
                search_Item();
            } else {
                changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            refresh_bn.setDisable(false);
            Platform.runLater(()->{
                tableView.setPlaceholder(new Label("Product not found"));
            });
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void search_Item() {

        filteredData = new FilteredList<>(invoiceList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(invoice -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = null;
                String searchType = searchTypeC.getSelectionModel().getSelectedItem();
                switch (searchType) {

                    case "INV" -> {
                        lowerCaseFilter = (searchTypeC.getSelectionModel().getSelectedItem() + newValue).toLowerCase();
                    }
                    case "CUSTOM" -> {
                        lowerCaseFilter = newValue.toLowerCase();
                    }
                }

                assert lowerCaseFilter != null;
                if (invoice.getInvoiceNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;

                } else if (invoice.getCustomerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;

                } else if (invoice.getInvoiceDate().toLowerCase().contains(lowerCaseFilter)) {
                    return true;

                } else return invoice.getCustomerPhone().toLowerCase().contains(lowerCaseFilter);
            });

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> changeTableView(newValue1.intValue(), rowsPerPage));

    }

    private void changeTableView(int index, int limit) {

        Platform.runLater(() -> {
            if (null == filteredData) {
                return;
            }

            int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
            pagination.setPageCount(totalPage);

            colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableView.getItems().indexOf(cellData.getValue()) + 1));
            colCusName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            colCusPhone.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
            colTotItems.setCellValueFactory(new PropertyValueFactory<>("totalItems"));
            colBillType.setCellValueFactory(new PropertyValueFactory<>("billType"));
            colInvoice.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

            colDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));

            setOptionalCell();

            int fromIndex = index * limit;
            int toIndex = Math.min(fromIndex + limit, invoiceList.size());

            int minIndex = Math.min(toIndex, filteredData.size());
            SortedList<InvoiceModel> sortedData = new SortedList<>(
                    FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
            sortedData.comparatorProperty().bind(tableView.comparatorProperty());

            tableView.setItems(sortedData);
        });

    }

    private void setOptionalCell() {

        Callback<TableColumn<InvoiceModel, String>, TableCell<InvoiceModel, String>>
                cellFactory = (TableColumn<InvoiceModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Label bnDownload = new Label("DOWNLOAD");
                    Label bnPrint = new Label("PRINT");

                    bnDownload.setMinWidth(55);
                    bnPrint.setMinWidth(55);
                    bnPrint.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    bnDownload.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                    bnDownload.setStyle("-fx-background-color: #030c3d; -fx-background-radius: 3 ;  " +
                            "-fx-padding: 2 2 2 2 ;-fx-font-family: Arial; -fx-text-fill: white;-fx-font-weight: bold; -fx-alignment: center;-fx-cursor: hand");

                    bnPrint.setStyle("-fx-background-color: #670283; -fx-background-radius: 3 ;-fx-font-weight: bold ;-fx-font-family: Arial; " +
                            "-fx-padding: 2 5 2 5 ; -fx-text-fill: white; -fx-alignment: center;-fx-cursor: hand");
                    ImageView down_iv = new ImageView();
                    ImageView print_iv = new ImageView();

                    String path = "img/icon/";

                    down_iv.setFitHeight(12);
                    down_iv.setFitWidth(12);

                    print_iv.setFitHeight(12);
                    print_iv.setFitWidth(12);

                    ImageLoader loader = new ImageLoader();

                    down_iv.setImage(loader.load(path.concat("download_ic.png")));
                    print_iv.setImage(loader.load(path.concat("print_ic.png")));

                    bnDownload.setGraphic(down_iv);
                    bnPrint.setGraphic(print_iv);

                    bnDownload.setOnMouseClicked(mouseEvent -> {

                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        File selectedPath = directoryChooser.showDialog(Main.primaryStage);

                        if (selectedPath != null) {

                            int saleMainId = tableView.getItems().get(getIndex()).getSale_main_id();
                            String billType = tableView.getItems().get(getIndex()).getBillType();
                            String invoiceNmber =  tableView.getItems().get(getIndex()).getInvoiceNumber();

                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            LocalDateTime now = LocalDateTime.now();

                            String fileName = invoiceNmber + "_" + dtf.format(now) + ".pdf";

                            String fullPath = selectedPath + "\\" + fileName;

                            Map<String, Object> map = new HashMap<>();
                            map.put("saleMainId",saleMainId);
                            map.put("path",fullPath);
                            map.put("isDownloadable",true);
                            map.put("isReportPrint",false);

                            map.put("button",bnDownload);


                            switch (billType) {

                                case "REGULAR" -> {

                                    map.put("billType","REGULAR");
                                    MyAsyncTask myAsyncTask = new MyAsyncTask(false,map);
                                    myAsyncTask.setDaemon(false);
                                    myAsyncTask.execute();
                                }

                                case "GST" -> {
                                    map.put("billType","GST");
                                    MyAsyncTask myAsyncTask = new MyAsyncTask(false,map);
                                    myAsyncTask.setDaemon(false);
                                    myAsyncTask.execute();
                                    }

                            }

                        }
                    });

                    bnPrint.setOnMouseClicked(mouseEvent -> {
                        int saleMainId = tableView.getItems().get(getIndex()).getSale_main_id();
                        String billType = tableView.getItems().get(getIndex()).getBillType();


                        String tempPath = method.getTempFile();
                        Map<String, Object> map = new HashMap<>();
                        map.put("saleMainId",saleMainId);
                        map.put("path",tempPath);
                        map.put("isDownloadable",false);
                        map.put("isReportPrint",true);
                        map.put("button",bnPrint);

                        switch (billType) {

                            case "REGULAR" -> {

                                map.put("billType","REGULAR");
                                MyAsyncTask myAsyncTask = new MyAsyncTask(false,map);
                                myAsyncTask.setDaemon(false);
                                myAsyncTask.execute();
                            }

                            case "GST" -> {
                                map.put("billType","GST");
                                MyAsyncTask myAsyncTask = new MyAsyncTask(false,map);
                                myAsyncTask.setDaemon(false);
                                myAsyncTask.execute();
                            }

                        }
                    });

                    HBox managebtn = new HBox(bnDownload, bnPrint);

                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(bnDownload, new Insets(0, 0, 0, 30));
                    HBox.setMargin(bnPrint, new Insets(0, 3, 0, 20));

                    setGraphic(managebtn);
                    setText(null);
                }
            }

        };

        colAction.setCellFactory(cellFactory);
        customColumn(colCusName);
        customColumn(colDate);
    }

    private void customColumn(TableColumn<InvoiceModel, String> columnName) {

        columnName.setCellFactory(tc -> {
            TableCell<InvoiceModel, String> cell = new TableCell<>();
            Text text = new Text();
            text.setStyle("-fx-font-size: 12");
            cell.setGraphic(text);
            text.setStyle("-fx-text-alignment: LEFT ;");
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(columnName.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    public void searchInvoice(ActionEvent event) {

        if ( fromDateP.getEditor().getText().isEmpty()) {
            method.show_popup_bottom("SELECT START DATE", fromDateP);
            return;
        } else if (toDateP.getEditor().getText().isEmpty()) {
            method.show_popup_bottom("SELECT END DATE", toDateP);
            return;
        }
        callThread(true);

    }

    private void convertDateFormat(DatePicker... date) {

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

    public void bnRefresh(MouseEvent mouseEvent) {
        getSaleItems(false);
    }
}
