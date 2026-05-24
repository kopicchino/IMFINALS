package dao;

import db.DatabaseConnection;
import model.CustomerCard;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDAO {
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    public List<CustomerCard> findByUserId(int userId) {
        List<CustomerCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM customer_cards WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CustomerCard card = new CustomerCard();
                card.setId(rs.getInt("id"));
                card.setUserId(rs.getInt("user_id"));
                card.setCardNumberToken(rs.getString("card_number_token"));
                card.setCardNumberMasked(rs.getString("card_number_masked"));
                card.setStatus(rs.getString("status"));
                card.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                card.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    public List<CustomerCard> findAll() {
        List<CustomerCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM customer_cards";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CustomerCard card = new CustomerCard();
                card.setId(rs.getInt("id"));
                card.setUserId(rs.getInt("user_id"));
                card.setCardNumberToken(rs.getString("card_number_token"));
                card.setCardNumberMasked(rs.getString("card_number_masked"));
                card.setStatus(rs.getString("status"));
                card.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                card.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    public boolean addCard(int userId, String cardNum) {
        if (cardNum == null || cardNum.length() < 12) return false;
        
        String masked = cardNum.substring(0, 4) + "xxxxxxxx" + cardNum.substring(cardNum.length() - 4);
        String token = "tok_" + cardNum.substring(0, 4) + "xxxxxx" + cardNum.substring(cardNum.length() - 4) + "_" + System.currentTimeMillis();
        
        String sql = "INSERT INTO customer_cards (user_id, card_number_token, card_number_masked, status) VALUES (?, ?, ?, 'Active')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setString(3, masked);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                auditLogDAO.log("CARD_ADDED", userId, "Linked new tokenized payment card " + masked, "Token generated: " + token, "127.0.0.1");
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCardStatus(int cardId, String status, int actorId, boolean applyCascadingLock) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get user_id and card details first
            int userId = 0;
            String maskedCard = "";
            String selectSql = "SELECT user_id, card_number_masked FROM customer_cards WHERE id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, cardId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                        maskedCard = rs.getString("card_number_masked");
                    }
                }
            }

            if (userId == 0) {
                conn.rollback();
                return false;
            }

            // Update card status
            String updateCardSql = "UPDATE customer_cards SET status = ? WHERE id = ?";
            try (PreparedStatement updateCardStmt = conn.prepareStatement(updateCardSql)) {
                updateCardStmt.setString(1, status);
                updateCardStmt.setInt(2, cardId);
                updateCardStmt.executeUpdate();
            }

            // Cascading Lock Rules: If card is reported lost/compromised/stolen, restrict the linked customer account!
            boolean lockedAccount = false;
            if (applyCascadingLock && ("Reported".equalsIgnoreCase(status) || "Locked".equalsIgnoreCase(status))) {
                String lockUserSql = "UPDATE users SET is_locked = TRUE WHERE id = ?";
                try (PreparedStatement lockUserStmt = conn.prepareStatement(lockUserSql)) {
                    lockUserStmt.setInt(1, userId);
                    lockUserStmt.executeUpdate();
                    lockedAccount = true;
                }
            } else if ("Active".equalsIgnoreCase(status) || "Resolved/Replaced".equalsIgnoreCase(status)) {
                // If resolving dispute, unlock the user account
                String unlockUserSql = "UPDATE users SET is_locked = FALSE WHERE id = ?";
                try (PreparedStatement unlockUserStmt = conn.prepareStatement(unlockUserSql)) {
                    unlockUserStmt.setInt(1, userId);
                    unlockUserStmt.executeUpdate();
                }
            }

            conn.commit();

            // Log details in our immutable trail
            String logDesc = "Card " + maskedCard + " status changed to " + status;
            if (lockedAccount) {
                logDesc += " (Cascading Lock Restricted Linked User Account)";
            }
            auditLogDAO.log("CARD_STATUS_UPDATE", actorId, logDesc, "Card ID: " + cardId + ", Target User ID: " + userId + ", Cascaded: " + lockedAccount, "127.0.0.1");

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
