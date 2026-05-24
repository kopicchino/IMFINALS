package ui;

import dao.*;
import model.*;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * InventoryPanel — Completely redesigned inventory tracking interface.
 * Introduces dynamic summary cards (Low Stock Alerts, Out of Stock Alerts, 
 * and Total Inventory Value), color-coded logs badge renderers,
 * and visual radio button layout for stock increments/decrements.
 */
public class InventoryPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JSpinner thresholdSpinner;
    
    // KPI summary fields
    private JLabel totalValueVal;
    private JLabel lowStockVal;
    private JLabel outOfStockVal;
    
    public InventoryPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Main North Container
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(UIHelper.CONTENT_BG);
        
        // 1. Header
        JPanel headerPanel = UIHelper.createPageHeader("Inventory Tracking", "Monitor real-time levels, trigger safety stock alerts and adjust counts.");
        northContainer.add(headerPanel);
        northContainer.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // 2. Summary Card Deck (Value, Low Stock, Out of Stock)
        JPanel summaryRow = createSummaryDeck();
        northContainer.add(summaryRow);
        northContainer.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // 3. Control Panel (Threshold + Filter Buttons)
        JPanel controlPanel = createControlPanel();
        northContainer.add(controlPanel);
        northContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        
        add(northContainer, BorderLayout.NORTH);
        
        // Center content card (Table)
        JPanel contentCard = UIHelper.createCard();
        contentCard.setLayout(new BorderLayout(0, 16));
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        
        String[] columns = {"ID", "Product", "Category", "Brand", "Stock Qty", "Cost Price", "Selling Price", "Stock Value", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        inventoryTable = new JTable(tableModel);
        UIHelper.styleTable(inventoryTable);
        UIHelper.applyAlternatingRows(inventoryTable);
        UIHelper.applyStatusRenderer(inventoryTable, 8); // Column 8 is "Status"
        
        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        inventoryTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        inventoryTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        inventoryTable.getColumnModel().getColumn(8).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        contentCard.add(tableContainer, BorderLayout.CENTER);
        
        // Bottom Action buttons
        JPanel actionPanel = createActionPanel();
        contentCard.add(actionPanel, BorderLayout.SOUTH);
        
        add(contentCard, BorderLayout.CENTER);
        
        loadInventory();
    }
    
    private JPanel createSummaryDeck() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        // Card 1: Total Valuation
        JPanel valCard = UIHelper.createCompactCard();
        valCard.setLayout(new BorderLayout(12, 0));
        JLabel valIcon = new JLabel("💰");
        valIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        JPanel valTextPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        valTextPanel.setBackground(Color.WHITE);
        JLabel valTitle = UIHelper.createCaptionLabel("TOTAL STOCK VALUATION");
        totalValueVal = new JLabel("₱0.00");
        totalValueVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalValueVal.setForeground(UIHelper.TEXT_PRIMARY);
        valTextPanel.add(valTitle);
        valTextPanel.add(totalValueVal);
        valCard.add(valIcon, BorderLayout.WEST);
        valCard.add(valTextPanel, BorderLayout.CENTER);
        
        // Card 2: Low Stock Alert
        JPanel lowCard = UIHelper.createCompactCard();
        lowCard.setLayout(new BorderLayout(12, 0));
        JLabel lowIcon = new JLabel("⚠️");
        lowIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        JPanel lowTextPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        lowTextPanel.setBackground(Color.WHITE);
        JLabel lowTitle = UIHelper.createCaptionLabel("LOW STOCK PRODUCTS");
        lowStockVal = new JLabel("0 items");
        lowStockVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lowStockVal.setForeground(new Color(180, 83, 9)); // Amber
        lowTextPanel.add(lowTitle);
        lowTextPanel.add(lowStockVal);
        lowCard.add(lowIcon, BorderLayout.WEST);
        lowCard.add(lowTextPanel, BorderLayout.CENTER);
        
        // Card 3: Out of Stock Alert
        JPanel outCard = UIHelper.createCompactCard();
        outCard.setLayout(new BorderLayout(12, 0));
        JLabel outIcon = new JLabel("🚨");
        outIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        JPanel outTextPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        outTextPanel.setBackground(Color.WHITE);
        JLabel outTitle = UIHelper.createCaptionLabel("OUT OF STOCK");
        outOfStockVal = new JLabel("0 items");
        outOfStockVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        outOfStockVal.setForeground(UIHelper.DANGER_COLOR);
        outTextPanel.add(outTitle);
        outTextPanel.add(outOfStockVal);
        outCard.add(outIcon, BorderLayout.WEST);
        outCard.add(outTextPanel, BorderLayout.CENTER);
        
        panel.add(valCard);
        panel.add(lowCard);
        panel.add(outCard);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setBackground(UIHelper.CONTENT_BG);
        
        leftControls.add(UIHelper.createLabel("Low Stock Alert Threshold:"));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        thresholdSpinner.setPreferredSize(new Dimension(80, 32));
        leftControls.add(thresholdSpinner);
        
        JButton showLowBtn = UIHelper.createPrimaryButton("Show Low Stock Alerts");
        showLowBtn.setPreferredSize(new Dimension(180, 32));
        showLowBtn.addActionListener(e -> showLowStock());
        
        JButton showAllBtn = UIHelper.createSecondaryButton("Show All Inventory");
        showAllBtn.setPreferredSize(new Dimension(160, 32));
        showAllBtn.addActionListener(e -> loadInventory());
        
        leftControls.add(showLowBtn);
        leftControls.add(showAllBtn);
        
        panel.add(leftControls, BorderLayout.WEST);
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        JButton adjustBtn = UIHelper.createSuccessButton("⚡ Adjust Stock Quantity");
        adjustBtn.addActionListener(e -> adjustStock());
        
        JButton viewLogsBtn = UIHelper.createSecondaryButton("📋 View Log History");
        viewLogsBtn.addActionListener(e -> viewInventoryLogs());
        
        panel.add(adjustBtn);
        panel.add(viewLogsBtn);
        
        return panel;
    }
    
    private void loadInventory() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.findAll();
        int threshold = (int) thresholdSpinner.getValue();
        
        double totalValuation = 0.0;
        int lowStockCount = 0;
        int outOfStockCount = 0;
        
        for (Product p : products) {
            int qty = p.getStockQuantity();
            double value = qty * p.getSellingPrice().doubleValue();
            totalValuation += value;
            
            String status;
            if (qty == 0) {
                status = "Out of Stock";
                outOfStockCount++;
            } else if (qty <= threshold) {
                status = "Low Stock";
                lowStockCount++;
            } else {
                status = "In Stock";
            }
            
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                qty,
                "₱" + p.getCostPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + p.getSellingPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + String.format("%.2f", value),
                status
            });
        }
        
        // Update summary numbers
        totalValueVal.setText("₱" + String.format("%,.2f", totalValuation));
        lowStockVal.setText(lowStockCount + " product" + (lowStockCount == 1 ? "" : "s"));
        outOfStockVal.setText(outOfStockCount + " product" + (outOfStockCount == 1 ? "" : "s"));
    }
    
    private void showLowStock() {
        int threshold = (int) thresholdSpinner.getValue();
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getLowStockProducts(threshold);
        
        for (Product p : products) {
            int qty = p.getStockQuantity();
            double value = qty * p.getSellingPrice().doubleValue();
            
            String status = qty == 0 ? "Out of Stock" : "Low Stock";
            
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                qty,
                "₱" + p.getCostPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + p.getSellingPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + String.format("%.2f", value),
                status
            });
        }
    }
    
    private void adjustStock() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a product from the table first");
            return;
        }
        
        int productId = (int) tableModel.getValueAt(row, 0);
        String productName = (String) tableModel.getValueAt(row, 1);
        int currentStock = (int) tableModel.getValueAt(row, 4);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Adjust Stock Level", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        UIHelper.styleDialog(dialog);
        
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = UIHelper.createDialogHeader(
            "Adjust Stock Quantity", 
            "Increase or decrease safety stock levels for physical warehousing.", 
            UIHelper.PRIMARY_COLOR
        );
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.CONTENT_BG);
        formPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        
        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.3;
        formPanel.add(UIHelper.createLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JLabel nameLbl = UIHelper.createLabel(productName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(nameLbl, gbc);
        
        r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.3;
        formPanel.add(UIHelper.createLabel("Current Inventory:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JLabel stockLbl = UIHelper.createLabel(currentStock + " units");
        stockLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(stockLbl, gbc);
        
        r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.3;
        formPanel.add(UIHelper.createLabel("Adjustment Mode:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        
        JRadioButton inRadio = new JRadioButton("Stock-In (Add)");
        JRadioButton outRadio = new JRadioButton("Stock-Out (Deduct)");
        JRadioButton setRadio = new JRadioButton("Absolute Set (Override)");
        
        inRadio.setFont(UIHelper.NORMAL_FONT);
        inRadio.setBackground(UIHelper.CONTENT_BG);
        outRadio.setFont(UIHelper.NORMAL_FONT);
        outRadio.setBackground(UIHelper.CONTENT_BG);
        setRadio.setFont(UIHelper.NORMAL_FONT);
        setRadio.setBackground(UIHelper.CONTENT_BG);
        
        inRadio.setSelected(true);
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(inRadio);
        radioGroup.add(outRadio);
        radioGroup.add(setRadio);
        
        JPanel radioPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        radioPanel.setBackground(UIHelper.CONTENT_BG);
        radioPanel.add(inRadio);
        radioPanel.add(outRadio);
        radioPanel.add(setRadio);
        formPanel.add(radioPanel, gbc);
        
        r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.3;
        formPanel.add(UIHelper.createLabel("Quantity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        qtySpinner.setFont(UIHelper.NORMAL_FONT);
        formPanel.add(qtySpinner, gbc);
        
        r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.3;
        formPanel.add(UIHelper.createLabel("Reason / Notes:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField notesField = UIHelper.createTextField("Enter adjustment reason...");
        formPanel.add(notesField, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        JButton saveBtn = UIHelper.createSuccessButton("Save Adjustment");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            String type = inRadio.isSelected() ? "IN" : outRadio.isSelected() ? "OUT" : "ADJUSTMENT";
            int quantity = (int) qtySpinner.getValue();
            String notes = notesField.getText().trim();
            
            int newStock = currentStock;
            if ("IN".equals(type)) {
                newStock += quantity;
            } else if ("OUT".equals(type)) {
                newStock -= quantity;
                if (newStock < 0) {
                    UIHelper.showError(dialog, "Stock level cannot be less than zero. Adjust your deduct count.");
                    return;
                }
            } else {
                newStock = quantity;
            }
            
            if (productDAO.updateStock(productId, newStock)) {
                inventoryDAO.logInventoryChange(productId, type, quantity, currentStock, newStock, notes.isEmpty() ? "Manual adjustment" : notes);
                UIHelper.showSuccess(this, "Stock adjusted successfully!");
                loadInventory();
                dialog.dispose();
            } else {
                UIHelper.showError(dialog, "Failed to adjust stock. Database write error.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void viewInventoryLogs() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Inventory Action Logs", true);
        dialog.setSize(880, 520);
        dialog.setLocationRelativeTo(this);
        UIHelper.styleDialog(dialog);
        
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = UIHelper.createDialogHeader(
            "Inventory Stocking Logs", 
            "Historical log record of manual edits, stock-ins, sales deductions and overrides.", 
            UIHelper.PRIMARY_COLOR
        );
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBackground(UIHelper.CONTENT_BG);
        container.setBorder(new EmptyBorder(16, 20, 16, 20));
        
        String[] columns = {"Timestamp", "Product", "Type", "Change Qty", "Prev Stock", "New Stock", "Adjustment Note"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        List<InventoryLog> logs = inventoryDAO.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        for (InventoryLog log : logs) {
            model.addRow(new Object[]{
                log.getLogDate().format(formatter),
                log.getProductName(),
                log.getChangeType(),
                log.getQuantity(),
                log.getPreviousStock(),
                log.getNewStock(),
                log.getNotes()
            });
        }
        
        JTable logsTable = new JTable(model);
        UIHelper.styleTable(logsTable);
        UIHelper.applyAlternatingRows(logsTable);
        
        // Style Type column 2 with badge rendering
        logsTable.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                wrapper.setBackground(isSelected ? UIHelper.PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : UIHelper.ROW_ALT));
                if (value != null) {
                    String val = value.toString().toUpperCase();
                    JLabel badge;
                    if ("IN".equals(val)) {
                        badge = UIHelper.createStatusBadge("IN STOCK");
                        badge.setText("IN");
                    } else if ("OUT".equals(val)) {
                        badge = UIHelper.createStatusBadge("OUT OF STOCK");
                        badge.setText("OUT");
                    } else {
                        badge = UIHelper.createStatusBadge("PENDING");
                        badge.setText("ADJUST");
                    }
                    wrapper.add(badge);
                }
                return wrapper;
            }
        });
        
        logsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        logsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        logsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        logsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        logsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        logsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        logsTable.getColumnModel().getColumn(6).setPreferredWidth(200);
        
        JScrollPane scroll = new JScrollPane(logsTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        container.add(scroll, BorderLayout.CENTER);
        
        // OK Close button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setBackground(UIHelper.CONTENT_BG);
        JButton okBtn = UIHelper.createPrimaryButton("Dismiss Logs");
        okBtn.addActionListener(e -> dialog.dispose());
        bottomBar.add(okBtn);
        container.add(bottomBar, BorderLayout.SOUTH);
        
        dialog.add(container, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}