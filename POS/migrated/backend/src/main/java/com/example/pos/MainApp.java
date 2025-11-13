package com.example.pos;

import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/Main.fxml")));
        
        primaryStage.setTitle("Point of Sale");
        primaryStage.setScene(new Scene(root));
        
        // Maximize the window
        primaryStage.setMaximized(true);
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}