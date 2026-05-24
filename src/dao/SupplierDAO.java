package dao;

import db.DatabaseConnection;
import model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    
    public boolean create(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContact());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getAddress());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, email = ?, address = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContact());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setInt(5, supplier.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(int id) {
        if (isUsedByProducts(id)) {
            return false;
        }
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isUsedByProducts(int supplierId) {
        String sql = "SELECT COUNT(*) FROM products WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Supplier supplier = new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getString("email"),
                    rs.getString("address")
                );
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }
    
    public Supplier findById(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getString("email"),
                    rs.getString("address")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}