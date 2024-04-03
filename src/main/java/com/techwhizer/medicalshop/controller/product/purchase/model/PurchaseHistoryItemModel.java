package com.techwhizer.medicalshop.controller.product.purchase.model;

public class PurchaseHistoryItemModel {

    private int purchaseItemsId;
    private int purchaseMainId;
    private int itemId;
    private String itemName;
    private String quantity;
    private String batch;
    private String expiryDate;
    private String lotNumber;
    private double purchaseRate;
    private double mrp;
    private double salePrice;

    public PurchaseHistoryItemModel(int purchaseItemsId, int purchaseMainId, int itemId, String itemName, String quantity, String batch,
                                    String expiryDate, String lotNumber, double purchaseRate, double mrp, double salePrice) {
        this.purchaseItemsId = purchaseItemsId;
        this.purchaseMainId = purchaseMainId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.batch = batch;
        this.expiryDate = expiryDate;
        this.lotNumber = lotNumber;
        this.purchaseRate = purchaseRate;
        this.mrp = mrp;
        this.salePrice = salePrice;
    }

    public int getPurchaseItemsId() {
        return purchaseItemsId;
    }

    public void setPurchaseItemsId(int purchaseItemsId) {
        this.purchaseItemsId = purchaseItemsId;
    }

    public int getPurchaseMainId() {
        return purchaseMainId;
    }

    public void setPurchaseMainId(int purchaseMainId) {
        this.purchaseMainId = purchaseMainId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }
}
