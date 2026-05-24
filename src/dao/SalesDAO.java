package dao;

import db.DatabaseConnection;
import model.Sale;
import model.SaleItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {
    private ProductDAO productDAO = new ProductDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    
    public boolean createSale(Sale sale) {
        return createSale(sale, null);
    }
    
    public boolean createSale(Sale sale, Integer userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // insert sale
            String saleSql = "INSERT INTO sales (user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int saleId = 0;
            try (PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                if (userId != null) {
                    saleStmt.setInt(1, userId);
                } else {
                    saleStmt.setNull(1, Types.INTEGER);
                }
                saleStmt.setTimestamp(2, Timestamp.valueOf(sale.getSaleDate()));
                saleStmt.setBigDecimal(3, sale.getSubtotal());
                saleStmt.setBigDecimal(4, sale.getTax());
                saleStmt.setBigDecimal(5, sale.getDeliveryFee() != null ? sale.getDeliveryFee() : java.math.BigDecimal.ZERO);
                saleStmt.setBigDecimal(6, sale.getTotal());
                saleStmt.setString(7, sale.getCustomerName());
                saleStmt.setString(8, sale.getDeliveryAddress());
                saleStmt.setString(9, sale.getDeliveryPhone());
                saleStmt.setString(10, sale.getStatus() != null ? sale.getStatus() : "COMPLETED");
                saleStmt.executeUpdate();

                try (ResultSet rs = saleStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                        sale.setId(saleId);
                    }
                }
            }
            
            // insert sale items in a batch and update stock using the same connection
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
            String selectProductSql = "SELECT stock_quantity, name FROM products WHERE id = ? FOR UPDATE";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                 PreparedStatement selectProductStmt = conn.prepareStatement(selectProductSql)) {

                java.util.Map<Integer, Integer> currentStockMap = new java.util.HashMap<>();
                java.util.Map<Integer, String> productNameMap = new java.util.HashMap<>();

                for (SaleItem item : sale.getItems()) {
                    int pid = item.getProductId();

                    if (!currentStockMap.containsKey(pid)) {
                        selectProductStmt.setInt(1, pid);
                        try (ResultSet prs = selectProductStmt.executeQuery()) {
                            if (prs.next()) {
                                currentStockMap.put(pid, prs.getInt("stock_quantity"));
                                productNameMap.put(pid, prs.getString("name"));
                            } else {
                                currentStockMap.put(pid, 0);
                                productNameMap.put(pid, "Product ID " + pid);
                            }
                        }
                    }

                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, pid);
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice());
                    itemStmt.setBigDecimal(5, item.getTotalPrice());
                    itemStmt.addBatch();
                }

                // Execute batch inserts for sale items (reduces round trips)
                itemStmt.executeBatch();

                // Now update stock and log changes for each item
                for (SaleItem item : sale.getItems()) {
                    int pid = item.getProductId();
                    int prevStock = currentStockMap.getOrDefault(pid, 0);
                    int newStock = prevStock - item.getQuantity();

                    if (newStock < 0) {
                        newStock = 0;
                    }

                    productDAO.updateStock(conn, pid, newStock);

                    // Log inventory change within the same transaction
                    inventoryDAO.logInventoryChange(conn, pid, "OUT", item.getQuantity(), prevStock, newStock,
                            "Sale #" + saleId);
                }
            }
            
            conn.commit();
            
            if (userId != null) {
                try {
                    new CustomerProfileDAO().reprofileCustomer(userId);
                    new AuditLogDAO().log("TRANSACTION_COMPLETED", userId, "Placed new e-commerce order #" + saleId + " for ₱" + sale.getTotal(), "Real-time purchase profiling updated.", "127.0.0.1");
                } catch (Exception ex) {
                    System.err.println("Real-time profiling failed: " + ex.getMessage());
                }
            }
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Returns the number of distinct registered customers (non-null user_id)
     * who have placed at least one completed order.
     * SQL: SELECT COUNT(DISTINCT user_id) FROM sales WHERE user_id IS NOT NULL
     */
    public int getDistinctCustomerCount() {
        String sql = "SELECT COUNT(DISTINCT user_id) AS cnt FROM sales WHERE user_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Sale> findAll() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sale sale = extractSaleFromResultSet(rs);
                sale.setItems(getSaleItems(sale.getId()));
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    public List<Sale> findByUserId(int userId) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE user_id = ? ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Sale sale = extractSaleFromResultSet(rs);
                sale.setItems(getSaleItems(sale.getId()));
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    public Sale findById(int id) {
        String sql = "SELECT * FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Sale sale = extractSaleFromResultSet(rs);
                sale.setItems(getSaleItems(id));
                return sale;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.name as product_name FROM sale_items si " +
                    "JOIN products p ON si.product_id = p.id WHERE si.sale_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setId(rs.getInt("id"));
                item.setSaleId(rs.getInt("sale_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    private Sale extractSaleFromResultSet(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        sale.setSubtotal(rs.getBigDecimal("subtotal"));
        sale.setTax(rs.getBigDecimal("tax"));
        sale.setDeliveryFee(rs.getBigDecimal("delivery_fee"));
        sale.setTotal(rs.getBigDecimal("total"));
        sale.setCustomerName(rs.getString("customer_name"));
        sale.setDeliveryAddress(rs.getString("delivery_address"));
        sale.setDeliveryPhone(rs.getString("delivery_phone"));
        sale.setStatus(rs.getString("status"));
        int userIdVal = rs.getInt("user_id");
        if (!rs.wasNull()) {
            sale.setUserId(userIdVal);
        }
        return sale;
    }

    public boolean updateSaleStatus(int saleId, String newStatus) {
        String sql = "UPDATE sales SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, saleId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try {
                    Sale sale = findById(saleId);
                    if (sale != null) {
                        Integer userId = sale.getUserId();
                        String details = "Order status updated to " + newStatus + ". Total: PHP " + sale.getTotal();
                        new AuditLogDAO().log(
                            "ORDER_STATUS_UPDATED",
                            userId != null ? userId : 1,
                            "Updated order #" + saleId + " status to " + newStatus,
                            details,
                            "127.0.0.1"
                        );
                    }
                } catch (Exception ex) {
                    System.err.println("Logging order status update failed: " + ex.getMessage());
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}