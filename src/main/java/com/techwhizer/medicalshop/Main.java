package com.techwhizer.medicalshop;

import com.techwhizer.medicalshop.controller.auth.Login;
import com.techwhizer.medicalshop.controller.dashboard.Billing;
import com.techwhizer.medicalshop.method.Method;
import com.techwhizer.medicalshop.util.AppConfig;
import com.techwhizer.medicalshop.util.DBConnection;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class Main extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

//        new Timer().schedule(
//                new TimerTask() {
//                    @Override
//                    public void run() {
//                        if(Login.currentlyLogin_Id > 0){
//                            new MyAsyncTask().execute();
//                        }
//                    }
//                }, 0, 10000);

        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("auth/login.fxml")));
      //  Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("dashboard.fxml")));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(AppConfig.APPLICATION_ICON))));
        stage.setTitle(AppConfig.APPLICATION_NAME);
        stage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();

    }

    public void changeScene(String fxml, String title) {

        try {
            if (null != primaryStage && primaryStage.isShowing()) {
                Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
                primaryStage.getScene().setRoot(pane);
                primaryStage.setTitle(AppConfig.APPLICATION_NAME + " ( " + title + " ) ");
                primaryStage.show();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        launch(args);
    }



}
