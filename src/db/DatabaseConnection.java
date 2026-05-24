package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    
    // Return a new Connection for each call. This avoids issues where
    // one DAO's use of try-with-resources closes a shared connection
    // while another DAO still expects it to be open.
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Deprecated: no-op since connections are created per-call. Kept for
    // backward compatibility if other code invokes it.
    public static void closeConnection() {
        // No global connection to close when using per-call connections.
    }
}