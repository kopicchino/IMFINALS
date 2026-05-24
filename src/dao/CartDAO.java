package dao;

import db.DatabaseConnection;
import model.CartItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    
    public boolean addToCart(int userId, int productId, int quantity) {
        String checkSql = "SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateSql = "UPDATE cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, productId);
                return updateStmt.executeUpdate() > 0;
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, productId);
                insertStmt.setInt(3, quantity);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateQuantity(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }
        
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeFromCart(int userId, int productId) {
        String sql = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.product_id, c.quantity, p.name, p.selling_price, p.stock_quantity " +
                    "FROM cart c " +
                    "JOIN products p ON c.product_id = p.id " +
                    "WHERE c.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("name"));
                
                java.math.BigDecimal unitPrice = rs.getBigDecimal("selling_price");
                if (unitPrice == null) {
                    unitPrice = java.math.BigDecimal.ZERO;
                }
                item.setUnitPrice(unitPrice);
                
                item.setQuantity(rs.getInt("quantity"));
                item.setAvailableStock(rs.getInt("stock_quantity"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getCartItemCount(int userId) {
        String sql = "SELECT SUM(quantity) FROM cart WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}