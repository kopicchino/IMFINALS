package ui.user;

import dao.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;
import util.UIHelper;

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
        setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Top panel with header and search
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("Products Management");
        topPanel.add(headerLabel, BorderLayout.NORTH);
        
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.WEST);
        
        // Search and filter panel
        JPanel searchPanel = createSearchPanel();
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadProducts();
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        searchField = UIHelper.createTextField();
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setToolTipText("Search by product name");
        
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem(null);
        categoryDAO.findAll().forEach(categoryFilter::addItem);
        categoryFilter.setPreferredSize(new Dimension(150, 35));
        
        brandFilter = new JComboBox<>();
        brandFilter.addItem(null);
        brandDAO.findAll().forEach(brandFilter::addItem);
        brandFilter.setPreferredSize(new Dimension(150, 35));
        
        supplierFilter = new JComboBox<>();
        supplierFilter.addItem(null);
        supplierDAO.findAll().forEach(supplierFilter::addItem);
        supplierFilter.setPreferredSize(new Dimension(150, 35));
        
        JButton searchBtn = UIHelper.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> searchProducts());
        
        JButton resetBtn = UIHelper.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            brandFilter.setSelectedIndex(0);
            supplierFilter.setSelectedIndex(0);
            loadProducts();
        });
        
        panel.add(UIHelper.createLabel("Search:"));
        panel.add(searchField);
        panel.add(UIHelper.createLabel("Category:"));
        panel.add(categoryFilter);
        panel.add(UIHelper.createLabel("Brand:"));
        panel.add(brandFilter);
        panel.add(searchBtn);
        panel.add(resetBtn);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout());
        
        String[] columns = {"ID", "Name", "Category", "Brand", "Supplier", "Cost", "Markup%", "Selling Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        UIHelper.styleTable(productTable);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JButton addBtn = UIHelper.createSuccessButton("Add Product");
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
        
        JButton deleteBtn = UIHelper.createDangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteProduct());
        
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        
        return panel;
    }
    
    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getSupplierName(),
                "₱" + p.getCostPrice(),
                p.getMarkupPercentage() + "%",
                "₱" + p.getSellingPrice(),
                p.getStockQuantity()
            });
        }
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
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getSupplierName(),
                "₱" + p.getCostPrice(),
                p.getMarkupPercentage() + "%",
                "₱" + p.getSellingPrice(),
                p.getStockQuantity()
            });
        }
    }
    
    private void showAddEditDialog(Product product) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                product == null ? "Add Product" : "Edit Product", true);
        dialog.setSize(600, 640);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIHelper.CONTENT_BG);
        panel.setBorder(new EmptyBorder(20, 28, 20, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField nameField = UIHelper.createTextField();
        JComboBox<Category> catCombo = new JComboBox<>();
        categoryDAO.findAll().forEach(catCombo::addItem);
        JComboBox<Brand> brandCombo = new JComboBox<>();
        brandDAO.findAll().forEach(brandCombo::addItem);
        JComboBox<Supplier> supplierCombo = new JComboBox<>();
        supplierDAO.findAll().forEach(supplierCombo::addItem);
        JTextField costField = UIHelper.createTextField();
        JTextField markupField = UIHelper.createTextField();
        JTextField stockField = UIHelper.createTextField();
        JTextArea descArea = new JTextArea(4, 24);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        // Calculated selling price preview (read-only)
        JLabel sellingPriceLabel = new JLabel("₱0.00");
        sellingPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sellingPriceLabel.setForeground(UIHelper.SUCCESS_COLOR.darker());

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
            for (int i = 0; i < brandCombo.getItemCount(); i++) {
                Brand b = brandCombo.getItemAt(i);
                if (b != null && b.getId() == product.getBrandId()) {
                    brandCombo.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                Supplier s = supplierCombo.getItemAt(i);
                if (s != null && s.getId() == product.getSupplierId()) {
                    supplierCombo.setSelectedIndex(i);
                    break;
                }
            }
            if (product.getCostPrice() != null) costField.setText(product.getCostPrice().toString());
            if (product.getMarkupPercentage() != null) markupField.setText(product.getMarkupPercentage().toString());
            stockField.setText(String.valueOf(product.getStockQuantity()));
            descArea.setText(product.getDescription());
            try {
                java.math.BigDecimal cost = product.getCostPrice();
                java.math.BigDecimal markup = product.getMarkupPercentage();
                java.math.BigDecimal selling = cost.add(cost.multiply(markup).divide(new java.math.BigDecimal("100")));
                sellingPriceLabel.setText("₱" + selling);
            } catch (Exception ex) {
                // ignore
            }
        }

        // Preferred sizes
        Dimension fullSize = new Dimension(420, 36);
        nameField.setPreferredSize(fullSize);
        catCombo.setPreferredSize(fullSize);
        brandCombo.setPreferredSize(fullSize);
        supplierCombo.setPreferredSize(fullSize);
        costField.setPreferredSize(new Dimension(150, 36));
        markupField.setPreferredSize(new Dimension(150, 36));
        stockField.setPreferredSize(new Dimension(200, 36));
        descScroll.setPreferredSize(new Dimension(420, 120));

        int row = 0;
        addFormField(panel, gbc, row++, "Product Name:", nameField);
        addFormField(panel, gbc, row++, "Category:", catCombo);
        addFormField(panel, gbc, row++, "Brand:", brandCombo);
        addFormField(panel, gbc, row++, "Supplier:", supplierCombo);

        // Cost and Markup on same row
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(UIHelper.createLabel("Cost Price:"), gbc);
        gbc.gridx = 1;
        JPanel costPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        costPanel.setBackground(UIHelper.CONTENT_BG);
        costPanel.add(costField);
        costPanel.add(UIHelper.createLabel("Markup %:"));
        costPanel.add(markupField);
        panel.add(costPanel, gbc);
        row++;

        addFormField(panel, gbc, row++, "Stock Quantity:", stockField);
        addFormField(panel, gbc, row++, "Description:", descScroll);


        // Selling price preview
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(UIHelper.createLabel("Selling Price:"), gbc);
        gbc.gridx = 1;
        panel.add(sellingPriceLabel, gbc);
        row++;

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setBackground(UIHelper.CONTENT_BG);
        JButton saveBtn = UIHelper.createSuccessButton("Save");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");

        // Live calculation listener
        javax.swing.event.DocumentListener calcListener = new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    String costText = costField.getText().trim();
                    String markupText = markupField.getText().trim();

                    if (!costText.isEmpty() && !markupText.isEmpty()) {
                        BigDecimal cost = new BigDecimal(costText);
                        BigDecimal markup = new BigDecimal(markupText);

                        BigDecimal markupAmount = cost.multiply(markup)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                        BigDecimal selling = cost.add(markupAmount);

                        sellingPriceLabel.setText("₱" + selling.setScale(2, RoundingMode.HALF_UP));
                    } else {
                        sellingPriceLabel.setText("₱0.00");
                    }
                } catch (NumberFormatException ex) {
                    sellingPriceLabel.setText("₱0.00");
                }
                /*try {

                    java.math.BigDecimal cost = new java.math.BigDecimal(costField.getText());
                    java.math.BigDecimal markup = new java.math.BigDecimal(markupField.getText());
                    java.math.BigDecimal selling = cost.add(cost.multiply(markup).divide(new java.math.BigDecimal("100")));
                    sellingPriceLabel.setText("₱" + selling);
                } catch (Exception ex) {
                    sellingPriceLabel.setText("₱0.00");
                }*/
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };
        costField.getDocument().addDocumentListener(calcListener);
        markupField.getDocument().addDocumentListener(calcListener);

        // Validation & save
        /*saveBtn.addActionListener(e -> {
            try {
                Product p = product != null ? product : new Product();
                String name = nameField.getText().trim();
                if (name.isEmpty()) { UIHelper.showError(dialog, "Product name is required"); return; }
                Category c = (Category) catCombo.getSelectedItem();
                Brand b = (Brand) brandCombo.getSelectedItem();
                Supplier s = (Supplier) supplierCombo.getSelectedItem();
                if (c == null || b == null || s == null) { UIHelper.showError(dialog, "Please select category, brand and supplier"); return; }

                p.setName(name);
                p.setCategoryId(c.getId());
                p.setBrandId(b.getId());
                p.setSupplierId(s.getId());
                p.setCostPrice(new BigDecimal(costField.getText()));
                p.setMarkupPercentage(new BigDecimal(markupField.getText()));
                p.setStockQuantity(Integer.parseInt(stockField.getText()));
                p.setDescription(descArea.getText());

                boolean success = product != null ? productDAO.update(p) : productDAO.create(p);
                if (success) {
                    UIHelper.showSuccess(this, "Product saved successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    UIHelper.showError(this, "Failed to save product");
                }
            } catch (NumberFormatException ex) {
                UIHelper.showError(dialog, "Invalid number format");
            }
        });*/

        saveBtn.addActionListener(e -> {
            try {
                // Get and trim all text field values
                String name = nameField.getText().trim();
                String costText = costField.getText().trim();
                String stockText = stockField.getText().trim();
                
                // Validate product name
                if (name.isEmpty()) {
                    UIHelper.showError(dialog, "Product name is required");
                    nameField.requestFocus();
                    return;
                }
                
                // Validate category, brand, and supplier selection
                Category c = (Category) catCombo.getSelectedItem();
                Brand b = (Brand) brandCombo.getSelectedItem();
                Supplier s = (Supplier) supplierCombo.getSelectedItem();
                
                if (c == null || b == null || s == null) {
                    UIHelper.showError(dialog, "Please select category, brand and supplier");
                    return;
                }
                
                // Validate cost price
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
                    UIHelper.showError(dialog, "Invalid cost price. Please enter a valid number (e.g., 899 or 899.50)");
                    costField.requestFocus();
                    return;
                }
                
                // Validate stock quantity
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
                    UIHelper.showError(dialog, "Invalid stock quantity. Please enter a whole number (e.g., 50)");
                    stockField.requestFocus();
                    return;
                }
                
                // All validations passed - create or update the product
                Product p = product != null ? product : new Product();
                p.setName(name);
                p.setCategoryId(c.getId());
                p.setBrandId(b.getId());
                p.setSupplierId(s.getId()); 
                p.setCostPrice(costPrice);
                p.setMarkupPercentage(new BigDecimal("8")); // Set markup to 10 since no field exists
                p.setStockQuantity(stockQuantity);
                p.setDescription(descArea.getText().trim());
                
                // Save to database
                boolean success = product != null ? productDAO.update(p) : productDAO.create(p);
                
                if (success) {
                    UIHelper.showSuccess(this, "Product saved successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    UIHelper.showError(this, "Failed to save product");
                }
                
            } catch (Exception ex) {
                UIHelper.showError(dialog, "An unexpected error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(UIHelper.createLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
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
                    UIHelper.showError(this, "Failed to delete product");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a product to delete");
        }
    }
}