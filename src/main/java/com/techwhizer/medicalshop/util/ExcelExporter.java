package com.techwhizer.medicalshop.util;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.StockModel;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {


    public final <T> void exportToExcel(TableView<T> tableView, String sheetName,
                                        String filePath, Button exportButton,
                                        Object hideColumnList) {

        MyAsyncTask task = new MyAsyncTask(tableView, sheetName, filePath, exportButton,hideColumnList );
        task.execute();

    }

    public class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Object tableView;
        String sheetName;
        String filePath;

        Button exportButton;
        Object hideColumnList;

        public<T> MyAsyncTask(TableView<T> tableView, String sheetName,
                              String filePath, Button exportButton,  Object hideColumns) {
            this.tableView = tableView;
            this.sheetName = sheetName;
            this.filePath = filePath;
            this.exportButton = exportButton;
            this.hideColumnList = hideColumns;
        }


        @Override
        public void onPreExecute() {

            ProgressIndicator pi = new Method().getProgressBarWhite(20, 20);
            if (null != exportButton) {
               exportButton.setGraphic(pi);
                exportButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }

        @Override
        public Boolean doInBackground(String... params) {
            startExport(tableView, sheetName, filePath,hideColumnList);
            new CustomDialog().showAlertBox("Success","Report Successfully Exported.");
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            if (null != exportButton) {
                exportButton.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    public final <T> void startExport1(Object tableView2, String sheetName,
                                      String filePath, Object hideColumns) {

        TableView<T> tableView = ((TableView<T>) tableView2);

        List<T> hideColumnList = (List<T>)hideColumns;

        for(T tc: hideColumnList){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    tableView.getColumns().remove(tc);
                }
            });
        }

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);

            ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();

            // Create header row
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.size(); i++) {
                headerRow.createCell(i).setCellValue(columns.get(i).getText());
            }

            // Fill data
            ObservableList<T> items = tableView.getItems();

            for (int rowIdx = 0; rowIdx < items.size(); rowIdx++) {

                Row row = sheet.createRow(rowIdx + 1);
                T item = items.get(rowIdx);

                for (int colIdx = 0; colIdx < columns.size(); colIdx++) {

                    Object cellValue = columns.get(colIdx).getCellData(item);

                    if (cellValue != null) {
                        row.createCell(colIdx).setCellValue(cellValue.toString());
                    }
                }
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public final <T> void startExport(Object tableView2, String sheetName,
                                      String filePath, Object hideColumns) {

        TableView<T> tableView = ((TableView<T>) tableView2);
        ObservableList<StockModel> items =( ObservableList<StockModel>)tableView.getItems();


        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);

            headerRow.createCell(0).setCellValue("SR#");
            headerRow.createCell(1).setCellValue("ITEM NAME");
            headerRow.createCell(2).setCellValue("BATCH");
            headerRow.createCell(3).setCellValue("EXPIRY DATE");
            headerRow.createCell(4).setCellValue("QUANTITY");
            headerRow.createCell(5).setCellValue("DOSE");
            headerRow.createCell(6).setCellValue("COMPOSITION");

            for (int rowIdx = 0; rowIdx < items.size(); rowIdx++) {

                Row row = sheet.createRow(rowIdx + 1);
                StockModel item = items.get(rowIdx);

                row.createCell(0).setCellValue(rowIdx+1);
                row.createCell(1).setCellValue(item.getItemName());
                row.createCell(2).setCellValue(item.getBatch());
                row.createCell(3).setCellValue(item.getExpiry());
                row.createCell(4).setCellValue(item.getFullQty());
                row.createCell(5).setCellValue(item.getDose());
                row.createCell(6).setCellValue(item.getComposition());
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
