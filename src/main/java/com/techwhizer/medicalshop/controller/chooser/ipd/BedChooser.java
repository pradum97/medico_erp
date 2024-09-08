package com.techwhizer.medicalshop.controller.chooser.ipd;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.ResourceBundle;

public class BedChooser implements Initializable {
    public Pagination pagination;
    private static final int ITEMS_PER_PAGE = 20;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pagination.setPageFactory(this::createPage);
    }
//
//    private TilePane createPage(int pageIndex) {
//        TilePane tilePane = new TilePane();
//        tilePane.setHgap(10);
//        tilePane.setVgap(10);
//        tilePane.setPadding(new Insets(10));
//
//        int start = pageIndex * ITEMS_PER_PAGE;
//        int end = Math.min(start + ITEMS_PER_PAGE, 200); // Assuming 200 items for this example
//
//        for (int i = start; i < end; i++) {
//            tilePane.getChildren().add(createCard("Label-" + i));
//        }
//
//        return tilePane;
//    }

    private ScrollPane createPage(int pageIndex) {
        TilePane tilePane = new TilePane();
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPadding(new Insets(10));

        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, 200); // Assuming 200 items for this example

        for (int i = start; i < end; i++) {
            tilePane.getChildren().add(createCard("Label-" + i));
        }

        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToWidth(true); // Ensures the scroll pane's width matches the parent
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scrollbar

        return scrollPane;
    }
    private VBox createCard(String date) {
        VBox cardWrapper = new VBox();
        cardWrapper.setMinSize(185, 50);

        cardWrapper.getChildren().addAll(
                createText("Bed Name",date),
                createText("Bed #","add"),
                createText("Column","1"),
                createText("Row","2")
        );

        cardWrapper.getStyleClass().add("card-wrapper");


        return cardWrapper;
    }

    private HBox createText(String labelStr, String valueStr) {

        HBox textHb = new HBox();
        Label labelL = new Label(labelStr);
        Label colon = new Label(":");
        Label valueL = new Label(valueStr);

        labelL.setStyle("-fx-font-weight: bold;-fx-font: 11px");
        colon.setStyle("-fx-min-width: 10px;-fx-alignment: center;-fx-font-weight: bold");


        textHb.getChildren().addAll(labelL, colon, valueL);

        textHb.getStyleClass().add("text-container");

        return textHb;
    }


    public void searchBed(ActionEvent actionEvent) {

    }
}
