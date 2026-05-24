package ui;

import dao.CartDAO;
import model.User;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class MiniCart extends JFrame {
    private User currentUser;
    private CartDAO cartDAO = new CartDAO();
    private JLabel itemCountLabel;
    private JLabel totalLabel;
    
    public MiniCart(User user) {
        this.currentUser = user;
        setTitle("Mini Cart - " + user.getFullName());
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
        loadCartData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("Shopping Cart Summary");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(UIHelper.PRIMARY_COLOR);
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        contentPanel.setBackground(Color.WHITE);
        
        // Item count
        JLabel itemLabel = new JLabel("Items in Cart:");
        itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemCountLabel = new JLabel("0 items");
        itemCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        itemCountLabel.setForeground(UIHelper.TEXT_PRIMARY);
        
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.add(itemLabel);
        itemPanel.add(itemCountLabel);
        
        // Total price
        JLabel totalLabelText = new JLabel("Total Amount:");
        totalLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalLabel = new JLabel("₱0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(UIHelper.SUCCESS_COLOR);
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.add(totalLabelText);
        totalPanel.add(totalLabel);
        
        // Empty space
        JLabel spacer = new JLabel();
        
        contentPanel.add(itemPanel);
        contentPanel.add(totalPanel);
        contentPanel.add(spacer);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton viewFullBtn = UIHelper.createPrimaryButton("View Full Cart");
        viewFullBtn.addActionListener(e -> openFullCart());
        
        JButton closeBtn = UIHelper.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(viewFullBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadCartData() {
        try {
            int itemCount = cartDAO.getCartItemCount(currentUser.getId());
            itemCountLabel.setText(itemCount + " " + (itemCount == 1 ? "item" : "items"));
            
            BigDecimal total = BigDecimal.ZERO;
            List<?> items = cartDAO.getCartItems(currentUser.getId());
            
            for (Object obj : items) {
                if (obj instanceof model.CartItem) {
                    model.CartItem item = (model.CartItem) obj;
                    BigDecimal itemTotal = item.getTotalPrice();
                    if (itemTotal != null) {
                        total = total.add(itemTotal);
                    }
                }
            }
            
            totalLabel.setText("₱" + total);
        } catch (Exception e) {
            e.printStackTrace();
            itemCountLabel.setText("Error");
            totalLabel.setText("₱0.00");
        }
    }
    
    private void openFullCart() {
        // This can be implemented based on your needs
        // For now, just show a message
        JOptionPane.showMessageDialog(this, "Full cart view coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
