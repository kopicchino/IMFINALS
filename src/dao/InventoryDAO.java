package dao;

import db.DatabaseConnection;
import model.InventoryLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    
    public boolean logInventoryChange(int productId, String changeType, int quantity, 
                                     int previousStock, int newStock, String notes) {
        String sql = "INSERT INTO inventory_logs (product_id, change_type, quantity, previous_stock, new_stock, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setString(2, changeType);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, previousStock);
            pstmt.setInt(5, newStock);
            pstmt.setString(6, notes);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logInventoryChange(Connection conn, int productId, String changeType, int quantity,
                                      int previousStock, int newStock, String notes) throws SQLException {
        String sql = "INSERT INTO inventory_logs (product_id, change_type, quantity, previous_stock, new_stock, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setString(2, changeType);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, previousStock);
            pstmt.setInt(5, newStock);
            pstmt.setString(6, notes);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<InventoryLog> findAll() {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name FROM inventory_logs il " +
                    "JOIN products p ON il.product_id = p.id ORDER BY il.log_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    public List<InventoryLog> findByProduct(int productId) {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name FROM inventory_logs il " +
                    "JOIN products p ON il.product_id = p.id WHERE il.product_id = ? ORDER BY il.log_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    private InventoryLog extractLogFromResultSet(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog();
        log.setId(rs.getInt("id"));
        log.setProductId(rs.getInt("product_id"));
        log.setProductName(rs.getString("product_name"));
        log.setChangeType(rs.getString("change_type"));
        log.setQuantity(rs.getInt("quantity"));
        log.setPreviousStock(rs.getInt("previous_stock"));
        log.setNewStock(rs.getInt("new_stock"));
        log.setNotes(rs.getString("notes"));
        log.setLogDate(rs.getTimestamp("log_date").toLocalDateTime());
        return log;
    }
}