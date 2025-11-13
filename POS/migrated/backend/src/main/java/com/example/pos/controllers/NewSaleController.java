package com.example.pos.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import com.example.pos.models.Product;
import com.example.pos.models.Sale;
import com.example.pos.models.SaleItem;
import com.example.pos.service.DatabaseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class NewSaleController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableView<SaleItem> saleItemsTable;
    @FXML
    private TableColumn<SaleItem, String> saleItemNameColumn;
    @FXML
    private TableColumn<SaleItem, Integer> saleItemQuantityColumn;

    @FXML
    private Label totalLabel;

    private final DatabaseService dbService = new DatabaseService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<SaleItem> saleItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup products table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        loadProducts();

        // Setup sale items table
        saleItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        saleItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        saleItemsTable.setItems(saleItems);

        updateTotal();
    }

    private void loadProducts() {
        productList.setAll(dbService.getAllProducts());
        productTable.setItems(productList);
    }

    @FXML
    private void handleAddProductToSale() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("No Product Selected", "Please select a product to add to the sale.");
            return;
        }

        if (selectedProduct.getStock() <= 0) {
            showAlert("Out of Stock", "This product is out of stock.");
            return;
        }

        Optional<SaleItem> existingItem = saleItems.stream()
                .filter(item -> item.getProduct().getId() == selectedProduct.getId())
                .findFirst();

        if (existingItem.isPresent()) {
            SaleItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
            saleItemsTable.refresh();
        } else {
            saleItems.add(new SaleItem(selectedProduct, 1));
        }

        selectedProduct.setStock(selectedProduct.getStock() - 1);
        productTable.refresh();
        updateTotal();
    }

    @FXML
    private void handleRemoveProductFromSale() {
        SaleItem selectedItem = saleItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Item Selected", "Please select an item to remove from the sale.");
            return;
        }

        Product product = selectedItem.getProduct();
        product.setStock(product.getStock() + selectedItem.getQuantity());
        saleItems.remove(selectedItem);
        
        productTable.refresh();
        updateTotal();
    }

    private void updateTotal() {
        double total = saleItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        totalLabel.setText(String.format("₱%.2f", total));
    }

    @FXML
    private void handleFinalizeSale() {
        if (saleItems.isEmpty()) {
            showAlert("Empty Sale", "Cannot finalize an empty sale.");
            return;
        }

        Sale sale = new Sale(LocalDateTime.now(), new ArrayList<>(saleItems));
        dbService.saveSale(sale);

        showAlert("Sale Finalized", "The sale has been successfully recorded.");
        saleItems.clear();
        loadProducts();
        updateTotal();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (productTable.getScene() != null && productTable.getScene().getWindow() != null) {
            alert.initOwner(productTable.getScene().getWindow());
        }
        alert.showAndWait();
    }
}