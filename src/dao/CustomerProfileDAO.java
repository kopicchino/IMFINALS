package dao;

import db.DatabaseConnection;
import model.CustomerProfile;
import model.TargetedOffer;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerProfileDAO {
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    public CustomerProfile findByUserId(int userId) {
        String sql = "SELECT * FROM customer_profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                CustomerProfile cp = new CustomerProfile();
                cp.setUserId(rs.getInt("user_id"));
                cp.setSegment(rs.getString("segment"));
                cp.setPredictivePreferences(rs.getString("predictive_preferences"));
                cp.setDynamicTags(rs.getString("dynamic_tags"));
                cp.setConsentDpa(rs.getBoolean("consent_dpa"));
                cp.setRiskScore(rs.getBigDecimal("risk_score"));
                cp.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                cp.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return cp;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createProfile(int userId, boolean consentDpa) {
        String sql = "INSERT INTO customer_profiles (user_id, segment, consent_dpa) VALUES (?, 'Standard Consumer', ?) " +
                     "ON DUPLICATE KEY UPDATE consent_dpa = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setBoolean(2, consentDpa);
            pstmt.setBoolean(3, consentDpa);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveConsent(int userId, boolean consentDpa) {
        String sql = "UPDATE customer_profiles SET consent_dpa = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, consentDpa);
            pstmt.setInt(2, userId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                auditLogDAO.log("DPA_CONSENT_CHANGE", userId, "Updated PH Data Privacy Act Consent: " + consentDpa, "Consent state saved.", "127.0.0.1");
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * CONTINUOUS ANALYTICS ROUTINE
     * Uses dynamic GROUP BY and HAVING filters to automatically cluster consumers,
     * assigning lifestyle segment tags and anticipated future need profiles based on transactional historical data.
     */
    public boolean reprofileCustomer(int userId) {
        // Verify Data Privacy Act consent first. If not consented, profiling is blocked!
        CustomerProfile cp = findByUserId(userId);
        if (cp == null || !cp.isConsentDpa()) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // 1. Calculate Apple purchases (Apple brand id query)
            String appleSql = "SELECT SUM(si.total_price) as apple_spend FROM sale_items si " +
                              "JOIN products p ON si.product_id = p.id " +
                              "JOIN sales s ON si.sale_id = s.id " +
                              "WHERE s.user_id = ? AND p.brand_id = (SELECT id FROM brands WHERE name = 'Apple') " +
                              "GROUP BY s.user_id";
            
            BigDecimal appleSpend = BigDecimal.ZERO;
            try (PreparedStatement pstmt = conn.prepareStatement(appleSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        appleSpend = rs.getBigDecimal("apple_spend");
                    }
                }
            }

            // 2. Calculate Travel / Sports gear purchases
            String travelSql = "SELECT SUM(si.quantity) as travel_qty, SUM(si.total_price) as travel_spend FROM sale_items si " +
                               "JOIN products p ON si.product_id = p.id " +
                               "JOIN sales s ON si.sale_id = s.id " +
                               "WHERE s.user_id = ? AND p.category_id = (SELECT id FROM categories WHERE name = 'Sports & Outdoors') " +
                               "GROUP BY s.user_id";
            
            int travelQty = 0;
            BigDecimal travelSpend = BigDecimal.ZERO;
            try (PreparedStatement pstmt = conn.prepareStatement(travelSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        travelQty = rs.getInt("travel_qty");
                        travelSpend = rs.getBigDecimal("travel_spend");
                    }
                }
            }

            // Determine profile classifiers
            String segment = "Standard Consumer";
            List<String> tags = new ArrayList<>();
            String predictiveNeed = "None anticipated at this stage";
            BigDecimal riskScore = BigDecimal.ZERO;

            if (appleSpend.compareTo(new BigDecimal("15000.00")) > 0) {
                segment = "Premium Tech Consumer";
                tags.add("#brand-loyal");
                tags.add("#premium-tech");
                predictiveNeed = "MacBook Pro upgrades, high-end phone cases, Wireless Airbuds Pro accessory bundle";
            } else if (travelQty >= 3 || travelSpend.compareTo(new BigDecimal("5000.00")) > 0) {
                segment = "Travel Enthusiast";
                tags.add("#frequent-traveler");
                tags.add("#outdoor-affinity");
                predictiveNeed = "High-traction trail footwear, travel hydration insulation flasks, anti-theft backpacks";
            }

            // Check if high spender general classifier
            String spendingSql = "SELECT SUM(total) as total_spend FROM sales WHERE user_id = ? AND status = 'COMPLETED'";
            BigDecimal totalSpend = BigDecimal.ZERO;
            try (PreparedStatement pstmt = conn.prepareStatement(spendingSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        totalSpend = rs.getBigDecimal("total_spend");
                        if (totalSpend == null) totalSpend = BigDecimal.ZERO;
                    }
                }
            }

            if (totalSpend.compareTo(new BigDecimal("20000.00")) > 0) {
                tags.add("#high-spender");
            }
            if (tags.isEmpty()) {
                tags.add("#new-shopper");
            }

            // Anomaly Detection: Sudden foreign transaction or massive single cart deviance
            // Flag as risk if single transaction is > 10x their average transaction
            String avgSql = "SELECT AVG(total) as avg_trans FROM sales WHERE user_id = ? AND status = 'COMPLETED' AND id != (SELECT MAX(id) FROM sales WHERE user_id = ?)";
            BigDecimal avgTrans = BigDecimal.ZERO;
            try (PreparedStatement pstmt = conn.prepareStatement(avgSql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        avgTrans = rs.getBigDecimal("avg_trans");
                        if (avgTrans == null) avgTrans = BigDecimal.ZERO;
                    }
                }
            }

            String lastSql = "SELECT total FROM sales WHERE user_id = ? ORDER BY id DESC LIMIT 1";
            BigDecimal lastTrans = BigDecimal.ZERO;
            try (PreparedStatement pstmt = conn.prepareStatement(lastSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        lastTrans = rs.getBigDecimal("total");
                        if (lastTrans == null) lastTrans = BigDecimal.ZERO;
                    }
                }
            }

            if (avgTrans.compareTo(BigDecimal.ZERO) > 0 && lastTrans.compareTo(avgTrans.multiply(new BigDecimal("10"))) > 0) {
                // Potential fraud anomaly flagged!
                riskScore = new BigDecimal("85.00");
                tags.add("#anomaly-detected");
                auditLogDAO.log("ANOMALY_DETECTED", userId, "Sudden high spend transaction deviation detected. Average: ₱" + avgTrans + ", Last: ₱" + lastTrans, "Risk Score updated to 85.00%", "127.0.0.1");
            }

            String tagsCsv = String.join(", ", tags);

            // Update Customer Profile
            String updateSql = "UPDATE customer_profiles SET segment = ?, predictive_preferences = ?, dynamic_tags = ?, risk_score = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, segment);
                pstmt.setString(2, predictiveNeed);
                pstmt.setString(3, tagsCsv);
                pstmt.setBigDecimal(4, riskScore);
                pstmt.setInt(5, userId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomerProfile> findAll() {
        List<CustomerProfile> list = new ArrayList<>();
        String sql = "SELECT cp.*, u.full_name FROM customer_profiles cp JOIN users u ON cp.user_id = u.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CustomerProfile cp = new CustomerProfile();
                cp.setUserId(rs.getInt("user_id"));
                cp.setSegment(rs.getString("segment"));
                cp.setPredictivePreferences(rs.getString("predictive_preferences"));
                cp.setDynamicTags(rs.getString("dynamic_tags"));
                cp.setConsentDpa(rs.getBoolean("consent_dpa"));
                cp.setRiskScore(rs.getBigDecimal("risk_score"));
                list.add(cp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TargetedOffer> findOffersBySegment(String segment) {
        List<TargetedOffer> offers = new ArrayList<>();
        String sql = "SELECT toff.*, p.name as product_name FROM targeted_offers toff " +
                     "JOIN products p ON toff.product_id = p.id " +
                     "WHERE toff.segment = ? AND toff.is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, segment);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TargetedOffer offer = new TargetedOffer();
                offer.setId(rs.getInt("id"));
                offer.setTitle(rs.getString("title"));
                offer.setDescription(rs.getString("description"));
                offer.setSegment(rs.getString("segment"));
                offer.setProductId(rs.getInt("product_id"));
                offer.setProductName(rs.getString("product_name"));
                offer.setPromoCode(rs.getString("promo_code"));
                offer.setTriggerCondition(rs.getString("trigger_condition"));
                offer.setActive(rs.getBoolean("is_active"));
                offers.add(offer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offers;
    }
}
