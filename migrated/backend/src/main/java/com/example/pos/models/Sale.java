package com.example.pos.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private LocalDateTime date;
    private List<SaleItem> items;
    private double total;

    public Sale(int id) {
        this.id = id;
        this.date = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.total = 0.0;
    }

    public void addItem(Product product, int quantity) {
        SaleItem item = new SaleItem(product, quantity);
        items.add(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        total = items.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDate() { return date; }
    public List<SaleItem> getItems() { return items; }
    public double getTotal() { return total; }
}