package dao;

import db.DatabaseConnection;
import model.Brand;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {
    public boolean create(Brand brand) {
        String sql = "INSERT INTO brands (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, brand.getName());
            pstmt.setString(2, brand.getDescription());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(Brand brand) {
        String sql = "UPDATE brands SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, brand.getName());
            pstmt.setString(2, brand.getDescription());
            pstmt.setInt(3, brand.getId());
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
        String sql = "DELETE FROM brands WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isUsedByProducts(int brandId) {
        String sql = "SELECT COUNT(*) FROM products WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, brandId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Brand> findAll() {
        List<Brand> brands = new ArrayList<>();
        String sql = "SELECT * FROM brands ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Brand brand = new Brand(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")
                );
                brands.add(brand);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brands;
    }
    
    public Brand findById(int id) {
        String sql = "SELECT * FROM brands WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Brand(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
