package ui.admin;

import dao.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;
import util.UIHelper;

public class SalesPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private JTable salesHistoryTable;
    private DefaultTableModel historyTableModel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private List<SaleItem> cartItems = new ArrayList<>();
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private JTextField customerNameField;
    private JComboBox<Product> productCombo;
    private JSpinner quantitySpinner;
    
    public SalesPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("💰 Sales Management");
        add(headerLabel, BorderLayout.NORTH);
        
        // Split panel for new sale and history
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);
        
        // Top: New Sale
        JPanel newSalePanel = createNewSalePanel();
        splitPane.setTopComponent(newSalePanel);
        
        // Bottom: Sales History
        JPanel historyPanel = createSalesHistoryPanel();
        splitPane.setBottomComponent(historyPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        loadSalesHistory();
    }
    
    private JPanel createNewSalePanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout(10, 10));
        
        JLabel title = UIHelper.createSubHeaderLabel("Create New Sale");
        panel.add(title, BorderLayout.NORTH);
        
        // Product selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        selectionPanel.setBackground(Color.WHITE);
        
        productCombo = new JComboBox<>();
        productCombo.setPreferredSize(new Dimension(300, 35));
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            if (p.getStockQuantity() > 0) {
                productCombo.addItem(p);
            }
        }
        
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setPreferredSize(new Dimension(80, 35));
        
        JButton addToCartBtn = new JButton("Add to Cart");
        final Color addCartColor = new Color(40, 167, 69); // Bootstrap green #28a745
        addToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addToCartBtn.setBackground(addCartColor);
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setBorderPainted(false);
        addToCartBtn.setOpaque(true);
        addToCartBtn.setContentAreaFilled(true);
        addToCartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToCartBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addToCartBtn.setBackground(addCartColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addToCartBtn.setBackground(addCartColor);
            }
        });
        addToCartBtn.addActionListener(e -> addToCart());
        
        selectionPanel.add(UIHelper.createLabel("Product:"));
        selectionPanel.add(productCombo);
        selectionPanel.add(UIHelper.createLabel("Quantity:"));
        selectionPanel.add(quantitySpinner);
        selectionPanel.add(addToCartBtn);
        
        panel.add(selectionPanel, BorderLayout.AFTER_LINE_ENDS);
        
        // Cart table
        String[] cartColumns = {"Product", "Quantity", "Unit Price", "Total"};
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        UIHelper.styleTable(cartTable);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setPreferredSize(new Dimension(0, 150));
        panel.add(cartScroll, BorderLayout.CENTER);
        
        // Bottom panel with totals and actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Left: Customer name and buttons
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(UIHelper.createLabel("Customer Name:"));
        customerNameField = UIHelper.createTextField();
        customerNameField.setPreferredSize(new Dimension(200, 35));
        leftPanel.add(customerNameField);
        
        JButton removeBtn = UIHelper.createDangerButton("Remove Item");
        removeBtn.addActionListener(e -> removeFromCart());
        leftPanel.add(removeBtn);
        
        JButton clearBtn = UIHelper.createSecondaryButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());
        leftPanel.add(clearBtn);
        
        // Right: Totals and complete button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        
        subtotalLabel = UIHelper.createLabel("Subtotal: ₱0.00");
        taxLabel = UIHelper.createLabel("Tax (0%): ₱0.00");
        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(UIHelper.SUCCESS_COLOR);
        
        JButton completeSaleBtn = UIHelper.createSuccessButton("Complete Sale");
        completeSaleBtn.addActionListener(e -> completeSale());
        
        rightPanel.add(subtotalLabel);
        rightPanel.add(taxLabel);
        rightPanel.add(totalLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(completeSaleBtn);
        
        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSalesHistoryPanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout(10, 10));
        
        JLabel title = UIHelper.createSubHeaderLabel("Sales History");
        panel.add(title, BorderLayout.NORTH);
        
        String[] columns = {"Sale ID", "Date", "Customer", "Items", "Subtotal", "Tax", "Total"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesHistoryTable = new JTable(historyTableModel);
        UIHelper.styleTable(salesHistoryTable);
        
        JScrollPane scrollPane = new JScrollPane(salesHistoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton viewDetailsBtn = UIHelper.createPrimaryButton("View Details");
        viewDetailsBtn.addActionListener(e -> viewSaleDetails());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(viewDetailsBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void addToCart() {
        Product product = (Product) productCombo.getSelectedItem();
        if (product == null) {
            UIHelper.showError(this, "Please select a product");
            return;
        }
        
        int quantity = (int) quantitySpinner.getValue();
        if (quantity > product.getStockQuantity()) {
            UIHelper.showError(this, "Insufficient stock. Available: " + product.getStockQuantity());
            return;
        }
        
        // Check if product already in cart
        for (SaleItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                updateCartDisplay();
                return;
            }
        }
        
        SaleItem item = new SaleItem(product.getId(), product.getName(), quantity, product.getSellingPrice());
        cartItems.add(item);
        updateCartDisplay();
    }
    
    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cartItems.remove(row);
            updateCartDisplay();
        } else {
            UIHelper.showError(this, "Please select an item to remove");
        }
    }
    
    private void clearCart() {
        if (UIHelper.showConfirm(this, "Clear all items from cart?")) {
            cartItems.clear();
            updateCartDisplay();
        }
    }
    
    private void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (SaleItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                "₱" + item.getUnitPrice(),
                "₱" + item.getTotalPrice()
            });
            subtotal = subtotal.add(item.getTotalPriceSafe());
        }
        
        BigDecimal tax = BigDecimal.ZERO; // 0% tax for simplicity
        BigDecimal total = subtotal.add(tax);
        
        subtotalLabel.setText("Subtotal: ₱" + subtotal.toString());
        taxLabel.setText("Tax (0%): ₱" + tax.toString());
        totalLabel.setText("Total: ₱" + total.toString());
    }
    
    private void completeSale() {
        if (cartItems.isEmpty()) {
            UIHelper.showError(this, "Cart is empty");
            return;
        }
        
        Sale sale = new Sale();
        sale.setCustomerName(customerNameField.getText().trim());
        sale.setItems(new ArrayList<>(cartItems));
        sale.setTax(BigDecimal.ZERO);
        sale.calculateTotals();
        
        if (salesDAO.createSale(sale)) {
            UIHelper.showSuccess(this, "Sale completed successfully!");
            clearCart();
            customerNameField.setText("");
            loadSalesHistory();
            
            // Refresh product combo to update stock
            productCombo.removeAllItems();
            List<Product> products = productDAO.findAll();
            for (Product p : products) {
                if (p.getStockQuantity() > 0) {
                    productCombo.addItem(p);
                }
            }
        } else {
            UIHelper.showError(this, "Failed to complete sale");
        }
    }
    
    private void loadSalesHistory() {
        historyTableModel.setRowCount(0);
        List<Sale> sales = salesDAO.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        for (Sale sale : sales) {
            historyTableModel.addRow(new Object[]{
                sale.getId(),
                sale.getSaleDate().format(formatter),
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in",
                sale.getItems().size() + " items",
                "₱" + sale.getSubtotal(),
                "₱" + sale.getTax(),
                "₱" + sale.getTotal()
            });
        }
    }
    
    private void viewSaleDetails() {
        int row = salesHistoryTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select a sale to view details");
            return;
        }
        
        int saleId = (int) historyTableModel.getValueAt(row, 0);
        Sale sale = salesDAO.findById(saleId);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sale Details", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header info
        JPanel headerPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(UIHelper.createLabel("Sale ID:"));
        headerPanel.add(UIHelper.createLabel(String.valueOf(sale.getId())));
        headerPanel.add(UIHelper.createLabel("Date:"));
        headerPanel.add(UIHelper.createLabel(sale.getSaleDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
        headerPanel.add(UIHelper.createLabel("Customer:"));
        headerPanel.add(UIHelper.createLabel(sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in"));
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Items table
        String[] columns = {"Product", "Quantity", "Unit Price", "Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (SaleItem item : sale.getItems()) {
            model.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                "₱" + item.getUnitPrice(),
                "₱" + item.getTotalPrice()
            });
        }
        JTable itemsTable = new JTable(model);
        UIHelper.styleTable(itemsTable);
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        
        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2));
        totalsPanel.setBackground(Color.WHITE);
        totalsPanel.add(UIHelper.createLabel("Subtotal:"));
        totalsPanel.add(UIHelper.createLabel("₱" + sale.getSubtotal()));
        totalsPanel.add(UIHelper.createLabel("Tax:"));
        totalsPanel.add(UIHelper.createLabel("₱" + sale.getTax()));
        totalsPanel.add(new JLabel("Total:"));
        JLabel totalLbl = new JLabel("₱" + sale.getTotal());
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLbl.setForeground(UIHelper.SUCCESS_COLOR);
        totalsPanel.add(totalLbl);
        panel.add(totalsPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}