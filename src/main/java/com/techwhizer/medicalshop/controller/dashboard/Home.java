package com.techwhizer.medicalshop.controller.dashboard;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.Main;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.DailySaleReport;
import com.techwhizer.medicalshop.report.ReportConfig;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Home implements Initializable {
    public Button downloadReportBn;
    int rowsPerPage = 30;
    public TableColumn<DailySaleReport, String> colTotalItem;
    public TableColumn<DailySaleReport, String> colNetAmount;
    public Label totalNetAmountL;
    public TableColumn<DailySaleReport, String> colItemName;
    public TableColumn<DailySaleReport, String> colQuantity;
    public TableColumn<DailySaleReport, String> colBatch;
    public TableColumn<DailySaleReport, String> colExpiryDate;
    public BorderPane mainContainer;
    public TableView<DailySaleReport> tableViewHome;
    public TableColumn<DailySaleReport, Integer> col_sno;
    public Button refresh_bn;
    public Pagination pagination;
    private DBConnection dbConnection;
    private DecimalFormat df = new DecimalFormat("0.##");
    private Method method;
    private ObservableList<DailySaleReport> reportList = FXCollections.observableArrayList();
    private String downloadPath = "";


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DBConnection();
        method = new Method();
        tableViewHome.setFixedCellSize(28.0);
        callThread("GET_ITEM");
    }

    private void callThread(String type) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(type);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void bnRefresh(MouseEvent event) {
        if (null == reportList) {
            return;
        }


        callThread("GET_ITEM");

        changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(reportList.size() * 1.0 / rowsPerPage));
        pagination.setPageCount(totalPage);
        col_sno.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableViewHome.getItems().indexOf(cellData.getValue()) + 1));
        colTotalItem.setCellValueFactory(new PropertyValueFactory<>("totalItems"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colNetAmount.setCellValueFactory(new PropertyValueFactory<>("totalNetAmount"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("fullQuantity"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, reportList.size());

        int minIndex = Math.min(toIndex, reportList.size());
        SortedList<DailySaleReport> sortedData = new SortedList<>(
                FXCollections.observableArrayList(reportList.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableViewHome.comparatorProperty());

        tableViewHome.setItems(sortedData);

    }

    public void downloadReportBnClick(ActionEvent actionEvent) {

        if (reportList.size() > 0){

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedPath = directoryChooser.showDialog(Main.primaryStage);

            if (selectedPath != null) {
                String path = selectedPath.getAbsolutePath();
                downloadPath = path;
                callThread("DOWNLOAD_REPORT");
            }else {

                //new CustomDialog().showAlertBox("Failed","Download Path Not Found..");
            }


        }else {

            new CustomDialog().showAlertBox("Enpty","Item Not Availablle");
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        String type;

        public MyAsyncTask(String type) {
            this.type = type;
        }

        @Override
        public void onPreExecute() {

            switch (type){

                case "GET_ITEM"->{
                    refresh_bn.setDisable(true);
                    if (null != tableViewHome){
                        tableViewHome.setItems(null);
                    }
                    assert tableViewHome != null;
                    tableViewHome.setPlaceholder(method.getProgressBarRed(40,40));
                }
                case "DOWNLOAD_REPORT"->{
                    downloadReportBn.setGraphic(method.getProgressBarWhite(15,15));
                }
            }


        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (type){

                case "GET_ITEM"->{
                   getSaleItem();
                }
                case "DOWNLOAD_REPORT"->{
                    downloadReport();
                }
            }


            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {

            switch (type){

                case "GET_ITEM"->{
                    refresh_bn.setDisable(false);
                    tableViewHome.setPlaceholder(new Label("Item not available"));
                }
                case "DOWNLOAD_REPORT"->{
                    downloadReportBn.setGraphic(new ImageLoader().getDownloadIcon(15,15));
                }
            }

        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }


    private void downloadReport() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        String currentDate = currentDateTime.format(formatter);

        String path  = downloadPath+"\\REPORT_ON_"+currentDateTime.format(DateTimeFormatter.ofPattern("dd_MM_yyyy_hh_mm_a"))+".pdf";

        Document document = ReportConfig.GetDocument(path);
        try {

            Paragraph title = new Paragraph("REPORT ON ["+currentDate+"]");
            title.setAlignment(Paragraph.ALIGN_CENTER);
            Font titleFont = new Font();
            titleFont.setStyle(Font.BOLD);
            titleFont.setColor(BaseColor.RED);
            title.setFont(titleFont);
            document.add(title);

            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(6);

            table.setWidthPercentage(new float[]{40,215, 90, 70,80,80}, PageSize.A4);
            // Set border color to gray
            table.getDefaultCell().setBorderColor(BaseColor.GRAY);
            table.getDefaultCell().setBorderWidth(0.7f);

            // Add headers
            table.addCell(ReportConfig.getColumnCell("#", PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Item Name",PdfPCell.LEFT));
            table.addCell(ReportConfig.getColumnCell("Batch",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Quantity",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Total Items",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Amount",PdfPCell.ALIGN_RIGHT));
            int count = 0;
            for(DailySaleReport ds : reportList){
                table.addCell(ReportConfig.getRowCell(String.valueOf(++count),PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell(ds.getProductName(),PdfPCell.LEFT));
                table.addCell(ReportConfig.getRowCell(ds.getBatch(),PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell(ds.getFullQuantity(),PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell(String.valueOf(ds.getTotalItems()),PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell(String.valueOf(ds.getTotalNetAmount()),PdfPCell.ALIGN_RIGHT));
            }

            // Add the table to the document
            document.add(table);

            PdfPTable footerTable = new PdfPTable(2);

            footerTable.setWidthPercentage(new float[]{345,230}, PageSize.A4);
            footerTable.getDefaultCell().setBorderColor(BaseColor.GRAY);
            footerTable.getDefaultCell().setBorderWidth(0.7f);

            footerTable.addCell(ReportConfig.getColumnCell("TOTAL NET AMOUNT : ",PdfPCell.ALIGN_RIGHT));
            footerTable.addCell(ReportConfig.getColumnCell(totalNetAmountL.getText(),PdfPCell.ALIGN_RIGHT));


            document.add(footerTable);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }finally {
            document.close();
        }


    }


    private void getSaleItem() {

        reportList.clear();
        if (null!=tableViewHome){
            tableViewHome.setItems(null);
            tableViewHome.refresh();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Thread.sleep(100);
            connection = dbConnection.getConnection();

            String query = """
                 select  tsi.item_name ,tsi.stock_id,tsi.batch,
                         count(*)as total_Item  ,tsi.expiry_date,
                         sum(net_amount) as total_Net_Amount,
                         ( sum(tsi.strip*(select strip_tab from tbl_items_master tim where tsi.item_id = tim.item_id )))+ ( sum(tsi.pcs))as totalTab,
                         (select quantity_unit from tbl_stock ts where tsi.stock_id = ts.stock_id) as qtyUnit,
                         tsi.strip_tab as stripTab
                 from tbl_sale_Items tsi
                 where TO_CHAR(sale_date, 'yyyy-MM-dd' ) =TO_CHAR(CURRENT_DATE, 'yyyy-MM-dd') 
                   group by stock_id,tsi.batch, tsi.item_name, tsi.strip_tab, tsi.expiry_date
                    """;

            ps = connection.prepareStatement(query);

            rs = ps.executeQuery();

            double totalNetAmount = 0;

            int res = 0;

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                int stockId = rs.getInt("stock_id");
                int totalItem = rs.getInt("total_Item");
                double totalNet_Amount = rs.getDouble("total_Net_Amount");
                int totalTab = rs.getInt("totalTab");
                String qtyUnit = rs.getString("qtyUnit");
                String batch  = rs.getString("batch");
                String expiryDate = rs.getString("expiry_date");
                int stripTab = rs.getInt("stripTab");
                String qty = method.tabToStrip(totalTab,stripTab,qtyUnit);;

                if (stripTab > 0 && qtyUnit != null && qtyUnit.equalsIgnoreCase("tab")) {
                    itemName = itemName.concat(" ( STRIP-"+stripTab+" )");
                }

                reportList.add(new DailySaleReport(0 ,totalItem, itemName, Method.removeDecimal(totalNet_Amount),totalTab,qtyUnit,qty,stripTab,batch,stockId,expiryDate));
                totalNetAmount = totalNetAmount + totalNet_Amount;
                res++;
            }
            double finalTotalNetAmount = totalNetAmount;
            Platform.runLater(()-> totalNetAmountL.setText(String.valueOf(Double.parseDouble(df.format(finalTotalNetAmount)))));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            refresh_bn.setDisable(false);
            DBConnection.closeConnection(connection, ps, rs);
        }

        if (reportList.size() > 0) {
            pagination.setVisible(true);
            pagination.setCurrentPageIndex(0);
            Platform.runLater(()->changeTableView(0, rowsPerPage));
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> changeTableView(newValue1.intValue(), rowsPerPage));
        }
    }
}
