package com.techwhizer.medicalshop.controller.CustomDate;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class DateTimePicker extends HBox implements Initializable {
    private ObjectProperty<LocalDateTime> dateTime;

    public DateTimePicker() {

        // Load FXML
        final FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("datetimepicker.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            // Should never happen.  If it does however, we cannot recover
            // from this
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



    }
}
