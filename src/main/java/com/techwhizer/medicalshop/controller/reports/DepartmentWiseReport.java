package com.techwhizer.medicalshop.controller.reports;

import com.techwhizer.medicalshop.controller.common.model.DepartmentModel;
import com.techwhizer.medicalshop.controller.reports.model.DepartmentWiseReportModel;
import com.techwhizer.medicalshop.util.CommonUtil;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.techwhizer.medicalshop.controller.reports.BillingReport.convertDateFormat;

public class DepartmentWiseReport implements Initializable {

    int rowsPerPage = 100;
    public ComboBox<DepartmentModel> departmentCom;
    public DatePicker fromDateP;
    public DatePicker toDateP;
    public Button searchReportBn;
    public TextField searchTf;
    public Label totalNetAmountL;
    public Pagination pagination;
    public TableView<DepartmentWiseReportModel> tableView;
    public TableColumn<DepartmentWiseReportModel, Integer> colSrNum;
    public TableColumn<DepartmentWiseReportModel, String> colPatientName;
    public TableColumn<DepartmentWiseReportModel, String> colPatientPhoneNum;
    public TableColumn<DepartmentWiseReportModel, String> colPatientAddress;
    public TableColumn<DepartmentWiseReportModel, String> colItemName;
    public TableColumn<DepartmentWiseReportModel, String> colDepartment;
    public TableColumn<DepartmentWiseReportModel, String> colMrp;
    public TableColumn<DepartmentWiseReportModel, String> colNetAmt;
    public TableColumn<DepartmentWiseReportModel, String> colBillingDate;

    private double netAmount;
    LocalDate currentDate = LocalDate.now();
    LocalDate prevDate_15 = currentDate.minusDays(15);

    private ObservableList<DepartmentWiseReportModel> itemList = FXCollections.observableArrayList();
    private FilteredList<DepartmentWiseReportModel> filteredData;

    enum TaskType {
        SEARCH, INIT
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        fromDateP.setValue(prevDate_15);
        toDateP.setValue(currentDate);
        convertDateFormat(fromDateP, toDateP);


        callThread(new HashMap<>(), TaskType.INIT);
        callThread(new HashMap<>(), TaskType.SEARCH);

    }

    public void refreshBnClick(ActionEvent actionEvent) {
        fromDateP.setValue(prevDate_15);
        toDateP.setValue(currentDate);
        setDefaultDepartment();
        callThread(new HashMap<>(), TaskType.SEARCH);
    }

    private void setDefaultDepartment(){

        ObservableList<DepartmentModel> dm= departmentCom.getItems();

        for (int i = 0; i < dm.size(); i++) {
            if (dm.get(i).getDepartmentCode().equals("Consultation")) {
                int finalI = i;
                Platform.runLater(() -> departmentCom.getSelectionModel().select(finalI));
            }
        }
    }
    public void searchReportBn(ActionEvent actionEvent) {
        callThread(new HashMap<>(), TaskType.SEARCH);
    }

    private void callThread(Map<String, Object> data, TaskType taskType) {

        new MyAsyncTask(data, taskType).execute();

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        Map<String, Object> data;
        TaskType taskType;

        public MyAsyncTask(Map<String, Object> data, TaskType taskType) {
            this.data = data;
            this.taskType = taskType;
        }

        @Override
        public void onPreExecute() {
            switch (taskType) {
                case INIT -> {
                }
                case SEARCH -> {
                }
            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (taskType) {
                case INIT -> {

                    ObservableList<DepartmentModel> dm = CommonUtil.getDepartmentsList();
                    departmentCom.setItems(dm);
                    setDefaultDepartment();
                }
                case SEARCH -> getItem(data);
            }
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {

        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getItem(Map<String, Object> data) {
        itemList.clear();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();
            String qry = """
                    select pv.fullname as patient_name,pv.phone,pv.address,tim.items_name,tdep.department_name,tsi.mrp,
                           tsi.net_amount,TO_CHAR(tsm.sale_date,'DD-MM-YYYY HH:mm PM') as sale_date
                    from tbl_sale_items tsi
                    left join tbl_sale_main tsm on tsm.sale_main_id = tsi.sale_main_id
                    left join tbl_items_master tim on tsi.item_id = tim.item_id
                    left join patient_v pv on pv.patient_id = tsm.patient_id
                    left join tbl_departments tdep on tdep.department_code = tim.department_code
                    where tim.is_stockable = false and tim.department_code not in('Medicine')
                     and to_char(tsm.sale_date,'YYYY-MM-DD') between ? and ? and tim.department_code = ? order by tsm.sale_main_id desc 
                    """;

            ps = connection.prepareStatement(qry);
            ps.setString(1, String.valueOf(fromDateP.getValue()));
            ps.setString(2, String.valueOf(toDateP.getValue()));
            ps.setString(3, departmentCom.getSelectionModel().isEmpty() ? "Consultation" : departmentCom.getSelectionModel().getSelectedItem().getDepartmentCode());

            rs = ps.executeQuery();

            while (rs.next()) {

                String patientName = rs.getString("patient_name");
                String patientPhone = rs.getString("phone");
                String patientAddress = rs.getString("address");
                String itemName = rs.getString("items_name");
                String departmentName = rs.getString("department_name");
                String saleDate = rs.getString("sale_date");

                double mrp = rs.getDouble("mrp");
                double netAmt = rs.getDouble("net_amount");

                netAmount += netAmt;


                DepartmentWiseReportModel reportModel = new DepartmentWiseReportModel(patientName, patientPhone, patientAddress, itemName, departmentName, saleDate, mrp, netAmt);
                itemList.add(reportModel);

            }
            Platform.runLater(() -> totalNetAmountL.setText(String.valueOf(netAmount)));
            if (null != itemList) {
                if (!itemList.isEmpty()) {
                    pagination.setVisible(true);
                }

            }
            search_Item();

        } catch (SQLException e) {


        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(itemList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(patient -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (patient.getItemName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (patient.getPatientName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (patient.getPatientPhoneNum().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else {
                    return patient.getPatientAddress().toLowerCase().contains(lowerCaseFilter);
                }

            });

            changeTableViewPatient(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableViewPatient(0, rowsPerPage);
        Platform.runLater(() -> {
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        changeTableViewPatient(newValue1.intValue(), rowsPerPage);
                    });
        });

    }

    private void changeTableViewPatient(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));
        colSrNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPatientPhoneNum.setCellValueFactory(new PropertyValueFactory<>("patientPhoneNum"));
        colPatientAddress.setCellValueFactory(new PropertyValueFactory<>("patientAddress"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        colNetAmt.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        colBillingDate.setCellValueFactory(new PropertyValueFactory<>("billingDate"));

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, itemList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<DepartmentWiseReportModel> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

}
