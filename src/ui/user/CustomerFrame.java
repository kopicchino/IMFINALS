package ui.user;

import dao.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;
import ui.shared.LoginFrame;
import util.UIHelper;

public class CustomerFrame extends JFrame {
    private User currentUser;
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private CartDAO cartDAO = new CartDAO();
    private SalesDAO salesDAO = new SalesDAO();
    
    private JPanel contentPanel;
    private JLabel cartCountLabel;
    
    public CustomerFrame(User user) {
        this.currentUser = user;
        setTitle("Inventory System - Customer Portal");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Top Navigation Bar
        JPanel navbar = createNavbar();
        add(navbar, BorderLayout.NORTH);
        
        // Content Panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIHelper.CONTENT_BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
        
        showShopPanel();
    }
    
    private JPanel createNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.WHITE);
        navbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, UIHelper.BORDER_COLOR),
            new EmptyBorder(15, 25, 15, 25)
        ));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel logoLabel = new JLabel("Inventory Shop");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(UIHelper.PRIMARY_COLOR);
        
        JButton shopBtn = createNavButton("Shop");
        shopBtn.addActionListener(e -> showShopPanel());
        
        JButton ordersBtn = createNavButton("My Orders");
        ordersBtn.addActionListener(e -> showOrdersPanel());
        
        JButton profileBtn = createNavButton("Profile");
        profileBtn.addActionListener(e -> showProfilePanel());
        
        leftPanel.add(logoLabel);
        leftPanel.add(shopBtn);
        leftPanel.add(ordersBtn);
        leftPanel.add(profileBtn);
        
        // right side - Cart and Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(Color.WHITE);
        
        JButton cartBtn = createNavButton("Cart");
        cartBtn.addActionListener(e -> showCartPanel());
        
        cartCountLabel = new JLabel();
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setBackground(UIHelper.DANGER_COLOR);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        updateCartCount();
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
        welcomeLabel.setFont(UIHelper.NORMAL_FONT);
        welcomeLabel.setForeground(UIHelper.TEXT_SECONDARY);
        
        JButton logoutBtn = createNavButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        
        rightPanel.add(cartBtn);
        rightPanel.add(cartCountLabel);
        rightPanel.add(welcomeLabel);
        rightPanel.add(logoutBtn);
        
        navbar.add(leftPanel, BorderLayout.WEST);
        navbar.add(rightPanel, BorderLayout.EAST);
        
        return navbar;
    }
    
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        Color darkBlue = new Color(74, 144, 226); // Darker blue like search button
        Color darkBlueDarker = new Color(55, 120, 190); // Hover effect
        Color textColor = new Color(255, 255, 255); // White text
        
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(darkBlue);
        btn.setForeground(textColor);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(darkBlueDarker);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(darkBlue);
            }
        });
        
        return btn;
    }
    
    private void showShopPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerShopPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showCartPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerCartPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showOrdersPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerOrdersPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showProfilePanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerProfilePanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void updateCartCount() {
        int count = cartDAO.getCartItemCount(currentUser.getId());
        cartCountLabel.setText(String.valueOf(count));
        cartCountLabel.setVisible(count > 0);
    }
    
    private void logout() {
        if (UIHelper.showConfirm(this, "Are you sure you want to logout?")) {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
    
    class CustomerShopPanel extends JPanel {
        private JPanel productsPanel;
        private JTextField searchField;
        private JComboBox<Category> categoryFilter;
        
        public CustomerShopPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);
            
            // Header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(UIHelper.CONTENT_BG);
            
            JLabel titleLabel = UIHelper.createHeaderLabel("Browse Products");
            headerPanel.add(titleLabel, BorderLayout.WEST);
            
            // Search and Filter
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            filterPanel.setBackground(UIHelper.CONTENT_BG);
            
            searchField = UIHelper.createTextField();
            searchField.setPreferredSize(new Dimension(200, 35));
            searchField.setToolTipText("Search products...");
            
            categoryFilter = new JComboBox<>();
            categoryFilter.addItem(null);
            categoryDAO.findAll().forEach(categoryFilter::addItem);
            categoryFilter.setPreferredSize(new Dimension(150, 35));
            
            JButton searchBtn = UIHelper.createPrimaryButton("Search");
            // Ensure the search button shows its label (filled background + white text)
            searchBtn.setForeground(Color.WHITE);
            searchBtn.setOpaque(true);
            searchBtn.setContentAreaFilled(true);
            searchBtn.addActionListener(e -> loadProducts());
            
            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            searchLabel.setForeground(UIHelper.TEXT_PRIMARY);
            filterPanel.add(searchLabel);
            filterPanel.add(searchField);
            JLabel categoryLabel = new JLabel("Category:");
            categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            categoryLabel.setForeground(UIHelper.TEXT_PRIMARY);
            filterPanel.add(categoryLabel);
            filterPanel.add(categoryFilter);
            filterPanel.add(searchBtn);
            
            headerPanel.add(filterPanel, BorderLayout.EAST);
            add(headerPanel, BorderLayout.NORTH);
            
            // Products Grid
            productsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
            productsPanel.setBackground(UIHelper.CONTENT_BG);
            
            JScrollPane scrollPane = new JScrollPane(productsPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(scrollPane, BorderLayout.CENTER);
            
            loadProducts();
        }
        
        private void loadProducts() {
            productsPanel.removeAll();
            
            String keyword = searchField.getText().trim();
            Category cat = (Category) categoryFilter.getSelectedItem();
            Integer catId = cat != null ? cat.getId() : null;
            
            List<Product> products = productDAO.search(keyword.isEmpty() ? null : keyword, catId, null, null);
            
            for (Product product : products) {
                if (product.getStockQuantity() > 0) {
                    productsPanel.add(createProductCard(product));
                }
            }
            
            productsPanel.revalidate();
            productsPanel.repaint();
        }
        
        private JPanel createProductCard(Product product) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
            ));
            
            // Product Icon
            JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Product Name
            JLabel nameLabel = new JLabel(product.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setForeground(UIHelper.TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Category
            JLabel categoryLabel = new JLabel(product.getCategoryName());
            categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            categoryLabel.setForeground(UIHelper.TEXT_SECONDARY);
            categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Price
            JLabel priceLabel = new JLabel("₱" + product.getSellingPrice());
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            priceLabel.setForeground(UIHelper.SUCCESS_COLOR);
            priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Stock
            JLabel stockLabel = new JLabel("In Stock: " + product.getStockQuantity());
            stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            stockLabel.setForeground(product.getStockQuantity() < 10 ? UIHelper.WARNING_COLOR : UIHelper.TEXT_SECONDARY);
            stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Add to Cart Button
            final Color addCartColor = new Color(40, 167, 69); // Bootstrap green #28a745
            JButton addBtn = new JButton("Add to Cart");
            addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            addBtn.setForeground(Color.WHITE);
            addBtn.setBackground(addCartColor);
            addBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            addBtn.setFocusPainted(false);
            addBtn.setBorderPainted(false);
            addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            addBtn.setMaximumSize(new Dimension(200, 40));
            addBtn.setOpaque(true);
            addBtn.setContentAreaFilled(true);
            addBtn.addActionListener(e -> addToCart(product));
            
            addBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    addBtn.setBackground(addCartColor.darker());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    addBtn.setBackground(addCartColor);
                }
            });
            
            card.add(iconLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(nameLabel);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(categoryLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(priceLabel);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(stockLabel);
            card.add(Box.createRigidArea(new Dimension(0, 15)));
            card.add(addBtn);
            
            return card;
        }
        
        private void addToCart(Product product) {
            String input = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
            if (input != null) {
                try {
                    int quantity = Integer.parseInt(input);
                    if (quantity <= 0) {
                        UIHelper.showError(this, "Quantity must be greater than 0");
                        return;
                    }
                    if (quantity > product.getStockQuantity()) {
                        UIHelper.showError(this, "Insufficient stock. Available: " + product.getStockQuantity());
                        return;
                    }
                    
                    if (cartDAO.addToCart(currentUser.getId(), product.getId(), quantity)) {
                        UIHelper.showSuccess(this, "Added to cart successfully!");
                        updateCartCount();
                    } else {
                        UIHelper.showError(this, "Failed to add to cart");
                    }
                } catch (NumberFormatException ex) {
                    UIHelper.showError(this, "Invalid quantity");
                }
            }
        }
    }
    
    class CustomerCartPanel extends JPanel {
        private JTable cartTable;
        private DefaultTableModel tableModel;
        private JLabel subtotalLabel, taxLabel, totalLabel;
        
        public CustomerCartPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);

            JLabel titleLabel = UIHelper.createHeaderLabel("Shopping Cart");
            add(titleLabel, BorderLayout.NORTH);

            // Table
            JPanel tablePanel = UIHelper.createCard();
            tablePanel.setLayout(new BorderLayout());

            String[] columns = {"Product", "Price", "Quantity", "Total", "Stock"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            cartTable = new JTable(tableModel);
            UIHelper.styleTable(cartTable);
            JScrollPane scrollPane = new JScrollPane(cartTable);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            add(tablePanel, BorderLayout.CENTER);

            // Bottom panel
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(UIHelper.CONTENT_BG);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.setBackground(UIHelper.CONTENT_BG);

            JButton updateBtn = UIHelper.createPrimaryButton("Update Quantity");
            updateBtn.addActionListener(e -> updateQuantity());

            JButton removeBtn = UIHelper.createDangerButton("Remove Item");
            removeBtn.addActionListener(e -> removeItem());

            JButton clearBtn = UIHelper.createSecondaryButton("Clear Cart");
            clearBtn.addActionListener(e -> clearCart());

            buttonPanel.add(updateBtn);
            buttonPanel.add(removeBtn);
            buttonPanel.add(clearBtn);

            // Totals
            JPanel totalsPanel = new JPanel();
            totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
            totalsPanel.setBackground(UIHelper.CONTENT_BG);

                subtotalLabel = UIHelper.createLabel("Subtotal: ₱0.00");
                taxLabel = UIHelper.createLabel("Tax: ₱0.00");
                totalLabel = new JLabel("Total: ₱0.00");
            totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            totalLabel.setForeground(UIHelper.SUCCESS_COLOR);

            JButton checkoutBtn = UIHelper.createPrimaryButton("Checkout");
            checkoutBtn.setMaximumSize(new Dimension(160, 42));
            checkoutBtn.addActionListener(e -> openCheckoutDialog());

            JButton quickCheckoutBtn = UIHelper.createSuccessButton("Quick Checkout");
            quickCheckoutBtn.setMaximumSize(new Dimension(160, 42));
            quickCheckoutBtn.addActionListener(e -> checkoutSelectedItem());

            totalsPanel.add(subtotalLabel);
            totalsPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            totalsPanel.add(taxLabel);
            totalsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
                totalsPanel.add(totalLabel);
            totalsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            
            JPanel checkoutPanel = new JPanel();
            checkoutPanel.setLayout(new BoxLayout(checkoutPanel, BoxLayout.X_AXIS));
            checkoutPanel.setBackground(UIHelper.CONTENT_BG);
            checkoutPanel.add(checkoutBtn);
            checkoutPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            checkoutPanel.add(quickCheckoutBtn);
            totalsPanel.add(checkoutPanel);

            bottomPanel.add(buttonPanel, BorderLayout.WEST);
            bottomPanel.add(totalsPanel, BorderLayout.EAST);

            add(bottomPanel, BorderLayout.SOUTH);

            loadCart();
        }
        
        private void loadCart() {
            tableModel.setRowCount(0);
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem item : items) {
                tableModel.addRow(new Object[]{
                    item.getProductName(),
                    "₱" + item.getUnitPrice(),
                    item.getQuantity(),
                    "₱" + item.getTotalPrice(),
                    item.getAvailableStock()
                });
                subtotal = subtotal.add(item.getTotalPriceSafe());
            }
            
            BigDecimal tax = util.CheckoutCalculator.calculateTax(subtotal);
            BigDecimal total = subtotal.add(tax);

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            
            subtotalLabel.setText("Subtotal: ₱" + df.format(subtotal));
            taxLabel.setText("Tax: ₱" + df.format(tax));
            totalLabel.setText("Total: ₱" + df.format(total));
        }
        
        private void updateQuantity() {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Please select an item");
                return;
            }
            
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            CartItem item = items.get(row);
            
            String input = JOptionPane.showInputDialog(this, "Enter new quantity:", item.getQuantity());
            if (input != null) {
                try {
                    int quantity = Integer.parseInt(input);
                    if (quantity <= 0) {
                        UIHelper.showError(this, "Quantity must be greater than 0");
                        return;
                    }
                    if (quantity > item.getAvailableStock()) {
                        UIHelper.showError(this, "Insufficient stock");
                        return;
                    }
                    
                    if (cartDAO.updateQuantity(currentUser.getId(), item.getProductId(), quantity)) {
                        loadCart();
                        updateCartCount();
                    }
                } catch (NumberFormatException ex) {
                    UIHelper.showError(this, "Invalid quantity");
                }
            }
        }
        
        
        private void removeItem() {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Please select an item");
                return;
            }

            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            CartItem item = items.get(row);

            if (UIHelper.showConfirm(this, "Remove this item from cart?")) {
                if (cartDAO.removeFromCart(currentUser.getId(), item.getProductId())) {
                    loadCart();
                    updateCartCount();
                }
            }
        }
        private void clearCart() {
            if (UIHelper.showConfirm(this, "Clear all items from cart?")) {
                cartDAO.clearCart(currentUser.getId());
                loadCart();
                updateCartCount();
            }
        }
        
        private void checkoutSelectedItem() {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select an item to checkout.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Extract product details from the selected row
            String productName = cartTable.getValueAt(selectedRow, 0).toString();
            String priceStr = cartTable.getValueAt(selectedRow, 1).toString().replace("₱", "");
            int quantity = Integer.parseInt(cartTable.getValueAt(selectedRow, 2).toString());
            String totalStr = cartTable.getValueAt(selectedRow, 3).toString().replace("₱", "");
            
            BigDecimal itemPrice = new BigDecimal(priceStr);
            BigDecimal itemTotal = new BigDecimal(totalStr);
            BigDecimal tax = util.CheckoutCalculator.calculateTax(itemTotal);
            BigDecimal totalWithTax = itemTotal.add(tax);

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            
            // Show confirmation dialog with item details
            String confirmMessage = "Confirm Quick Checkout?\n\n" +
                "Product: " + productName + "\n" +
                "Quantity: " + quantity + "\n" +
                "Unit Price: ₱" + itemPrice + "\n" +
                "Item Total: ₱" + itemTotal + "\n" +
                "Tax: ₱" + df.format(tax) + "\n" +
                "---\n" +
                "Total: ₱" + df.format(totalWithTax);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                confirmMessage,
                "Confirm Item Checkout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Create sale for this single item
            List<CartItem> allItems = cartDAO.getCartItems(currentUser.getId());
            CartItem selectedItem = null;
            
            if (selectedRow < allItems.size()) {
                selectedItem = allItems.get(selectedRow);
            }
            
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Error: Could not find item details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verify stock availability
            if (selectedItem.getQuantity() > selectedItem.getAvailableStock()) {
                UIHelper.showError(this, "Insufficient stock for " + selectedItem.getProductName());
                return;
            }
            
            // Create sale for this item only
            Sale sale = new Sale();
            sale.setCustomerName(currentUser.getFullName());
            
            SaleItem saleItem = new SaleItem();
            saleItem.setProductId(selectedItem.getProductId());
            saleItem.setProductName(selectedItem.getProductName());
            saleItem.setQuantity(selectedItem.getQuantity());
            
            // Defensive null checks
            BigDecimal unitPrice = selectedItem.getUnitPrice();
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }
            saleItem.setUnitPrice(unitPrice);
            
            BigDecimal totalPrice = selectedItem.getTotalPrice();
            if (totalPrice == null) {
                totalPrice = BigDecimal.ZERO;
            }
            saleItem.setTotalPrice(totalPrice);
            
            sale.addItem(saleItem);
            
            sale.setSubtotal(selectedItem.getTotalPrice());
            sale.setTax(BigDecimal.ZERO);
            sale.setTotal(selectedItem.getTotalPrice());
            
            if (salesDAO.createSale(sale, currentUser.getId())) {
                // Remove only this item from cart
                cartDAO.removeFromCart(currentUser.getId(), selectedItem.getProductId());
                UIHelper.showSuccess(this, "Item checked out successfully!");
                loadCart();
                updateCartCount();
            } else {
                UIHelper.showError(this, "Failed to checkout item. Please try again.");
            }
        }
        
        private void openCheckoutDialog() {
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            if (items.isEmpty()) {
                UIHelper.showError(this, "Cart is empty");
                return;
            }
            
            CheckoutDialog checkoutDialog = new CheckoutDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentUser);
            checkoutDialog.setVisible(true);
            
            // Refresh cart after checkout
            loadCart();
            updateCartCount();
        }
        
        /*private void checkout() {
            try {
                List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
                if (items.isEmpty()) {
                    UIHelper.showError(this, "Cart is empty");
                    return;
                }
                
                // Calculate totals from table
                BigDecimal subtotal = BigDecimal.ZERO;
                for (int i = 0; i < cartTable.getRowCount(); i++) {
                    String totalStr = cartTable.getValueAt(i, 3).toString().replace("₱", "");
                    subtotal = subtotal.add(new BigDecimal(totalStr));
                }
                
                BigDecimal tax = BigDecimal.ZERO;
                BigDecimal total = subtotal.add(tax);
                
                // Show confirmation dialog
                int result = JOptionPane.showConfirmDialog(
                    this, 
                    "Confirm Checkout?\n\nSubtotal: ₱" + subtotal + "\nTax: ₱" + tax + "\nTotal: ₱" + total,
                    "Confirm Checkout",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                
                // Verify stock availability
                for (CartItem item : items) {
                    if (item.getQuantity() > item.getAvailableStock()) {
                        UIHelper.showError(this, "Insufficient stock for " + item.getProductName());
                        return;
                    }
                }
                
                // Create sale
                Sale sale = new Sale();
                sale.setCustomerName(currentUser.getFullName());
                
                for (CartItem cartItem : items) {
                    SaleItem saleItem = new SaleItem();
                    saleItem.setProductId(cartItem.getProductId());
                    saleItem.setProductName(cartItem.getProductName());
                    saleItem.setQuantity(cartItem.getQuantity());
                    
                    // Defensive null checks
                    BigDecimal unitPrice = cartItem.getUnitPrice();
                    if (unitPrice == null) {
                        unitPrice = BigDecimal.ZERO;
                    }
                    saleItem.setUnitPrice(unitPrice);
                    
                    BigDecimal totalPrice = cartItem.getTotalPrice();
                    if (totalPrice == null) {
                        totalPrice = BigDecimal.ZERO;
                    }
                    saleItem.setTotalPrice(totalPrice);
                    
                    sale.addItem(saleItem);
                }
                
                sale.setSubtotal(subtotal);
                sale.setTax(tax);
                sale.setTotal(total);
                
                // Create sale in database
                boolean success = salesDAO.createSale(sale, currentUser.getId());
                
                // Clear cart from database
                cartDAO.clearCart(currentUser.getId());
                
                // ALWAYS clear the JTable model after operations complete
                DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
                model.setRowCount(0);
                
                // ALWAYS reset all labels to ₱0.00
                subtotalLabel.setText("Subtotal: ₱0.00");
                taxLabel.setText("Tax: ₱0.00");
                totalLabel.setText("Total: ₱0.00");
                
                // Update cart count
                updateCartCount();
                
                // Show result message
                if (success) {
                    UIHelper.showSuccess(this, "Order placed successfully!");
                } else {
                    UIHelper.showError(this, "Failed to place order. Please try again.");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                UIHelper.showError(this, "An error occurred during checkout: " + e.getMessage());
            }
        } */
    } 
    
    class CustomerOrdersPanel extends JPanel {
        private JTable ordersTable;
        private DefaultTableModel tableModel;
        
        public CustomerOrdersPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);
            
            JLabel titleLabel = UIHelper.createHeaderLabel("My Orders");
            add(titleLabel, BorderLayout.NORTH);
            
            // Table
            JPanel tablePanel = UIHelper.createCard();
            tablePanel.setLayout(new BorderLayout());
            
            String[] columns = {"Order ID", "Date", "Items", "Total", "Status"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            ordersTable = new JTable(tableModel);
            UIHelper.styleTable(ordersTable);
            JScrollPane scrollPane = new JScrollPane(ordersTable);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            
            add(tablePanel, BorderLayout.CENTER);
            
            // Button
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(UIHelper.CONTENT_BG);
            
            JButton viewBtn = UIHelper.createPrimaryButton("View Details");
            viewBtn.addActionListener(e -> viewOrderDetails());
            buttonPanel.add(viewBtn);
            
            add(buttonPanel, BorderLayout.SOUTH);
            
            loadOrders();
        }
        
        private void loadOrders() {
            tableModel.setRowCount(0);
            List<Sale> sales = salesDAO.findByUserId(currentUser.getId());
            
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            for (Sale sale : sales) {
                    tableModel.addRow(new Object[]{
                    sale.getId(),
                    sale.getSaleDate().format(formatter),
                    sale.getItems().size() + " items",
                    "₱" + sale.getTotal(),
                    "Completed"
                });
            }
        }
        
        private void viewOrderDetails() {
            int row = ordersTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Please select an order");
                return;
            }
            
            int saleId = (int) tableModel.getValueAt(row, 0);
            Sale sale = salesDAO.findById(saleId);
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Order Details", true);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Header
            JPanel headerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            headerPanel.setBackground(Color.WHITE);
            headerPanel.add(UIHelper.createLabel("Order ID:"));
            headerPanel.add(UIHelper.createLabel(String.valueOf(sale.getId())));
            headerPanel.add(UIHelper.createLabel("Date:"));
            headerPanel.add(UIHelper.createLabel(sale.getSaleDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
            panel.add(headerPanel, BorderLayout.NORTH);
            
            // Items
            String[] columns = {"Product", "Quantity", "Price", "Total"};
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
            JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
            totalsPanel.setBackground(Color.WHITE);
            totalsPanel.add(UIHelper.createLabel("Subtotal:"));
            totalsPanel.add(UIHelper.createLabel("₱" + sale.getSubtotal()));
            totalsPanel.add(UIHelper.createLabel("Tax:"));
            totalsPanel.add(UIHelper.createLabel("₱" + sale.getTax()));
            totalsPanel.add(new JLabel("Total:"));
            JLabel totalLbl = new JLabel("₱" + sale.getTotal());
            totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            totalLbl.setForeground(UIHelper.SUCCESS_COLOR);
            totalsPanel.add(totalLbl);
            panel.add(totalsPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            dialog.setVisible(true);
        }
    }
    
    // ========== INNER CLASS: CustomerProfilePanel ==========
    class CustomerProfilePanel extends JPanel {
        private JTextField fullNameField, emailField, phoneField;
        private JTextArea addressArea;
        private UserDAO userDAO = new UserDAO();
        
        public CustomerProfilePanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);
            
            JLabel titleLabel = UIHelper.createHeaderLabel("My Profile");
            add(titleLabel, BorderLayout.NORTH);
            
            // Form Panel
            JPanel formPanel = UIHelper.createCard();
            formPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);
            
            // Username (read-only)
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(UIHelper.createLabel("Username:"), gbc);
            gbc.gridx = 1;
            JLabel usernameLabel = UIHelper.createLabel(currentUser.getUsername());
            usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            formPanel.add(usernameLabel, gbc);
            
            // Full Name
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(UIHelper.createLabel("Full Name:"), gbc);
            gbc.gridx = 1;
            fullNameField = UIHelper.createTextField();
            fullNameField.setText(currentUser.getFullName());
            fullNameField.setPreferredSize(new Dimension(300, 35));
            formPanel.add(fullNameField, gbc);
            
            // Email
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(UIHelper.createLabel("Email:"), gbc);
            gbc.gridx = 1;
            emailField = UIHelper.createTextField();
            emailField.setText(currentUser.getEmail());
            emailField.setPreferredSize(new Dimension(300, 35));
            formPanel.add(emailField, gbc);
            
            // Phone
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(UIHelper.createLabel("Phone:"), gbc);
            gbc.gridx = 1;
            phoneField = UIHelper.createTextField();
            phoneField.setText(currentUser.getPhone());
            phoneField.setPreferredSize(new Dimension(300, 35));
            formPanel.add(phoneField, gbc);
            
            // Address
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(UIHelper.createLabel("Address:"), gbc);
            gbc.gridx = 1;
            addressArea = new JTextArea(3, 20);
            addressArea.setText(currentUser.getAddress());
            addressArea.setFont(UIHelper.NORMAL_FONT);
            addressArea.setLineWrap(true);
            addressArea.setWrapStyleWord(true);
            addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR),
                new EmptyBorder(8, 12, 8, 12)
            ));
            formPanel.add(new JScrollPane(addressArea), gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonPanel.setBackground(Color.WHITE);
            
            JButton updateBtn = UIHelper.createPrimaryButton("Update Profile");
            updateBtn.addActionListener(e -> updateProfile());
            
            JButton changePassBtn = UIHelper.createSecondaryButton("Change Password");
            changePassBtn.addActionListener(e -> changePassword());
            
            buttonPanel.add(updateBtn);
            buttonPanel.add(changePassBtn);
            formPanel.add(buttonPanel, gbc);
            
            add(formPanel, BorderLayout.CENTER);
        }
        
        private void updateProfile() {
            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());
            currentUser.setAddress(addressArea.getText().trim());
            
            if (userDAO.updateProfile(currentUser)) {
                UIHelper.showSuccess(this, "Profile updated successfully!");
            } else {
                UIHelper.showError(this, "Failed to update profile");
            }
        }
        
        private void changePassword() {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Password", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JPasswordField oldPassField = new JPasswordField();
            JPasswordField newPassField = new JPasswordField();
            JPasswordField confirmPassField = new JPasswordField();
            
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(UIHelper.createLabel("Old Password:"), gbc);
            gbc.gridx = 1;
            panel.add(oldPassField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(UIHelper.createLabel("New Password:"), gbc);
            gbc.gridx = 1;
            panel.add(newPassField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(UIHelper.createLabel("Confirm Password:"), gbc);
            gbc.gridx = 1;
            panel.add(confirmPassField, gbc);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(Color.WHITE);
            
            JButton saveBtn = UIHelper.createSuccessButton("Change");
            saveBtn.addActionListener(e -> {
                String oldPass = new String(oldPassField.getPassword());
                String newPass = new String(newPassField.getPassword());
                String confirmPass = new String(confirmPassField.getPassword());
                
                if (!newPass.equals(confirmPass)) {
                    UIHelper.showError(dialog, "Passwords do not match");
                    return;
                }
                
                if (newPass.length() < 6) {
                    UIHelper.showError(dialog, "Password must be at least 6 characters");
                    return;
                }
                
                if (userDAO.changePassword(currentUser.getId(), oldPass, newPass)) {
                    UIHelper.showSuccess(dialog, "Password changed successfully!");
                    dialog.dispose();
                } else {
                    UIHelper.showError(dialog, "Old password is incorrect");
                }
            });
            
            JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
            cancelBtn.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);
            
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            panel.add(buttonPanel, gbc);
            
            dialog.add(panel);
            dialog.setVisible(true);
        }
    }
}