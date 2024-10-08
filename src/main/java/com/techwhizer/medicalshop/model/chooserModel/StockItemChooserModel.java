package com.techwhizer.medicalshop.model.chooserModel;

import com.techwhizer.medicalshop.model.GstModel;

public class StockItemChooserModel {
    private int itemId ;
    private String itemName;
    private String packing;
    private GstModel discountModel;
    private String unit;
    private int tabPerStrip;
    private String composition,productTag,medicineDose,availableQuantity;
    private boolean isStockable;
    private int department_id;
    private String department_name;
    private String batch,expiryDate;
    private boolean isExpired;
    private int stockId;

    public StockItemChooserModel(int itemId, String itemName, String packing,
                            GstModel discountModel,String unit,int tabPerStrip,
                            String composition, String productTag,String medicineDose,String availableQuantity,
                            boolean isStockable,int department_id, String department_name,String batch,String expiryDate,
                            boolean isExpired,int stockId) {
        this.itemId = itemId;
        this.itemName = itemName;
        //  this.discountModel = discountModel;
        this.unit = unit;
        this.tabPerStrip = tabPerStrip;
        this.packing = packing;
        this.composition = composition;
        this.productTag = productTag;
        this.medicineDose = medicineDose;
        this.availableQuantity = availableQuantity;
        this.isStockable = isStockable;
        this.department_id = department_id;
        this.department_name = department_name;
        this.batch = batch;
        this.expiryDate = expiryDate;
        this.isExpired = isExpired;
        this.stockId = stockId;
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

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public GstModel getDiscountModel() {
        return discountModel;
    }

    public void setDiscountModel(GstModel discountModel) {
        this.discountModel = discountModel;
    }

    public int getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(int department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public boolean isStockable() {
        return isStockable;
    }

    public void setStockable(boolean stockable) {
        isStockable = stockable;
    }

    public String getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(String availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getMedicineDose() {
        return medicineDose;
    }

    public void setMedicineDose(String medicineDose) {
        this.medicineDose = medicineDose;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public String getProductTag() {
        return productTag;
    }

    public void setProductTag(String productTag) {
        this.productTag = productTag;
    }

//    public GstModel getDiscountModel() {
//        return discountModel;
//    }
//
//    public void setDiscountModel(GstModel discountModel) {
//        this.discountModel = discountModel;
//    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getTabPerStrip() {
        return tabPerStrip;
    }

    public void setTabPerStrip(int tabPerStrip) {
        this.tabPerStrip = tabPerStrip;
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

    public String getPacking() {
        return packing;
    }

    public void setPacking(String packing) {
        this.packing = packing;
    }

    public GstModel getGstModel() {
        return discountModel;
    }

    public void setGstModel(GstModel discountModel) {
        this.discountModel = discountModel;
    }
}
