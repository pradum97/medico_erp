package com.techwhizer.medicalshop.controller.prescription;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.MedicineTimeModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class MedicineTime implements Initializable {

    public TextField timeTf;
    public TableView<MedicineTimeModel> tableView;
    public TableColumn<MedicineTimeModel,Integer> colSr;
    public TableColumn<MedicineTimeModel,String> colTime;
    private Method method;

    private ObservableList<MedicineTimeModel> timeList = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        getTimes();
    }

    public void addClick(ActionEvent actionEvent) {

        String time = timeTf.getText();

        if (time.isEmpty()){
            method.show_popup("Please enter medicine time",timeTf, Side.RIGHT);
            return;
        }


        Connection connection = null;
        PreparedStatement ps = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = "INSERT INTO tbl_medicine_time(time, CREATED_BY) VALUES (?,?)";
            ps = connection.prepareStatement(qry);
            ps.setString(1,time);
            ps.setInt(2, Login.currentlyLogin_Id);

            int res = ps.executeUpdate();

            if (res> 0){
                getTimes();
                timeTf.setText("");
            }

        }catch (Exception e){

        }finally {
            DBConnection.closeConnection(connection,ps,null);
        }

    }


    public void getTimes() {

        if(null != timeList){
            timeList.clear();
        }


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = "select * from tbl_medicine_time";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();
            while (rs.next()){

                String time = rs.getString("time");
                MedicineTimeModel fm = new MedicineTimeModel(time);
                timeList.add(fm);
            }

            tableView.setItems(timeList);

            colSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableView.getItems().indexOf(cellData.getValue()) + 1));
            colTime.setCellValueFactory(new PropertyValueFactory<>("time"));


        }catch (Exception e){

        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }


    }

    public void closeButton(ActionEvent actionEvent) {
        new Method().closeStage(tableView);
    }
}
