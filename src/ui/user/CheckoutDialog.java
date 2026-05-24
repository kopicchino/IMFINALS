package ui.user;

import dao.CartDAO;
import dao.SalesDAO;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.CartItem;
import model.Sale;
import model.SaleItem;
import model.User;
import util.CheckoutCalculator;
import util.UIHelper;
//import java.util.Objects;

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
        super(parent, "Checkout", true);
        this.currentUser = user;
        this.cartItems = cartDAO.getCartItems(user.getId());
        
        setSize(600, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initComponents();
        loadCartData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(UIHelper.CONTENT_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = UIHelper.createHeaderLabel("Complete Your Order");
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Center: Scrollable content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIHelper.CONTENT_BG);
        
        // Order Summary Section
        contentPanel.add(createOrderSummarySection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Customer Info Section
        contentPanel.add(createCustomerInfoSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Total Section
        contentPanel.add(createTotalSection());
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createOrderSummarySection() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = UIHelper.createSubHeaderLabel("Order Summary");
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Items list with null-safe handling
        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                BigDecimal itemTotal = item.getTotalPriceSafe();
                JLabel itemLabel = new JLabel(
                    item.getProductName() + " x" + item.getQuantity() + " = ₱" + itemTotal
                );
                itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                itemLabel.setForeground(UIHelper.TEXT_PRIMARY);
                panel.add(itemLabel);
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        } else {
            JLabel emptyLabel = new JLabel("No items in cart");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(UIHelper.TEXT_SECONDARY);
            panel.add(emptyLabel);
        }
        
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return panel;
    }
    
    private JPanel createCustomerInfoSection() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        
        JLabel titleLabel = UIHelper.createSubHeaderLabel("Delivery Information");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(UIHelper.createLabel("Full Name *"), gbc);
        
        gbc.gridx = 1;
        fullNameField = UIHelper.createTextField();
        fullNameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        fullNameField.setPreferredSize(new Dimension(300, 35));
        panel.add(fullNameField, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(UIHelper.createLabel("Address *"), gbc);
        
        gbc.gridx = 1;
        addressArea = new JTextArea(3, 20);
        addressArea.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
        addressArea.setFont(UIHelper.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
        panel.add(new JScrollPane(addressArea), gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(UIHelper.createLabel("Phone (Optional)"), gbc);
        
        gbc.gridx = 1;
        phoneField = UIHelper.createTextField();
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPreferredSize(new Dimension(300, 35));
        panel.add(phoneField, gbc);
        
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        return panel;
    }
    
    private JPanel createTotalSection() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new GridLayout(4, 2, 20, 10));
        
        // Subtotal
        JLabel subtotalLabelText = new JLabel("Subtotal:");
        subtotalLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtotalLabel = new JLabel("₱0.00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subtotalLabel.setForeground(UIHelper.TEXT_PRIMARY);
        
        panel.add(subtotalLabelText);
        panel.add(subtotalLabel);
        
        // Delivery Fee
        JLabel deliveryLabelText = new JLabel("Delivery Fee:");
        deliveryLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deliveryLabel = new JLabel("₱" + CheckoutCalculator.DELIVERY_FEE);
        deliveryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deliveryLabel.setForeground(UIHelper.WARNING_COLOR);
        
        panel.add(deliveryLabelText);
        panel.add(deliveryLabel);
        
        // Tax
        JLabel taxLabelText = new JLabel("Tax:");
        taxLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taxLabel = new JLabel("₱0.00");
        taxLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        taxLabel.setForeground(UIHelper.TEXT_PRIMARY);
        
        panel.add(taxLabelText);
        panel.add(taxLabel);
        
        // Total
        JLabel totalLabelText = new JLabel("Total:");
        totalLabelText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel = new JLabel("₱0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(UIHelper.SUCCESS_COLOR);
        
        panel.add(totalLabelText);
        panel.add(totalLabel);
        
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        JButton checkoutBtn = UIHelper.createPrimaryButton("Complete Order");
        checkoutBtn.addActionListener(e -> processCheckout());
        
        panel.add(cancelBtn);
        panel.add(checkoutBtn);
        
        return panel;
    }
    
    private void loadCartData() {
        // Use centralized calculation utility
        CheckoutCalculator.CheckoutSummary summary = CheckoutCalculator.calculateCheckout(cartItems);

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
        
        subtotalLabel.setText("₱" + df.format(summary.subtotal));
        taxLabel.setText("₱" +df.format(summary.tax));
        deliveryLabel.setText("₱" + df.format(summary.deliveryFee));
        totalLabel.setText("₱" + df.format(summary.total));
    }
    
   
    private boolean validateCustomerInfo() {
        String fullName = fullNameField.getText().trim();
        String address = addressArea.getText().trim();
        
        if (fullName.isEmpty()) {
            UIHelper.showError(this, "Full Name is required");
            fullNameField.requestFocus();
            return false;
        }
        
        if (address.isEmpty()) {
            UIHelper.showError(this, "Address is required");
            addressArea.requestFocus();
            return false;
        }
        
        if (fullName.length() < 3) {
            UIHelper.showError(this, "Full Name must be at least 3 characters");
            fullNameField.requestFocus();
            return false;
        }
        
        if (address.length() < 10) {
            UIHelper.showError(this, "Address must be at least 10 characters");
            addressArea.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void processCheckout() {
        // Validate customer info
        if (!validateCustomerInfo()) {
            return;
        }
        
        CheckoutCalculator.CheckoutSummary summary = CheckoutCalculator.calculateCheckout(cartItems);
        
        if (!summary.isValid) {
            UIHelper.showError(this, summary.validationError);
            return;
        }
        
        try {
            String fullName = fullNameField.getText().trim();
            String address = addressArea.getText().trim();
            String phone = phoneField.getText().trim();
            
            // Create sale with all details
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
                
                // Use null-safe getters from CartItem
                saleItem.setUnitPrice(cartItem.getUnitPriceSafe());
                saleItem.setTotalPrice(cartItem.getTotalPriceSafe());
                
                sale.addItem(saleItem);
            }
            
            // Set totals from calculation
            sale.setSubtotal(summary.subtotal);
            sale.setTax(summary.tax);
            sale.setTotal(summary.total);
            
            // Save to database
            if (salesDAO.createSale(sale, currentUser.getId())) {
                cartDAO.clearCart(currentUser.getId());
                UIHelper.showSuccess(this, 
                    "Order Placed Successfully!\n\n" +
                    "Order ID: " + sale.getId() + "\n" +
                    "Total: ₱" + new java.text.DecimalFormat("#,##0.00").format(summary.total) + "\n\n" +
                    "Delivery Address:\n" + address
                );
                dispose();
            } else {
                UIHelper.showError(this, "Failed to process order. Please try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            UIHelper.showError(this, "An error occurred during checkout:\n" + e.getMessage());
        }
    }
}
