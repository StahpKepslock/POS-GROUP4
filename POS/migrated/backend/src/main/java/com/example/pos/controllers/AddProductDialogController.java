package com.example.pos.controllers;

import com.example.pos.models.Product;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockField;

    private Stage dialogStage;
    private Product product;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public Product getProduct() {
        return product;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            product = new Product(0, nameField.getText(), Double.parseDouble(priceField.getText()), Integer.parseInt(stockField.getText()));
            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            errorMessage += "No valid product name!\n";
        }
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            errorMessage += "No valid price!\n";
        } else {
            // try to parse the price
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid price (must be a number)!\n";
            }
        }
        if (stockField.getText() == null || stockField.getText().isEmpty()) {
            errorMessage += "No valid stock!\n";
        } else {
            // try to parse the stock
            try {
                Integer.parseInt(stockField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid stock (must be an integer)!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}