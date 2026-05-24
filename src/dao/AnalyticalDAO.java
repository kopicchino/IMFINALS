package dao;

import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticalDAO — Query-Oriented MySQL Analytics Engine
 *
 * All SQL queries are declared as public static final String constants so the
 * professor and students can read the exact GROUP BY / HAVING / ORDER BY clauses
 * directly in this source file. The QueryWorkbench panel also renders each string
 * on-screen for in-app visibility.
 *
 * Query index:
 *   1. QUERY_BRAND_LOYALTY          — GROUP BY u.id, b.id  | HAVING total_spend > 5000
 *   2. QUERY_CATEGORY_PERFORMANCE   — GROUP BY c.id        | HAVING avg_selling_price > 500
 *   3. QUERY_AUDIT_LOG_STATISTICS   — GROUP BY actor, type | HAVING total_events >= 1
 *   4. QUERY_TOP_CUSTOMERS_BY_SPEND — GROUP BY u.id        | HAVING total_spend > 0  (ORDER BY DESC)
 *   5. QUERY_TOP_PRODUCTS_BY_REVENUE— GROUP BY p.id        | HAVING total_units_sold > 0
 *   6. QUERY_SEGMENT_DISTRIBUTION   — GROUP BY cp.segment  | HAVING customer_count >= 1
 */
public class AnalyticalDAO {

    // =========================================================================
    // QUERY 1 — Customer Brand Loyalty & Markup Analytics
    //
    // Demonstrates:
    //   JOIN  : Correlates sale_items -> products -> brands -> sales -> users
    //   GROUP BY u.id, b.id : One row per (customer, brand) combination.
    //          Grouping by u.id — NOT u.full_name — ensures two customers
    //          named "May" with different IDs remain separate rows.
    //   HAVING : Dynamically filters groups; only keeps (customer, brand)
    //            pairs where combined spend exceeds PHP 5,000.
    //   ORDER BY total_spend DESC : Highest spenders appear first.
    // =========================================================================
    public static final String QUERY_BRAND_LOYALTY =
        "SELECT u.id AS customer_id, u.full_name, b.name AS brand_name, " +
        "       COUNT(si.id) AS total_items, " +
        "       SUM(si.total_price) AS total_spend, " +
        "       ROUND(AVG(p.markup_percentage), 2) AS avg_markup " +
        "FROM sale_items si " +
        "JOIN products p ON si.product_id = p.id " +
        "JOIN brands b ON p.brand_id = b.id " +
        "JOIN sales s ON si.sale_id = s.id " +
        "JOIN users u ON s.user_id = u.id " +
        "GROUP BY u.id, u.full_name, b.id, b.name " +
        "HAVING total_spend > 5000.00 " +
        "ORDER BY total_spend DESC";

    // =========================================================================
    // QUERY 2 — Category Sales & Stock Performance
    //
    // Demonstrates:
    //   COUNT(DISTINCT p.id) : Counts distinct product models sold (not rows).
    //   SUM / AVG aggregates  : Revenue and average price per category.
    //   GROUP BY c.id         : One row per product category.
    //   HAVING avg_selling_price > 500 : Filters high-value categories only.
    //   ORDER BY total_revenue DESC    : Best-performing category shown first.
    // =========================================================================
    public static final String QUERY_CATEGORY_PERFORMANCE =
        "SELECT c.name AS category_name, " +
        "       COUNT(DISTINCT p.id) AS products_sold, " +
        "       SUM(si.quantity) AS total_quantity, " +
        "       SUM(si.total_price) AS total_revenue, " +
        "       ROUND(AVG(si.unit_price), 2) AS avg_selling_price " +
        "FROM sale_items si " +
        "JOIN products p ON si.product_id = p.id " +
        "JOIN categories c ON p.category_id = c.id " +
        "GROUP BY c.id, c.name " +
        "HAVING avg_selling_price > 500.00 " +
        "ORDER BY total_revenue DESC";

    // =========================================================================
    // QUERY 3 — Security & Fraud Risk Audit Log Statistics
    //
    // Demonstrates:
    //   LEFT JOIN + COALESCE : Handles null actor_id (system-generated events)
    //                          by defaulting the display name to 'System'.
    //   GROUP BY actor, type : Aggregates events per operator per event class.
    //   HAVING total_events >= 1 : Includes all groups (threshold can be raised).
    //   ORDER BY total_events DESC : Most active operators listed first.
    // =========================================================================
    public static final String QUERY_AUDIT_LOG_STATISTICS =
        "SELECT COALESCE(u.full_name, 'System') AS actor, al.event_type, " +
        "       COUNT(al.id) AS total_events, " +
        "       MAX(al.event_time) AS last_action " +
        "FROM audit_logs al " +
        "LEFT JOIN users u ON al.actor_id = u.id " +
        "GROUP BY al.actor_id, al.event_type " +
        "HAVING total_events >= 1 " +
        "ORDER BY total_events DESC";

    // =========================================================================
    // QUERY 4 — Top Customers by Total Spend (Dashboard KPI)
    //
    // KEY PROFESSOR REQUIREMENT:
    //   GROUP BY u.id, u.full_name — using u.id as the primary grouping key
    //   means two customers BOTH named "May" (e.g., May Santos ID=6 and
    //   May Reyes ID=7) will appear as TWO SEPARATE ROWS in the result,
    //   correctly showing their individual spending totals.
    //
    //   If we had used GROUP BY u.full_name instead, both "May" entries
    //   would collapse into one row — a critical data error.
    //
    // Demonstrates:
    //   JOIN         : Links sale_items -> sales -> users in one query.
    //   WHERE        : Pre-filters to CUSTOMER accounts only (excludes admins).
    //   GROUP BY u.id: One aggregated row per unique customer, not per name.
    //   HAVING       : Excludes customers with zero spend (data quality filter).
    //   ORDER BY DESC: Highest-value customer ranks first (Top Customers list).
    //   LIMIT 10     : Returns only the top 10 for dashboard display.
    // =========================================================================
    public static final String QUERY_TOP_CUSTOMERS_BY_SPEND =
        "SELECT u.id AS customer_id, " +
        "       u.full_name, " +
        "       COUNT(DISTINCT s.id) AS total_orders, " +
        "       SUM(si.quantity) AS total_items_bought, " +
        "       SUM(si.total_price) AS total_spend " +
        "FROM sale_items si " +
        "JOIN sales s ON si.sale_id = s.id " +
        "JOIN users u ON s.user_id = u.id " +
        "WHERE u.user_type = 'CUSTOMER' " +
        "GROUP BY u.id, u.full_name " +
        "HAVING total_spend > 0 " +
        "ORDER BY total_spend DESC " +
        "LIMIT 10";

    // =========================================================================
    // QUERY 5 — Top Products by Revenue (Dashboard KPI)
    //
    // Demonstrates:
    //   JOIN (3 tables): Enriches sale_items with product name, category, brand.
    //   SUM(si.quantity)    : Total units sold across all transactions.
    //   SUM(si.total_price) : Total gross revenue generated by each product.
    //   ROUND(AVG(...), 2)  : Average selling price rounded to 2 decimal places.
    //   GROUP BY p.id       : One row per product SKU.
    //   HAVING total_units_sold > 0 : Excludes products with no recorded sales.
    //   ORDER BY total_revenue DESC  : Highest-revenue product ranks first.
    //   LIMIT 10            : Dashboard top-10 list.
    // =========================================================================
    public static final String QUERY_TOP_PRODUCTS_BY_REVENUE =
        "SELECT p.id AS product_id, " +
        "       p.name AS product_name, " +
        "       c.name AS category, " +
        "       b.name AS brand, " +
        "       SUM(si.quantity) AS total_units_sold, " +
        "       SUM(si.total_price) AS total_revenue, " +
        "       ROUND(AVG(si.unit_price), 2) AS avg_selling_price " +
        "FROM sale_items si " +
        "JOIN products p ON si.product_id = p.id " +
        "JOIN categories c ON p.category_id = c.id " +
        "JOIN brands b ON p.brand_id = b.id " +
        "GROUP BY p.id, p.name, c.name, b.name " +
        "HAVING total_units_sold > 0 " +
        "ORDER BY total_revenue DESC " +
        "LIMIT 10";

    // =========================================================================
    // QUERY 6 — Customer Segment Distribution (Dashboard Panel)
    //
    // Demonstrates:
    //   JOIN             : Links customer_profiles -> sales for spend totals.
    //   COUNT(cp.user_id): Customers per lifestyle segment.
    //   SUM(s.total)     : Total spend originating from each segment.
    //   ROUND(AVG(...))  : Average order value per segment.
    //   GROUP BY segment : One row per behavioral segment classification.
    //   HAVING           : Only shows segments with at least 1 customer.
    //   ORDER BY DESC    : Highest-spending segment listed first.
    // =========================================================================
    public static final String QUERY_SEGMENT_DISTRIBUTION =
        "SELECT cp.segment, " +
        "       COUNT(cp.user_id) AS customer_count, " +
        "       SUM(s.total) AS segment_total_spend, " +
        "       ROUND(AVG(s.total), 2) AS avg_order_value " +
        "FROM customer_profiles cp " +
        "JOIN sales s ON cp.user_id = s.user_id " +
        "GROUP BY cp.segment " +
        "HAVING customer_count >= 1 " +
        "ORDER BY segment_total_spend DESC";

    // =========================================================================
    // QUERY 7 — Order Status Monitoring Statistics
    //
    // Demonstrates:
    //   GROUP BY status : Aggregates order volume and total revenue per order state
    //                     (PENDING, COMPLETED, CANCELLED).
    //   HAVING          : Filters status categories with at least 1 order.
    // =========================================================================
    public static final String QUERY_ORDER_STATUS_STATS =
        "SELECT status, " +
        "       COUNT(id) AS order_count, " +
        "       SUM(total) AS total_value, " +
        "       ROUND(AVG(total), 2) AS avg_value " +
        "FROM sales " +
        "GROUP BY status " +
        "HAVING order_count >= 1";

    // =========================================================================
    // QUERY 8 — Loyal Repeat Customers Analytics
    //
    // Demonstrates:
    //   JOIN            : Links users to sales.
    //   GROUP BY u.id   : Aggregates order count and total spend per customer.
    //   HAVING          : Limits to repeat buyers with more than 1 transaction.
    // =========================================================================
    public static final String QUERY_REPEAT_CUSTOMERS =
        "SELECT u.id AS customer_id, " +
        "       u.full_name, " +
        "       COUNT(s.id) AS total_orders, " +
        "       SUM(s.total) AS total_spend, " +
        "       ROUND(AVG(s.total), 2) AS avg_order_value " +
        "FROM sales s " +
        "JOIN users u ON s.user_id = u.id " +
        "GROUP BY u.id, u.full_name " +
        "HAVING total_orders > 1 " +
        "ORDER BY total_orders DESC";

    // =========================================================================
    // QUERY 9 — Category Inventory and Restock Alerts
    //
    // Demonstrates:
    //   JOIN            : Links products -> categories.
    //   GROUP BY c.id   : Groups product items by category.
    //   HAVING          : Alerts when the average stock level in category is < 30.
    // =========================================================================
    public static final String QUERY_PRODUCT_RESTOCK_ALERT =
        "SELECT c.name AS category_name, " +
        "       COUNT(p.id) AS total_products, " +
        "       SUM(p.stock_quantity) AS total_stock, " +
        "       ROUND(AVG(p.stock_quantity), 2) AS avg_stock " +
        "FROM products p " +
        "JOIN categories c ON p.category_id = c.id " +
        "GROUP BY c.id, c.name " +
        "HAVING avg_stock < 30.00 " +
        "ORDER BY avg_stock ASC";

    // =========================================================================
    // Generic Query Execution Helpers
    // =========================================================================

    /**
     * Executes any SQL query string and returns results as a list of ordered maps.
     * Column order is preserved using LinkedHashMap so table columns render
     * in the same sequence as the SELECT clause.
     *
     * @param sql  The full SQL string (use one of the QUERY_ constants above)
     * @return     List of rows; each row is a column-name -> value map
     */
    public List<Map<String, Object>> runQuery(String sql) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                // LinkedHashMap preserves column insertion order
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Returns the ordered column labels for a given SQL query.
     * Used to build JTable column headers before populating rows.
     *
     * @param sql  The full SQL string
     * @return     Ordered list of column label strings
     */
    public List<String> getColumns(String sql) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                columns.add(meta.getColumnLabel(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }
}
