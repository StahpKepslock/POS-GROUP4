package com.example.pos.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private LocalDateTime saleDate;
    private List<SaleItem> items;
    private double total;

    /**
     * Constructor for creating a Sale object from database data.
     */
    public Sale(int id, LocalDateTime saleDate, double total) {
        this.id = id;
        this.saleDate = saleDate;
        this.total = total;
        this.items = new ArrayList<>();
    }

    /**
     * Constructor for creating a new sale before saving it to the database.
     */
    public Sale(LocalDateTime saleDate, ArrayList<SaleItem> items) {
        this.saleDate = saleDate;
        this.items = items;
        this.total = calculateTotal();
    }

    private double calculateTotal() {
        return items.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}