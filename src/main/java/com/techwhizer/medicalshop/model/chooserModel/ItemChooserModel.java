package com.techwhizer.medicalshop.model.chooserModel;

import com.techwhizer.medicalshop.model.GstModel;
import com.techwhizer.medicalshop.model.GstModel;

public class ItemChooserModel {

    private int itemId = 0 ;
    private String itemName;
    private String packing;
    private GstModel discountModel;
    private String unit;
    private int tabPerStrip;
    private String composition,productTag,medicineDose;

    public ItemChooserModel(int itemId, String itemName, String packing,
                             GstModel discountModel,String unit,int tabPerStrip,
                            String composition, String productTag,String medicineDose) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.discountModel = discountModel;
        this.unit = unit;
        this.tabPerStrip = tabPerStrip;
        this.packing = packing;
        this.composition = composition;
        this.productTag = productTag;
        this.medicineDose = medicineDose;
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

    public GstModel getDiscountModel() {
        return discountModel;
    }

    public void setDiscountModel(GstModel discountModel) {
        this.discountModel = discountModel;
    }

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
