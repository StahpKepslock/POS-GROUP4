package com.example.pos.controllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void handleNewSale(ActionEvent event) {
        // TODO: Open new sale window
    }

    @FXML
    private void handleViewSales(ActionEvent event) {
        // TODO: Open sales history window
    }

    /**
     * Handles the action of clicking the "Manage Products" button.
     * Opens the manage products window.
     * @param event The action event.
     */
    @FXML
    private void handleManageProducts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManageProducts.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Manage Products");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}