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
import java.util.List;

/**
 * ProductPanel — Redesigned product catalog management.
 * Features a modern top toolbar layout, inline search-as-you-type,
 * custom stock status rendering, and a polished 2-column edit dialog.
 */
public class ProductPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private BrandDAO brandDAO = new BrandDAO();
    private SupplierDAO supplierDAO = new SupplierDAO();
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<Category> categoryFilter;
    private JComboBox<Brand> brandFilter;
    private JComboBox<Supplier> supplierFilter;
    
    public ProductPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Page Header
        JPanel headerPanel = UIHelper.createPageHeader("Products Catalog", "View, search, and manage your inventory products.");
        add(headerPanel, BorderLayout.NORTH);
        
        // Main container card for toolbar + table
        JPanel contentCard = UIHelper.createCard();
        contentCard.setLayout(new BorderLayout(0, 16));
        
        // Top Toolbar (Filters + Actions)
        JPanel toolbarPanel = createToolbarPanel();
        contentCard.add(toolbarPanel, BorderLayout.NORTH);
        
        // Table container
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        
        String[] columns = {"ID", "Name", "Category", "Brand", "Supplier", "Cost", "Markup", "Selling Price", "Stock", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        UIHelper.styleTable(productTable);
        UIHelper.applyAlternatingRows(productTable);
        UIHelper.applyStatusRenderer(productTable, 9); // Column 9 is "Status"
        
        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Name
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Category
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Brand
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Supplier
        productTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Cost
        productTable.getColumnModel().getColumn(6).setPreferredWidth(70);   // Markup
        productTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // Selling Price
        productTable.getColumnModel().getColumn(8).setPreferredWidth(60);   // Stock
        productTable.getColumnModel().getColumn(9).setPreferredWidth(110);  // Status
        
        // Double click to edit listener
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && productTable.getSelectedRow() != -1) {
                    int row = productTable.getSelectedRow();
                    int productId = (int) tableModel.getValueAt(row, 0);
                    Product product = productDAO.findById(productId);
                    showAddEditDialog(product);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        contentCard.add(tableContainer, BorderLayout.CENTER);
        add(contentCard, BorderLayout.CENTER);
        
        loadProducts();
    }
    
    private JPanel createToolbarPanel() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        // Filters on the left
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setBackground(Color.WHITE);
        
        searchField = UIHelper.createTextField("Search by name...");
        searchField.setPreferredSize(new Dimension(180, 36));
        
        // Inline real-time search on type
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void trigger() { searchProducts(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
        });
        
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem(null);
        categoryDAO.findAll().forEach(categoryFilter::addItem);
        UIHelper.styleComboBox(categoryFilter);
        categoryFilter.setPreferredSize(new Dimension(130, 36));
        categoryFilter.addActionListener(e -> searchProducts());
        
        brandFilter = new JComboBox<>();
        brandFilter.addItem(null);
        brandDAO.findAll().forEach(brandFilter::addItem);
        UIHelper.styleComboBox(brandFilter);
        brandFilter.setPreferredSize(new Dimension(130, 36));
        brandFilter.addActionListener(e -> searchProducts());
        
        supplierFilter = new JComboBox<>();
        supplierFilter.addItem(null);
        supplierDAO.findAll().forEach(supplierFilter::addItem);
        UIHelper.styleComboBox(supplierFilter);
        supplierFilter.setPreferredSize(new Dimension(130, 36));
        supplierFilter.addActionListener(e -> searchProducts());
        
        JButton resetBtn = UIHelper.createGhostButton("Reset");
        resetBtn.setPreferredSize(new Dimension(80, 36));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            brandFilter.setSelectedIndex(0);
            supplierFilter.setSelectedIndex(0);
            loadProducts();
        });
        
        leftFilters.add(UIHelper.createSecondaryLabel("Search:"));
        leftFilters.add(searchField);
        leftFilters.add(UIHelper.createSecondaryLabel("Category:"));
        leftFilters.add(categoryFilter);
        leftFilters.add(UIHelper.createSecondaryLabel("Brand:"));
        leftFilters.add(brandFilter);
        leftFilters.add(UIHelper.createSecondaryLabel("Supplier:"));
        leftFilters.add(supplierFilter);
        leftFilters.add(resetBtn);
        
        // Actions on the right
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setBackground(Color.WHITE);
        
        JButton addBtn = UIHelper.createSuccessButton("+ Add Product");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        
        JButton editBtn = UIHelper.createPrimaryButton("Edit");
        editBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                int productId = (int) tableModel.getValueAt(row, 0);
                Product product = productDAO.findById(productId);
                showAddEditDialog(product);
            } else {
                UIHelper.showError(this, "Please select a product to edit");
            }
        });
        
        JButton deleteBtn = UIHelper.createDangerButton("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteProduct());
        
        rightActions.add(addBtn);
        rightActions.add(editBtn);
        rightActions.add(deleteBtn);
        
        bar.add(leftFilters, BorderLayout.CENTER);
        bar.add(rightActions, BorderLayout.EAST);
        
        return bar;
    }
    
    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.findAll();
        populateTable(products);
    }
    
    private void searchProducts() {
        String keyword = searchField.getText().trim();
        Category cat = (Category) categoryFilter.getSelectedItem();
        Brand brand = (Brand) brandFilter.getSelectedItem();
        Supplier supplier = (Supplier) supplierFilter.getSelectedItem();
        
        Integer catId = cat != null ? cat.getId() : null;
        Integer brandId = brand != null ? brand.getId() : null;
        Integer supplierId = supplier != null ? supplier.getId() : null;
        
        tableModel.setRowCount(0);
        List<Product> products = productDAO.search(keyword.isEmpty() ? null : keyword, catId, brandId, supplierId);
        populateTable(products);
    }
    
    private void populateTable(List<Product> products) {
        for (Product p : products) {
            String status = p.getStockQuantity() == 0 ? "Out of Stock" : 
                            p.getStockQuantity() <= 10 ? "Low Stock" : "In Stock";
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getSupplierName(),
                "₱" + p.getCostPrice().setScale(2, RoundingMode.HALF_UP),
                p.getMarkupPercentage() + "%",
                "₱" + p.getSellingPrice().setScale(2, RoundingMode.HALF_UP),
                p.getStockQuantity(),
                status
            });
        }
    }
    
    private void showAddEditDialog(Product product) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                product == null ? "Add Product" : "Edit Product", true);
        dialog.setSize(680, 560);
        dialog.setLocationRelativeTo(this);
        UIHelper.styleDialog(dialog);
        
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = UIHelper.createDialogHeader(
            product == null ? "Add New Product" : "Edit Product Details", 
            "Configure product pricing, category relations and inventory counts.", 
            UIHelper.PRIMARY_COLOR
        );
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Form Panel in a centered ScrollPane (just in case)
        JPanel mainFormPanel = new JPanel(new GridBagLayout());
        mainFormPanel.setBackground(UIHelper.CONTENT_BG);
        mainFormPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1.0;
        
        // Column 1 Fields
        JTextField nameField = UIHelper.createTextField();
        JComboBox<Category> catCombo = new JComboBox<>();
        categoryDAO.findAll().forEach(catCombo::addItem);
        UIHelper.styleComboBox(catCombo);
        
        JComboBox<Brand> brandCombo = new JComboBox<>();
        brandDAO.findAll().forEach(brandCombo::addItem);
        UIHelper.styleComboBox(brandCombo);
        
        JComboBox<Supplier> supplierCombo = new JComboBox<>();
        supplierDAO.findAll().forEach(supplierCombo::addItem);
        UIHelper.styleComboBox(supplierCombo);
        
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(UIHelper.NORMAL_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setPreferredSize(new Dimension(240, 80));
        
        // Column 2 Fields
        JTextField costField = UIHelper.createTextField();
        JTextField markupField = UIHelper.createTextField();
        JTextField stockField = UIHelper.createTextField();
        
        // Selling price preview card
        JPanel previewCard = UIHelper.createCompactCard();
        previewCard.setLayout(new BorderLayout());
        previewCard.setPreferredSize(new Dimension(240, 80));
        
        JLabel previewTitle = UIHelper.createCaptionLabel("DYNAMICAL SELLING PRICE");
        JLabel sellingPriceLabel = new JLabel("₱0.00", SwingConstants.CENTER);
        sellingPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sellingPriceLabel.setForeground(UIHelper.SUCCESS_DARK);
        
        previewCard.add(previewTitle, BorderLayout.NORTH);
        previewCard.add(sellingPriceLabel, BorderLayout.CENTER);
        
        // Populate fields if editing
        if (product != null) {
            nameField.setText(product.getName());
            
            // select category
            for (int i = 0; i < catCombo.getItemCount(); i++) {
                Category c = catCombo.getItemAt(i);
                if (c != null && c.getId() == product.getCategoryId()) {
                    catCombo.setSelectedIndex(i);
                    break;
                }
            }
            // select brand
            for (int i = 0; i < brandCombo.getItemCount(); i++) {
                Brand b = brandCombo.getItemAt(i);
                if (b != null && b.getId() == product.getBrandId()) {
                    brandCombo.setSelectedIndex(i);
                    break;
                }
            }
            // select supplier
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                Supplier s = supplierCombo.getItemAt(i);
                if (s != null && s.getId() == product.getSupplierId()) {
                    supplierCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            if (product.getCostPrice() != null) costField.setText(product.getCostPrice().setScale(2, RoundingMode.HALF_UP).toString());
            if (product.getMarkupPercentage() != null) markupField.setText(product.getMarkupPercentage().toString());
            stockField.setText(String.valueOf(product.getStockQuantity()));
            descArea.setText(product.getDescription());
            
            try {
                BigDecimal cost = product.getCostPrice();
                BigDecimal markup = product.getMarkupPercentage();
                BigDecimal selling = cost.add(cost.multiply(markup).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                sellingPriceLabel.setText("₱" + selling.setScale(2, RoundingMode.HALF_UP));
            } catch (Exception ex) {
                // ignore
            }
        } else {
            markupField.setText("8"); // Default 8% markup for new products
            sellingPriceLabel.setText("₱0.00");
        }
        
        // Layout: 2 Columns
        // Left Column (Col 0-1) | Right Column (Col 2-3)
        
        // Row 0: Name (Full Width)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.9;
        mainFormPanel.add(nameField, gbc);
        
        // Row 1: Category & Cost Price
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(catCombo, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Cost Price:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(costField, gbc);
        
        // Row 2: Brand & Markup
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Brand:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(brandCombo, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Markup %:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(markupField, gbc);
        
        // Row 3: Supplier & Stock Quantity
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(supplierCombo, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Stock Qty:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(stockField, gbc);
        
        // Row 4: Description (Left) & Selling Price Card (Right)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(descScroll, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.1;
        mainFormPanel.add(UIHelper.createLabel("Selling Price:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.4;
        mainFormPanel.add(previewCard, gbc);
        
        dialog.add(mainFormPanel, BorderLayout.CENTER);
        
        // Live price calculation listener
        javax.swing.event.DocumentListener calcListener = new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    String costText = costField.getText().trim();
                    String markupText = markupField.getText().trim();
                    if (!costText.isEmpty() && !markupText.isEmpty()) {
                        BigDecimal cost = new BigDecimal(costText);
                        BigDecimal markup = new BigDecimal(markupText);
                        BigDecimal markupAmount = cost.multiply(markup).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        BigDecimal selling = cost.add(markupAmount);
                        sellingPriceLabel.setText("₱" + selling.setScale(2, RoundingMode.HALF_UP));
                    } else {
                        sellingPriceLabel.setText("₱0.00");
                    }
                } catch (NumberFormatException ex) {
                    sellingPriceLabel.setText("₱0.00");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };
        costField.getDocument().addDocumentListener(calcListener);
        markupField.getDocument().addDocumentListener(calcListener);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        JButton saveBtn = UIHelper.createSuccessButton("Save Product");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String costText = costField.getText().trim();
                String markupText = markupField.getText().trim();
                String stockText = stockField.getText().trim();
                
                if (name.isEmpty()) {
                    UIHelper.showError(dialog, "Product name is required");
                    nameField.requestFocus();
                    return;
                }
                
                Category c = (Category) catCombo.getSelectedItem();
                Brand b = (Brand) brandCombo.getSelectedItem();
                Supplier s = (Supplier) supplierCombo.getSelectedItem();
                
                if (c == null || b == null || s == null) {
                    UIHelper.showError(dialog, "Please select category, brand and supplier");
                    return;
                }
                
                if (costText.isEmpty()) {
                    UIHelper.showError(dialog, "Cost price is required");
                    costField.requestFocus();
                    return;
                }
                
                BigDecimal costPrice;
                try {
                    costPrice = new BigDecimal(costText);
                    if (costPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        UIHelper.showError(dialog, "Cost price must be greater than zero");
                        costField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    UIHelper.showError(dialog, "Invalid cost price format");
                    costField.requestFocus();
                    return;
                }
                
                BigDecimal markupPercentage;
                if (markupText.isEmpty()) {
                    markupPercentage = new BigDecimal("8");
                } else {
                    try {
                        markupPercentage = new BigDecimal(markupText);
                        if (markupPercentage.compareTo(BigDecimal.ZERO) < 0) {
                            UIHelper.showError(dialog, "Markup percentage cannot be negative");
                            markupField.requestFocus();
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        UIHelper.showError(dialog, "Invalid markup format");
                        markupField.requestFocus();
                        return;
                    }
                }
                
                if (stockText.isEmpty()) {
                    UIHelper.showError(dialog, "Stock quantity is required");
                    stockField.requestFocus();
                    return;
                }
                
                int stockQuantity;
                try {
                    stockQuantity = Integer.parseInt(stockText);
                    if (stockQuantity < 0) {
                        UIHelper.showError(dialog, "Stock quantity cannot be negative");
                        stockField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    UIHelper.showError(dialog, "Stock quantity must be a valid whole number");
                    stockField.requestFocus();
                    return;
                }
                
                Product p = product != null ? product : new Product();
                p.setName(name);
                p.setCategoryId(c.getId());
                p.setBrandId(b.getId());
                p.setSupplierId(s.getId());
                p.setCostPrice(costPrice);
                p.setMarkupPercentage(markupPercentage);
                p.setStockQuantity(stockQuantity);
                p.setDescription(descArea.getText().trim());
                
                boolean success = product != null ? productDAO.update(p) : productDAO.create(p);
                if (success) {
                    UIHelper.showSuccess(this, "Product saved successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    UIHelper.showError(dialog, "Failed to save product. Database error.");
                }
            } catch (Exception ex) {
                UIHelper.showError(dialog, "Unexpected error: " + ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            if (UIHelper.showConfirm(this, "Are you sure you want to delete this product?")) {
                int id = (int) tableModel.getValueAt(row, 0);
                if (productDAO.delete(id)) {
                    UIHelper.showSuccess(this, "Product deleted successfully!");
                    loadProducts();
                } else {
                    UIHelper.showError(this, "Failed to delete product. It may be linked to transaction history.");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a product to delete");
        }
    }
}