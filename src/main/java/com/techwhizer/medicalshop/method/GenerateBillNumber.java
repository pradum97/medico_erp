package com.techwhizer.medicalshop.method;

import com.techwhizer.medicalshop.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class GenerateBillNumber {

    public String generatePurchaseBillNum() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select max(purchase_main_id) from tbl_purchase_main");
            rs = ps.executeQuery();
            String invoiceNum = null;

            if (rs.next()) {
                long id = rs.getInt(1) + 1;

                invoiceNum = String.format("%07d", id);
            }
            return "P"+invoiceNum;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnection.closeConnection(connection , ps , rs);

        }

    }
    public String getSaleBillNum() {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select max(sale_main_id) from tbl_sale_main");
            rs = ps.executeQuery();
            String invoiceNum = null;

            if (rs.next()) {
                long id = rs.getInt(1) + 1;

                invoiceNum = String.format("%07d", id);
            }
            return "INV"+invoiceNum;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnection.closeConnection(connection , ps , rs);

        }

    }
    public String getPrescriptionBillNumber() {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select max(PRESCRIBE_MASTER_MEDICINE_ID) from prescribe_medicine_master");
            rs = ps.executeQuery();
            String invoiceNum = null;

            if (rs.next()) {
                long id = rs.getInt(1) + 1;
                invoiceNum = String.format("%06d", id);
            }
            return "EP"+invoiceNum;
        } catch (SQLException e) {
            return "";
        } finally {
            DBConnection.closeConnection(connection , ps , rs);

        }

    }

    public String getReturnBillNumb() {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select max(return_main_id) from tbl_return_main");
            rs = ps.executeQuery();
            String invoiceNum = null;

            if (rs.next()) {
                long id = rs.getInt(1) + 1;

                invoiceNum = String.format("%07d", id);
            }
            return "RTN"+invoiceNum;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnection.closeConnection(connection , ps , rs);

        }

    }


    public String generatorAdmissionNumber() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select max(patient_id) from tbl_patient");
            rs = ps.executeQuery();
            String invoiceNum = null;

            if (rs.next()) {
                long id = rs.getInt(1) + 1;
                invoiceNum = String.format("%04d", id);
            }

            String year = String.valueOf(Year.now().getValue()).substring(2);
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH);

            return "ADM/" + year + "/" + month + "/" + invoiceNum;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnection.closeConnection(connection, ps, rs);

        }

    }

    public String generateUHIDNum() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select cast(coalesce(max(patient_id),'0') as int) from tbl_patient");
            rs = ps.executeQuery();
            String uhidNum = null;

            if (rs.next()) {
                long  count = rs.getInt(1)+1;
                uhidNum = String.format("%06d", count );
            }
            return uhidNum;
        } catch (Exception e) {
            connection = new DBConnection().getConnection();
            String uhidNum = null;

            try {
                ps = connection.prepareStatement("select  (CAST(coalesce(max(patient_id), '0') AS integer)+\n" +
                        "CAST(coalesce(TO_CHAR(CURRENT_TIMESTAMP, 'MMMISS'), '0') AS integer))  from tbl_patient");
                rs = ps.executeQuery();
                if (rs.next()) {
                    uhidNum = rs.getString(1);
                }

            } catch (SQLException ex) {

            }

            return uhidNum;
        } finally {
            DBConnection.closeConnection(connection, ps, rs);

        }

    }


    public String generatorConsultReceiptNumber(String receiptIType) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();
            ps = connection.prepareStatement("select max(consultation_id) from patient_consultation");
            rs = ps.executeQuery();
            String invoiceNum = null;

            if (rs.next()) {
                long id = rs.getInt(1) + 1;
                invoiceNum = String.format("%04d", id);
            }

            return receiptIType+"-" + invoiceNum;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnection.closeConnection(connection, ps, rs);

        }

    }
}
