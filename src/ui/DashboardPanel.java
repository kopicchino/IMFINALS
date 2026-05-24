package ui;

import dao.AnalyticalDAO;
import dao.ProductDAO;
import dao.SalesDAO;
import model.Product;
import model.Sale;
import util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * DashboardPanel — Clean, icon-driven analytics overview.
 *
 * Layout:
 *   Row 0 — Page header + date + Quick Actions
 *   Row 1 — 5 KPI gradient cards
 *   Row 2 — Top Customers | Top Products
 *   Row 3 — Recent Transactions | Segment Distribution
 *   Row 4 — Order Status Stats | Loyal Repeat Customers
 *
 * SQL query previews have been moved to the Query Workbench panel.
 */
public class DashboardPanel extends JPanel {

    private final ProductDAO    productDAO   = new ProductDAO();
    private final SalesDAO      salesDAO     = new SalesDAO();
    private final AnalyticalDAO analyticsDAO = new AnalyticalDAO();

    private static final DecimalFormat PESO_FMT = new DecimalFormat("#,##0.00");

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.CONTENT_BG);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(UIHelper.CONTENT_BG);
        main.setBorder(new EmptyBorder(0, 0, 24, 0));

        // ── Page Header ────────────────────────────────────────────────────────
        main.add(buildHeader());
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        // ── Row 1: KPI Cards ──────────────────────────────────────────────────
        main.add(buildKpiRow());
        main.add(Box.createRigidArea(new Dimension(0, 24)));

        // ── Row 2: Top Customers | Top Products ───────────────────────────────
        main.add(UIHelper.createSectionDivider("Customer & Product Analytics"));
        main.add(Box.createRigidArea(new Dimension(0, 10)));
        main.add(buildAnalyticsRow());
        main.add(Box.createRigidArea(new Dimension(0, 24)));

        // ── Row 3: Recent Sales | Segment Distribution ────────────────────────
        main.add(UIHelper.createSectionDivider("Transaction Activity & Segment Overview"));
        main.add(Box.createRigidArea(new Dimension(0, 10)));
        main.add(buildActivityRow());
        main.add(Box.createRigidArea(new Dimension(0, 24)));

        // ── Row 4: Order Status | Loyal Repeat Customers ──────────────────────
        main.add(UIHelper.createSectionDivider("Order Monitoring & Loyal Customers"));
        main.add(Box.createRigidArea(new Dimension(0, 10)));
        main.add(buildMonitoringRow());

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // =========================================================================
    // HEADER
    // =========================================================================

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(UIHelper.CONTENT_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Left: title + date
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UIHelper.CONTENT_BG);
        JLabel title = UIHelper.createHeaderLabel("Operations Dashboard");
        JLabel date  = UIHelper.createSecondaryLabel(
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        left.add(title);
        left.add(Box.createRigidArea(new Dimension(0, 2)));
        left.add(date);

        panel.add(left, BorderLayout.WEST);
        return panel;
    }

    // =========================================================================
    // ROW 1 — KPI GRADIENT CARDS
    // =========================================================================

    private JPanel buildKpiRow() {
        List<Product> products = productDAO.findAll();
        List<Sale>    sales    = salesDAO.findAll();
        int totalProducts      = products.size();
        int totalSales         = sales.size();
        int lowStockCount      = productDAO.getLowStockProducts(10).size();
        int customerCount      = salesDAO.getDistinctCustomerCount();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Sale s : sales) totalRevenue = totalRevenue.add(s.getTotal());

        JPanel row = new JPanel(new GridLayout(1, 5, 16, 0));
        row.setBackground(UIHelper.CONTENT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        row.add(buildKpiCard("Total Products",   String.valueOf(totalProducts), "Catalog SKUs",    "\uD83D\uDCE6", UIHelper.GRAD_BLUE));
        row.add(buildKpiCard("Total Orders",     String.valueOf(totalSales),    "Completed sales", "\uD83D\uDED2", UIHelper.GRAD_GREEN));
        row.add(buildKpiCard("Gross Revenue",    "PHP " + PESO_FMT.format(totalRevenue), "All time", "\uD83D\uDCB0", UIHelper.GRAD_VIOLET));
        row.add(buildKpiCard("Low Stock Alerts", String.valueOf(lowStockCount), "Items below 10",  "\u26A0\uFE0F", UIHelper.GRAD_ORANGE));
        row.add(buildKpiCard("Active Customers", String.valueOf(customerCount), "With purchases",  "\uD83D\uDC65", UIHelper.GRAD_TEAL));

        return row;
    }

    private JPanel buildKpiCard(String title, String value, String subtitle, String icon, Color[] grad) {
        JPanel card = UIHelper.createGradientCard(grad[0], grad[1]);
        card.setLayout(new BorderLayout(12, 0));
        card.setPreferredSize(new Dimension(0, 120));

        // Icon
        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(new Color(255, 255, 255, 30));
        iconLbl.setPreferredSize(new Dimension(56, 56));
        iconLbl.setOpaque(false);

        // Text block
        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));

        JLabel ttl = new JLabel(title);
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ttl.setForeground(new Color(255, 255, 255, 210));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 21));
        val.setForeground(Color.WHITE);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 255, 255, 160));

        txt.add(ttl);
        txt.add(Box.createRigidArea(new Dimension(0, 3)));
        txt.add(val);
        txt.add(Box.createRigidArea(new Dimension(0, 2)));
        txt.add(sub);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(txt,     BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // ROW 2 — TOP CUSTOMERS | TOP PRODUCTS
    // =========================================================================

    private JPanel buildAnalyticsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setBackground(UIHelper.CONTENT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        row.add(buildTopCustomersPanel());
        row.add(buildTopProductsPanel());
        return row;
    }

    private JPanel buildTopCustomersPanel() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setBackground(Color.WHITE);
        titleRow.add(UIHelper.createSubHeaderLabel("Top Customers by Spend"), BorderLayout.WEST);
        titleRow.add(UIHelper.createSqlBadge("GROUP BY user_id"), BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Rank", "ID", "Customer", "Orders", "Items", "Total Spend"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Map<String, Object>> rows = analyticsDAO.runQuery(AnalyticalDAO.QUERY_TOP_CUSTOMERS_BY_SPEND);
        int rank = 1;
        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                "#" + rank++,
                row.get("customer_id"),
                row.get("full_name"),
                row.get("total_orders"),
                row.get("total_items_bought"),
                "PHP " + PESO_FMT.format(row.get("total_spend"))
            });
        }
        if (rows.isEmpty()) model.addRow(new Object[]{"--","--","No sales data yet","--","--","--"});

        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.applyAlternatingRows(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(45);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTopProductsPanel() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setBackground(Color.WHITE);
        titleRow.add(UIHelper.createSubHeaderLabel("Top Products by Revenue"), BorderLayout.WEST);
        titleRow.add(UIHelper.createSqlBadge("GROUP BY product_id"), BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Rank", "Product", "Category", "Units", "Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Map<String, Object>> rows = analyticsDAO.runQuery(AnalyticalDAO.QUERY_TOP_PRODUCTS_BY_REVENUE);
        int rank = 1;
        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                "#" + rank++,
                row.get("product_name"),
                row.get("category"),
                row.get("total_units_sold"),
                "PHP " + PESO_FMT.format(row.get("total_revenue"))
            });
        }
        if (rows.isEmpty()) model.addRow(new Object[]{"--","No sales data yet","--","--","--"});

        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.applyAlternatingRows(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // ROW 3 — RECENT SALES | SEGMENT DISTRIBUTION
    // =========================================================================

    private JPanel buildActivityRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setBackground(UIHelper.CONTENT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        row.add(buildRecentSalesPanel());
        row.add(buildSegmentDistributionPanel());
        return row;
    }

    private JPanel buildRecentSalesPanel() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Color.WHITE);
        titleRow.add(UIHelper.createSubHeaderLabel("Recent Transactions"), BorderLayout.WEST);
        JLabel subLbl = UIHelper.createSecondaryLabel("Last 8 orders");
        titleRow.add(subLbl, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        List<Sale> recentSales = salesDAO.findAll();
        String[] cols = {"Order ID", "Date", "Customer", "Status", "Total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        int limit = Math.min(recentSales.size(), 8);
        for (int i = 0; i < limit; i++) {
            Sale s = recentSales.get(i);
            model.addRow(new Object[]{
                "#" + s.getId(),
                s.getSaleDate().format(fmt),
                s.getCustomerName() != null ? s.getCustomerName() : "Walk-in",
                s.getStatus() != null ? s.getStatus() : "COMPLETED",
                "PHP " + PESO_FMT.format(s.getTotal())
            });
        }
        if (recentSales.isEmpty()) model.addRow(new Object[]{"--","--","No transactions yet","--","--"});

        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.applyAlternatingRows(table);
        UIHelper.applyStatusRenderer(table, 3);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSegmentDistributionPanel() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setBackground(Color.WHITE);
        titleRow.add(UIHelper.createSubHeaderLabel("Segment Distribution"), BorderLayout.WEST);
        titleRow.add(UIHelper.createSqlBadge("GROUP BY segment"), BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Segment", "Customers", "Total Spend", "Avg Order"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Map<String, Object>> rows = analyticsDAO.runQuery(AnalyticalDAO.QUERY_SEGMENT_DISTRIBUTION);
        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                row.get("segment"),
                row.get("customer_count"),
                "PHP " + PESO_FMT.format(row.get("segment_total_spend")),
                "PHP " + PESO_FMT.format(row.get("avg_order_value"))
            });
        }
        if (rows.isEmpty()) model.addRow(new Object[]{"No data","--","--","--"});

        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.applyAlternatingRows(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // ROW 4 — ORDER STATUS STATS | LOYAL REPEAT CUSTOMERS
    // =========================================================================

    private JPanel buildMonitoringRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setBackground(UIHelper.CONTENT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        row.add(buildOrderStatusStatsPanel());
        row.add(buildRepeatCustomersPanel());
        return row;
    }

    private JPanel buildOrderStatusStatsPanel() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setBackground(Color.WHITE);
        titleRow.add(UIHelper.createSubHeaderLabel("Order Status Statistics"), BorderLayout.WEST);
        titleRow.add(UIHelper.createSqlBadge("GROUP BY status"), BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Status", "Orders", "Total Value", "Avg Value"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Map<String, Object>> rows = analyticsDAO.runQuery(AnalyticalDAO.QUERY_ORDER_STATUS_STATS);
        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                row.get("status"),
                row.get("order_count"),
                "PHP " + PESO_FMT.format(row.get("total_value")),
                "PHP " + PESO_FMT.format(row.get("avg_value"))
            });
        }
        if (rows.isEmpty()) model.addRow(new Object[]{"No orders registered","--","--","--"});

        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.applyAlternatingRows(table);
        UIHelper.applyStatusRenderer(table, 0);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRepeatCustomersPanel() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setBackground(Color.WHITE);
        titleRow.add(UIHelper.createSubHeaderLabel("Loyal Repeat Customers"), BorderLayout.WEST);
        titleRow.add(UIHelper.createSqlBadge("HAVING orders > 1"), BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Orders", "Total Spent", "Avg Value"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Map<String, Object>> rows = analyticsDAO.runQuery(AnalyticalDAO.QUERY_REPEAT_CUSTOMERS);
        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                row.get("customer_id"),
                row.get("full_name"),
                row.get("total_orders"),
                "PHP " + PESO_FMT.format(row.get("total_spend")),
                "PHP " + PESO_FMT.format(row.get("avg_order_value"))
            });
        }
        if (rows.isEmpty()) model.addRow(new Object[]{"--","No repeat customers yet","--","--","--"});

        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.applyAlternatingRows(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }
}