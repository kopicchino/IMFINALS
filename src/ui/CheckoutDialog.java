package ui;

import dao.CartDAO;
import dao.SalesDAO;
import model.CartItem;
import model.Sale;
import model.SaleItem;
import model.User;
import util.UIHelper;
import util.CheckoutCalculator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * CheckoutDialog — Completely polished, high-fidelity checkout interface.
 * Implements a modern multi-section wizard look with styled form inputs, 
 * detailed invoice breakdown, and a prominent Place Order CTA footer.
 */
public class CheckoutDialog extends JDialog {
    private User currentUser;
    private CartDAO cartDAO = new CartDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private List<CartItem> cartItems;
    
    // Customer info fields
    private JTextField fullNameField;
    private JTextArea addressArea;
    private JTextField phoneField;
    
    // Summary labels
    private JLabel subtotalLabel;
    private JLabel deliveryLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    
    public CheckoutDialog(JFrame parent, User user) {
        super(parent, "Secure Checkout Portal", true);
        this.currentUser = user;
        this.cartItems = cartDAO.getCartItems(user.getId());
        
        setSize(560, 680);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        UIHelper.styleDialog(this);
        initComponents();
        loadCartData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 1. Header
        JPanel headerPanel = UIHelper.createDialogHeader(
            "Secure Checkout Portal", 
            "Please review order items, specify shipping coordinates, and complete order placement.", 
            UIHelper.SUCCESS_COLOR
        );
        add(headerPanel, BorderLayout.NORTH);
        
        // 2. Central Scrollable Form Container
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIHelper.CONTENT_BG);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        
        // Order Summary
        contentPanel.add(createOrderSummarySection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // Delivery Details
        contentPanel.add(createCustomerInfoSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // Pricing Summary
        contentPanel.add(createTotalSection());
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // 3. Footer Checkout Action Bar
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createOrderSummarySection() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout(0, 12));
        
        JLabel titleLabel = UIHelper.createSubHeaderLabel("Order Summary Checklist");
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Visual text layout of cart items
        JPanel itemsListPanel = new JPanel();
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));
        itemsListPanel.setBackground(Color.WHITE);
        
        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                BigDecimal itemTotal = item.getTotalPriceSafe();
                
                JPanel itemRow = new JPanel(new BorderLayout());
                itemRow.setBackground(Color.WHITE);
                itemRow.setBorder(new EmptyBorder(4, 0, 4, 0));
                
                JLabel descLbl = UIHelper.createLabel(item.getProductName() + " (x" + item.getQuantity() + ")");
                JLabel priceLbl = new JLabel("₱" + itemTotal.setScale(2, RoundingMode.HALF_UP));
                priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                priceLbl.setForeground(UIHelper.TEXT_PRIMARY);
                
                itemRow.add(descLbl, BorderLayout.WEST);
                itemRow.add(priceLbl, BorderLayout.EAST);
                
                itemsListPanel.add(itemRow);
            }
        } else {
            JLabel emptyLabel = new JLabel("No active items located in shopping cart.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(UIHelper.TEXT_SECONDARY);
            itemsListPanel.add(emptyLabel);
        }
        
        panel.add(itemsListPanel, BorderLayout.CENTER);
        
        // Enforce maximum sizing for clean scrolling layout
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        return panel;
    }
    
    private JPanel createCustomerInfoSection() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        
        JLabel titleLabel = UIHelper.createSubHeaderLabel("Delivery & Consignee Information");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 12, 6);
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.25;
        panel.add(UIHelper.createLabel("Full Name *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.75;
        fullNameField = UIHelper.createTextField();
        fullNameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        fullNameField.setPreferredSize(new Dimension(0, 36));
        panel.add(fullNameField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.25;
        panel.add(UIHelper.createLabel("Delivery Address *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.75;
        addressArea = new JTextArea(3, 20);
        addressArea.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
        addressArea.setFont(UIHelper.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Focus highlights for JTextArea
        addressArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                addressArea.setBorder(BorderFactory.createCompoundBorder(
                    new UIHelper.RoundedBorder(UIHelper.INPUT_FOCUS, 8, 2),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                addressArea.setBorder(BorderFactory.createCompoundBorder(
                    new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(addrScroll, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.25;
        panel.add(UIHelper.createLabel("Contact Phone"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.75;
        phoneField = UIHelper.createTextField();
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPreferredSize(new Dimension(0, 36));
        panel.add(phoneField, gbc);
        
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        return panel;
    }
    
    private JPanel createTotalSection() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 1.0;
        
        JLabel title = UIHelper.createSubHeaderLabel("Payment & Cost Breakdown");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 10, 6);
        panel.add(title, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);
        
        // Subtotal
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.6;
        panel.add(UIHelper.createLabel("Basket Subtotal:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.EAST;
        subtotalLabel = new JLabel("₱0.00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subtotalLabel.setForeground(UIHelper.TEXT_PRIMARY);
        panel.add(subtotalLabel, gbc);
        
        // Delivery Fee
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.6; gbc.anchor = GridBagConstraints.WEST;
        panel.add(UIHelper.createLabel("Standard Delivery Fee:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.EAST;
        deliveryLabel = new JLabel("₱" + CheckoutCalculator.DELIVERY_FEE);
        deliveryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deliveryLabel.setForeground(UIHelper.WARNING_COLOR.darker());
        panel.add(deliveryLabel, gbc);
        
        // Tax
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.6; gbc.anchor = GridBagConstraints.WEST;
        panel.add(UIHelper.createLabel("Government VAT Tax (0.00%):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.EAST;
        taxLabel = new JLabel("₱0.00");
        taxLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        taxLabel.setForeground(UIHelper.TEXT_PRIMARY);
        panel.add(taxLabel, gbc);
        
        // Divider line
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(8, 6, 8, 6);
        JSeparator sep = new JSeparator();
        sep.setForeground(UIHelper.BORDER_COLOR);
        panel.add(sep, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.6; gbc.anchor = GridBagConstraints.WEST;
        JLabel totalText = new JLabel("GRAND INVOICE TOTAL:");
        totalText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalText.setForeground(UIHelper.TEXT_PRIMARY);
        panel.add(totalText, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.EAST;
        totalLabel = new JLabel("₱0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(UIHelper.SUCCESS_DARK);
        panel.add(totalLabel, gbc);
        
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel Checkout");
        cancelBtn.setPreferredSize(new Dimension(0, 46));
        cancelBtn.addActionListener(e -> dispose());
        
        JButton checkoutBtn = UIHelper.createSuccessButton("⚡ Securely Place Order Now");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.setPreferredSize(new Dimension(0, 46));
        checkoutBtn.addActionListener(e -> processCheckout());
        
        buttonRow.add(cancelBtn);
        buttonRow.add(checkoutBtn);
        
        footer.add(buttonRow, BorderLayout.CENTER);
        return footer;
    }
    
    private void loadCartData() {
        CheckoutCalculator.CheckoutSummary summary = CheckoutCalculator.calculateCheckout(cartItems);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
        
        subtotalLabel.setText("₱" + df.format(summary.subtotal));
        taxLabel.setText("₱" + df.format(summary.tax));
        deliveryLabel.setText("₱" + df.format(summary.deliveryFee));
        totalLabel.setText("₱" + df.format(summary.total));
    }
    
    private boolean validateCustomerInfo() {
        String fullName = fullNameField.getText().trim();
        String address = addressArea.getText().trim();
        
        if (fullName.isEmpty()) {
            UIHelper.showError(this, "Recipient Full Name is required to dispatch delivery.");
            fullNameField.requestFocus();
            return false;
        }
        
        if (address.isEmpty()) {
            UIHelper.showError(this, "Shipping Address is required to calculate dispatch route.");
            addressArea.requestFocus();
            return false;
        }
        
        if (fullName.length() < 3) {
            UIHelper.showError(this, "Full Name must contain at least 3 character details.");
            fullNameField.requestFocus();
            return false;
        }
        
        if (address.length() < 10) {
            UIHelper.showError(this, "Address details are too brief. Specify building, street, and city (min 10 chars).");
            addressArea.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void processCheckout() {
        if (!validateCustomerInfo()) return;
        
        CheckoutCalculator.CheckoutSummary summary = CheckoutCalculator.calculateCheckout(cartItems);
        if (!summary.isValid) {
            UIHelper.showError(this, summary.validationError);
            return;
        }
        
        try {
            String fullName = fullNameField.getText().trim();
            String address = addressArea.getText().trim();
            String phone = phoneField.getText().trim();
            
            // Create sale details
            Sale sale = new Sale();
            sale.setCustomerName(fullName);
            sale.setDeliveryAddress(address);
            sale.setDeliveryPhone(phone.isEmpty() ? null : phone);
            sale.setDeliveryFee(CheckoutCalculator.DELIVERY_FEE);
            
            for (CartItem cartItem : cartItems) {
                SaleItem saleItem = new SaleItem();
                saleItem.setProductId(cartItem.getProductId());
                saleItem.setProductName(cartItem.getProductName());
                saleItem.setQuantity(cartItem.getQuantity());
                saleItem.setUnitPrice(cartItem.getUnitPriceSafe());
                saleItem.setTotalPrice(cartItem.getTotalPriceSafe());
                
                sale.addItem(saleItem);
            }
            
            sale.setSubtotal(summary.subtotal);
            sale.setTax(summary.tax);
            sale.setTotal(summary.total);
            
            if (salesDAO.createSale(sale, currentUser.getId())) {
                cartDAO.clearCart(currentUser.getId());
                UIHelper.showSuccess(this, 
                    "Order Successfully Placed!\n\n" +
                    "Invoice ID: #" + sale.getId() + "\n" +
                    "Grand Total: ₱" + new java.text.DecimalFormat("#,##0.00").format(summary.total) + "\n\n" +
                    "Your order will be dispatched to:\n" + address
                );
                dispose();
            } else {
                UIHelper.showError(this, "Transaction rejected by database layer. Recheck credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIHelper.showError(this, "Unexpected exception compiling checkout: " + e.getMessage());
        }
    }
}
