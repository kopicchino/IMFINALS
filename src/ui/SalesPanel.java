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
import java.util.ArrayList;
import java.util.List;

/**
 * SalesPanel — Completely redesigned POS sales panel.
 * Replaces the awkward split pane with a structured JTabbedPane:
 * Tab 1: "Create New Sale" (modern 2-column layout with product picking and live cart summary)
 * Tab 2: "Sales History & Orders" (detailed historical monitoring with status badges)
 * Includes a beautiful invoice/receipt modal for sale logs.
 */
public class SalesPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();
    
    // Tab components
    private JTabbedPane tabbedPane;
    
    // Create Sale Tab components
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private List<SaleItem> cartItems = new ArrayList<>();
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private JTextField customerNameField;
    private JComboBox<Product> productCombo;
    private JSpinner quantitySpinner;
    
    // Sales History Tab components
    private JTable salesHistoryTable;
    private DefaultTableModel historyTableModel;
    private JComboBox<String> statusFilterCombo;
    
    public SalesPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Page Header
        JPanel headerPanel = UIHelper.createPageHeader("Sales & Orders", "Process checkouts, view transaction history, and manage order statuses.");
        add(headerPanel, BorderLayout.NORTH);
        
        // Modern Styled JTabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.SUBHEADER_FONT);
        
        // Tab 1: New Sale
        JPanel newSalePanel = createNewSaleTab();
        tabbedPane.addTab("🛒 Create New Sale", newSalePanel);
        
        // Tab 2: Sales History
        JPanel historyPanel = createSalesHistoryTab();
        tabbedPane.addTab("📋 Sales History", historyPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        loadSalesHistory();
    }
    
    private JPanel createNewSaleTab() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UIHelper.CONTENT_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        // Left Column: Selection form
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(UIHelper.CONTENT_BG);
        leftPanel.setPreferredSize(new Dimension(340, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1.0;
        
        JPanel pickCard = UIHelper.createCard();
        pickCard.setLayout(new GridBagLayout());
        GridBagConstraints pcGbc = new GridBagConstraints();
        pcGbc.fill = GridBagConstraints.HORIZONTAL;
        pcGbc.insets = new Insets(6, 12, 6, 12);
        pcGbc.weightx = 1.0;
        
        JLabel pickTitle = UIHelper.createSubHeaderLabel("Add Items to Order");
        pcGbc.gridx = 0; pcGbc.gridy = 0; pcGbc.gridwidth = 2;
        pickCard.add(pickTitle, pcGbc);
        pcGbc.gridwidth = 1;
        
        pcGbc.gridx = 0; pcGbc.gridy = 1; pcGbc.weightx = 0.2;
        pickCard.add(UIHelper.createLabel("Product:"), pcGbc);
        pcGbc.gridx = 1; pcGbc.weightx = 0.8;
        productCombo = new JComboBox<>();
        UIHelper.styleComboBox(productCombo);
        productCombo.setPreferredSize(new Dimension(200, 36));
        loadProductCombo();
        pickCard.add(productCombo, pcGbc);
        
        pcGbc.gridx = 0; pcGbc.gridy = 2; pcGbc.weightx = 0.2;
        pickCard.add(UIHelper.createLabel("Qty:"), pcGbc);
        pcGbc.gridx = 1; pcGbc.weightx = 0.8;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        quantitySpinner.setFont(UIHelper.NORMAL_FONT);
        quantitySpinner.setPreferredSize(new Dimension(200, 36));
        pickCard.add(quantitySpinner, pcGbc);
        
        pcGbc.gridx = 0; pcGbc.gridy = 3; pcGbc.gridwidth = 2; pcGbc.weightx = 1.0;
        JButton addToCartBtn = UIHelper.createPrimaryButton("➕ Add Product to Cart");
        addToCartBtn.addActionListener(e -> addToCart());
        pickCard.add(addToCartBtn, pcGbc);
        pcGbc.gridwidth = 1;
        
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(pickCard, gbc);
        
        // Customer Details Card
        JPanel custCard = UIHelper.createCard();
        custCard.setLayout(new GridBagLayout());
        GridBagConstraints ccGbc = new GridBagConstraints();
        ccGbc.fill = GridBagConstraints.HORIZONTAL;
        ccGbc.insets = new Insets(6, 12, 6, 12);
        ccGbc.weightx = 1.0;
        
        JLabel custTitle = UIHelper.createSubHeaderLabel("Customer Information");
        ccGbc.gridx = 0; ccGbc.gridy = 0; ccGbc.gridwidth = 2;
        custCard.add(custTitle, ccGbc);
        ccGbc.gridwidth = 1;
        
        ccGbc.gridx = 0; ccGbc.gridy = 1; ccGbc.weightx = 0.3;
        custCard.add(UIHelper.createLabel("Customer:"), ccGbc);
        ccGbc.gridx = 1; ccGbc.weightx = 0.7;
        customerNameField = UIHelper.createTextField("Enter customer name (optional)");
        customerNameField.setPreferredSize(new Dimension(160, 36));
        custCard.add(customerNameField, ccGbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        leftPanel.add(custCard, gbc);
        
        // Fill remaining height
        gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 1.0;
        leftPanel.add(Box.createGlue(), gbc);
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // Right Column: Cart details
        JPanel rightCard = UIHelper.createCard();
        rightCard.setLayout(new BorderLayout(0, 16));
        
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setBackground(Color.WHITE);
        JLabel cartTitle = UIHelper.createSubHeaderLabel("Active Shopping Cart Items");
        cartHeader.add(cartTitle, BorderLayout.WEST);
        
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        cartActions.setBackground(Color.WHITE);
        JButton removeBtn = UIHelper.createDangerButton("Remove Item");
        removeBtn.addActionListener(e -> removeFromCart());
        JButton clearBtn = UIHelper.createSecondaryButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());
        cartActions.add(removeBtn);
        cartActions.add(clearBtn);
        cartHeader.add(cartActions, BorderLayout.EAST);
        
        rightCard.add(cartHeader, BorderLayout.NORTH);
        
        // Cart Table
        String[] cartColumns = {"Product Name", "Quantity Requested", "Unit Price", "Total Sum"};
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        UIHelper.styleTable(cartTable);
        UIHelper.applyAlternatingRows(cartTable);
        
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.getViewport().setBackground(Color.WHITE);
        rightCard.add(cartScroll, BorderLayout.CENTER);
        
        // Checkout section at the bottom of the card
        JPanel checkoutPanel = new JPanel(new BorderLayout());
        checkoutPanel.setBackground(Color.WHITE);
        checkoutPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        // Summary pricing panel
        JPanel pricingPanel = new JPanel(new GridBagLayout());
        pricingPanel.setBackground(Color.WHITE);
        pricingPanel.setBorder(new EmptyBorder(16, 0, 16, 0));
        GridBagConstraints pGbc = new GridBagConstraints();
        pGbc.fill = GridBagConstraints.HORIZONTAL;
        pGbc.insets = new Insets(4, 8, 4, 8);
        pGbc.weightx = 1.0;
        
        pGbc.gridx = 0; pGbc.gridy = 0; pGbc.weightx = 0.5;
        subtotalLabel = UIHelper.createLabel("Subtotal:");
        pricingPanel.add(subtotalLabel, pGbc);
        pGbc.gridx = 1; pGbc.weightx = 0.5; pGbc.anchor = GridBagConstraints.EAST;
        taxLabel = UIHelper.createLabel("Tax (0.00%): ₱0.00");
        pricingPanel.add(taxLabel, pGbc);
        
        pGbc.gridx = 0; pGbc.gridy = 1; pGbc.gridwidth = 2; pGbc.weightx = 1.0; pGbc.insets = new Insets(10, 8, 4, 8);
        totalLabel = new JLabel("Total Due: ₱0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(UIHelper.SUCCESS_DARK);
        pricingPanel.add(totalLabel, pGbc);
        
        checkoutPanel.add(pricingPanel, BorderLayout.CENTER);
        
        JButton completeSaleBtn = UIHelper.createSuccessButton("⚡ Complete Checkout & Record Sale");
        completeSaleBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        completeSaleBtn.setPreferredSize(new Dimension(0, 48));
        completeSaleBtn.addActionListener(e -> completeSale());
        checkoutPanel.add(completeSaleBtn, BorderLayout.SOUTH);
        
        rightCard.add(checkoutPanel, BorderLayout.SOUTH);
        panel.add(rightCard, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSalesHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UIHelper.CONTENT_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        JPanel contentCard = UIHelper.createCard();
        contentCard.setLayout(new BorderLayout(0, 16));
        
        // Toolbar for filtering
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(Color.WHITE);
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilter.setBackground(Color.WHITE);
        leftFilter.add(UIHelper.createSecondaryLabel("Filter Status:"));
        
        statusFilterCombo = new JComboBox<>(new String[]{"All", "Pending", "Completed", "Cancelled"});
        UIHelper.styleComboBox(statusFilterCombo);
        statusFilterCombo.setPreferredSize(new Dimension(150, 36));
        statusFilterCombo.addActionListener(e -> loadSalesHistory());
        leftFilter.add(statusFilterCombo);
        toolbarPanel.add(leftFilter, BorderLayout.WEST);
        
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setBackground(Color.WHITE);
        JButton viewDetailsBtn = UIHelper.createPrimaryButton("🔍 View Selected Invoice");
        viewDetailsBtn.addActionListener(e -> viewSaleDetails());
        rightActions.add(viewDetailsBtn);
        toolbarPanel.add(rightActions, BorderLayout.EAST);
        
        contentCard.add(toolbarPanel, BorderLayout.NORTH);
        
        // JTable
        String[] columns = {"Invoice ID", "Checkout Timestamp", "Customer Name", "Total Items", "Subtotal", "Tax Total", "Final Total", "Order Status"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesHistoryTable = new JTable(historyTableModel);
        UIHelper.styleTable(salesHistoryTable);
        UIHelper.applyAlternatingRows(salesHistoryTable);
        UIHelper.applyStatusRenderer(salesHistoryTable, 7); // Column 7 is Status badge
        
        salesHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        salesHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        salesHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        salesHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        salesHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        salesHistoryTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        salesHistoryTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        salesHistoryTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        
        // Double-click row listener to view details
        salesHistoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && salesHistoryTable.getSelectedRow() != -1) {
                    viewSaleDetails();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(salesHistoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        contentCard.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentCard, BorderLayout.CENTER);
        return panel;
    }
    
    private void loadProductCombo() {
        productCombo.removeAllItems();
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            if (p.getStockQuantity() > 0) {
                productCombo.addItem(p);
            }
        }
    }
    
    private void addToCart() {
        Product product = (Product) productCombo.getSelectedItem();
        if (product == null) {
            UIHelper.showError(this, "Please select a product");
            return;
        }
        
        int quantity = (int) quantitySpinner.getValue();
        if (quantity > product.getStockQuantity()) {
            UIHelper.showError(this, "Insufficient inventory. Available stock count is: " + product.getStockQuantity());
            return;
        }
        
        // Check if product already in cart
        for (SaleItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                int newQty = item.getQuantity() + quantity;
                if (newQty > product.getStockQuantity()) {
                    UIHelper.showError(this, "Insufficient stock. Already have " + item.getQuantity() + " in cart. Max available is " + product.getStockQuantity());
                    return;
                }
                item.setQuantity(newQty);
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
            UIHelper.showError(this, "Please select a row from the cart list to remove");
        }
    }
    
    private void clearCart() {
        if (cartItems.isEmpty()) return;
        if (UIHelper.showConfirm(this, "Are you sure you want to empty the shopping cart?")) {
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
                item.getQuantity() + " units",
                "₱" + item.getUnitPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + item.getTotalPrice().setScale(2, RoundingMode.HALF_UP)
            });
            subtotal = subtotal.add(item.getTotalPriceSafe());
        }
        
        BigDecimal tax = BigDecimal.ZERO; // 0% VAT
        BigDecimal total = subtotal.add(tax);
        
        subtotalLabel.setText("Subtotal: ₱" + subtotal.setScale(2, RoundingMode.HALF_UP).toString());
        taxLabel.setText("Tax (0.00%): ₱" + tax.setScale(2, RoundingMode.HALF_UP).toString());
        totalLabel.setText("Total Due: ₱" + total.setScale(2, RoundingMode.HALF_UP).toString());
    }
    
    private void completeSale() {
        if (cartItems.isEmpty()) {
            UIHelper.showError(this, "Your cart is empty. Please select products first.");
            return;
        }
        
        Sale sale = new Sale();
        String customer = customerNameField.getText().trim();
        sale.setCustomerName(customer.isEmpty() ? "Walk-in Customer" : customer);
        sale.setItems(new ArrayList<>(cartItems));
        sale.setTax(BigDecimal.ZERO);
        sale.calculateTotals();
        sale.setStatus("COMPLETED");
        
        if (salesDAO.createSale(sale)) {
            UIHelper.showSuccess(this, "Invoice generated and checkout completed successfully!");
            cartItems.clear();
            updateCartDisplay();
            customerNameField.setText("");
            loadSalesHistory();
            loadProductCombo(); // Refresh quantities
        } else {
            UIHelper.showError(this, "Failed to write checkout to database.");
        }
    }
    
    private void loadSalesHistory() {
        historyTableModel.setRowCount(0);
        List<Sale> sales = salesDAO.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        String filter = statusFilterCombo != null ? (String) statusFilterCombo.getSelectedItem() : "All";
        
        for (Sale sale : sales) {
            String status = sale.getStatus() != null ? sale.getStatus() : "COMPLETED";
            if (!filter.equals("All") && !status.equalsIgnoreCase(filter)) {
                continue;
            }
            historyTableModel.addRow(new Object[]{
                "#" + sale.getId(),
                sale.getSaleDate().format(formatter),
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in Customer",
                sale.getItems().size() + " unique item" + (sale.getItems().size() == 1 ? "" : "s"),
                "₱" + sale.getSubtotal().setScale(2, RoundingMode.HALF_UP),
                "₱" + sale.getTax().setScale(2, RoundingMode.HALF_UP),
                "₱" + sale.getTotal().setScale(2, RoundingMode.HALF_UP),
                status.toUpperCase()
            });
        }
    }
    
    private void viewSaleDetails() {
        int row = salesHistoryTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please select an invoice from the history list");
            return;
        }
        
        // Parse ID from "#X" format
        String rawId = historyTableModel.getValueAt(row, 0).toString().replace("#", "");
        int saleId = Integer.parseInt(rawId);
        Sale sale = salesDAO.findById(saleId);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Invoice Details", true);
        dialog.setSize(640, 560);
        dialog.setLocationRelativeTo(this);
        UIHelper.styleDialog(dialog);
        
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = UIHelper.createDialogHeader(
            "INVOICE RECEIPT #" + sale.getId(), 
            "Transaction record and breakdown of customer purchase.", 
            UIHelper.SUCCESS_COLOR
        );
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Center layout: invoice sheets
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(UIHelper.CONTENT_BG);
        centerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        
        // Invoice metadata card
        JPanel metaCard = UIHelper.createCompactCard();
        metaCard.setLayout(new GridLayout(4, 2, 8, 4));
        
        metaCard.add(UIHelper.createCaptionLabel("INVOICE NUMBER"));
        metaCard.add(UIHelper.createLabel("#" + sale.getId()));
        
        metaCard.add(UIHelper.createCaptionLabel("TRANSACTION DATE"));
        metaCard.add(UIHelper.createLabel(sale.getSaleDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy - HH:mm"))));
        
        metaCard.add(UIHelper.createCaptionLabel("CUSTOMER BILL TO"));
        metaCard.add(UIHelper.createLabel(sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in Customer"));
        
        metaCard.add(UIHelper.createCaptionLabel("ORDER STATUS"));
        String saleStatus = sale.getStatus() != null ? sale.getStatus() : "COMPLETED";
        JLabel statusPill = UIHelper.createStatusBadge(saleStatus);
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusWrap.setBackground(Color.WHITE);
        statusWrap.add(statusPill);
        metaCard.add(statusWrap);
        
        centerPanel.add(metaCard, BorderLayout.NORTH);
        
        // Items Table sheet
        String[] columns = {"Product Catalog Description", "Qty Ordered", "Unit Selling Price", "Subtotal Cost"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (SaleItem item : sale.getItems()) {
            model.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity() + " units",
                "₱" + item.getUnitPrice().setScale(2, RoundingMode.HALF_UP),
                "₱" + item.getTotalPrice().setScale(2, RoundingMode.HALF_UP)
            });
        }
        JTable itemsTable = new JTable(model);
        UIHelper.styleTable(itemsTable);
        UIHelper.applyAlternatingRows(itemsTable);
        
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel sheetCard = UIHelper.createCard();
        sheetCard.setLayout(new BorderLayout());
        sheetCard.add(scrollPane, BorderLayout.CENTER);
        
        centerPanel.add(sheetCard, BorderLayout.CENTER);
        dialog.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom container: Totals + Actions
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        // Totals Grid
        JPanel totalsCard = new JPanel(new GridBagLayout());
        totalsCard.setBackground(Color.WHITE);
        totalsCard.setBorder(new EmptyBorder(12, 24, 12, 24));
        GridBagConstraints tGbc = new GridBagConstraints();
        tGbc.fill = GridBagConstraints.HORIZONTAL;
        tGbc.insets = new Insets(4, 0, 4, 0);
        tGbc.weightx = 1.0;
        
        tGbc.gridx = 0; tGbc.gridy = 0; tGbc.weightx = 0.6;
        totalsCard.add(UIHelper.createSecondaryLabel("Purchase Subtotal:"), tGbc);
        tGbc.gridx = 1; tGbc.weightx = 0.4; tGbc.anchor = GridBagConstraints.EAST;
        JLabel subLbl = UIHelper.createLabel("₱" + sale.getSubtotal().setScale(2, RoundingMode.HALF_UP));
        totalsCard.add(subLbl, tGbc);
        
        tGbc.gridx = 0; tGbc.gridy = 1; tGbc.weightx = 0.6; tGbc.anchor = GridBagConstraints.WEST;
        totalsCard.add(UIHelper.createSecondaryLabel("VAT Tax (0.00%):"), tGbc);
        tGbc.gridx = 1; tGbc.weightx = 0.4; tGbc.anchor = GridBagConstraints.EAST;
        JLabel taxLbl = UIHelper.createLabel("₱" + sale.getTax().setScale(2, RoundingMode.HALF_UP));
        totalsCard.add(taxLbl, tGbc);
        
        tGbc.gridx = 0; tGbc.gridy = 2; tGbc.weightx = 0.6; tGbc.anchor = GridBagConstraints.WEST;
        JLabel totTitle = new JLabel("INVOICE GRAND TOTAL:");
        totTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalsCard.add(totTitle, tGbc);
        tGbc.gridx = 1; tGbc.weightx = 0.4; tGbc.anchor = GridBagConstraints.EAST;
        JLabel totVal = new JLabel("₱" + sale.getTotal().setScale(2, RoundingMode.HALF_UP));
        totVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totVal.setForeground(UIHelper.SUCCESS_DARK);
        totalsCard.add(totVal, tGbc);
        
        bottomContainer.add(totalsCard);
        
        // Order Status Actions (Admin Console Controls)
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        adminPanel.setBackground(new Color(248, 250, 252));
        adminPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR),
            new EmptyBorder(8, 24, 8, 24)
        ));
        
        adminPanel.add(UIHelper.createSecondaryLabel("Modify Status:"));
        JComboBox<String> updateStatusCombo = new JComboBox<>(new String[]{"PENDING", "COMPLETED", "CANCELLED"});
        UIHelper.styleComboBox(updateStatusCombo);
        updateStatusCombo.setSelectedItem(saleStatus.toUpperCase());
        updateStatusCombo.setPreferredSize(new Dimension(140, 36));
        adminPanel.add(updateStatusCombo);
        
        JButton updateBtn = UIHelper.createPrimaryButton("Update");
        updateBtn.setPreferredSize(new Dimension(90, 36));
        updateBtn.addActionListener(e -> {
            String newStatus = (String) updateStatusCombo.getSelectedItem();
            if (salesDAO.updateSaleStatus(sale.getId(), newStatus)) {
                UIHelper.showSuccess(dialog, "Order #" + sale.getId() + " updated successfully to " + newStatus);
                dialog.dispose();
                loadSalesHistory();
            } else {
                UIHelper.showError(dialog, "Failed to update order status.");
            }
        });
        adminPanel.add(updateBtn);
        
        // Spacer and Dismiss Button
        adminPanel.add(Box.createRigidArea(new Dimension(100, 0)));
        JButton dismissBtn = UIHelper.createSecondaryButton("Close Invoice");
        dismissBtn.setPreferredSize(new Dimension(120, 36));
        dismissBtn.addActionListener(e -> dialog.dispose());
        adminPanel.add(dismissBtn);
        
        bottomContainer.add(adminPanel);
        dialog.add(bottomContainer, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}