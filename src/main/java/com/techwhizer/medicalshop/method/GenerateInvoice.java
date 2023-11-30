package com.techwhizer.medicalshop.method;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.FileLoader;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.InvoiceModel.PaymentChargeModel;
import com.techwhizer.medicalshop.model.*;
import com.techwhizer.medicalshop.util.DBConnection;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GenerateInvoice {

    private FileLoader fileLoader;
    private float pdfZoomRatio = 0.65f;
    private static final String INVOICE_ROOT_PATH = "invoice/gangotri/";

    public void gstInvoice(int saleMainId, boolean isDownLoad, String downloadPath, Label button) {

        ImageView down_iv = new ImageView();
        ImageView print_iv = new ImageView();

        String rootPath = "img/icon/";
        down_iv.setFitHeight(18);
        down_iv.setFitWidth(18);
        print_iv.setFitHeight(18);
        print_iv.setFitWidth(18);
        ImageLoader loader = new ImageLoader();
        down_iv.setImage(loader.load(rootPath.concat("download_ic.png")));
        print_iv.setImage(loader.load(rootPath.concat("print_ic.png")));

        List<GstInvoiceModel> modelList = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();

        fileLoader = new FileLoader();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String query = """

                    select tsi.item_name,td.dr_name , tp.name as patient_name,tp.phone as patient_phone ,
                           tp.address as patient_address ,
                           tml.manufacturer_name,tsi.batch,
                           case when ts.quantity_unit = 'TAB' then (coalesce(tsi.sale_rate,0)/coalesce(tsi.strip_tab,0))
                                else coalesce(tsi.sale_rate,0) end as sale_rate,

                           ( ((coalesce(tsi.strip,0)*coalesce(tsi.strip_tab,0))+coalesce(tsi.pcs,0))*
                             case when ts.quantity_unit = 'TAB' then (coalesce(tsi.sale_rate,0)/coalesce(tsi.strip_tab,0))
                                  else coalesce(tsi.sale_rate,0) end )*coalesce(tsi.discount,0)/100 as discountAmount,

                           tsi.expiry_date,tsi.pack,tsi.discount,
                           tsm.invoice_number ,(TO_CHAR(tsm.sale_date, 'DD-MM-YYYY')) as sale_date,
                           tsd.shop_name , tsd.shop_address , tsd.shop_email,tsd.shop_gst_number , tsd.shop_phone_1 , tsd.shop_phone_2,
                           tsd.shop_food_licence,tsd.shop_drug_licence,(tsi.strip*tsi.strip_tab)+tsi.pcs as totalTab,
                           tsi.sgst, tsi.cgst,tsi.igst , tsi.hsn_sac ,
                           tsm.additional_discount as ADDITIONAL_DISCOUNT
                    from tbl_sale_main tsm
                             Left Join tbl_sale_items tsi on tsm.sale_main_id = tsi.sale_main_id
                             LEFT JOIN tbl_doctor td on tsm.doctor_id = td.doctor_id
                             left join tbl_stock ts on tsi.stock_id = ts.stock_id
                             LEFT JOIN tbl_patient tp on tsm.patient_id = tp.patient_id
                             left join tbl_manufacturer_list tml on tsi.mfr_id = tml.mfr_id
                             CROSS JOIN tbl_shop_details tsd
                    where tsm.sale_main_id = ?

                                        """;

            ps = connection.prepareStatement(query);
            ps.setInt(1, saleMainId);

            rs = ps.executeQuery();

            while (rs.next()) {

                String productName = rs.getString("item_name");
                String mfrName = rs.getString("manufacturer_name");
                String batch = rs.getString("batch");
                String pack = rs.getString("pack");
                String exp = rs.getString("expiry_date");
                int totalTab = rs.getInt("totalTab");
                double saleRate = rs.getDouble("sale_rate");
                double discountAmount = rs.getDouble("discountAmount");

                String patientName = rs.getString("patient_name");
                String patientPhone = rs.getString("patient_phone");
                String patientAddress = rs.getString("patient_address");

                String invoiceNum = rs.getString("invoice_number");
                String saleDate = rs.getString("sale_date");

                String shopName = rs.getString("shop_name");
                String shopAddress = rs.getString("shop_address");
                String shopEmail = rs.getString("shop_email");
                String shopPhone1 = rs.getString("shop_phone_1");
                String shopPhone2 = rs.getString("shop_phone_2");
                String shopGstNum = rs.getString("shop_gst_number");
                String fl = rs.getString("shop_food_licence");
                String dl = rs.getString("shop_drug_licence");
                String drName = rs.getString("dr_name");
                double additional_discount = rs.getDouble("ADDITIONAL_DISCOUNT");

                if (null == shopPhone2 || shopPhone2.isEmpty()) {
                    shopPhone2 = "";
                } else {
                    shopPhone2 = "," + shopPhone2;
                }

                long hsn = rs.getLong("hsn_sac");
                double sgst = rs.getDouble("sgst");
                double cgst = rs.getDouble("cgst");
                double igst = rs.getDouble("igst");
                Method m = new Method();
                shop_customer_details(param, rs, patientName, m.rec(patientPhone), m.rec(patientAddress), invoiceNum, saleDate, shopName, m.rec(shopAddress), shopEmail, shopPhone1, shopPhone2, m.rec(shopGstNum), additional_discount, m.rec(drName), m.rec(fl), m.rec(dl));
                modelList.add(new GstInvoiceModel(productName, m.rec(mfrName), m.rec(pack), m.rec(batch), m.rec(exp), saleRate, discountAmount, totalTab, saleDate, hsn, sgst, cgst, igst));
            }

            if (null == modelList) {
                new CustomDialog().showAlertBox("Failed", "Something went wrong!");
            } else {
                Map<Long, TaxDetails> map = new HashMap<>();

                for (GstInvoiceModel cm : modelList) {
                    long key = cm.getHsn();

                    double netAmount = ((cm.getMrp() * cm.getQuantity()));
                    double taxableAmount = (netAmount * 100) / (100 + (cm.getSgst() + cm.getCgst() + cm.getIgst()));

                    if (map.containsKey(key)) {
                        // update value
                        TaxDetails td = new TaxDetails(cm.getSgst(), cm.getCgst(), cm.getIgst(), map.get(key).getTaxableAmount() + taxableAmount, cm.getHsn());

                        map.put(key, td);

                    } else {
                        TaxDetails td = new TaxDetails(cm.getSgst(), cm.getCgst(), cm.getIgst(), taxableAmount, cm.getHsn());
                        map.put(key, td);
                    }
                }
                List<TaxDetails> taxList = new ArrayList<>(map.values());

                JRBeanCollectionDataSource productBean = new JRBeanCollectionDataSource(modelList);
                JRBeanCollectionDataSource taxBean = new JRBeanCollectionDataSource(taxList);

                param.put("productDetails", productBean);
                param.put("tax", taxBean);

                JasperReport subJasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "Gst_Invoice_Tax.jrxml"));
                param.put("SUBREPORT_DIR", subJasperReport);

                JasperReport jasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "Gst_Invoice.jrxml"));
                JasperPrint print = JasperFillManager.fillReport(jasperReport, param, new JREmptyDataSource());

                if (isDownLoad && null != downloadPath) {
                    JasperExportManager.exportReportToPdfFile(print, downloadPath);
                    Platform.runLater(() -> button.setGraphic(down_iv));
                    new CustomDialog().showAlertBox("Successful", "Invoice Successfully Download");

                } else {
                    Platform.runLater(() -> {
                        button.setGraphic(print_iv);
                    });
                    JasperViewer viewer = new JasperViewer(print, false);
                    viewer.setZoomRatio(pdfZoomRatio);
                    viewer.setVisible(true);
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JRException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void shop_customer_details(Map<String, Object> param, ResultSet rs, String customerName,
                                       String customerPhone, String customerAddress, String invoiceNum,
                                       String saleDate, String shopName, String shopAddress, String shopEmail,
                                       String shopPhone1, String shopPhone2, String shopGstNum,
                                       double additional_discount, String doctorName, String foodLicence,
                                       String drugLicence) throws SQLException {
        if (rs.isLast()) {

            // SHOP DETAILS
            param.put("SHOP_NAME", shopName.toUpperCase());
            param.put("SHOP_PHONE_1", shopPhone1);
            param.put("SHOP_PHONE_2", shopPhone2);
            param.put("SHOP_EMAIL", shopEmail);
            param.put("SHOP_GST_NUMBER", shopGstNum);
            param.put("SHOP_ADDRESS", shopAddress);
            param.put("INVOICE_NUMBER", invoiceNum);
            param.put("INVOICE_DATE", saleDate);

            // CUSTOMER DETAILS
            param.put("CUSTOMER_NAME", customerName.toUpperCase());
            param.put("CUSTOMER_PHONE", customerPhone.toUpperCase());
            param.put("CUSTOMER_ADDRESS", customerAddress.toUpperCase());
            param.put("add_discount", additional_discount);
            param.put("doctorName", doctorName);
            param.put("foodLicence", foodLicence);
            param.put("drugLicence", drugLicence);
            param.put("title_logo", new ImageLoader().reportLogo("img/company/gangotri_company_logo.png"));

        }
    }

    public void regularInvoice(int saleMainId, boolean isDownLoad, String downloadPath, Label button) {
        ImageView down_iv = new ImageView();
        ImageView print_iv = new ImageView();

        String rootPath = "img/icon/";
        down_iv.setFitHeight(18);
        down_iv.setFitWidth(18);
        print_iv.setFitHeight(18);
        print_iv.setFitWidth(18);
        ImageLoader loader = new ImageLoader();
        down_iv.setImage(loader.load(rootPath.concat("download_ic.png")));
        print_iv.setImage(loader.load(rootPath.concat("print_ic.png")));

        List<RegularInvoiceModel> modelList = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        fileLoader = new FileLoader();
        try {
            connection = new DBConnection().getConnection();
            String query = """
                    select tsi.item_name,td.dr_name , tp.name as patient_name,tp.phone as patient_phone ,
                           tp.address as patient_address ,
                           tml.manufacturer_name,tsi.batch,
                           case when ts.quantity_unit = 'TAB' then (coalesce(tsi.sale_rate,0)/coalesce(tsi.strip_tab,0))
                               else coalesce(tsi.sale_rate,0) end as sale_rate,
                                        
                           ( ((coalesce(tsi.strip,0)*coalesce(tsi.strip_tab,0))+coalesce(tsi.pcs,0))*
                             case when ts.quantity_unit = 'TAB' then (coalesce(tsi.sale_rate,0)/coalesce(tsi.strip_tab,0))
                                  else coalesce(tsi.sale_rate,0) end )*coalesce(tsi.discount,0)/100 as discountAmount,
                                        
                           tsi.expiry_date,tsi.pack,tsi.discount,
                           tsm.invoice_number ,(TO_CHAR(tsm.sale_date, 'DD-MM-YYYY')) as sale_date,
                           tsd.shop_name , tsd.shop_address , tsd.shop_email,tsd.shop_gst_number , tsd.shop_phone_1 , tsd.shop_phone_2,
                           tsd.shop_food_licence,tsd.shop_drug_licence,(tsi.strip*tsi.strip_tab)+tsi.pcs as totalTab,
                           tsm.additional_discount as ADDITIONAL_DISCOUNT
                    from tbl_sale_main tsm
                             Left Join tbl_sale_items tsi on tsm.sale_main_id = tsi.sale_main_id
                             LEFT JOIN tbl_doctor td on tsm.doctor_id = td.doctor_id
                        left join tbl_stock ts on tsi.stock_id = ts.stock_id
                             LEFT JOIN tbl_patient tp on tsm.patient_id = tp.patient_id
                             left join tbl_manufacturer_list tml on tsi.mfr_id = tml.mfr_id
                             CROSS JOIN tbl_shop_details tsd
                                                            where tsm.sale_main_id = ?""";

            ps = connection.prepareStatement(query);
            ps.setInt(1, saleMainId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String productName = rs.getString("item_name");
                String mfrName = rs.getString("manufacturer_name");
                String batch = rs.getString("batch");
                String pack = rs.getString("pack");
                String exp = rs.getString("expiry_date");
                int totalTab = rs.getInt("totalTab");
                double saleRate = rs.getDouble("sale_rate");
                double discountAmount = rs.getDouble("discountAmount");

                String patientName = rs.getString("patient_name");
                String patientPhone = rs.getString("patient_phone");
                String patientAddress = rs.getString("patient_address");

                String invoiceNum = rs.getString("invoice_number");
                String saleDate = rs.getString("sale_date");

                String shopName = rs.getString("shop_name");
                String shopAddress = rs.getString("shop_address");
                String shopEmail = rs.getString("shop_email");
                String shopPhone1 = rs.getString("shop_phone_1");
                String shopPhone2 = rs.getString("shop_phone_2");
                String shopGstNum = rs.getString("shop_gst_number");
                String fl = rs.getString("shop_food_licence");
                String dl = rs.getString("shop_drug_licence");
                String drName = rs.getString("dr_name");
                double additional_discount = rs.getDouble("ADDITIONAL_DISCOUNT");

                if (null == shopPhone2 || shopPhone2.isEmpty()) {
                    shopPhone2 = "";
                } else {
                    shopPhone2 = "," + shopPhone2;
                }

                Method m = new Method();

                shop_customer_details(param, rs, patientName, m.rec(patientPhone), m.rec(patientAddress), invoiceNum, saleDate, shopName, m.rec(shopAddress), shopEmail, shopPhone1, shopPhone2, m.rec(shopGstNum), additional_discount, m.rec(drName), m.rec(fl), m.rec(dl));
                modelList.add(new RegularInvoiceModel(productName, m.rec(mfrName), m.rec(pack), m.rec(batch), m.rec(exp), saleRate, discountAmount, totalTab, saleDate));
            }

            JRBeanCollectionDataSource cartBean = new JRBeanCollectionDataSource(modelList);
            param.put("productDetails", cartBean);
            JasperReport jasperReport = null;
            jasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "Regular_Invoice.jrxml"));
            JasperPrint print = JasperFillManager.fillReport(jasperReport, param, new JREmptyDataSource());

            if (isDownLoad && null != downloadPath) {
                JasperExportManager.exportReportToPdfFile(print, downloadPath);
                Platform.runLater(() -> button.setGraphic(down_iv));
                new CustomDialog().showAlertBox("Successful", "Invoice Successfully Download");

            } else {
                Platform.runLater(() -> {
                    button.setGraphic(print_iv);
                });
                JasperViewer viewer = new JasperViewer(print, false);
                viewer.setZoomRatio(pdfZoomRatio);
                viewer.setVisible(true);
            }
        } catch (SQLException | JRException e) {
            Platform.runLater(() -> {
                if (null != button) {
                    if (isDownLoad) {
                        button.setGraphic(down_iv);
                    } else {
                        button.setGraphic(print_iv);

                    }
                }
            });
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    public void prescriptionInvoice(int patientId, String consultDoctorName,
                                    ObservableList<PrescribedMedicineModel> medicineList,
                                    String invoiceNum,String precribeDate) {
        ImageView down_iv = new ImageView();
        ImageView print_iv = new ImageView();

        String rootPath = "img/icon/";
        down_iv.setFitHeight(18);
        down_iv.setFitWidth(18);
        print_iv.setFitHeight(18);
        print_iv.setFitWidth(18);
        ImageLoader loader = new ImageLoader();
        down_iv.setImage(loader.load(rootPath.concat("download_ic.png")));
        print_iv.setImage(loader.load(rootPath.concat("print_ic.png")));

        List<PrescriptionsBillModel> modelList = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        fileLoader = new FileLoader();

        try {
            connection = new DBConnection().getConnection();
            String query = """
                                       
                    select trim( concat(COALESCE(ts.name, ''), ' ',
                    COALESCE(tp.first_name, ''), ' ',
                    COALESCE(tp.middle_name, ''), ' ',
                    COALESCE(tp.last_name, '')) )AS name,*
                    from tbl_patient tp
                    left join public.tbl_salutation ts on tp.salutation_id = ts.salutation_id
                    where  patient_id = ?
                    """;

            ps = connection.prepareStatement(query);
            ps.setInt(1, patientId);
            rs = ps.executeQuery();
            while (rs.next()) {

                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String address = rs.getString("address");

                String gender = rs.getString("gender");
                String age = rs.getString("age");

                String weight = rs.getString("weight");
                String bp = rs.getString("bp");
                String pulse = rs.getString("pulse");

                String sugar = rs.getString("sugar");

                String spo2 = rs.getString("spo2");
                String temp = rs.getString("temp");
                String cvs = rs.getString("cvs");
                String cns = rs.getString("cns");
                String chest = rs.getString("chest");
                String  uhid_no= rs.getString("uhid_no");

                param.put("age", age);
                param.put("gender", String.valueOf(gender.charAt(0)).toUpperCase());
                param.put("weight", weight);
                param.put("name", name);
                param.put("address", address);
                param.put("date", precribeDate);
                param.put("phone", phone);
                param.put("invoiceNumber",invoiceNum);
                param.put("uhidNum",uhid_no);

                String companyLogoPath = "img/company/gangotri_company_logo.png";

                param.put("company_logo1", new ImageLoader().reportLogo(companyLogoPath));
                param.put("company_logo2", new ImageLoader().reportLogo(companyLogoPath));
                param.put("doctorName", consultDoctorName);
                param.put("watermark", new ImageLoader().reportLogo("img/company/watermark_2.png"));

                modelList.add(new PrescriptionsBillModel(patientId, bp, pulse, spo2, temp, chest, cvs, cns, sugar));
            }

            JRBeanCollectionDataSource cartBean = new JRBeanCollectionDataSource(modelList);
            JRBeanCollectionDataSource medicineListBean = new JRBeanCollectionDataSource(medicineList);
            param.put("patientDetails", cartBean);
            param.put("medicineList", medicineListBean);
            JasperReport jasperReport = null;
            jasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "prescriptions.jrxml"));
            JasperPrint print = JasperFillManager.fillReport(jasperReport, param, new JREmptyDataSource());
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setZoomRatio(pdfZoomRatio);
            viewer.setVisible(true);

        } catch (SQLException | JRException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }


    public void generatePaymentSlip(int patientId, int sourceId, String slipType) {

        List<PaymentChargeModel> paymentInfo = new ArrayList<>();

        Map<String, Object> param = new HashMap<>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        fileLoader = new FileLoader();

        try {
            connection = new DBConnection().getConnection();

            if (Objects.equals(slipType, "Consultant")) {

                String query = """
                        select * from print_consultant_slip_v where patient_id  = ? and consultation_id = ?
                         """;

                ps = connection.prepareStatement(query);
                ps.setInt(1, patientId);
                ps.setInt(2, sourceId);

                System.out.println(ps);
                rs = ps.executeQuery();
                while (rs.next()) {

                    int consultation_id = rs.getInt("consultation_id");
                    int referred_by_doctor_id = rs.getInt("referred_by_doctor_id");
                    int consultation_doctor_id = rs.getInt("consultation_doctor_id");
                    int patient_id = rs.getInt("patient_id");

                    String patient_name = rs.getString("patient_name");

                    System.out.println(patient_name);

                    String uhid_no = rs.getString("uhid_no");
                    String address = rs.getString("address");

                    String consult_name = rs.getString("consult_name");
                    String referred_by = rs.getString("referred_by");

                    String receipt_num = rs.getString("receipt_num");
                    String receipt_date = rs.getString("receipt_date");
                    String gender_age = rs.getString("gender_age");
                    String phone = rs.getString("phone");
                    String fee_valid_date = rs.getString("fee_valid_date");
                    String preparedBy = rs.getString("preparedby");
                    String receipt_type = rs.getString("receipt_type");
                    String patient_category = rs.getString("patient_category");


                    param.put("patientName", patient_name);
                    param.put("uhidNum", uhid_no);
                    param.put("address", address);
                    param.put("consultantDoctorName", consult_name);
                    param.put("ReferredBy", referred_by);
                    param.put("ReceiptNum", receipt_num);
                    param.put("ReceiptDateAndTime", receipt_date);
                    param.put("genderAge", gender_age);
                    param.put("MobileNum", phone);
                    param.put("feeVaildDate", fee_valid_date);
                    param.put("printedBy", preparedBy);
                    param.put("receiptType", receipt_type);
                    param.put("patientCategory", patient_category);

                }


            }

            ps = null;
            rs = null;

            String paymentQry = """
                    select pc.consultation_id,pi.purpose,pi.payment_method,pi.transaction_id,pi.amount,pi.remarks
                    from patient_consultation pc
                             left join payment_information pi on pi.document_id = pc.consultation_id
                             
                             where pc.consultation_id = ?
                    """;

            ps = connection.prepareStatement(paymentQry);
            ps.setInt(1, sourceId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int consultation_id = rs.getInt("consultation_id");
                String purpose = rs.getString("purpose");
                String payment_method = rs.getString("payment_method");
                String transaction_id = rs.getString("transaction_id");
                double amount = rs.getDouble("amount");
                String remarks = rs.getString("remarks");

                PaymentChargeModel pcm = new PaymentChargeModel(purpose, payment_method,
                        transaction_id, remarks, amount);

                paymentInfo.add(pcm);

            }


            String companyLogoPath = "img/company/gangotri_company_logo.png";
            param.put("company_logo1", new ImageLoader().reportLogo(companyLogoPath));
            param.put("company_logo2", new ImageLoader().reportLogo(companyLogoPath));
            // param.put("watermark",new ImageLoader().reportLogo("img/company/watermark_2.png"));

            JRBeanCollectionDataSource paymentInfoDs = new JRBeanCollectionDataSource(paymentInfo);
            param.put("paymentDetails", paymentInfoDs);
            JasperReport jasperReport = null;
            jasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "payment_slip.jrxml"));

            JasperPrint print = JasperFillManager.fillReport(jasperReport, param, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setZoomRatio(pdfZoomRatio);
            viewer.setVisible(true);


        } catch (SQLException | JRException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

}
