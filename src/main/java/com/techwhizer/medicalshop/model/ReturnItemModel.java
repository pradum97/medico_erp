package com.techwhizer.medicalshop.model;

public class ReturnItemModel {

    private int returnItemId;
    private String productName;
    private String quantity;
    private double amount,discountAmount,netAmount;

    public ReturnItemModel(int returnItemId, String productName, String quantity,
                           double amount, double discountAmount, double netAmount) {
        this.returnItemId = returnItemId;
        this.productName = productName;
        this.quantity = quantity;
        this.amount = amount;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public int getReturnItemId() {
        return returnItemId;
    }

    public void setReturnItemId(int returnItemId) {
        this.returnItemId = returnItemId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
