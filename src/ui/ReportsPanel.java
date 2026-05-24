package ui;

import dao.*;
import model.*;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * ReportsPanel — Redesigned advanced analytics and reports generation suite.
 * Replaces outdated plain JTextArea output with high-fidelity, styled interactive
 * JTables and dynamic summary cards. Incorporates direct inline period filters,
 * custom badge renderers, and robust CSV exporters.
 */
public class ReportsPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();
    
    // UI Layout components
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScroll;
    private JPanel summaryDeck;
    private JPanel filterToolbar;
    
    // KPI Summary Card Labels
    private JLabel card1Title, card1Val, card1Icon;
    private JLabel card2Title, card2Val, card2Icon;
    private JLabel card3Title, card3Val, card3Icon;
    
    // Filtering parameters
    private JComboBox<String> periodCombo;
    private JTextField specificDateField;
    private JPanel specificDatePanel;
    private JButton generateBtn;
    
    private String currentReportType = "Product"; // Default active report
    private String currentReportPeriod = "All Time";
    private LocalDate currentSpecificDate = null;
    
    // Left-hand sidebar buttons
    private JButton productReportBtn;
    private JButton salesReportBtn;
    private JButton inventoryReportBtn;
    
    public ReportsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Page Header
        JPanel headerPanel = UIHelper.createPageHeader("System Reports & Analytics", "Generate spreadsheet reports, query sales trends, and inspect inventory status.");
        add(headerPanel, BorderLayout.NORTH);
        
        // Sidebar list panel (Left)
        JPanel sidebarCard = createSidebarPanel();
        
        // Reporting Workbench (Right)
        JPanel workbenchCard = createWorkbenchPanel();
        
        // Split pane to separate selector and preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarCard, workbenchCard);
        splitPane.setDividerLocation(260);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Generate default report on startup
        selectReport("Product");
    }
    
    private JPanel createSidebarPanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(260, 0));
        
        JLabel title = UIHelper.createSubHeaderLabel("Select Report Category");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // Styled Buttons with Emojis
        productReportBtn = UIHelper.createMenuButton("📦 Product Catalog");
        productReportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        productReportBtn.setMaximumSize(new Dimension(240, 48));
        productReportBtn.addActionListener(e -> selectReport("Product"));
        
        salesReportBtn = UIHelper.createMenuButton("📊 Sales & Revenue");
        salesReportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        salesReportBtn.setMaximumSize(new Dimension(240, 48));
        salesReportBtn.addActionListener(e -> selectReport("Sales"));
        
        inventoryReportBtn = UIHelper.createMenuButton("⚠️ Inventory stock-alert");
        inventoryReportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        inventoryReportBtn.setMaximumSize(new Dimension(240, 48));
        inventoryReportBtn.addActionListener(e -> selectReport("Inventory"));
        
        panel.add(productReportBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(salesReportBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(inventoryReportBtn);
        
        // Fill empty area
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createWorkbenchPanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout(0, 16));
        
        // 1. Dynamic Top Configurations (Visible filters / headers)
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(Color.WHITE);
        
        // Filter toolbar for parameters (e.g. Sales dates)
        filterToolbar = createFilterToolbar();
        northContainer.add(filterToolbar);
        northContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // KPI deck for summaries
        summaryDeck = createSummaryDeck();
        northContainer.add(summaryDeck);
        
        panel.add(northContainer, BorderLayout.NORTH);
        
        // 2. Table rendering in the center
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        UIHelper.styleTable(reportTable);
        UIHelper.applyAlternatingRows(reportTable);
        
        tableScroll = new JScrollPane(reportTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);
        
        // 3. Export Actions (Bottom)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        bottomPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        JButton exportBtn = UIHelper.createSuccessButton("📥 Export current view to CSV");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exportBtn.setPreferredSize(new Dimension(240, 40));
        exportBtn.addActionListener(e -> exportCurrentReport());
        bottomPanel.add(exportBtn, BorderLayout.EAST);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFilterToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(Color.WHITE);
        
        bar.add(UIHelper.createSecondaryLabel("Date Range Period:"));
        periodCombo = new JComboBox<>(new String[]{"All Time", "Today", "This Month", "This Year", "Specific Date"});
        UIHelper.styleComboBox(periodCombo);
        periodCombo.setPreferredSize(new Dimension(130, 32));
        periodCombo.addActionListener(e -> {
            boolean specific = "Specific Date".equals(periodCombo.getSelectedItem());
            specificDatePanel.setVisible(specific);
            bar.revalidate();
            bar.repaint();
        });
        bar.add(periodCombo);
        
        specificDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        specificDatePanel.setBackground(Color.WHITE);
        specificDatePanel.setVisible(false);
        specificDatePanel.add(UIHelper.createSecondaryLabel("Date:"));
        specificDateField = UIHelper.createTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        specificDateField.setPreferredSize(new Dimension(100, 32));
        specificDateField.setToolTipText("Format: YYYY-MM-DD");
        specificDatePanel.add(specificDateField);
        bar.add(specificDatePanel);
        
        generateBtn = UIHelper.createPrimaryButton("🔄 Load Report");
        generateBtn.setPreferredSize(new Dimension(130, 32));
        generateBtn.addActionListener(e -> generateActiveReport());
        bar.add(generateBtn);
        
        return bar;
    }
    
    private JPanel createSummaryDeck() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setBackground(Color.WHITE);
        
        // Card 1
        JPanel card1 = UIHelper.createCompactCard();
        card1.setLayout(new BorderLayout(12, 0));
        card1Icon = new JLabel("📝");
        card1Icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JPanel text1 = new JPanel(new GridLayout(2, 1, 2, 2));
        text1.setBackground(Color.WHITE);
        card1Title = UIHelper.createCaptionLabel("METRIC 1");
        card1Val = new JLabel("0");
        card1Val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        text1.add(card1Title);
        text1.add(card1Val);
        card1.add(card1Icon, BorderLayout.WEST);
        card1.add(text1, BorderLayout.CENTER);
        
        // Card 2
        JPanel card2 = UIHelper.createCompactCard();
        card2.setLayout(new BorderLayout(12, 0));
        card2Icon = new JLabel("📊");
        card2Icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JPanel text2 = new JPanel(new GridLayout(2, 1, 2, 2));
        text2.setBackground(Color.WHITE);
        card2Title = UIHelper.createCaptionLabel("METRIC 2");
        card2Val = new JLabel("0");
        card2Val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        text2.add(card2Title);
        text2.add(card2Val);
        card2.add(card2Icon, BorderLayout.WEST);
        card2.add(text2, BorderLayout.CENTER);
        
        // Card 3
        JPanel card3 = UIHelper.createCompactCard();
        card3.setLayout(new BorderLayout(12, 0));
        card3Icon = new JLabel("💰");
        card3Icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JPanel text3 = new JPanel(new GridLayout(2, 1, 2, 2));
        text3.setBackground(Color.WHITE);
        card3Title = UIHelper.createCaptionLabel("METRIC 3");
        card3Val = new JLabel("0");
        card3Val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        text3.add(card3Title);
        text3.add(card3Val);
        card3.add(card3Icon, BorderLayout.WEST);
        card3.add(text3, BorderLayout.CENTER);
        
        panel.add(card1);
        panel.add(card2);
        panel.add(card3);
        
        return panel;
    }
    
    private void selectReport(String type) {
        currentReportType = type;
        
        // Update sidebar button states visually
        Color activeBg = UIHelper.PRIMARY_DARK;
        Color inactiveBg = UIHelper.SIDEBAR_BG;
        
        productReportBtn.setBackground(type.equals("Product") ? activeBg : inactiveBg);
        salesReportBtn.setBackground(type.equals("Sales") ? activeBg : inactiveBg);
        inventoryReportBtn.setBackground(type.equals("Inventory") ? activeBg : inactiveBg);
        
        // Configure parameters toolbar visibility
        // Only Sales needs active date ranges
        filterToolbar.setVisible(type.equals("Sales"));
        
        generateActiveReport();
    }
    
    private void generateActiveReport() {
        if ("Product".equals(currentReportType)) {
            generateProductReport();
        } else if ("Sales".equals(currentReportType)) {
            // Read filter toolbar
            String period = (String) periodCombo.getSelectedItem();
            currentReportPeriod = period;
            
            if ("Specific Date".equals(period)) {
                try {
                    currentSpecificDate = LocalDate.parse(specificDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception ex) {
                    UIHelper.showError(this, "Invalid date format. Please write in YYYY-MM-DD.");
                    specificDateField.requestFocus();
                    return;
                }
            } else {
                currentSpecificDate = null;
            }
            generateSalesReport(currentReportPeriod, currentSpecificDate);
        } else if ("Inventory".equals(currentReportType)) {
            generateInventoryReport();
        }
    }
    
    private void generateProductReport() {
        // Configure Summary deck
        card1Title.setText("TOTAL DISTINCT PRODUCTS");
        card1Icon.setText("📦");
        card2Title.setText("OUT OF STOCK ITEMS");
        card2Icon.setText("🚨");
        card3Title.setText("AGGREGATE INVENTORY VALUE");
        card3Icon.setText("💰");
        
        // Populate Table Model
        tableModel.setColumnCount(0);
        tableModel.setRowCount(0);
        
        String[] cols = {"Product ID", "Item Name", "Category Group", "Stock Count", "Purchase Cost", "Profit Markup %", "Store Retail Price", "Asset Valuation"};
        for (String c : cols) tableModel.addColumn(c);
        
        List<Product> products = productDAO.findAll();
        
        int oosCount = 0;
        double totalVal = 0.0;
        
        for (Product p : products) {
            int stock = p.getStockQuantity();
            if (stock == 0) oosCount++;
            
            double unitPrice = p.getSellingPrice().doubleValue();
            double assetVal = stock * unitPrice;
            totalVal += assetVal;
            
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                stock,
                "₱" + p.getCostPrice().setScale(2, RoundingMode.HALF_UP),
                p.getMarkupPercentage() + "%",
                "₱" + p.getSellingPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + String.format("%,.2f", assetVal)
            });
        }
        
        card1Val.setText(String.valueOf(products.size()));
        card1Val.setForeground(UIHelper.TEXT_PRIMARY);
        card2Val.setText(oosCount + " items");
        card2Val.setForeground(oosCount > 0 ? UIHelper.DANGER_COLOR : UIHelper.SUCCESS_DARK);
        card3Val.setText("₱" + String.format("%,.2f", totalVal));
        card3Val.setForeground(UIHelper.SUCCESS_DARK);
        
        // Reset column status renderers (Not badge styled for general lists)
        reportTable.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (value instanceof Integer) {
                    int val = (Integer) value;
                    if (val == 0) setForeground(UIHelper.DANGER_COLOR);
                    else if (val <= 10) setForeground(new Color(217, 119, 6)); // Warning
                    else setForeground(UIHelper.TEXT_PRIMARY);
                }
                return this;
            }
        });
    }
    
    private void generateSalesReport(String period, LocalDate specificDate) {
        card1Title.setText("TOTAL CHECKOUT ORDERS");
        card1Icon.setText("🧾");
        card2Title.setText("AGGREGATE SALES VOLUME");
        card2Icon.setText("📈");
        card3Title.setText("TOTAL REVENUE SUM");
        card3Icon.setText("💰");
        
        tableModel.setColumnCount(0);
        tableModel.setRowCount(0);
        
        String[] cols = {"Invoice ID", "Sale Timestamp", "Customer Profile", "Items Size", "Subtotal Cost", "VAT Tax (0%)", "Final Checkout", "Checkout Status"};
        for (String c : cols) tableModel.addColumn(c);
        
        List<Sale> allSales = salesDAO.findAll();
        LocalDate today = LocalDate.now();
        List<Sale> filteredSales = new ArrayList<>();
        
        for (Sale sale : allSales) {
            LocalDate saleDate = sale.getSaleDate().toLocalDate();
            boolean include = false;
            
            switch (period) {
                case "Today":
                    include = saleDate.equals(today);
                    break;
                case "This Month":
                    include = saleDate.getMonth() == today.getMonth() && saleDate.getYear() == today.getYear();
                    break;
                case "This Year":
                    include = saleDate.getYear() == today.getYear();
                    break;
                case "Specific Date":
                    include = (specificDate != null) && saleDate.equals(specificDate);
                    break;
                case "All Time":
                default:
                    include = true;
                    break;
            }
            if (include) filteredSales.add(sale);
        }
        
        double totalRev = 0.0;
        int itemVolume = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Sale s : filteredSales) {
            totalRev += s.getTotal().doubleValue();
            for (SaleItem item : s.getItems()) {
                itemVolume += item.getQuantity();
            }
            
            tableModel.addRow(new Object[]{
                "#" + s.getId(),
                s.getSaleDate().format(formatter),
                s.getCustomerName() != null ? s.getCustomerName() : "Walk-in Customer",
                s.getItems().size() + " types",
                "₱" + s.getSubtotal().setScale(2, RoundingMode.HALF_UP),
                "₱" + s.getTax().setScale(2, RoundingMode.HALF_UP),
                "₱" + s.getTotal().setScale(2, RoundingMode.HALF_UP),
                s.getStatus() != null ? s.getStatus().toUpperCase() : "COMPLETED"
            });
        }
        
        card1Val.setText(String.valueOf(filteredSales.size()));
        card1Val.setForeground(UIHelper.TEXT_PRIMARY);
        card2Val.setText(itemVolume + " units sold");
        card2Val.setForeground(UIHelper.PRIMARY_DARK);
        card3Val.setText("₱" + String.format("%,.2f", totalRev));
        card3Val.setForeground(UIHelper.SUCCESS_DARK);
        
        // Apply status cell badge renderer to checkout status (Column 7)
        UIHelper.applyStatusRenderer(reportTable, 7);
    }
    
    private void generateInventoryReport() {
        card1Title.setText("INVENTORY SKUs TOTAL");
        card1Icon.setText("📦");
        card2Title.setText("LOW STOCK ALERTS");
        card2Icon.setText("⚠️");
        card3Title.setText("OUT OF STOCK ALERTS");
        card3Icon.setText("🚨");
        
        tableModel.setColumnCount(0);
        tableModel.setRowCount(0);
        
        String[] cols = {"SKU ID", "Product Description", "Category Group", "Warehoused Qty", "Retail Value", "Stock Status"};
        for (String c : cols) tableModel.addColumn(c);
        
        List<Product> products = productDAO.findAll();
        
        int lowStockCount = 0;
        int outStockCount = 0;
        
        for (Product p : products) {
            int qty = p.getStockQuantity();
            String status;
            if (qty == 0) {
                status = "Out of Stock";
                outStockCount++;
            } else if (qty <= 10) {
                status = "Low Stock";
                lowStockCount++;
            } else {
                status = "In Stock";
            }
            
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                qty + " units",
                "₱" + p.getSellingPrice().setScale(2, RoundingMode.HALF_UP),
                status
            });
        }
        
        card1Val.setText(String.valueOf(products.size()));
        card1Val.setForeground(UIHelper.TEXT_PRIMARY);
        card2Val.setText(lowStockCount + " items");
        card2Val.setForeground(lowStockCount > 0 ? new Color(217, 119, 6) : UIHelper.SUCCESS_DARK);
        card3Val.setText(outStockCount + " items");
        card3Val.setForeground(outStockCount > 0 ? UIHelper.DANGER_COLOR : UIHelper.SUCCESS_DARK);
        
        // Apply status renderer on column 5
        UIHelper.applyStatusRenderer(reportTable, 5);
    }
    
    private void exportCurrentReport() {
        if (tableModel.getRowCount() == 0) {
            UIHelper.showError(this, "There is no report loaded to export. Generate a report first.");
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export CSV Report");
        
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = currentReportType + "_Report_" + timestamp + ".csv";
        chooser.setSelectedFile(new File(filename));
        
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        
        File file = chooser.getSelectedFile();
        
        try {
            if ("Product".equals(currentReportType)) {
                exportProductReportToCSV(file);
            } else if ("Sales".equals(currentReportType)) {
                exportSalesReportToCSV(file);
            } else if ("Inventory".equals(currentReportType)) {
                exportInventoryReportToCSV(file);
            }
            
            UIHelper.showSuccess(this, "Report data successfully exported to file:\n" + file.getAbsolutePath());
            
            // Confirm to open immediately
            int openConfirm = JOptionPane.showConfirmDialog(this,
                    "Exported completed. Would you like to open the CSV spreadsheet file?",
                    "Export Success", JOptionPane.YES_NO_OPTION);
            if (openConfirm == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "An error occurred exporting the report CSV: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void exportProductReportToCSV(File file) throws Exception {
        List<Product> products = productDAO.findAll();
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Product ID,Product Name,Category,Brand,Supplier,Cost Price,Markup %,Selling Price,Stock Quantity,Asset Value\n");
            for (Product p : products) {
                BigDecimal assetVal = p.getSellingPrice().multiply(BigDecimal.valueOf(p.getStockQuantity()));
                writer.append(String.valueOf(p.getId())).append(",")
                      .append("\"").append(p.getName().replace("\"", "\"\"")).append("\",")
                      .append("\"").append(p.getCategoryName()).append("\",")
                      .append("\"").append(p.getBrandName()).append("\",")
                      .append("\"").append(p.getSupplierName()).append("\",")
                      .append(p.getCostPrice().toString()).append(",")
                      .append(p.getMarkupPercentage().toString()).append(",")
                      .append(p.getSellingPrice().toString()).append(",")
                      .append(String.valueOf(p.getStockQuantity())).append(",")
                      .append(assetVal.toString()).append("\n");
            }
            writer.flush();
        }
    }
    
    private void exportSalesReportToCSV(File file) throws Exception {
        List<Sale> allSales = salesDAO.findAll();
        LocalDate today = LocalDate.now();
        List<Sale> filteredSales = new ArrayList<>();
        
        for (Sale sale : allSales) {
            LocalDate saleDate = sale.getSaleDate().toLocalDate();
            boolean include = false;
            
            switch (currentReportPeriod) {
                case "Today":
                    include = saleDate.equals(today);
                    break;
                case "This Month":
                    include = saleDate.getMonth() == today.getMonth() && saleDate.getYear() == today.getYear();
                    break;
                case "This Year":
                    include = saleDate.getYear() == today.getYear();
                    break;
                case "Specific Date":
                    include = (currentSpecificDate != null) && saleDate.equals(currentSpecificDate);
                    break;
                case "All Time":
                default:
                    include = true;
                    break;
            }
            if (include) filteredSales.add(sale);
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Invoice ID,Checkout Date,Customer Name,Items Size,Subtotal,Tax,Final Total,Checkout Status\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Sale s : filteredSales) {
                writer.append(String.valueOf(s.getId())).append(",")
                      .append(s.getSaleDate().format(formatter)).append(",")
                      .append("\"").append(s.getCustomerName().replace("\"", "\"\"")).append("\",")
                      .append(String.valueOf(s.getItems().size())).append(",")
                      .append(s.getSubtotal().toString()).append(",")
                      .append(s.getTax().toString()).append(",")
                      .append(s.getTotal().toString()).append(",")
                      .append(s.getStatus() != null ? s.getStatus() : "COMPLETED").append("\n");
            }
            writer.flush();
        }
    }
    
    private void exportInventoryReportToCSV(File file) throws Exception {
        List<Product> products = productDAO.findAll();
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("SKU ID,Product Name,Category Name,Stock Quantity,Selling Price,Stock Status\n");
            for (Product p : products) {
                int qty = p.getStockQuantity();
                String status = qty == 0 ? "Out of Stock" : qty <= 10 ? "Low Stock" : "In Stock";
                writer.append(String.valueOf(p.getId())).append(",")
                      .append("\"").append(p.getName().replace("\"", "\"\"")).append("\",")
                      .append("\"").append(p.getCategoryName()).append("\",")
                      .append(String.valueOf(p.getStockQuantity())).append(",")
                      .append(p.getSellingPrice().toString()).append(",")
                      .append(status).append("\n");
            }
            writer.flush();
        }
    }
}