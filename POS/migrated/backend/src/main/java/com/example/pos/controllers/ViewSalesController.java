package com.example.pos.controllers;

import java.time.LocalDateTime;

import com.example.pos.models.Sale;
import com.example.pos.service.DatabaseService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ViewSalesController {

    @FXML
    private TableView<Sale> salesTable;
    @FXML
    private TableColumn<Sale, Integer> saleIdColumn;
    @FXML
    private TableColumn<Sale, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<Sale, Double> totalColumn;

    private DatabaseService dbService = new DatabaseService();

    @FXML
    public void initialize() {
        saleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        salesTable.setItems(FXCollections.observableArrayList(dbService.getAllSales()));
    }
}