package com.example.pos.controllers;

import java.io.IOException;
import java.util.Optional;

import com.example.pos.models.Product;
import com.example.pos.service.DatabaseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

    private DatabaseService databaseService;
    private ObservableList<Product> productList;

    public void initialize() {
        databaseService = new DatabaseService();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        loadProducts();
    }

    private void loadProducts() {
        productList = FXCollections.observableArrayList(databaseService.getAllProducts());
        productTable.setItems(productList);
    }

    @FXML
    private void handleNewSale() {
        // TODO: Implement new sale functionality
    }

    @FXML
    private void handleViewSales() {
        // TODO: Implement view sales functionality
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/AddProductDialog.fxml"));
            GridPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Product");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            AddProductDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                Product newProduct = controller.getProduct();
                databaseService.addProduct(newProduct);
                loadProducts(); // Refresh the table
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
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/fxml/EditProductDialog.fxml"));
                GridPane page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Edit Product");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                EditProductDialogController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setProduct(selectedProduct);

                dialogStage.showAndWait();

                if (controller.isOkClicked()) {
                    databaseService.updateProduct(selectedProduct);
                    loadProducts(); // Refresh the table
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product in the table to edit.");
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
            alert.setContentText("Are you sure you want to delete the selected product: " + selectedProduct.getName() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                databaseService.deleteProduct(selectedProduct.getId());
                loadProducts();
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }
    }
}