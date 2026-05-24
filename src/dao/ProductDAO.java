package dao;

import db.DatabaseConnection;
import model.Product;
//import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    public boolean create(Product product) {
        String sql = "INSERT INTO products (name, category_id, brand_id, supplier_id, cost_price, " +
                    "markup_percentage, selling_price, stock_quantity, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            product.calculateSellingPrice();
            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getCategoryId());
            pstmt.setInt(3, product.getBrandId());
            pstmt.setInt(4, product.getSupplierId());
            pstmt.setBigDecimal(5, product.getCostPrice());
            pstmt.setBigDecimal(6, product.getMarkupPercentage());
            pstmt.setBigDecimal(7, product.getSellingPrice());
            pstmt.setInt(8, product.getStockQuantity());
            pstmt.setString(9, product.getDescription());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, category_id = ?, brand_id = ?, supplier_id = ?, " +
                    "cost_price = ?, markup_percentage = ?, selling_price = ?, stock_quantity = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            product.calculateSellingPrice();
            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getCategoryId());
            pstmt.setInt(3, product.getBrandId());
            pstmt.setInt(4, product.getSupplierId());
            pstmt.setBigDecimal(5, product.getCostPrice());
            pstmt.setBigDecimal(6, product.getMarkupPercentage());
            pstmt.setBigDecimal(7, product.getSellingPrice());
            pstmt.setInt(8, product.getStockQuantity());
            pstmt.setString(9, product.getDescription());
            pstmt.setInt(10, product.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, b.name as brand_name, s.name as supplier_name " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "LEFT JOIN brands b ON p.brand_id = b.id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                    "ORDER BY p.name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    public Product findById(int id) {
        String sql = "SELECT p.*, c.name as category_name, b.name as brand_name, s.name as supplier_name " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "LEFT JOIN brands b ON p.brand_id = b.id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                    "WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Product> search(String keyword, Integer categoryId, Integer brandId, Integer supplierId) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, c.name as category_name, b.name as brand_name, s.name as supplier_name " +
            "FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "LEFT JOIN brands b ON p.brand_id = b.id " +
            "LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE 1=1"
        );
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND p.name LIKE ?");
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
        }
        if (brandId != null) {
            sql.append(" AND p.brand_id = ?");
        }
        if (supplierId != null) {
            sql.append(" AND p.supplier_id = ?");
        }
        sql.append(" ORDER BY p.name");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword + "%");
            }
            if (categoryId != null) {
                pstmt.setInt(paramIndex++, categoryId);
            }
            if (brandId != null) {
                pstmt.setInt(paramIndex++, brandId);
            }
            if (supplierId != null) {
                pstmt.setInt(paramIndex++, supplierId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    public boolean updateStock(int productId, int newStock) {
        //String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            return updateStock(conn, productId, newStock);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public boolean updateStock(Connection conn, int productId, int newStock) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, b.name as brand_name, s.name as supplier_name " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "LEFT JOIN brands b ON p.brand_id = b.id " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                    "WHERE p.stock_quantity <= ? ORDER BY p.stock_quantity ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, threshold);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setBrandId(rs.getInt("brand_id"));
        product.setSupplierId(rs.getInt("supplier_id"));
        product.setCostPrice(rs.getBigDecimal("cost_price"));
        product.setMarkupPercentage(rs.getBigDecimal("markup_percentage"));
        product.setSellingPrice(rs.getBigDecimal("selling_price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setDescription(rs.getString("description"));
        product.setCategoryName(rs.getString("category_name"));
        product.setBrandName(rs.getString("brand_name"));
        product.setSupplierName(rs.getString("supplier_name"));
        return product;
    }
}