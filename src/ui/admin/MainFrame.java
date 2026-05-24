package ui.admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import ui.shared.LoginFrame;
import ui.user.ProductPanel;
import util.UIHelper;

public class MainFrame extends JFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JButton[] menuButtons;
    private String currentPanel = "dashboard";
    
    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Inventory Management System - Admin Panel");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        showPanel("dashboard");
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Main content area
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(UIHelper.CONTENT_BG);
        
        // Header
        JPanel header = createHeader();
        mainContent.add(header, BorderLayout.NORTH);
        
        // Content panel (where different panels will be shown)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIHelper.CONTENT_BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainContent.add(contentPanel, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIHelper.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Logo/Title
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(UIHelper.SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(250, 80));
        JLabel logoLabel = new JLabel("Inventory Admin");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        sidebar.add(logoPanel);
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Menu items
        String[] menuItems = {"Dashboard", "Products", "Categories", "Brands", "Suppliers", "Inventory", "Reports"};
        String[] menuIcons = {"", "", "", "", "", "", ""};
        menuButtons = new JButton[menuItems.length];
        
        for (int i = 0; i < menuItems.length; i++) {
            final String panelName = menuItems[i].toLowerCase();
            JButton btn = createMenuButton(menuIcons[i] + "  " + menuItems[i]);
            menuButtons[i] = btn;
            
            btn.addActionListener(e -> {
                showPanel(panelName);
                updateMenuButtonStyles();
            });
            
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(new Color(203, 213, 225)); // Slate 300
        btn.setBackground(UIHelper.SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 25, 12, 25));
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(UIHelper.PRIMARY_COLOR)) {
                    btn.setBackground(new Color(51, 65, 85)); // Slate 700
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(UIHelper.PRIMARY_COLOR)) {
                    btn.setBackground(UIHelper.SIDEBAR_BG);
                }
            }
        });
        
        return btn;
    }
    
    private void updateMenuButtonStyles() {
        for (JButton btn : menuButtons) {
            btn.setBackground(UIHelper.SIDEBAR_BG);
            btn.setForeground(new Color(203, 213, 225));
        }
        
        // Highlight current button
        String[] menuItems = {"Dashboard", "Products", "Categories", "Brands", "Suppliers", "Inventory", "Reports"};
        for (int i = 0; i < menuItems.length; i++) {
            if (menuItems[i].toLowerCase().equals(currentPanel)) {
                menuButtons[i].setBackground(UIHelper.PRIMARY_COLOR);
                menuButtons[i].setForeground(Color.WHITE);
                break;
            }
        }
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIHelper.BORDER_COLOR),
            new EmptyBorder(15, 25, 15, 25)
        ));
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIHelper.TEXT_PRIMARY);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(Color.WHITE);
        
        JLabel userLabel = new JLabel("👤 " + currentUser.getFullName());
        userLabel.setFont(UIHelper.NORMAL_FONT);
        userLabel.setForeground(UIHelper.TEXT_SECONDARY);
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UIHelper.NORMAL_FONT);
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(UIHelper.TEXT_PRIMARY);
        logoutBtn.setBorder(new EmptyBorder(5, 15, 5, 15));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());
        
        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private void logout() {
        if (UIHelper.showConfirm(this, "Are you sure you want to logout?")) {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
    
    private void showPanel(String panelName) {
        currentPanel = panelName;
        contentPanel.removeAll();
        
        JPanel panel;
        switch (panelName) {
            case "dashboard":
                panel = new DashboardPanel();
                break;
            case "products":
                panel = new ProductPanel();
                break;
            case "categories":
                panel = new CategoryPanel();
                break;
            case "brands":
                panel = new BrandPanel();
                break;
            case "suppliers":
                panel = new SupplierPanel();
                break;
            
            case "inventory":
                panel = new InventoryPanel();
                break;
            case "reports":
                panel = new ReportsPanel();
                break;
            default:
                panel = new DashboardPanel();
        }
        
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateMenuButtonStyles();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}