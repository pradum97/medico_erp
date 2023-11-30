package com.techwhizer.medicalshop.model;

public class ReturnItemModel {

    private int returnItemId;
    private String productName;
    private String quantity;

    public ReturnItemModel(int returnItemId, String productName, String quantity) {
        this.returnItemId = returnItemId;
        this.productName = productName;
        this.quantity = quantity;
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
