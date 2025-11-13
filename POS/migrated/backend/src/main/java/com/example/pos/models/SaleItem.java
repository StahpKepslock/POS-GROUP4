package com.example.pos.models;

public class SaleItem {
    private final Product product;
    private int quantity;

    public SaleItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductName() {
        return product.getName();
    }

    public double getPrice() {
        return product.getPrice();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}