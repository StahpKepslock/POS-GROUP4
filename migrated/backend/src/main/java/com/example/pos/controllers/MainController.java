package com.example.pos.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

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

    @FXML
    private void handleManageProducts(ActionEvent event) {
        // TODO: Open product management window
    }
}