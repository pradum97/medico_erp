package com.techwhizer.medicalshop;

import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class FileLoader {
    public InputStream load (String path){
      return   getClass().getResourceAsStream(path);
    }

    public static  <T> T loadFxmlFile(String fileName)  {
        try {
            return FXMLLoader.load(Objects.requireNonNull(FileLoader.class.getResource(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
