package com.example.pos.controllers;

import com.example.pos.models.Product;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditProductDialogController {

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

    public void setProduct(Product product) {
        this.product = product;

        nameField.setText(product.getName());
        priceField.setText(Double.toString(product.getPrice()));
        stockField.setText(Integer.toString(product.getStock()));
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            product.setName(nameField.getText());
            product.setPrice(Double.parseDouble(priceField.getText()));
            product.setStock(Integer.parseInt(stockField.getText()));

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

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "No valid product name!\n";
        }
        if (priceField.getText() == null || priceField.getText().length() == 0) {
            errorMessage += "No valid price!\n";
        } else {
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid price (must be a number)!\n";
            }
        }
        if (stockField.getText() == null || stockField.getText().length() == 0) {
            errorMessage += "No valid stock!\n";
        } else {
            try {
                Integer.parseInt(stockField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid stock (must be an integer)!\n";
            }
        }

        if (errorMessage.length() == 0) {
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