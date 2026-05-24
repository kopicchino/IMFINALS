package ui;

import model.User;
import model.CustomerProfile;
import model.CustomerCard;
import model.AuditLog;
import java.math.BigDecimal;
import dao.CustomerProfileDAO;
import dao.CardDAO;
import dao.AuditLogDAO;
import dao.AnalyticalDAO;
import dao.UserDAO;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * MainFrame — Enterprise Admin Shell.
 *
 * Layout:
 *   LEFT  — Dark sidebar with grouped navigation + user profile block
 *   RIGHT — Top header bar + scrollable content area
 *
 * Navigation groups:
 *   OVERVIEW    → Dashboard
 *   CATALOG     → Products, Categories, Brands, Suppliers
 *   OPERATIONS  → Inventory, Sales & Orders
 *   ANALYTICS   → Reports
 *   COMPLIANCE  → Compliance & Safety, Query Workbench
 */
public class MainFrame extends JFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JLabel headerTitleLabel;
    private String currentPanel = "dashboard";

    // Sidebar config: {panelKey, displayLabel, groupHeader (or null)}
    private static final String[][] NAV_ITEMS = {
        {null,              "OVERVIEW",          null},
        {"dashboard",       "Dashboard",         null},
        {null,              "CATALOG",           null},
        {"products",        "Products",          null},
        {"categories",      "Categories",        null},
        {"brands",          "Brands",            null},
        {"suppliers",       "Suppliers",         null},
        {null,              "OPERATIONS",        null},
        {"inventory",       "Inventory",         null},
        {"salesorders",     "Sales & Orders",    null},
        {null,              "ANALYTICS",         null},
        {"reports",         "Reports",           null},
        {null,              "COMPLIANCE",        null},
        {"compliancesafety","Compliance & Safety",null},
        {"queryworkbench",  "Query Workbench",   null},
    };

    // Panel display titles
    private static final java.util.Map<String, String> PANEL_TITLES = new java.util.HashMap<String, String>() {{
        put("dashboard",        "Operations Dashboard");
        put("products",         "Products Management");
        put("categories",       "Categories Management");
        put("brands",           "Brands Management");
        put("suppliers",        "Suppliers Management");
        put("inventory",        "Inventory Management");
        put("salesorders",      "Sales & Orders");
        put("reports",          "Reports & Analytics");
        put("compliancesafety", "Compliance & Safety Protocol");
        put("queryworkbench",   "Interactive Query Workbench");
    }};

    // Query explanations indexed to match the combo box order in QueryWorkbenchPanel
    static final String[] QUERY_EXPLANATIONS = {
        // 0 — Brand Loyalty
        "QUERY 1: Customer Brand Loyalty & Markup Analytics\n\n" +
        "Shows which customers have a strong affinity for a specific brand by grouping purchases by both customer ID and brand.\n\n" +
        "GROUP BY u.id, b.id\n" +
        "  -- Grouping by u.id (not u.full_name) ensures two customers named 'May' remain separate rows.\n\n" +
        "HAVING total_spend > 5000.00\n" +
        "  -- HAVING filters GROUP results (like WHERE but operates AFTER aggregation).\n" +
        "  -- Only keeps (customer, brand) pairs where total combined spend exceeds PHP 5,000.\n\n" +
        "ORDER BY total_spend DESC\n" +
        "  -- Highest brand-loyal spenders appear first.",

        // 1 — Category Performance
        "QUERY 2: Category Sales & Stock Performance\n\n" +
        "Aggregates product sales by category to identify which segments drive the most revenue.\n\n" +
        "COUNT(DISTINCT p.id)\n" +
        "  -- Counts unique product MODELS sold within the category, not total line items.\n\n" +
        "GROUP BY c.id\n" +
        "  -- One summary row per product category.\n\n" +
        "HAVING avg_selling_price > 500.00\n" +
        "  -- Filters out low-value budget categories; focuses on premium segments.\n\n" +
        "ORDER BY total_revenue DESC\n" +
        "  -- Highest-revenue category listed first.",

        // 2 — Audit Log Stats
        "QUERY 3: Security & Fraud Risk Audit Statistics\n\n" +
        "Aggregates compliance audit log events by operator and event type for monitoring.\n\n" +
        "COALESCE(u.full_name, 'System')\n" +
        "  -- Handles NULL actor_id (system-automated events) by defaulting display to 'System'.\n\n" +
        "LEFT JOIN\n" +
        "  -- Includes audit events even when actor_id is NULL (system logs).\n\n" +
        "GROUP BY al.actor_id, al.event_type\n" +
        "  -- One row per operator per event class.\n\n" +
        "HAVING total_events >= 1  -- Includes all groups (threshold adjustable).\n" +
        "ORDER BY total_events DESC  -- Most active operators shown first.",

        // 3 — Top Customers by Spend
        "QUERY 4: Top Customers by Total Spend\n\n" +
        "This is the professor-requested ranking query: shows the highest-value customers sorted by lifetime spend.\n\n" +
        "KEY CONCEPT — Customer Differentiation:\n" +
        "  GROUP BY u.id, u.full_name\n" +
        "  If two customers are BOTH named 'May' (e.g., May Santos ID=6 and May Reyes ID=7),\n" +
        "  grouping by u.id keeps them as TWO SEPARATE ROWS with their own spend totals.\n" +
        "  Grouping by u.full_name alone would incorrectly MERGE them into one row!\n\n" +
        "WHERE u.user_type = 'CUSTOMER'\n" +
        "  -- Pre-filters to exclude admin accounts from the ranking.\n\n" +
        "HAVING total_spend > 0\n" +
        "  -- Excludes registered customers who have never placed an order.\n\n" +
        "ORDER BY total_spend DESC LIMIT 10\n" +
        "  -- Ranks customers from highest to lowest lifetime value.",

        // 4 — Top Products by Revenue
        "QUERY 5: Top Products by Revenue\n\n" +
        "Identifies best-selling products by gross revenue using aggregation across sale line items.\n\n" +
        "SUM(si.quantity) AS total_units_sold\n" +
        "  -- Totals all units of this product sold across every order.\n\n" +
        "SUM(si.total_price) AS total_revenue\n" +
        "  -- Gross revenue = unit price x quantity, summed across all sales.\n\n" +
        "GROUP BY p.id, p.name, c.name, b.name\n" +
        "  -- Primary grouping is p.id (unique SKU); category and brand are included\n" +
        "  -- in GROUP BY because they appear in SELECT but are not aggregated.\n\n" +
        "HAVING total_units_sold > 0\n" +
        "  -- Only shows products with actual recorded sales.\n\n" +
        "ORDER BY total_revenue DESC LIMIT 10\n" +
        "  -- Highest-revenue product ranked first.",

        // 5 — Segment Distribution
        "QUERY 6: Customer Segment Distribution\n\n" +
        "Summarizes lifestyle segment performance — how much each behavioral group contributes to total revenue.\n\n" +
        "COUNT(cp.user_id) AS customer_count\n" +
        "  -- Number of profiled customers within each segment.\n\n" +
        "SUM(s.total) AS segment_total_spend\n" +
        "  -- Combined order value from all customers in the segment.\n\n" +
        "GROUP BY cp.segment\n" +
        "  -- One row per lifestyle segment (e.g. Premium Tech Consumer, Travel Enthusiast).\n\n" +
        "HAVING customer_count >= 1\n" +
        "  -- Only shows segments with at least one active customer.\n\n" +
        "ORDER BY segment_total_spend DESC\n" +
        "  -- Highest-spending segment listed first.",

        // 6 — Order Status Stats
        "QUERY 7: Order Status Monitoring Statistics\n\n" +
        "Aggregates order totals, transaction count, and average value by order status (PENDING, COMPLETED, CANCELLED).\n\n" +
        "GROUP BY status\n" +
        "  -- Summarizes transaction health and status distribution across the system.\n\n" +
        "HAVING order_count >= 1\n" +
        "  -- Filters status groups that contain at least one order record.\n\n" +
        "ORDER BY order_count DESC",

        // 7 — Repeat Customers
        "QUERY 8: Loyal Repeat Customers Analytics\n\n" +
        "Finds and ranks customers who have placed more than 1 transaction, showing their loyalty metrics.\n\n" +
        "GROUP BY u.id, u.full_name\n" +
        "  -- Unique u.id resolves two customers both named 'May' separately.\n\n" +
        "HAVING total_orders > 1\n" +
        "  -- Limits the result specifically to repeat buyers.\n\n" +
        "ORDER BY total_orders DESC",

        // 8 — Category Inventory and Restock Alerts
        "QUERY 9: Category Stock Restock Alerts\n\n" +
        "Groups catalog products by category to flag segments with average stock levels below 30.\n\n" +
        "GROUP BY c.id, c.name\n" +
        "  -- Collapses products into category summaries.\n\n" +
        "HAVING avg_stock < 30.00\n" +
        "  -- Triggers an alert list for product categories requiring stock replenishment."
    };

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("ISMS — Enterprise Admin Console");
        setSize(1440, 900);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        showPanel("dashboard");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main content area
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UIHelper.CONTENT_BG);

        JPanel header = createHeader();
        mainArea.add(header, BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIHelper.CONTENT_BG);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));
        mainArea.add(contentPanel, BorderLayout.CENTER);

        add(mainArea, BorderLayout.CENTER);
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UIHelper.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));

        // Top: Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 18));
        logoPanel.setBackground(UIHelper.SIDEBAR_BG);
        logoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 41, 59)));

        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.PRIMARY_COLOR);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String s = "IS";
                g2.drawString(s, (36 - fm.stringWidth(s)) / 2, 36 / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(36, 36));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setBackground(UIHelper.SIDEBAR_BG);
        JLabel appName = new JLabel("ISMS");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("Admin Console");
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        appSub.setForeground(UIHelper.SIDEBAR_SECTION);
        nameBlock.add(appName);
        nameBlock.add(appSub);

        logoPanel.add(iconCircle);
        logoPanel.add(nameBlock);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Navigation
        JPanel navWrapper = new JPanel();
        navWrapper.setLayout(new BoxLayout(navWrapper, BoxLayout.Y_AXIS));
        navWrapper.setBackground(UIHelper.SIDEBAR_BG);
        navWrapper.setBorder(new EmptyBorder(16, 0, 16, 0));

        for (String[] item : NAV_ITEMS) {
            String key = item[0];
            String label = item[1];
            if (key == null) {
                // Section header
                JLabel sectionLbl = new JLabel("  " + label);
                sectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                sectionLbl.setForeground(UIHelper.SIDEBAR_SECTION);
                sectionLbl.setBorder(new EmptyBorder(14, 20, 6, 20));
                sectionLbl.setMaximumSize(new Dimension(280, 30));
                navWrapper.add(sectionLbl);
            } else {
                navWrapper.add(createNavButton(key, label));
            }
        }

        JScrollPane navScroll = new JScrollPane(navWrapper);
        navScroll.setBorder(BorderFactory.createEmptyBorder());
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.getVerticalScrollBar().setUnitIncrement(12);

        // User profile block at bottom
        JPanel userBlock = createUserBlock();

        sidebar.add(navScroll, BorderLayout.CENTER);
        sidebar.add(userBlock, BorderLayout.SOUTH);
        return sidebar;
    }

    private final java.util.List<JButton> navButtons = new java.util.ArrayList<>();

    private JButton createNavButton(String key, String label) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = key.equals(currentPanel);
                if (active) {
                    // Active: accent left bar + slightly lighter bg
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(UIHelper.PRIMARY_COLOR);
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(UIHelper.SIDEBAR_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setName(key);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(key.equals(currentPanel)
            ? Color.WHITE : new Color(148, 163, 184));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(280, 44));
        btn.setPreferredSize(new Dimension(280, 44));
        btn.setMinimumSize(new Dimension(280, 44));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.addActionListener(e -> showPanel(key));
        navButtons.add(btn);
        return btn;
    }

    private JPanel createUserBlock() {
        JPanel block = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        block.setBackground(new Color(21, 32, 53));
        block.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)));

        // Avatar circle with initials
        String initials = getInitials(currentUser.getFullName());
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.SECONDARY_COLOR);
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (38 - fm.stringWidth(initials)) / 2,
                    38 / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setBackground(new Color(21, 32, 53));

        JLabel name = new JLabel(currentUser.getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Color.WHITE);

        JLabel role = new JLabel("Administrator");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        role.setForeground(UIHelper.SIDEBAR_SECTION);

        nameBlock.add(name);
        nameBlock.add(role);

        // Logout button
        JButton logoutBtn = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(185, 28, 28, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoutBtn.setForeground(new Color(148, 163, 184));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(new EmptyBorder(2, 4, 2, 4));
        logoutBtn.addActionListener(e -> logout());
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { logoutBtn.setForeground(UIHelper.DANGER_COLOR); }
            public void mouseExited(java.awt.event.MouseEvent e)  { logoutBtn.setForeground(new Color(148, 163, 184)); }
        });

        block.add(avatar);
        block.add(nameBlock);
        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        rightBlock.setBackground(new Color(21, 32, 53));
        rightBlock.add(logoutBtn);
        block.add(rightBlock);
        return block;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        }
        return sb.toString().toUpperCase();
    }

    private void updateNavStyles() {
        for (JButton btn : navButtons) {
            boolean active = btn.getName().equals(currentPanel);
            btn.setForeground(active ? Color.WHITE : new Color(148, 163, 184));
            btn.repaint();
        }
    }

    // =========================================================================
    // HEADER
    // =========================================================================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.HEADER_BG);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIHelper.BORDER_COLOR),
            new EmptyBorder(0, 24, 0, 24)
        ));

        headerTitleLabel = new JLabel("Operations Dashboard");
        headerTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        headerTitleLabel.setForeground(UIHelper.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(UIHelper.HEADER_BG);

        // User chip
        JPanel userChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userChip.setBackground(UIHelper.CONTENT_BG);
        userChip.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 20, 1),
            new EmptyBorder(4, 12, 4, 12)
        ));
        JLabel userLbl = new JLabel(currentUser.getFullName());
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel roleLbl = new JLabel(" — Admin");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLbl.setForeground(UIHelper.TEXT_SECONDARY);
        userChip.add(userLbl);
        userChip.add(roleLbl);
        right.add(userChip);

        header.add(headerTitleLabel, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // PANEL ROUTING
    // =========================================================================

    private void showPanel(String panelName) {
        currentPanel = panelName;
        contentPanel.removeAll();

        JPanel panel;
        switch (panelName) {
            case "dashboard":       panel = new DashboardPanel();       break;
            case "products":        panel = new ProductPanel();          break;
            case "categories":      panel = new CategoryPanel();         break;
            case "brands":          panel = new BrandPanel();            break;
            case "suppliers":       panel = new SupplierPanel();         break;
            case "inventory":       panel = new InventoryPanel();        break;
            case "reports":         panel = new ReportsPanel();          break;
            case "salesorders":     panel = new SalesPanel();            break;
            case "compliancesafety":panel = new ComplianceSafetyPanel(); break;
            case "queryworkbench":  panel = new QueryWorkbenchPanel();   break;
            default:                panel = new DashboardPanel();
        }

        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Update header title
        String title = PANEL_TITLES.getOrDefault(panelName, "Dashboard");
        if (headerTitleLabel != null) headerTitleLabel.setText(title);

        updateNavStyles();
    }

    private void logout() {
        if (UIHelper.showConfirm(this, "Are you sure you want to logout?")) {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }

    // =========================================================================
    // NESTED PANEL: COMPLIANCE & SAFETY PROTOCOL
    // =========================================================================

    class ComplianceSafetyPanel extends JPanel {
        private JTable profilesTable, cardsTable, auditsTable;
        private DefaultTableModel profilesModel, cardsModel, auditsModel;
        private CustomerProfileDAO profileDAO = new CustomerProfileDAO();
        private CardDAO cardDAO = new CardDAO();
        private AuditLogDAO auditDAO = new AuditLogDAO();

        public ComplianceSafetyPanel() {
            setLayout(new BorderLayout(0, 16));
            setBackground(UIHelper.CONTENT_BG);

            // Page header
            JPanel hdr = new JPanel(new BorderLayout());
            hdr.setBackground(UIHelper.CONTENT_BG);
            hdr.add(UIHelper.createHeaderLabel("Compliance & Safety Protocol"), BorderLayout.WEST);
            hdr.add(UIHelper.createSecondaryLabel("Customer behavioral analytics and fraud protocol management"), BorderLayout.SOUTH);
            add(hdr, BorderLayout.NORTH);

            // KPI row
            add(buildKpiRow(), BorderLayout.BEFORE_FIRST_LINE);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(UIHelper.SECTION_FONT);
            tabs.setBackground(UIHelper.CONTENT_BG);

            // Tab 1: Profiles
            JPanel tabProfiles = new JPanel(new BorderLayout(0, 10));
            tabProfiles.setBackground(UIHelper.CONTENT_BG);
            tabProfiles.setBorder(new EmptyBorder(12, 0, 0, 0));

            profilesModel = new DefaultTableModel(
                new String[]{"User ID", "Full Name", "Lifestyle Segment", "Behavioral Tags", "DPA Consent", "Risk Score"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            profilesTable = new JTable(profilesModel);
            UIHelper.styleTable(profilesTable);
            UIHelper.applyAlternatingRows(profilesTable);

            JPanel profToolbar = UIHelper.createToolbar();
            JButton recomputeBtn = UIHelper.createPrimaryButton("Run Continuous Analytics");
            recomputeBtn.addActionListener(e -> triggerRecompute());
            profToolbar.add(recomputeBtn);

            tabProfiles.add(profToolbar, BorderLayout.NORTH);
            tabProfiles.add(new JScrollPane(profilesTable), BorderLayout.CENTER);

            // Tab 2: Cards
            JPanel tabCards = new JPanel(new BorderLayout(0, 10));
            tabCards.setBackground(UIHelper.CONTENT_BG);
            tabCards.setBorder(new EmptyBorder(12, 0, 0, 0));

            cardsModel = new DefaultTableModel(
                new String[]{"Card ID", "User ID", "Masked Number", "Card Status"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            cardsTable = new JTable(cardsModel);
            UIHelper.styleTable(cardsTable);
            UIHelper.applyAlternatingRows(cardsTable);
            UIHelper.applyStatusRenderer(cardsTable, 3);

            JPanel cardToolbar = UIHelper.createToolbar();
            JButton lockBtn    = UIHelper.createDangerButton("Freeze Card & Account");
            JButton resolveBtn = UIHelper.createSuccessButton("Resolve / Unlock");
            JButton reviewBtn  = UIHelper.createWarningButton("Move to Under Review");
            lockBtn.addActionListener(e -> lockCardState("Locked", true));
            resolveBtn.addActionListener(e -> lockCardState("Active", false));
            reviewBtn.addActionListener(e -> lockCardState("Under Review", false));
            cardToolbar.add(lockBtn);
            cardToolbar.add(resolveBtn);
            cardToolbar.add(reviewBtn);

            tabCards.add(cardToolbar, BorderLayout.NORTH);
            tabCards.add(new JScrollPane(cardsTable), BorderLayout.CENTER);

            // Tab 3: Audit Trail
            JPanel tabAudits = new JPanel(new BorderLayout(0, 10));
            tabAudits.setBackground(UIHelper.CONTENT_BG);
            tabAudits.setBorder(new EmptyBorder(12, 0, 0, 0));

            auditsModel = new DefaultTableModel(
                new String[]{"Timestamp", "Event Type", "Actor", "Description", "Details", "IP Address"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            auditsTable = new JTable(auditsModel);
            UIHelper.styleTable(auditsTable);
            UIHelper.applyAlternatingRows(auditsTable);

            tabAudits.add(new JScrollPane(auditsTable), BorderLayout.CENTER);

            tabs.addTab("Customer Profiles & Consent", tabProfiles);
            tabs.addTab("Disputes & Record Locking", tabCards);
            tabs.addTab("Immutable Audit Trail", tabAudits);

            add(tabs, BorderLayout.CENTER);
            refreshData();
        }

        private JPanel buildKpiRow() {
            JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
            row.setBackground(UIHelper.CONTENT_BG);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            row.setBorder(new EmptyBorder(0, 0, 8, 0));

            List<CustomerProfile> profiles = profileDAO.findAll();
            List<CustomerCard> cards = cardDAO.findAll();
            List<AuditLog> audits = auditDAO.findAll();

            long lockedCards = cards.stream().filter(c -> "Locked".equalsIgnoreCase(c.getStatus()) ||
                "Reported".equalsIgnoreCase(c.getStatus())).count();

            row.add(makeKpi("Total Profiles", String.valueOf(profiles.size()), UIHelper.GRAD_BLUE));
            row.add(makeKpi("Locked / Reported Cards", String.valueOf(lockedCards), UIHelper.GRAD_ORANGE));
            row.add(makeKpi("Audit Events", String.valueOf(audits.size()), UIHelper.GRAD_VIOLET));
            return row;
        }

        private JPanel makeKpi(String title, String value, Color[] grad) {
            JPanel card = UIHelper.createGradientCard(grad[0], grad[1]);
            card.setLayout(new BorderLayout(0, 4));
            JLabel valLbl = new JLabel(value);
            valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
            valLbl.setForeground(Color.WHITE);
            JLabel ttlLbl = new JLabel(title);
            ttlLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            ttlLbl.setForeground(new Color(255, 255, 255, 200));
            JPanel txt = new JPanel();
            txt.setOpaque(false);
            txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
            txt.add(ttlLbl);
            txt.add(valLbl);
            card.add(txt, BorderLayout.CENTER);
            return card;
        }

        private void refreshData() {
            profilesModel.setRowCount(0);
            List<CustomerProfile> profiles = profileDAO.findAll();
            UserDAO userDAO = new UserDAO();
            for (CustomerProfile cp : profiles) {
                User u = userDAO.findById(cp.getUserId());
                profilesModel.addRow(new Object[]{
                    cp.getUserId(),
                    u != null ? u.getFullName() : "Unknown",
                    cp.getSegment(),
                    cp.getDynamicTags(),
                    cp.isConsentDpa() ? "Granted (PH DPA)" : "Withdrawn",
                    cp.getRiskScore() + "%"
                });
            }

            cardsModel.setRowCount(0);
            List<CustomerCard> cards = cardDAO.findAll();
            for (CustomerCard c : cards) {
                cardsModel.addRow(new Object[]{
                    c.getId(), c.getUserId(), c.getCardNumberMasked(), c.getStatus()
                });
            }

            auditsModel.setRowCount(0);
            List<AuditLog> audits = auditDAO.findAll();
            for (AuditLog al : audits) {
                auditsModel.addRow(new Object[]{
                    al.getEventTime().toString(),
                    al.getEventType(),
                    al.getActorName(),
                    al.getDescription(),
                    al.getDetails(),
                    al.getIpAddress()
                });
            }
        }

        private void triggerRecompute() {
            int row = profilesTable.getSelectedRow();
            if (row < 0) { UIHelper.showError(this, "Select a customer profile first."); return; }
            int userId = (int) profilesTable.getValueAt(row, 0);
            if (profileDAO.reprofileCustomer(userId)) {
                UIHelper.showSuccess(this, "Behavioral reprofiling completed.");
                refreshData();
            } else {
                UIHelper.showError(this, "Analytics blocked — customer has withdrawn DPA Consent.");
            }
        }

        private void lockCardState(String newStatus, boolean applyCascadingLock) {
            int row = cardsTable.getSelectedRow();
            if (row < 0) { UIHelper.showError(this, "Select a card record from the table."); return; }
            int cardId = (int) cardsTable.getValueAt(row, 0);
            int userId = (int) cardsTable.getValueAt(row, 1);
            String msg = "Transition card status to: " + newStatus + "?\n";
            msg += applyCascadingLock
                ? "WARNING: This will lock customer " + userId + "'s portal login simultaneously!"
                : "This will unlock linked customer " + userId + "'s account features.";
            if (UIHelper.showConfirm(this, msg)) {
                if (cardDAO.updateCardStatus(cardId, newStatus, currentUser.getId(), applyCascadingLock)) {
                    UIHelper.showSuccess(this, "Compliance transition resolved successfully.");
                    refreshData();
                } else {
                    UIHelper.showError(this, "Action failed.");
                }
            }
        }
    }

    // =========================================================================
    // NESTED PANEL: INTERACTIVE QUERY WORKBENCH
    // =========================================================================

    class QueryWorkbenchPanel extends JPanel {
        private JTable queryResultTable;
        private DefaultTableModel tableModel;
        private JTextArea sqlTextArea, explanationArea;
        private JList<String> queryList;
        private JLabel rowCountLabel;
        private AnalyticalDAO analyticalDAO = new AnalyticalDAO();

        public QueryWorkbenchPanel() {
            setLayout(new BorderLayout(0, 16));
            setBackground(UIHelper.CONTENT_BG);

            // Page header
            JPanel hdr = new JPanel(new BorderLayout());
            hdr.setBackground(UIHelper.CONTENT_BG);
            hdr.add(UIHelper.createHeaderLabel("Interactive MySQL Query Workbench"), BorderLayout.WEST);
            hdr.add(UIHelper.createSecondaryLabel("Run live analytical queries with GROUP BY, HAVING, and ORDER BY"), BorderLayout.SOUTH);
            add(hdr, BorderLayout.NORTH);

            // Main split: left selector, right SQL + results
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split.setDividerLocation(300);
            split.setDividerSize(6);
            split.setBorder(BorderFactory.createEmptyBorder());

            // ── Left: Query Selector ─────────────────────────────────────────
            JPanel leftCard = UIHelper.createCard();
            leftCard.setLayout(new BorderLayout(0, 12));

            JLabel selectorTitle = UIHelper.createSubHeaderLabel("Analytical Queries");
            leftCard.add(selectorTitle, BorderLayout.NORTH);

            String[] queries = {
                "1. Customer Brand Loyalty",
                "2. Category Sales Performance",
                "3. Security & Fraud Auditing",
                "4. Top Customers by Spend",
                "5. Top Products by Revenue",
                "6. Segment Distribution",
                "7. Order Status Monitoring",
                "8. Loyal Repeat Customers",
                "9. Category Restock Alerts"
            };
            queryList = new JList<>(queries);
            queryList.setFont(UIHelper.NORMAL_FONT);
            queryList.setSelectionBackground(UIHelper.PRIMARY_LIGHT);
            queryList.setSelectionForeground(UIHelper.TEXT_PRIMARY);
            queryList.setBackground(Color.WHITE);
            queryList.setFixedCellHeight(40);
            queryList.setBorder(new EmptyBorder(4, 0, 4, 0));
            queryList.setSelectedIndex(0);
            queryList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateQuerySelection();
            });

            explanationArea = new JTextArea();
            explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            explanationArea.setForeground(UIHelper.TEXT_SECONDARY);
            explanationArea.setLineWrap(true);
            explanationArea.setWrapStyleWord(true);
            explanationArea.setEditable(false);
            explanationArea.setBackground(new Color(248, 250, 252));
            explanationArea.setBorder(new EmptyBorder(10, 10, 10, 10));

            JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(queryList), new JScrollPane(explanationArea));
            leftSplit.setDividerLocation(220);
            leftSplit.setBorder(BorderFactory.createEmptyBorder());
            leftCard.add(leftSplit, BorderLayout.CENTER);

            split.setLeftComponent(leftCard);

            // ── Right: SQL + Results ─────────────────────────────────────────
            JPanel rightCard = UIHelper.createCard();
            rightCard.setLayout(new BorderLayout(0, 12));

            JLabel sqlLabel = new JLabel("SQL Query (MySQL Workbench)");
            sqlLabel.setFont(UIHelper.SECTION_FONT);
            sqlLabel.setForeground(UIHelper.TEXT_SECONDARY);

            sqlTextArea = UIHelper.createQueryPreviewArea("");
            sqlTextArea.setRows(6);
            JScrollPane sqlScroll = new JScrollPane(sqlTextArea);
            sqlScroll.setBorder(BorderFactory.createLineBorder(new Color(30, 41, 59), 1));
            sqlScroll.setPreferredSize(new Dimension(0, 140));

            JPanel sqlSection = new JPanel(new BorderLayout(0, 6));
            sqlSection.setBackground(Color.WHITE);
            sqlSection.add(sqlLabel, BorderLayout.NORTH);
            sqlSection.add(sqlScroll, BorderLayout.CENTER);

            // Run button row
            JPanel runRow = new JPanel(new BorderLayout(10, 0));
            runRow.setBackground(Color.WHITE);
            JButton runBtn = UIHelper.createPrimaryButton("▶  Run Live Query");
            runBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            runBtn.addActionListener(e -> executeQuery());
            rowCountLabel = UIHelper.createSecondaryLabel("Results will appear below");
            runRow.add(runBtn, BorderLayout.WEST);
            runRow.add(rowCountLabel, BorderLayout.CENTER);

            // Results table
            tableModel = new DefaultTableModel();
            queryResultTable = new JTable(tableModel);
            UIHelper.styleTable(queryResultTable);
            UIHelper.applyAlternatingRows(queryResultTable);

            JPanel resultsSection = new JPanel(new BorderLayout(0, 6));
            resultsSection.setBackground(Color.WHITE);
            JLabel resultsTitle = new JLabel("Query Results");
            resultsTitle.setFont(UIHelper.SECTION_FONT);
            resultsTitle.setForeground(UIHelper.TEXT_SECONDARY);
            resultsSection.add(resultsTitle, BorderLayout.NORTH);
            resultsSection.add(new JScrollPane(queryResultTable), BorderLayout.CENTER);

            rightCard.add(sqlSection, BorderLayout.NORTH);
            rightCard.add(runRow, BorderLayout.CENTER);

            JPanel centerRight = new JPanel(new BorderLayout(0, 12));
            centerRight.setBackground(Color.WHITE);
            centerRight.add(runRow, BorderLayout.NORTH);
            centerRight.add(resultsSection, BorderLayout.CENTER);
            rightCard.add(centerRight, BorderLayout.CENTER);

            split.setRightComponent(rightCard);
            add(split, BorderLayout.CENTER);

            updateQuerySelection();
        }

        private void updateQuerySelection() {
            int index = queryList.getSelectedIndex();
            if (index < 0) return;
            tableModel.setColumnCount(0);
            tableModel.setRowCount(0);
            rowCountLabel.setText("Results will appear below");

            String[] querySqls = {
                AnalyticalDAO.QUERY_BRAND_LOYALTY,
                AnalyticalDAO.QUERY_CATEGORY_PERFORMANCE,
                AnalyticalDAO.QUERY_AUDIT_LOG_STATISTICS,
                AnalyticalDAO.QUERY_TOP_CUSTOMERS_BY_SPEND,
                AnalyticalDAO.QUERY_TOP_PRODUCTS_BY_REVENUE,
                AnalyticalDAO.QUERY_SEGMENT_DISTRIBUTION,
                AnalyticalDAO.QUERY_ORDER_STATUS_STATS,
                AnalyticalDAO.QUERY_REPEAT_CUSTOMERS,
                AnalyticalDAO.QUERY_PRODUCT_RESTOCK_ALERT
            };

            if (index < querySqls.length) {
                sqlTextArea.setText(querySqls[index]);
                explanationArea.setText(QUERY_EXPLANATIONS[index]);
            }
        }

        private void executeQuery() {
            String sql = sqlTextArea.getText();
            tableModel.setColumnCount(0);
            tableModel.setRowCount(0);

            List<String> columns = analyticalDAO.getColumns(sql);
            for (String col : columns) tableModel.addColumn(col);

            List<Map<String, Object>> rows = analyticalDAO.runQuery(sql);
            DecimalFormat numDf = new DecimalFormat("#,##0.00");
            for (Map<String, Object> row : rows) {
                Object[] rowData = new Object[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    Object val = row.get(columns.get(i));
                    if (val instanceof BigDecimal) {
                        rowData[i] = "PHP " + numDf.format(val);
                    } else if (val instanceof Double) {
                        rowData[i] = numDf.format(val);
                    } else {
                        rowData[i] = val != null ? val.toString() : "N/A";
                    }
                }
                tableModel.addRow(rowData);
            }

            int count = rows.size();
            rowCountLabel.setText(count == 0
                ? "No rows matched the current threshold filters"
                : count + " row" + (count == 1 ? "" : "s") + " returned");
        }
    }
}