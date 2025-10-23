package com.example.pos.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.pos.models.Product;
import com.example.pos.models.Sale;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:pos.db";

    private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create products table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS products ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "price REAL NOT NULL,"
                    + "stock INTEGER NOT NULL"
                    + ")");
            }
            
            // Create sales table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS sales ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "date TIMESTAMP NOT NULL,"
                    + "total REAL NOT NULL"
                    + ")");
            }
            
            // Create sale_items table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS sale_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "sale_id INTEGER NOT NULL,"
                    + "product_id INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "FOREIGN KEY (sale_id) REFERENCES sales(id),"
                    + "FOREIGN KEY (product_id) REFERENCES products(id)"
                    + ")");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private Connection getConnection() throws SQLException {
        // Create database file if it doesn't exist
        File dbFile = new File("pos.db");
        if (!dbFile.exists()) {
            initializeDatabase();
        }
        return DriverManager.getConnection(DB_URL);
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStock());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to add product", e);
            throw new RuntimeException("Failed to add product", e);
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
            logger.log(Level.SEVERE, "Failed to get products", e);
            throw new RuntimeException("Failed to get products", e);
        }
        return products;
    }

    public void saveSale(Sale sale) {
        String salesql = "INSERT INTO sales (date, total) VALUES (?, ?)";
        String itemsql = "INSERT INTO sale_items (sale_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement salesStmt = conn.prepareStatement(salesql, Statement.RETURN_GENERATED_KEYS)) {
                salesStmt.setTimestamp(1, Timestamp.valueOf(sale.getDate()));
                salesStmt.setDouble(2, sale.getTotal());
                salesStmt.executeUpdate();

                try (ResultSet generatedKeys = salesStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int saleId = generatedKeys.getInt(1);
                        sale.setId(saleId);
                        try (PreparedStatement itemsStmt = conn.prepareStatement(itemsql)) {
                            for (var item : sale.getItems()) {
                                itemsStmt.setInt(1, saleId);
                                itemsStmt.setInt(2, item.getProduct().getId());
                                itemsStmt.setInt(3, item.getQuantity());
                                itemsStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save sale", e);
            throw new RuntimeException("Failed to save sale", e);
        }
    }
}