package com.techwhizer.medicalshop.util;

import com.techwhizer.medicalshop.controller.common.model.DepartmentModel;
import com.techwhizer.medicalshop.model.ConsultationSetupModel;
import com.techwhizer.medicalshop.model.DoctorModel;
import com.techwhizer.medicalshop.model.SalutationModel;
import com.techwhizer.medicalshop.util.type.DoctorType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CommonUtil {

    private static final String DATE_PATTERN = "dd/MM/yyyy";

    public static LocalDate calculateDOBFromAge(int age) {
        LocalDate currentDate = LocalDate.now();
        LocalDate tentativeDOB = currentDate.minusYears(age);

        if (tentativeDOB.plusYears(age).isBefore(currentDate)) {
            return tentativeDOB;
        } else {
            return tentativeDOB.minusYears(1);
        }
    }

    public static LocalDate stringToLocalDate(String date,String pattern){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(date, formatter);
    }

    public String formatLocalDate(LocalDate localDate){
        DateTimeFormatter format = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return localDate.format(format);
    }

    public static ConsultationSetupModel getConsultationSetup(){

        ConsultationSetupModel csm = new  ConsultationSetupModel(0,400,25,0,"");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            connection = new DBConnection().getConnection();

            String query = """
                    select *,to_char(creation_date,'DD/MM/YYYY') as creation_date from consultation_setup order by 1 desc limit 1 
                    """;
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();

            if (rs.next()) {
                int consultation_setup_id = rs.getInt("consultation_setup_id");
                int createdById = rs.getInt("created_by");
                double consultation_fee = rs.getDouble("consultation_fee");
                int fee_valid_days = rs.getInt("fee_valid_days");
                String creation_date = rs.getString("creation_date");

               csm = new  ConsultationSetupModel(consultation_setup_id,consultation_fee,
                        fee_valid_days,createdById,creation_date);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return csm;
    }

    public static ObservableList<String> getDocumentType() {

        return FXCollections.observableArrayList(
                "Aadhaar Card", "Pan Card", "Voter Card", "Driving Licence",
                "Ration Card", "Birth Certificate.", "Passport."

        );

    }

    public static ObservableList<DepartmentModel> getDepartmentsList(){

       ObservableList<DepartmentModel> departmentList = FXCollections.observableArrayList();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            connection = new DBConnection().getConnection();

            String query = """
                    select * from tbl_departments order by department_name asc
                    """;
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                int departmentId = rs.getInt("department_id");
                String departmentName = rs.getString("department_name");
                String departmentCode = rs.getString("department_code");
                departmentList.add(new DepartmentModel(departmentId,departmentName,departmentCode));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return departmentList;
    }


    public static DepartmentModel getDepartment(String departmentCode){

        DepartmentModel departmentModel = new  DepartmentModel();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            connection = new DBConnection().getConnection();

            String query = """
                    select * from tbl_departments where department_code = ? order by department_name asc
                    """;
            ps = connection.prepareStatement(query);
            ps.setString(1,departmentCode);
            rs = ps.executeQuery();

            if (rs.next()) {
                int departmentId = rs.getInt("department_id");
                String departmentName = rs.getString("department_name");
                String deptCode = rs.getString("department_code");
               departmentModel = new DepartmentModel(departmentId,departmentName,deptCode);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return departmentModel;
    }

    public static ObservableList<String> getPaymentMethod(){

        return FXCollections.observableArrayList(
                "Cash","Phone Pay","Google Pay","Paytm","Credit Card", "Debit Card",
                "Online Transfer", "UPI",
                "Mobile Wallet", "Cheque", "Bank Transfer",
               "PayPal", "Installment Plan"
        );
    }

    public static ObservableList<DoctorModel> getDoctor(DoctorType doctorType) {
        String docType = doctorType.toString().replaceAll("_", " ");

        DoctorModel dmm = new DoctorModel(0, "OTHER");

        ObservableList<DoctorModel> doctorList = doctorType == DoctorType.OUT_SIDE ?
                FXCollections.observableArrayList(dmm)
                : FXCollections.observableArrayList();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            connection = new DBConnection().getConnection();

            String query = """
                    SELECT doctor_id,dr_name,coalesce(dr_phone,'-')as dr_phone,coalesce(dr_address,'-') as dr_address,
                    coalesce(speciality,'-') as speciality ,doctor_type,
                    coalesce(dr_reg_num,'-') as dr_reg_num,coalesce(qualification,'-') as qualification 
                    ,(TO_CHAR(created_date, 'DD-MM-YYYY')) as created_date FROM tbl_doctor 
                    where doctor_type = ?
                    order by doctor_id desc
                    """;
            ps = connection.prepareStatement(query);
            ps.setString(1, docType);

            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("DOCTOR_ID");
                String name = rs.getString("DR_NAME");
                String phone = rs.getString("DR_PHONE");
                String address = rs.getString("DR_ADDRESS");
                String spec = rs.getString("SPECIALITY");
                String reg = rs.getString("DR_REG_NUM");
                String qly = rs.getString("QUALIFICATION");
                String date = rs.getString("CREATED_DATE");
                String dType = rs.getString("doctor_type");

                doctorList.add(new DoctorModel(id, name, phone,
                        address, reg, spec, qly, date, dType));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
        return doctorList;
    }

    public static ObservableList<SalutationModel> getSalutation(int salutationId) {

        ObservableList<SalutationModel> salutationList = FXCollections.observableArrayList();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "";

            if (salutationId > 0) {
                qry = "select * from tbl_salutation where salutation_id =" + salutationId;
            } else {

                qry = "select * from tbl_salutation";
            }

            ps = connection.prepareStatement(qry);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("salutation_id");
                String name = rs.getString("name");

                SalutationModel sm = new SalutationModel(id, name);
                salutationList.add(sm);

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
        return salutationList;
    }
}
