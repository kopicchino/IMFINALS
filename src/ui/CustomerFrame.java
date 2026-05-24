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
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CustomerFrame — Redesigned visual e-commerce portal for customers.
 * Introduces dynamic responsive navbar tabs, elegant 2-column shopping carts,
 * gorgeous retail catalog grids featuring visual stock meters and category bands,
 * and integrated transaction details.
 */
public class CustomerFrame extends JFrame {
    private User currentUser;
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private CartDAO cartDAO = new CartDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private CustomerProfileDAO profileDAO = new CustomerProfileDAO();
    private CardDAO cardDAO = new CardDAO();

    private JPanel contentPanel;
    private JLabel cartCountLabel;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    
    // Sidebar list buttons tracker
    private JButton shopNavBtn;
    private JButton ordersNavBtn;
    private JButton safetyNavBtn;
    private JButton profileNavBtn;
    private JButton cartNavBtn;

    public CustomerFrame(User user) {
        this.currentUser = user;
        setTitle("ISMS Retail Hub — Customer Portal");
        setSize(1350, 880);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.CONTENT_BG);

        // Navigation Bar
        JPanel navbar = createNavbar();
        add(navbar, BorderLayout.NORTH);

        // Content Area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIHelper.CONTENT_BG);
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        add(contentPanel, BorderLayout.CENTER);

        showShopPanel();
    }

    private JPanel createNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.WHITE);
        navbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIHelper.BORDER_COLOR),
            new EmptyBorder(12, 30, 12, 30)
        ));

        // Brand Logo Left
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setBackground(Color.WHITE);
        JLabel logoIcon = new JLabel("🛍️");
        logoIcon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JLabel brandLabel = new JLabel("ISMS E-SHOP");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(UIHelper.PRIMARY_COLOR);
        brandPanel.add(logoIcon);
        brandPanel.add(brandLabel);
        navbar.add(brandPanel, BorderLayout.WEST);

        // Middle Nav Tabs
        JPanel centerTabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        centerTabs.setBackground(Color.WHITE);

        shopNavBtn = createTabButton("Shop Catalog");
        shopNavBtn.addActionListener(e -> { setActiveTab(shopNavBtn); showShopPanel(); });
        centerTabs.add(shopNavBtn);

        ordersNavBtn = createTabButton("My Orders");
        ordersNavBtn.addActionListener(e -> { setActiveTab(ordersNavBtn); showOrdersPanel(); });
        centerTabs.add(ordersNavBtn);

        safetyNavBtn = createTabButton("Payment Security");
        safetyNavBtn.addActionListener(e -> { setActiveTab(safetyNavBtn); showCardsPanel(); });
        centerTabs.add(safetyNavBtn);

        profileNavBtn = createTabButton("My 360° Profile");
        profileNavBtn.addActionListener(e -> { setActiveTab(profileNavBtn); showProfilePanel(); });
        centerTabs.add(profileNavBtn);

        navbar.add(centerTabs, BorderLayout.CENTER);

        // Right Actions (Cart, Member info, Logout)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightActions.setBackground(Color.WHITE);

        // Cart button with pill badge count
        JPanel cartContainer = new JPanel(new BorderLayout(8, 0));
        cartContainer.setBackground(Color.WHITE);
        
        cartNavBtn = createTabButton("Cart 🛒");
        cartNavBtn.addActionListener(e -> { setActiveTab(cartNavBtn); showCartPanel(); });
        cartContainer.add(cartNavBtn, BorderLayout.CENTER);

        cartCountLabel = new JLabel("0", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.DANGER_COLOR);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setPreferredSize(new Dimension(20, 20));
        cartCountLabel.setOpaque(false);
        updateCartCount();
        
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        badgeWrapper.setBackground(Color.WHITE);
        badgeWrapper.add(cartCountLabel);
        cartContainer.add(badgeWrapper, BorderLayout.EAST);
        
        rightActions.add(cartContainer);

        // Welcome avatar circular simulation
        JLabel memberInfo = UIHelper.createSecondaryLabel("Member: " + currentUser.getFullName());
        memberInfo.setFont(UIHelper.NORMAL_FONT);
        rightActions.add(memberInfo);

        JButton logoutBtn = UIHelper.createDangerGhostButton("Logout Door");
        logoutBtn.setPreferredSize(new Dimension(110, 36));
        logoutBtn.addActionListener(e -> logout());
        rightActions.add(logoutBtn);

        navbar.add(rightActions, BorderLayout.EAST);

        // Default Active Selection
        setActiveTab(shopNavBtn);

        return navbar;
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() && !getBackground().equals(UIHelper.PRIMARY_LIGHT)) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getBackground().equals(UIHelper.PRIMARY_LIGHT)) {
                    g2.setColor(UIHelper.PRIMARY_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIHelper.BUTTON_FONT);
        btn.setForeground(UIHelper.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(Color.WHITE);
        return btn;
    }

    private void setActiveTab(JButton activeBtn) {
        JButton[] buttons = {shopNavBtn, ordersNavBtn, safetyNavBtn, profileNavBtn, cartNavBtn};
        for (JButton btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackground(UIHelper.PRIMARY_LIGHT);
                btn.setForeground(UIHelper.PRIMARY_COLOR);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(UIHelper.TEXT_SECONDARY);
            }
        }
    }

    private void updateCartCount() {
        int count = cartDAO.getCartItemCount(currentUser.getId());
        cartCountLabel.setText(String.valueOf(count));
        cartCountLabel.setVisible(count > 0);
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

    private void showCardsPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerCardsPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showProfilePanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerProfilePanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        if (UIHelper.showConfirm(this, "Are you sure you want to exit the store and logout?")) {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }

    // =========================================================================
    // INNER CLASSES — PORTAL PANELS
    // =========================================================================

    /**
     * CustomerShopPanel — Interactive catalog shopping layout.
     * Features grid of category striped cards, progress stock indicators,
     * and auto-triggered in-memory searches.
     */
    class CustomerShopPanel extends JPanel {
        private JPanel productsPanel;
        private JTextField searchField;
        private JComboBox<Category> categoryFilter;

        public CustomerShopPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);

            // Filter Area Card
            JPanel filterCard = UIHelper.createCard();
            filterCard.setLayout(new BorderLayout());
            filterCard.setBorder(BorderFactory.createCompoundBorder(
                new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 10, 1),
                new EmptyBorder(12, 16, 12, 16)
            ));

            JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            leftFilter.setBackground(Color.WHITE);

            searchField = UIHelper.createTextField("Search catalog by keywords...");
            searchField.setPreferredSize(new Dimension(280, 36));
            
            // Auto real-time search
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void run() { loadProducts(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { run(); }
            });

            categoryFilter = new JComboBox<>();
            categoryFilter.addItem(null);
            categoryDAO.findAll().forEach(categoryFilter::addItem);
            UIHelper.styleComboBox(categoryFilter);
            categoryFilter.setPreferredSize(new Dimension(180, 36));
            categoryFilter.addActionListener(e -> loadProducts());

            leftFilter.add(UIHelper.createSecondaryLabel("Filter Keywords:"));
            leftFilter.add(searchField);
            leftFilter.add(UIHelper.createSecondaryLabel("Category Group:"));
            leftFilter.add(categoryFilter);

            filterCard.add(leftFilter, BorderLayout.WEST);
            
            add(filterCard, BorderLayout.NORTH);

            // Product Cards Grid Scroll
            productsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
            productsPanel.setBackground(UIHelper.CONTENT_BG);

            JScrollPane scrollPane = new JScrollPane(productsPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            
            add(scrollPane, BorderLayout.CENTER);

            loadProducts();
        }

        private void loadProducts() {
            productsPanel.removeAll();
            String keyword = searchField.getText().trim();
            Category cat = (Category) categoryFilter.getSelectedItem();
            Integer catId = cat != null ? cat.getId() : null;

            List<Product> products = productDAO.search(keyword.isEmpty() ? null : keyword, catId, null, null);

            if (products.isEmpty()) {
                JPanel emptyState = UIHelper.createEmptyState("🔍", "No Products Available", "Try searching for a different item name or clear filters.");
                productsPanel.setLayout(new BorderLayout());
                productsPanel.add(emptyState, BorderLayout.CENTER);
            } else {
                productsPanel.setLayout(new GridLayout(0, 3, 20, 20));
                for (Product p : products) {
                    productsPanel.add(createProductCard(p));
                }
            }
            productsPanel.revalidate();
            productsPanel.repaint();
        }

        private JPanel createProductCard(Product p) {
            JPanel card = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw category color stripe at the top of the card (6px thick)
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Generate color based on category name hashCode
                    int nameHash = p.getCategoryName() != null ? p.getCategoryName().hashCode() : 0;
                    Color categoryStripeColor = new Color(
                        Math.abs((nameHash * 17) % 200) + 30,
                        Math.abs((nameHash * 31) % 200) + 30,
                        Math.abs((nameHash * 43) % 200) + 30
                    );
                    
                    g2.setColor(categoryStripeColor);
                    g2.fillRoundRect(0, 0, getWidth(), 8, 8, 8);
                    g2.fillRect(0, 4, getWidth(), 4); // flatten bottom rounded edges of stripe
                    g2.dispose();
                }
            };
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 12, 1),
                new EmptyBorder(16, 20, 20, 20)
            ));

            JLabel nameLabel = new JLabel(p.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            nameLabel.setForeground(UIHelper.TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel catLabel = new JLabel(p.getCategoryName().toUpperCase() + " • " + p.getBrandName());
            catLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            catLabel.setForeground(UIHelper.TEXT_MUTED);
            catLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel priceLabel = new JLabel("₱" + df.format(p.getSellingPrice()));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            priceLabel.setForeground(UIHelper.SUCCESS_DARK);
            priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            int qty = p.getStockQuantity();
            JLabel stockLabel = new JLabel(qty == 0 ? "Out of Stock" : qty <= 5 ? "Only " + qty + " left!" : "In Stock: " + qty);
            stockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            stockLabel.setForeground(qty == 0 ? UIHelper.DANGER_COLOR : qty <= 5 ? UIHelper.WARNING_COLOR.darker() : UIHelper.TEXT_SECONDARY);
            stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Dynamic mini stock visual meter bar
            JPanel stockMeter = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = getWidth();
                    int h = getHeight();
                    g2.setColor(new Color(241, 245, 249)); // Slate-100 bg
                    g2.fillRoundRect(0, 0, w, h, h, h);
                    
                    if (qty > 0) {
                        double percentage = Math.min(1.0, qty / 50.0); // 50 units = 100% capacity
                        Color fill = qty <= 5 ? UIHelper.WARNING_COLOR : UIHelper.PRIMARY_COLOR;
                        g2.setColor(fill);
                        g2.fillRoundRect(0, 0, (int)(w * percentage), h, h, h);
                    }
                    g2.dispose();
                }
            };
            stockMeter.setPreferredSize(new Dimension(160, 6));
            stockMeter.setMaximumSize(new Dimension(160, 6));
            stockMeter.setAlignmentX(Component.CENTER_ALIGNMENT);
            stockMeter.setOpaque(false);

            JButton addBtn = UIHelper.createPrimaryButton("🛒 Buy Now");
            addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            addBtn.addActionListener(e -> addToCart(p));
            addBtn.setEnabled(qty > 0);

            card.add(Box.createRigidArea(new Dimension(0, 8)));
            card.add(catLabel);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(nameLabel);
            
            card.add(Box.createRigidArea(new Dimension(0, 12)));
            card.add(priceLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            
            card.add(stockLabel);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(stockMeter);
            
            card.add(Box.createRigidArea(new Dimension(0, 16)));
            card.add(addBtn);

            return card;
        }

        private void addToCart(Product p) {
            String input = JOptionPane.showInputDialog(this, "Enter shopping quantity details for " + p.getName() + ":", "1");
            if (input != null) {
                try {
                    int qty = Integer.parseInt(input.trim());
                    if (qty <= 0) {
                        UIHelper.showError(this, "Requested quantity must be greater than zero.");
                        return;
                    }
                    if (qty > p.getStockQuantity()) {
                        UIHelper.showError(this, "Available warehouse stock exceeded. Limit is: " + p.getStockQuantity());
                        return;
                    }

                    if (cartDAO.addToCart(currentUser.getId(), p.getId(), qty)) {
                        UIHelper.showSuccess(this, "Successfully locked items into cart!");
                        updateCartCount();
                    } else {
                        UIHelper.showError(this, "Failed to write cart records.");
                    }
                } catch (NumberFormatException ex) {
                    UIHelper.showError(this, "Invalid numeric formatting inputs.");
                }
            }
        }
    }

    /**
     * CustomerCartPanel — Redesigned checkout table portal.
     * Splitted into two columns: shopping list (left) and invoice totals (right).
     */
    class CustomerCartPanel extends JPanel {
        private JTable cartTable;
        private DefaultTableModel tableModel;
        private JLabel subtotalLabel, taxLabel, totalLabel;

        public CustomerCartPanel() {
            setLayout(new BorderLayout(20, 0));
            setBackground(UIHelper.CONTENT_BG);
            setBorder(new EmptyBorder(12, 0, 0, 0));

            // Left side: Items Table card
            JPanel leftCard = UIHelper.createCard();
            leftCard.setLayout(new BorderLayout(0, 16));
            
            JPanel listHeader = new JPanel(new BorderLayout());
            listHeader.setBackground(Color.WHITE);
            listHeader.add(UIHelper.createSubHeaderLabel("Shopping Cart Items List"), BorderLayout.WEST);
            
            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actionsPanel.setBackground(Color.WHITE);
            JButton updateBtn = UIHelper.createPrimaryButton("Update Qty");
            updateBtn.addActionListener(e -> updateQty());
            JButton removeBtn = UIHelper.createDangerButton("Remove");
            removeBtn.addActionListener(e -> removeItem());
            JButton clearBtn = UIHelper.createSecondaryButton("Empty Cart");
            clearBtn.addActionListener(e -> clearCart());
            actionsPanel.add(updateBtn);
            actionsPanel.add(removeBtn);
            actionsPanel.add(clearBtn);
            listHeader.add(actionsPanel, BorderLayout.EAST);
            
            leftCard.add(listHeader, BorderLayout.NORTH);

            String[] cols = {"Product Name", "Unit Selling Price", "Units Ordered", "Calculated Total", "Warehouse Available"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

            cartTable = new JTable(tableModel);
            UIHelper.styleTable(cartTable);
            UIHelper.applyAlternatingRows(cartTable);
            
            cartTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            cartTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            cartTable.getColumnModel().getColumn(2).setPreferredWidth(90);
            cartTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            cartTable.getColumnModel().getColumn(4).setPreferredWidth(110);
            
            JScrollPane scroll = new JScrollPane(cartTable);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            leftCard.add(scroll, BorderLayout.CENTER);
            add(leftCard, BorderLayout.CENTER);

            // Right side: Sticky checkout breakdown card
            JPanel rightCard = UIHelper.createCard();
            rightCard.setLayout(new BorderLayout(0, 20));
            rightCard.setPreferredSize(new Dimension(380, 0));
            
            rightCard.add(UIHelper.createSubHeaderLabel("Order Cost Summary"), BorderLayout.NORTH);
            
            JPanel billingPanel = new JPanel(new GridBagLayout());
            billingPanel.setBackground(Color.WHITE);
            GridBagConstraints bGbc = new GridBagConstraints();
            bGbc.fill = GridBagConstraints.HORIZONTAL;
            bGbc.insets = new Insets(8, 0, 8, 0);
            bGbc.weightx = 1.0;
            
            bGbc.gridx = 0; bGbc.gridy = 0; bGbc.weightx = 0.6;
            billingPanel.add(UIHelper.createSecondaryLabel("Items Subtotal:"), bGbc);
            bGbc.gridx = 1; bGbc.weightx = 0.4; bGbc.anchor = GridBagConstraints.EAST;
            subtotalLabel = new JLabel("₱0.00");
            subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            billingPanel.add(subtotalLabel, bGbc);
            
            bGbc.gridx = 0; bGbc.gridy = 1; bGbc.weightx = 0.6; bGbc.anchor = GridBagConstraints.WEST;
            billingPanel.add(UIHelper.createSecondaryLabel("Government VAT Tax (12%):"), bGbc);
            bGbc.gridx = 1; bGbc.weightx = 0.4; bGbc.anchor = GridBagConstraints.EAST;
            taxLabel = new JLabel("₱0.00");
            taxLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            billingPanel.add(taxLabel, bGbc);
            
            bGbc.gridx = 0; bGbc.gridy = 2; bGbc.gridwidth = 2; bGbc.weightx = 1.0; bGbc.insets = new Insets(12, 0, 12, 0);
            JSeparator sep = new JSeparator();
            sep.setForeground(UIHelper.BORDER_COLOR);
            billingPanel.add(sep, bGbc);
            bGbc.gridwidth = 1;
            bGbc.insets = new Insets(8, 0, 8, 0);
            
            bGbc.gridx = 0; bGbc.gridy = 3; bGbc.weightx = 0.6; bGbc.anchor = GridBagConstraints.WEST;
            JLabel totalText = new JLabel("ORDER GRAND TOTAL:");
            totalText.setFont(new Font("Segoe UI", Font.BOLD, 14));
            billingPanel.add(totalText, bGbc);
            
            bGbc.gridx = 1; bGbc.weightx = 0.4; bGbc.anchor = GridBagConstraints.EAST;
            totalLabel = new JLabel("₱0.00");
            totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            totalLabel.setForeground(UIHelper.SUCCESS_DARK);
            billingPanel.add(totalLabel, bGbc);
            
            JPanel centerWrap = new JPanel(new BorderLayout());
            centerWrap.setBackground(Color.WHITE);
            centerWrap.add(billingPanel, BorderLayout.NORTH);
            rightCard.add(centerWrap, BorderLayout.CENTER);
            
            // Buttons at the bottom of the right card
            JPanel checkoutBtnsPanel = new JPanel(new GridLayout(2, 1, 0, 12));
            checkoutBtnsPanel.setBackground(Color.WHITE);
            
            JButton checkoutBtn = UIHelper.createPrimaryButton("Proceed Securely to Checkout");
            checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            checkoutBtn.setPreferredSize(new Dimension(0, 44));
            checkoutBtn.addActionListener(e -> openCheckout());
            
            JButton quickCheckoutBtn = UIHelper.createSuccessButton("⚡ Direct Profile Quick Checkout");
            quickCheckoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            quickCheckoutBtn.setPreferredSize(new Dimension(0, 44));
            quickCheckoutBtn.addActionListener(e -> quickCheckout());
            
            checkoutBtnsPanel.add(checkoutBtn);
            checkoutBtnsPanel.add(quickCheckoutBtn);
            rightCard.add(checkoutBtnsPanel, BorderLayout.SOUTH);
            
            add(rightCard, BorderLayout.EAST);

            loadCart();
        }

        private void loadCart() {
            tableModel.setRowCount(0);
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            BigDecimal subtotal = BigDecimal.ZERO;

            for (CartItem item : items) {
                tableModel.addRow(new Object[]{
                    item.getProductName(),
                    "₱" + df.format(item.getUnitPriceSafe()),
                    item.getQuantity() + " units",
                    "₱" + df.format(item.getTotalPriceSafe()),
                    item.getAvailableStock() + " in stock"
                });
                subtotal = subtotal.add(item.getTotalPriceSafe());
            }

            BigDecimal tax = subtotal.multiply(new BigDecimal("0.12")); // 12% standard VAT
            BigDecimal total = subtotal.add(tax);

            subtotalLabel.setText("₱" + df.format(subtotal));
            taxLabel.setText("₱" + df.format(tax));
            totalLabel.setText("₱" + df.format(total));
        }

        private void updateQty() {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Select a shopping cart row from the table first.");
                return;
            }
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            CartItem item = items.get(row);

            String input = JOptionPane.showInputDialog(this, "Update order count for " + item.getProductName() + ":", item.getQuantity());
            if (input != null) {
                try {
                    int qty = Integer.parseInt(input.trim());
                    if (qty <= 0) {
                        removeItem();
                        return;
                    }
                    if (qty > item.getAvailableStock()) {
                        UIHelper.showError(this, "Insufficient stock left in store. Available stock count is: " + item.getAvailableStock());
                        return;
                    }
                    if (cartDAO.updateQuantity(currentUser.getId(), item.getProductId(), qty)) {
                        loadCart();
                        updateCartCount();
                    }
                } catch (NumberFormatException ex) {
                    UIHelper.showError(this, "Invalid quantity inputs.");
                }
            }
        }

        private void removeItem() {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Select a shopping cart row from the table first.");
                return;
            }
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            CartItem item = items.get(row);

            if (UIHelper.showConfirm(this, "Are you sure you want to remove " + item.getProductName() + " from your cart?")) {
                if (cartDAO.removeFromCart(currentUser.getId(), item.getProductId())) {
                    loadCart();
                    updateCartCount();
                }
            }
        }

        private void clearCart() {
            if (cartTable.getRowCount() == 0) return;
            if (UIHelper.showConfirm(this, "Are you sure you want to completely empty your cart?")) {
                cartDAO.clearCart(currentUser.getId());
                loadCart();
                updateCartCount();
            }
        }

        private void quickCheckout() {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Please highlight a specific card item to quick checkout.");
                return;
            }
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            CartItem selectedItem = items.get(row);

            if (selectedItem.getQuantity() > selectedItem.getAvailableStock()) {
                UIHelper.showError(this, "Insufficient warehouse stock for " + selectedItem.getProductName());
                return;
            }

            BigDecimal totalWithTax = selectedItem.getTotalPriceSafe().multiply(new BigDecimal("1.12"));

            int result = JOptionPane.showConfirmDialog(this,
                "Confirm Direct Purchase Checkout?\n\n" +
                "Product: " + selectedItem.getProductName() + "\n" +
                "Qty Requested: " + selectedItem.getQuantity() + "\n" +
                "Subtotal Cost: ₱" + df.format(selectedItem.getTotalPriceSafe()) + "\n" +
                "Total Due (incl 12% VAT): ₱" + df.format(totalWithTax) + "\n\n" +
                "Order will be dispatched immediately using your profile home address.",
                "Confirm Quick Purchase", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                Sale sale = new Sale();
                sale.setCustomerName(currentUser.getFullName());
                sale.setDeliveryAddress(currentUser.getAddress() != null ? currentUser.getAddress() : "Default home coordinates");
                sale.setDeliveryPhone(currentUser.getPhone() != null ? currentUser.getPhone() : "N/A");
                sale.setDeliveryFee(BigDecimal.ZERO);

                SaleItem sItem = new SaleItem();
                sItem.setProductId(selectedItem.getProductId());
                sItem.setProductName(selectedItem.getProductName());
                sItem.setQuantity(selectedItem.getQuantity());
                sItem.setUnitPrice(selectedItem.getUnitPriceSafe());
                sItem.setTotalPrice(selectedItem.getTotalPriceSafe());
                sale.addItem(sItem);

                sale.setSubtotal(selectedItem.getTotalPriceSafe());
                sale.setTax(selectedItem.getTotalPriceSafe().multiply(new BigDecimal("0.12")));
                sale.setTotal(totalWithTax);

                if (salesDAO.createSale(sale, currentUser.getId())) {
                    cartDAO.removeFromCart(currentUser.getId(), selectedItem.getProductId());
                    UIHelper.showSuccess(this, "Checkout complete! Order dispatched.");
                    loadCart();
                    updateCartCount();
                } else {
                    UIHelper.showError(this, "Database rejected transaction record.");
                }
            }
        }

        private void openCheckout() {
            List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
            if (items.isEmpty()) {
                UIHelper.showError(this, "Your cart is empty. Pick items first.");
                return;
            }

            CheckoutDialog dialog = new CheckoutDialog(CustomerFrame.this, currentUser);
            dialog.setVisible(true);

            loadCart();
            updateCartCount();
        }
    }

    /**
     * CustomerOrdersPanel — Simple tracking panel for checking dispatch reports.
     */
    class CustomerOrdersPanel extends JPanel {
        private JTable ordersTable;
        private DefaultTableModel tableModel;

        public CustomerOrdersPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);

            JPanel contentCard = UIHelper.createCard();
            contentCard.setLayout(new BorderLayout(0, 16));
            
            contentCard.add(UIHelper.createSubHeaderLabel("Historical Checkout Purchase Logs"), BorderLayout.NORTH);

            String[] cols = {"Invoice ID", "Checkout Timestamp", "Destination Address", "Order Subtotal", "12% VAT Paid", "Checkout Grand Total", "Status badge"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

            ordersTable = new JTable(tableModel);
            UIHelper.styleTable(ordersTable);
            UIHelper.applyAlternatingRows(ordersTable);
            UIHelper.applyStatusRenderer(ordersTable, 6); // Column 6 status badge
            
            ordersTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            ordersTable.getColumnModel().getColumn(1).setPreferredWidth(160);
            ordersTable.getColumnModel().getColumn(2).setPreferredWidth(250);
            ordersTable.getColumnModel().getColumn(3).setPreferredWidth(110);
            ordersTable.getColumnModel().getColumn(4).setPreferredWidth(90);
            ordersTable.getColumnModel().getColumn(5).setPreferredWidth(110);
            ordersTable.getColumnModel().getColumn(6).setPreferredWidth(120);

            JScrollPane scroll = new JScrollPane(ordersTable);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            contentCard.add(scroll, BorderLayout.CENTER);
            
            add(contentCard, BorderLayout.CENTER);

            loadOrders();
        }

        private void loadOrders() {
            tableModel.setRowCount(0);
            List<Sale> sales = salesDAO.findByUserId(currentUser.getId());
            for (Sale s : sales) {
                tableModel.addRow(new Object[]{
                    "#" + s.getId(),
                    s.getSaleDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    s.getDeliveryAddress() != null ? s.getDeliveryAddress() : "Default home coordinates",
                    "₱" + df.format(s.getSubtotal()),
                    "₱" + df.format(s.getTax()),
                    "₱" + df.format(s.getTotal()),
                    "COMPLETED"
                });
            }
        }
    }

    /**
     * CustomerCardsPanel — Disputes payment center with token security lockouts.
     */
    class CustomerCardsPanel extends JPanel {
        private JTable cardsTable;
        private DefaultTableModel tableModel;
        private JTextField cardNumField;

        public CustomerCardsPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);

            JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
            panel.setBackground(UIHelper.CONTENT_BG);

            // Left side: token cards list
            JPanel listCard = UIHelper.createCard();
            listCard.setLayout(new BorderLayout(0, 16));
            listCard.add(UIHelper.createSubHeaderLabel("Linked Payment Cards (Tokenized Vault)"), BorderLayout.NORTH);

            String[] cols = {"Card Index", "Masked Security Number", "Restriction Status"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            cardsTable = new JTable(tableModel);
            UIHelper.styleTable(cardsTable);
            UIHelper.applyAlternatingRows(cardsTable);
            UIHelper.applyStatusRenderer(cardsTable, 2); // Column 2 status badge
            
            JScrollPane listScroll = new JScrollPane(cardsTable);
            listScroll.setBorder(BorderFactory.createEmptyBorder());
            listScroll.getViewport().setBackground(Color.WHITE);
            listCard.add(listScroll, BorderLayout.CENTER);

            // Right side: security dispute card
            JPanel controlCard = UIHelper.createCard();
            controlCard.setLayout(new BoxLayout(controlCard, BoxLayout.Y_AXIS));

            JLabel disputeTitle = UIHelper.createSubHeaderLabel("Dispute & Lost Card Safety Protocols");
            
            JLabel safetyText = new JLabel("<html><body>Under the strict <b>Card Lock Policy</b>, flagging a card lost or compromised triggers an <b>instant token freeze</b> and triggers a <b>Cascading Security Lockout</b>, which disables user login permissions immediately pending manual compliance review.</body></html>");
            safetyText.setFont(UIHelper.NORMAL_FONT);
            safetyText.setForeground(UIHelper.TEXT_SECONDARY);

            cardNumField = UIHelper.createTextField("Enter 16-digit credit card...");
            cardNumField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            
            JButton addCardBtn = UIHelper.createPrimaryButton("➕ Link New Payment Card");
            addCardBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            addCardBtn.addActionListener(e -> linkCard());

            JButton disputeBtn = UIHelper.createDangerButton("🚨 Report compromised selected card lost/stolen");
            disputeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            disputeBtn.addActionListener(e -> disputeCard());

            controlCard.add(disputeTitle);
            controlCard.add(Box.createRigidArea(new Dimension(0, 12)));
            controlCard.add(safetyText);
            controlCard.add(Box.createRigidArea(new Dimension(0, 20)));
            
            controlCard.add(UIHelper.createSecondaryLabel("Input New Card details:"));
            controlCard.add(Box.createRigidArea(new Dimension(0, 4)));
            controlCard.add(cardNumField);
            controlCard.add(Box.createRigidArea(new Dimension(0, 12)));
            controlCard.add(addCardBtn);
            
            controlCard.add(Box.createRigidArea(new Dimension(0, 16)));
            JSeparator sep = new JSeparator();
            sep.setForeground(UIHelper.BORDER_COLOR);
            controlCard.add(sep);
            
            controlCard.add(Box.createRigidArea(new Dimension(0, 16)));
            controlCard.add(UIHelper.createSecondaryLabel("Dispute Actions:"));
            controlCard.add(Box.createRigidArea(new Dimension(0, 4)));
            controlCard.add(disputeBtn);

            panel.add(listCard);
            panel.add(controlCard);

            add(UIHelper.createPageHeader("Payment Security center", "Manage token cards and disputes safety controls."), BorderLayout.NORTH);
            add(panel, BorderLayout.CENTER);

            loadCards();
        }

        private void loadCards() {
            tableModel.setRowCount(0);
            List<CustomerCard> cards = cardDAO.findByUserId(currentUser.getId());
            for (CustomerCard c : cards) {
                String cardStatus = c.getStatus() != null ? c.getStatus() : "ACTIVE";
                tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getCardNumberMasked(),
                    cardStatus.toUpperCase()
                });
            }
        }

        private void linkCard() {
            String cardNum = cardNumField.getText().trim();
            if (cardNum.length() != 16 || !cardNum.matches("\\d+")) {
                UIHelper.showError(this, "Please enter a valid 16-digit credit card number layout.");
                return;
            }
            if (cardDAO.addCard(currentUser.getId(), cardNum)) {
                UIHelper.showSuccess(this, "Card tokenized and secured to payment profile.");
                cardNumField.setText("");
                loadCards();
            } else {
                UIHelper.showError(this, "Failed to link card to payment registry.");
            }
        }

        private void disputeCard() {
            int row = cardsTable.getSelectedRow();
            if (row < 0) {
                UIHelper.showError(this, "Please select a specific card row from the table first.");
                return;
            }
            int cardId = (int) cardsTable.getValueAt(row, 0);
            String masked = (String) cardsTable.getValueAt(row, 1);

            int res = JOptionPane.showConfirmDialog(this,
                "CRITICAL SECURITY PROTOCOL INITIATING\n\n" +
                "You are flagging card " + masked + " as compromised.\n\n" +
                "Initiating lost dispute cascade will:\n" +
                "1. Freeze card token from processing payments.\n" +
                "2. Engages user lockout locks suspending this account instantly.\n" +
                "3. Terminate active session and drop user to portal login.\n\n" +
                "Do you confirm that you want to fire the cascading locking cascade?",
                "Trigger Account Lockdown", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (res == JOptionPane.YES_OPTION) {
                if (cardDAO.updateCardStatus(cardId, "Reported", currentUser.getId(), true)) {
                    JOptionPane.showMessageDialog(this,
                        "ACCOUNT SYSTEM ACCESS SUSPENDED\n\n" +
                        "This shopper session is now terminated. The user account has been locked.\n" +
                        "Please coordinate with safety support teams to verify identity.",
                        "Access Revoked", JOptionPane.ERROR_MESSAGE);
                    
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                    CustomerFrame.this.dispose();
                } else {
                    UIHelper.showError(this, "Failed to initiate lock cascade.");
                }
            }
        }
    }

    /**
     * CustomerProfilePanel — Interactive behavioral tags page.
     */
    class CustomerProfilePanel extends JPanel {
        private JCheckBox consentBox;
        private JLabel segmentValLabel, riskValLabel, tagsValLabel, predictivePreferencesValLabel;
        private JPanel offersPanel;

        public CustomerProfilePanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(UIHelper.CONTENT_BG);

            JPanel mainGrid = new JPanel(new GridLayout(1, 2, 20, 0));
            mainGrid.setBackground(UIHelper.CONTENT_BG);

            // Left card: consent + values
            JPanel profileCard = UIHelper.createCard();
            profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));

            JLabel title = UIHelper.createSubHeaderLabel("360° Behavioral Intelligence Profiling Card");
            
            JPanel consentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            consentPanel.setBackground(Color.WHITE);
            consentBox = new JCheckBox("I consent to PH Data Privacy Act (DPA) of 2012 dynamic profiling");
            consentBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
            consentBox.setBackground(Color.WHITE);
            consentBox.setForeground(UIHelper.PRIMARY_COLOR);
            consentBox.addActionListener(e -> updateConsent());
            consentPanel.add(consentBox);

            profileCard.add(title);
            profileCard.add(Box.createRigidArea(new Dimension(0, 16)));
            profileCard.add(consentPanel);
            profileCard.add(Box.createRigidArea(new Dimension(0, 16)));
            
            JSeparator sep = new JSeparator();
            sep.setForeground(UIHelper.BORDER_COLOR);
            profileCard.add(sep);
            profileCard.add(Box.createRigidArea(new Dimension(0, 16)));

            segmentValLabel = UIHelper.createLabel("Lifestyle Classification: --");
            segmentValLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            tagsValLabel = UIHelper.createLabel("Dynamic Behavioral Tags: --");
            tagsValLabel.setForeground(UIHelper.PRIMARY_COLOR);
            tagsValLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            riskValLabel = UIHelper.createLabel("Security Risk Profile Deviation: --");
            riskValLabel.setFont(UIHelper.NORMAL_FONT);
            
            predictivePreferencesValLabel = new JLabel("<html><body><b>Anticipated Future Purchases:</b><br>--</td></body></html>");
            predictivePreferencesValLabel.setFont(UIHelper.NORMAL_FONT);
            predictivePreferencesValLabel.setForeground(UIHelper.TEXT_SECONDARY);

            profileCard.add(segmentValLabel);
            profileCard.add(Box.createRigidArea(new Dimension(0, 12)));
            profileCard.add(tagsValLabel);
            profileCard.add(Box.createRigidArea(new Dimension(0, 12)));
            profileCard.add(riskValLabel);
            profileCard.add(Box.createRigidArea(new Dimension(0, 20)));
            profileCard.add(predictivePreferencesValLabel);

            // Right card: Personalized recommendations
            JPanel recommendationsCard = UIHelper.createCard();
            recommendationsCard.setLayout(new BorderLayout(0, 16));
            recommendationsCard.add(UIHelper.createSubHeaderLabel("Targeted Discount & Promotion Recommendations"), BorderLayout.NORTH);

            offersPanel = new JPanel();
            offersPanel.setLayout(new BoxLayout(offersPanel, BoxLayout.Y_AXIS));
            offersPanel.setBackground(Color.WHITE);
            
            JScrollPane scroll = new JScrollPane(offersPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            recommendationsCard.add(scroll, BorderLayout.CENTER);

            mainGrid.add(profileCard);
            mainGrid.add(recommendationsCard);

            add(UIHelper.createPageHeader("Compliance & Behavioral Analytics dashboard", "Control profile DPA consents and view dynamic discount offers."), BorderLayout.NORTH);
            add(mainGrid, BorderLayout.CENTER);

            loadProfileData();
        }

        private void updateConsent() {
            boolean consent = consentBox.isSelected();
            if (profileDAO.saveConsent(currentUser.getId(), consent)) {
                if (consent) {
                    profileDAO.reprofileCustomer(currentUser.getId());
                    UIHelper.showSuccess(this, "DPA Profiling Consent Granted. Behavioral trackers active.");
                } else {
                    UIHelper.showSuccess(this, "Consent Withdrawn. Profiling databases purged.");
                }
                loadProfileData();
            } else {
                UIHelper.showError(this, "Failed to commit consent adjustments.");
            }
        }

        private void loadProfileData() {
            CustomerProfile cp = profileDAO.findByUserId(currentUser.getId());
            
            if (cp == null) {
                profileDAO.createProfile(currentUser.getId(), false);
                cp = profileDAO.findByUserId(currentUser.getId());
            }

            consentBox.setSelected(cp.isConsentDpa());
            offersPanel.removeAll();

            if (cp.isConsentDpa()) {
                segmentValLabel.setText("Lifestyle Classification: " + cp.getSegment());
                tagsValLabel.setText("Dynamic Behavioral Tags: " + cp.getDynamicTags());
                
                String color = cp.getRiskScore().doubleValue() > 50 ? "red" : "green";
                riskValLabel.setText("<html><body>Security Risk Profile: <font color='" + color + "'><b>" + cp.getRiskScore() + "%</b> Deviation</font></body></html>");
                
                predictivePreferencesValLabel.setText("<html><body><b>Anticipated Future Purchases:</b><br>" + 
                    (cp.getPredictivePreferences() != null ? cp.getPredictivePreferences() : "Analytical modeling in progress.") + "</body></html>");

                List<TargetedOffer> offers = profileDAO.findOffersBySegment(cp.getSegment());
                for (TargetedOffer offer : offers) {
                    offersPanel.add(createOfferCard(offer));
                    offersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
                if (offers.isEmpty()) {
                    JLabel label = new JLabel("No retail promotional codes matching segment are active.", SwingConstants.CENTER);
                    label.setFont(UIHelper.NORMAL_FONT);
                    label.setForeground(UIHelper.TEXT_SECONDARY);
                    offersPanel.add(label);
                }
            } else {
                segmentValLabel.setText("Lifestyle Classification: [RESTRICTED — PRIVACY RULE]");
                tagsValLabel.setText("Dynamic Behavioral Tags: [RESTRICTED — PRIVACY RULE]");
                riskValLabel.setText("Security Risk Profile: [RESTRICTED — PRIVACY RULE]");
                predictivePreferencesValLabel.setText("<html><body><b>Anticipated Future Purchases:</b><br>[Profiling inactive. Grant DPA profiling consent to trigger recommendation engine.]</body></html>");

                JLabel label = new JLabel("<html><body>Personalized targeted campaigns are disabled.<br>Enable DPA Consent to allow purchase pattern analytics and receive discount codes.</body></html>", SwingConstants.CENTER);
                label.setFont(UIHelper.NORMAL_FONT);
                label.setForeground(UIHelper.TEXT_SECONDARY);
                offersPanel.add(label);
            }
            offersPanel.revalidate();
            offersPanel.repaint();
        }

        private JPanel createOfferCard(TargetedOffer offer) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(UIHelper.CONTENT_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
                new EmptyBorder(12, 12, 12, 12)
            ));

            JLabel title = new JLabel(offer.getTitle());
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            title.setForeground(UIHelper.TEXT_PRIMARY);

            JLabel desc = new JLabel("<html><body>" + offer.getDescription() + "</body></html>");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            desc.setForeground(UIHelper.TEXT_SECONDARY);

            JLabel promo = new JLabel("PROMO CODE: " + offer.getPromoCode());
            promo.setFont(new Font("Segoe UI", Font.BOLD, 12));
            promo.setForeground(UIHelper.SUCCESS_DARK);

            card.add(title);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(desc);
            card.add(Box.createRigidArea(new Dimension(0, 8)));
            card.add(promo);

            return card;
        }
    }
}