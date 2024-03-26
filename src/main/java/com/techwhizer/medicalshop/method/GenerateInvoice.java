package com.techwhizer.medicalshop.method;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.FileLoader;
import com.techwhizer.medicalshop.ImageLoader;
import com.techwhizer.medicalshop.InvoiceModel.PaymentChargeModel;
import com.techwhizer.medicalshop.model.*;
import com.techwhizer.medicalshop.util.CommonUtil;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

enum PrintType {
    GST, REGULAR
}

public class GenerateInvoice {

    private FileLoader fileLoader;
    private float pdfZoomRatio = 0.65f;
    private static final String INVOICE_ROOT_PATH = "invoice/gangotri/";

    public void billingInvoice(int saleMainId, boolean isDownLoad, String downloadPath, Label labelButton) {

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
                    select tsi.item_name,td.dr_name , regexp_replace(trim( concat(COALESCE(sal.name, ''), ' ',
                    COALESCE(tp.first_name, ''), ' ',
                    COALESCE(tp.middle_name, ''), ' ',
                    COALESCE(tp.last_name, '')) ),'  ',' ' ) as patient_name,tp.phone as patient_phone ,
                           tp.address as patient_address ,
                           tml.manufacturer_name,tsi.batch,
                           case when ts.quantity_unit = 'TAB' then (coalesce(tsi.sale_rate,0)/coalesce(tsi.strip_tab,0))
                                else coalesce(tsi.sale_rate,0) end as sale_rate,tp.uhid_no,

                           ( ((coalesce(tsi.strip,0)*coalesce(tsi.strip_tab,0))+coalesce(tsi.pcs,0))*
                             case when ts.quantity_unit = 'TAB' then (coalesce(tsi.sale_rate,0)/coalesce(tsi.strip_tab,0))
                                  else coalesce(tsi.sale_rate,0) end )*coalesce(tsi.discount,0)/100 as discountAmount,

                           tsi.expiry_date,tsi.pack,tsi.discount,
                           tsm.invoice_number ,(TO_CHAR(tsm.sale_date, 'DD-MM-YYYY')) as sale_date,
                          (tsi.strip*tsi.strip_tab)+tsi.pcs as totalTab,
                           tsi.sgst, tsi.cgst,tsi.igst , tsi.hsn_sac ,
                           tsm.additional_discount_amount as additional_discount_amount,
                           get_remaining_dues(tsm.sale_main_id) as dues_amount,tsm.total_discount_amount,
                           get_received_amount(tsm.sale_main_id) as received_amount,
                           tsm.bill_type
                    from tbl_sale_main tsm
                             Left Join tbl_sale_items tsi on tsm.sale_main_id = tsi.sale_main_id
                             LEFT JOIN tbl_doctor td on tsm.referred_by = td.doctor_id
                             left join tbl_stock ts on tsi.stock_id = ts.stock_id
                             LEFT JOIN tbl_patient tp on tsm.patient_id = tp.patient_id
                            left join tbl_salutation sal on sal.salutation_id = tp.salutation_id

                             left join tbl_manufacturer_list tml on tsi.mfr_id = tml.mfr_id
                    where tsm.sale_main_id = ?

                                        """;

            ps = connection.prepareStatement(query);
            ps.setInt(1, saleMainId);

            rs = ps.executeQuery();

            String billType = "REGULAR";

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
                billType = rs.getString("bill_type");


                String drName = rs.getString("dr_name");
                double additional_discount = rs.getDouble("additional_discount_amount");
                param.put("duesAmount",rs.getDouble("dues_amount"));
                param.put("totalSaving",rs.getDouble("total_discount_amount"));

                param.put("receivedAmount", rs.getDouble("received_amount"));
                param.put("uhid", rs.getString("uhid_no"));


                long hsn = rs.getLong("hsn_sac");
                double sgst = rs.getDouble("sgst");
                double cgst = rs.getDouble("cgst");
                double igst = rs.getDouble("igst");
                Method m = new Method();
                shop_customer_details(param, rs, patientName.toUpperCase(), m.rec(patientPhone), m.rec(patientAddress.toUpperCase()), invoiceNum, saleDate, additional_discount, m.rec(drName));
                modelList.add(new GstInvoiceModel(productName, m.rec(mfrName), m.rec(pack), m.rec(batch), m.rec(exp),
                        saleRate, discountAmount, totalTab, saleDate, hsn, sgst, cgst, igst, billType));
            }

            Map<Long, TaxDetails> map = new HashMap<>();

            for (GstInvoiceModel cm : modelList) {
                long key = cm.getHsn();

                double netAmount = ((cm.getMrp() * cm.getQuantity()));
                double taxableAmount = (netAmount * 100) / (100 + (cm.getSgst() + cm.getCgst() + cm.getIgst()));

                if (map.containsKey(key)) {
                    TaxDetails td = new TaxDetails(cm.getSgst(), cm.getCgst(), cm.getIgst(),
                            map.get(key).getTaxableAmount() + taxableAmount, cm.getHsn());
                    map.put(key, td);

                } else {
                    TaxDetails td = new TaxDetails(cm.getSgst(), cm.getCgst(), cm.getIgst(), taxableAmount, cm.getHsn());
                    map.put(key, td);
                }
            }
            List<TaxDetails> taxList = new ArrayList<>(map.values());

            DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
            JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.xpath.executer.factory",
                    "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");


            JRBeanCollectionDataSource productBean = new JRBeanCollectionDataSource(modelList);
            JRBeanCollectionDataSource taxBean = new JRBeanCollectionDataSource(taxList);
            JasperReport subJasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "GstInvoiceSubReport.jrxml"));

            param.put("productDetails", productBean);
            param.put("tax", Objects.equals(billType, PrintType.GST.name()) ? taxBean : null);
            param.putAll(setHospitalReportHeader());
            param.put("SUBREPORT_DIR", subJasperReport);

            JasperReport jasperReport = JasperCompileManager.compileReport(fileLoader.load(INVOICE_ROOT_PATH + "BillingInvoice.jrxml"));
            JasperPrint print = JasperFillManager.fillReport(jasperReport, param, new JREmptyDataSource());

            if (isDownLoad && null != downloadPath) {
                JasperExportManager.exportReportToPdfFile(print, downloadPath);
                Platform.runLater(() -> labelButton.setGraphic(down_iv));
                new CustomDialog().showAlertBox("Successful", "Invoice Successfully Download");

            } else {
                Platform.runLater(() -> {
                    labelButton.setGraphic(print_iv);
                });
                JasperViewer viewer = new JasperViewer(print, false);
                viewer.setZoomRatio(pdfZoomRatio);
                viewer.setVisible(true);
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
                                       String saleDate,
                                       double additional_discount, String doctorName) throws SQLException {
        if (rs.isLast()) {

            // SHOP DETAILS

            param.put("INVOICE_NUMBER", invoiceNum);
            param.put("INVOICE_DATE", saleDate);

            // CUSTOMER DETAILS
            param.put("CUSTOMER_NAME", customerName.toUpperCase());
            param.put("CUSTOMER_PHONE", customerPhone.toUpperCase());
            param.put("CUSTOMER_ADDRESS", customerAddress.toUpperCase());
            param.put("add_discount", additional_discount);
            param.put("doctorName", doctorName);
        }
    }


    private static Map<String, Object> setHospitalReportHeader() {

        Method method = new Method();
        Map<String, Object> param = new HashMap<>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "select * from tbl_shop_details order by 1 desc limit 1";
            ps = connection.prepareStatement(qry);

            rs = ps.executeQuery();

            if (rs.next()) {
                String shopName = rs.getString("shop_name");
                String shopAddress = rs.getString("shop_address");
                String shopEmail = rs.getString("shop_email");
                String shopPhone1 = rs.getString("shop_phone_1");
                String shopPhone2 = rs.getString("shop_phone_2");
                String shopGstNum = rs.getString("shop_gst_number");
                String foodLicence = rs.getString("shop_food_licence");
                String drugLicence = rs.getString("shop_drug_licence");

                if (null == shopPhone2 || shopPhone2.isEmpty()) {
                    shopPhone2 = "";
                } else {
                    shopPhone2 = "," + shopPhone2;
                }

                param.put("SHOP_NAME", shopName.toUpperCase());
                param.put("SHOP_PHONE_1", shopPhone1);
                param.put("SHOP_PHONE_2", shopPhone2);
                param.put("SHOP_EMAIL", shopEmail.toUpperCase());
                param.put("SHOP_GST_NUMBER", method.rec(shopGstNum));
                param.put("SHOP_ADDRESS", method.rec(shopAddress.toUpperCase()));
                param.put("foodLicence", method.rec(foodLicence));
                param.put("drugLicence", method.rec(drugLicence));
                param.put("title_logo", new ImageLoader().reportLogo("img/company/gangotri_company_logo.png"));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return param;

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
                param.put("gender", String.valueOf(gender.charAt(0)).toUpperCase());
                param.put("weight", weight);
                param.put("name", name);
                param.put("address", address);
                param.put("date", precribeDate);
                param.put("phone", phone);
                param.put("invoiceNumber",invoiceNum);
                param.put("uhidNum",uhid_no);

                ConsultationSetupModel csm = CommonUtil.getConsultationSetup();

                param.put("fee_valid_days",csm==null?25:csm.getFee_valid_days());

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
                    String consult_date = rs.getString("consult_date");
                    String preparedBy = rs.getString("preparedby");
                    String receipt_type = rs.getString("receipt_type");
                    String patient_category = rs.getString("patient_category");

                    System.out.println(consult_date);

                    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate lDate = LocalDate.parse(consult_date,format);

                    ConsultationSetupModel csm = CommonUtil.getConsultationSetup();

                    String feeValidUpTo = lDate.plusDays(csm == null?25:csm.getFee_valid_days()).format(format).toString();

                    param.put("patientName", patient_name);
                    param.put("uhidNum", uhid_no);
                    param.put("address", address);
                    param.put("consultantDoctorName", consult_name);
                    param.put("ReferredBy", referred_by);
                    param.put("ReceiptNum", receipt_num);
                    param.put("ReceiptDateAndTime", receipt_date);
                    param.put("genderAge", gender_age);
                    param.put("MobileNum", phone);
                    param.put("feeVaildDate", feeValidUpTo);
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
