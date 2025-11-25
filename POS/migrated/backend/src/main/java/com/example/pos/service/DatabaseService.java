package com.example.pos.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.pos.models.Product;
import com.example.pos.models.Sale;
import com.example.pos.models.SaleItem;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:pos.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create products table
            String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "stock INTEGER NOT NULL)";
            stmt.execute(createProductsTable);

            // Create sales table
            String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sale_date DATETIME NOT NULL," +
                    "total REAL NOT NULL)";
            stmt.execute(createSalesTable);

            // Create sale_items table
            String createSaleItemsTable = "CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sale_id INTEGER NOT NULL," +
                    "product_id INTEGER NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "price REAL NOT NULL," +
                    "FOREIGN KEY (sale_id) REFERENCES sales(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id))";
            stmt.execute(createSaleItemsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO products(name, price, stock) VALUES(?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, stock = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveSale(Sale sale) {
        String salesSql = "INSERT INTO sales (sale_date, total) VALUES (?, ?)";
        String saleItemsSql = "INSERT INTO sale_items (sale_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Insert into sales table
            try (PreparedStatement salesStmt = conn.prepareStatement(salesSql, Statement.RETURN_GENERATED_KEYS)) {
                salesStmt.setTimestamp(1, Timestamp.valueOf(sale.getSaleDate()));
                salesStmt.setDouble(2, sale.getTotal());
                salesStmt.executeUpdate();

                // Get the generated sale_id
                try (ResultSet generatedKeys = salesStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int saleId = generatedKeys.getInt(1);
                        sale.setId(saleId);

                        // Insert into sale_items table
                        try (PreparedStatement saleItemsStmt = conn.prepareStatement(saleItemsSql)) {
                            for (SaleItem item : sale.getItems()) {
                                saleItemsStmt.setInt(1, saleId);
                                saleItemsStmt.setInt(2, item.getProduct().getId());
                                saleItemsStmt.setInt(3, item.getQuantity());
                                saleItemsStmt.setDouble(4, item.getProduct().getPrice());
                                saleItemsStmt.addBatch();

                                // Update product stock
                                try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)) {
                                    updateStockStmt.setInt(1, item.getQuantity());
                                    updateStockStmt.setInt(2, item.getProduct().getId());
                                    updateStockStmt.executeUpdate();
                                }
                            }
                            saleItemsStmt.executeBatch();
                        }
                    }
                }
            }
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            e.printStackTrace();
            // Consider rolling back transaction here in a real application
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, sale_date, total FROM sales ORDER BY sale_date DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDateTime date = rs.getTimestamp("sale_date").toLocalDateTime();
                double total = rs.getDouble("total");
                sales.add(new Sale(id, date, total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
}