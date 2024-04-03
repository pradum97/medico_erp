package com.techwhizer.medicalshop.controller.product.purchase.model;

public class PurchaseHistoryMainModel {

    private int purchaseMainId;
    private int dealerId;
    private String dealerName;
    private String dealerPhone;
    private String dealerAddress;
    private String invoiceNumber;
    private String billDate;

    public PurchaseHistoryMainModel(int purchaseMainId, int dealerId, String dealerName,
                                    String dealerPhone, String dealerAddress, String invoiceNumber, String billDate) {
        this.purchaseMainId = purchaseMainId;
        this.dealerId = dealerId;
        this.dealerName = dealerName;
        this.dealerPhone = dealerPhone;
        this.dealerAddress = dealerAddress;
        this.invoiceNumber = invoiceNumber;
        this.billDate = billDate;
    }

    public int getPurchaseMainId() {
        return purchaseMainId;
    }

    public void setPurchaseMainId(int purchaseMainId) {
        this.purchaseMainId = purchaseMainId;
    }

    public int getDealerId() {
        return dealerId;
    }

    public void setDealerId(int dealerId) {
        this.dealerId = dealerId;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public String getDealerPhone() {
        return dealerPhone;
    }

    public void setDealerPhone(String dealerPhone) {
        this.dealerPhone = dealerPhone;
    }

    public String getDealerAddress() {
        return dealerAddress;
    }

    public void setDealerAddress(String dealerAddress) {
        this.dealerAddress = dealerAddress;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }
}
