package com.techwhizer.medicalshop.controller.prescription;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.model.FrequencyModel;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class Frequency implements Initializable
{
    public TextField frequencyTf;
    public TableView<FrequencyModel> tableView;
    public TableColumn<FrequencyModel,Integer> colSr;
    public TableColumn<FrequencyModel,String> colFrequency;
    private Method method;

    private ObservableList<FrequencyModel> frequencyList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new Method();
        getFrequency();
    }

    public void addClick(ActionEvent actionEvent) {

        String freq = frequencyTf.getText();

        if (freq.isEmpty()){
            method.show_popup("Please enter frequency value",frequencyTf);
            return;
        }


        Connection connection = null;
        PreparedStatement ps = null;

        try{
             connection = new DBConnection().getConnection();
             String qry = "INSERT INTO tbl_frequency(frequency, CREATED_BY) VALUES (?,?)";
             ps = connection.prepareStatement(qry);
             ps.setString(1,freq);
             ps.setInt(2, Login.currentlyLogin_Id);

             int res = ps.executeUpdate();

             if (res> 0){
                 getFrequency();
                 frequencyTf.setText("");
             }

        }catch (Exception e){

        }finally {
            DBConnection.closeConnection(connection,ps,null);
        }


    }


    public void getFrequency() {

        if(null != frequencyList){
            frequencyList.clear();
        }


        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            connection = new DBConnection().getConnection();
            String qry = "select * from tbl_frequency";
            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();
            while (rs.next()){

                String frequency = rs.getString("frequency");
                FrequencyModel fm = new FrequencyModel(frequency);
                frequencyList.add(fm);
            }

            tableView.setItems(frequencyList);

            colSr.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                    tableView.getItems().indexOf(cellData.getValue()) + 1));
            colFrequency.setCellValueFactory(new PropertyValueFactory<>("frequency"));


        }catch (Exception e){

        }finally {
            DBConnection.closeConnection(connection,ps,rs);
        }


    }

    public void closeButton(ActionEvent actionEvent) {
        new Method().closeStage(tableView);
    }
}
