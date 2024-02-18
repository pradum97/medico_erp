package com.techwhizer.medicalshop;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class ImageLoader {

    public Image load(String imagePath) {

        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/icon/img_preview.png")));
        }
    }

    public Object reportLogo(String imagePath) {

        try {
            return getClass().getResourceAsStream(imagePath);
        } catch (Exception e) {
            return "img/icon/img_preview.png";
        }
    }

    public Image loadWithSize(String imagePath) {

        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)), 100, 100, false, true);
        } catch (Exception e) {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/icon/img_preview.png")));
        }
    }

    public ImageView getPrintIcon() {

        ImageView iv = new ImageView(load("img/icon/print_ic.png"));
        iv.setFitWidth(17);
        iv.setFitHeight(17);

        return iv;
    }

    public ImageView getDownloadIcon(double width,double height) {

        ImageView iv = new ImageView(load("img/icon/download_ic.png"));
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        return iv;
    }

}
