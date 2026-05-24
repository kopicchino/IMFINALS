package dao;

import db.DatabaseConnection;
import model.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public boolean log(String eventType, Integer actorId, String description, String details, String ipAddress) {
        String sql = "INSERT INTO audit_logs (event_type, actor_id, description, details, ip_address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eventType);
            if (actorId != null) {
                pstmt.setInt(2, actorId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, description);
            pstmt.setString(4, details);
            pstmt.setString(5, ipAddress);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AuditLog> findAll() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name as actor_name FROM audit_logs al " +
                     "LEFT JOIN users u ON al.actor_id = u.id ORDER BY al.event_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setEventTime(rs.getTimestamp("event_time").toLocalDateTime());
                log.setEventType(rs.getString("event_type"));
                
                int actorId = rs.getInt("actor_id");
                if (!rs.wasNull()) {
                    log.setActorId(actorId);
                    log.setActorName(rs.getString("actor_name"));
                } else {
                    log.setActorName("System");
                }
                
                log.setDescription(rs.getString("description"));
                log.setDetails(rs.getString("details"));
                log.setIpAddress(rs.getString("ip_address"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
