package com.example.pos.controllers;

import com.example.pos.models.Product;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Window;

public class AddProductDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;

    private Product product;

    @FXML
    private void initialize() {
    }

    public Product getProduct() {
        return product;
    }

    public void createProduct() {
        product = new Product(nameField.getText(), Double.parseDouble(priceField.getText()), Integer.parseInt(stockField.getText()));
    }

    public boolean isInputValid(Window owner) {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            errorMessage += "No valid product name!\n";
        }
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            errorMessage += "No valid price!\n";
        } else {
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid price (must be a number)!\n";
            }
        }
        if (stockField.getText() == null || stockField.getText().isEmpty()) {
            errorMessage += "No valid stock!\n";
        } else {
            try {
                Integer.parseInt(stockField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid stock (must be an integer)!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show the error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(owner);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}