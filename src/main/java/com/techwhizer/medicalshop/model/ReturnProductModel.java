package com.techwhizer.medicalshop.model;

import java.io.StringReader;

public class ReturnProductModel {

    private int saleItemID;
    private String itemName;
    private double netAmount;
    private double mrp;
    private String quantity;
    private String saleDate;
    private double discountAmount;
    private String returnQuantity;
    private boolean isReturn;
    private int stockId;
    private double mrpPerTab,discountPercentage;
    private String returnableQuantity;

    public ReturnProductModel(int saleItemID, String itemName, double netAmount, double mrp, String quantity, String saleDate, double discountAmount, String returnQuantity,
                              boolean isReturn, int stockId, double mrpPerTab, double discountPercentage, String returnableQuantity) {
        this.saleItemID = saleItemID;
        this.itemName = itemName;
        this.netAmount = netAmount;
        this.mrp = mrp;
        this.quantity = quantity;
        this.saleDate = saleDate;
        this.discountAmount = discountAmount;
        this.returnQuantity = returnQuantity;
        this.isReturn = isReturn;
        this.stockId = stockId;
        this.mrpPerTab = mrpPerTab;
        this.discountPercentage = discountPercentage;
        this.returnableQuantity = returnableQuantity;
    }

    public String getReturnableQuantity() {
        return returnableQuantity;
    }

    public void setReturnableQuantity(String returnableQuantity) {
        this.returnableQuantity = returnableQuantity;
    }

    public double getMrpPerTab() {
        return mrpPerTab;
    }

    public void setMrpPerTab(double mrpPerTab) {
        this.mrpPerTab = mrpPerTab;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public String getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(String returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public int getSaleItemID() {
        return saleItemID;
    }

    public void setSaleItemID(int saleItemID) {
        this.saleItemID = saleItemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
}

