package com.techwhizer.medicalshop;

import com.itextpdf.text.BadElementException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class ImageLoader {

    public static com.itextpdf.text.Image reportImageLoader(String path) {
        try {
            return com.itextpdf.text.Image.getInstance(path);
        } catch (BadElementException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



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
