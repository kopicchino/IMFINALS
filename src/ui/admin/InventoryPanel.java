package ui.admin;

import dao.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;
import util.UIHelper;

public class InventoryPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JSpinner thresholdSpinner;
    
    public InventoryPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("Inventory Management");
        topPanel.add(headerLabel, BorderLayout.WEST);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(UIHelper.CONTENT_BG);
        controlPanel.add(UIHelper.createLabel("Low Stock Threshold:"));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        thresholdSpinner.setPreferredSize(new Dimension(80, 30));
        controlPanel.add(thresholdSpinner);
        JButton filterBtn = UIHelper.createPrimaryButton("Show Low Stock");
        filterBtn.addActionListener(e -> showLowStock());
        controlPanel.add(filterBtn);
        JButton showAllBtn = UIHelper.createSecondaryButton("Show All");
        showAllBtn.addActionListener(e -> loadInventory());
        controlPanel.add(showAllBtn);
        
        topPanel.add(controlPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadInventory();
    }
    
    private JPanel createTablePanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout());
        
        String[] columns = {"ID", "Product", "Category", "Brand", "Stock", "Cost Price", "Selling Price", "Stock Value", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        inventoryTable = new JTable(tableModel);
        UIHelper.styleTable(inventoryTable);
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JButton adjustBtn = UIHelper.createPrimaryButton("Adjust Stock");
        adjustBtn.addActionListener(e -> adjustStock());
        
        JButton viewLogsBtn = UIHelper.createSecondaryButton("View Logs");
        viewLogsBtn.addActionListener(e -> viewInventoryLogs());
        
        panel.add(adjustBtn);
        panel.add(viewLogsBtn);
        
        return panel;
    }
    
    private void loadInventory() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.findAll();
        int threshold = (int) thresholdSpinner.getValue();
        
        for (Product p : products) {
            String status = p.getStockQuantity() == 0 ? "Out of Stock" : 
                           p.getStockQuantity() <= threshold ? "Low Stock" : "In Stock";
            
            double stockValue = p.getStockQuantity() * p.getSellingPrice().doubleValue();
            
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getStockQuantity(),
                "₱" + p.getCostPrice(),
                "₱" + p.getSellingPrice(),
                "₱" + String.format("%.2f", stockValue),
                status
            });
        }
    }
    
    private void showLowStock() {
        int threshold = (int) thresholdSpinner.getValue();
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getLowStockProducts(threshold);
        
        for (Product p : products) {
            String status = p.getStockQuantity() == 0 ? "Out of Stock" : "Low Stock";
            double stockValue = p.getStockQuantity() * p.getSellingPrice().doubleValue();
            
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getStockQuantity(),
                "₱" + p.getCostPrice(),
                "₱" + p.getSellingPrice(),
                "₱" + String.format("%.2f", stockValue),
                status
            });
        }
    }
    
    private void adjustStock() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a product");
            return;
        }
        
        int productId = (int) tableModel.getValueAt(row, 0);
        String productName = (String) tableModel.getValueAt(row, 1);
        int currentStock = (int) tableModel.getValueAt(row, 4);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Adjust Stock", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(UIHelper.createLabel("Product:"), gbc);
        gbc.gridx = 1;
        panel.add(UIHelper.createLabel(productName), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(UIHelper.createLabel("Current Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(UIHelper.createLabel(String.valueOf(currentStock)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(UIHelper.createLabel("Change Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"IN", "OUT", "ADJUSTMENT"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        panel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(UIHelper.createLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(UIHelper.createLabel("Notes:"), gbc);
        gbc.gridx = 1;
        JTextField notesField = UIHelper.createTextField();
        panel.add(notesField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createSuccessButton("Save");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            int quantity = (int) quantitySpinner.getValue();
            String notes = notesField.getText().trim();
            
            int newStock = currentStock;
            if ("IN".equals(type)) {
                newStock += quantity;
            } else if ("OUT".equals(type)) {
                newStock -= quantity;
                if (newStock < 0) {
                    UIHelper.showError(this, "Insufficient stock");
                    return;
                }
            } else {
                newStock = quantity;
            }
            
            if (productDAO.updateStock(productId, newStock)) {
                inventoryDAO.logInventoryChange(productId, type, quantity, currentStock, newStock, notes);
                UIHelper.showSuccess(this, "Stock adjusted successfully!");
                loadInventory();
                dialog.dispose();
            } else {
                UIHelper.showError(this, "Failed to adjust stock");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void viewInventoryLogs() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Inventory Logs", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Date", "Product", "Type", "Quantity", "Previous", "New", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
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
        panel.add(new JScrollPane(logsTable), BorderLayout.CENTER);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}