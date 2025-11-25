package com.example.pos.controllers;

import java.io.IOException;
import java.util.Optional;

import com.example.pos.models.Product;
import com.example.pos.service.DatabaseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ManageProductsController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;

    private ObservableList<Product> productData = FXCollections.observableArrayList();
    private DatabaseService databaseService;

    @FXML
    private void initialize() {
        databaseService = new DatabaseService();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        loadProductData();
    }

    @FXML
    private void handleNewSale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewSale.fxml"));
            Stage stage = new Stage();
            stage.setTitle("New Sale");
            stage.setScene(new Scene(loader.load()));
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    stage.close();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewSales() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewSales.fxml"));
            Stage stage = new Stage();
            stage.setTitle("View Sales");
            stage.setScene(new Scene(loader.load()));
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    stage.close();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProductDialog.fxml"));
            DialogPane page = loader.load();
            AddProductDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(page);
            dialog.setTitle("Add New Product");

            // Prevent dialog from closing on invalid input
            final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.isInputValid(dialog.getDialogPane().getScene().getWindow())) {
                    event.consume();
                } else {
                    controller.createProduct();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                databaseService.addProduct(controller.getProduct());
                loadProductData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProductDialog.fxml"));
                GridPane page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Edit Product");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(productTable.getScene().getWindow());
                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                EditProductDialogController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setProduct(selectedProduct);

                dialogStage.showAndWait();

                if (controller.isOkClicked()) {
                    databaseService.updateProduct(selectedProduct);
                    loadProductData();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Product");
            alert.setContentText("Are you sure you want to delete the selected product?");

            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        databaseService.deleteProduct(selectedProduct.getId());
                        productData.remove(selectedProduct);
                    });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            Stage currentStage = (Stage) productTable.getScene().getWindow();
            Scene currentScene = currentStage.getScene();

            boolean darkThemeEnabled = false;
            String darkThemeUrlString = getClass().getResource("/dark-theme.css").toExternalForm();
            if(currentScene.getStylesheets().contains(darkThemeUrlString)) {
                darkThemeEnabled = true;
            }

            // Load the main menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Point of Sale");
            Scene newScene = new Scene(root);

            if (darkThemeEnabled) {
                newScene.getStylesheets().add(darkThemeUrlString);
            }
            
            stage.setScene(newScene);
            stage.setMaximized(true);
            stage.show();

            // Close the current window
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProductData() {
        productData.setAll(databaseService.getAllProducts());
        productTable.setItems(productData);
    }
}