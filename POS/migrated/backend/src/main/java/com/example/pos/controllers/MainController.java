package com.example.pos.controllers;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Button toggleThemeButton;

    @FXML
    private void handleToggleTheme(ActionEvent event) {
        Scene scene = rootPane.getScene();
        if (scene != null) {
            URL darkThemeUrl = getClass().getResource("/dark-theme.css");
            if (darkThemeUrl != null) {
                String darkTheme = darkThemeUrl.toExternalForm();
                if (scene.getStylesheets().contains(darkTheme)) {
                    scene.getStylesheets().remove(darkTheme);
                } else {
                    scene.getStylesheets().add(darkTheme);
                }
            } else {
                System.err.println("Could not find dark-theme.css");
            }
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }

    private void applyCurrentTheme(Stage stage) {
        Scene mainScene = rootPane.getScene();
        if (mainScene != null && stage.getScene() != null) {
            URL darkThemeUrl = getClass().getResource("/dark-theme.css");
            if (darkThemeUrl != null) {
                String darkTheme = darkThemeUrl.toExternalForm();
                if (mainScene.getStylesheets().contains(darkTheme)) {
                    stage.getScene().getStylesheets().add(darkTheme);
                }
            }
        }
    }

    @FXML
    private void handleNewSale(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewSale.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("New Sale");
            stage.setScene(new Scene(root));
            
            applyCurrentTheme(stage);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewSales(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewSales.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("View Sales");
            stage.setScene(new Scene(root));

            applyCurrentTheme(stage);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageProducts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManageProducts.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Manage Products");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

            applyCurrentTheme(stage);

            stage.show();

            // Close the main window
            Stage mainStage = (Stage) rootPane.getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}