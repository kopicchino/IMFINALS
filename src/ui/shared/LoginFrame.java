package ui.shared;

import dao.UserDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import ui.admin.MainFrame;
import ui.user.CustomerFrame;
import util.UIHelper;


public class LoginFrame extends JFrame {
    private UserDAO userDAO = new UserDAO();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    
    public LoginFrame() {
        setTitle("Inventory Management System - Login");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.CONTENT_BG);
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(500, 180));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
        
        JLabel logoLabel = new JLabel("");
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Inventory System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Please login to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(219, 234, 254));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(logoLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 50, 40, 50));
        
        // User Type Selection
        JLabel typeLabel = new JLabel("Login As:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        typeLabel.setForeground(UIHelper.TEXT_PRIMARY);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String[] userTypes = {"Customer", "Admin"};
        userTypeCombo = new JComboBox<>(userTypes);
        userTypeCombo.setFont(UIHelper.NORMAL_FONT);
        userTypeCombo.setMaximumSize(new Dimension(400, 50));
        userTypeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        userTypeCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Username Field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        usernameLabel.setForeground(UIHelper.TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = new JTextField();
        usernameField.setFont(UIHelper.NORMAL_FONT);
        usernameField.setMaximumSize(new Dimension(400, 50));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Password Field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        passwordLabel.setForeground(UIHelper.TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField = new JPasswordField();
        passwordField.setFont(UIHelper.NORMAL_FONT);
        passwordField.setMaximumSize(new Dimension(400, 50));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Login Button
        JButton loginButton = UIHelper.createPrimaryButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setMaximumSize(new Dimension(400, 45));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());
        
        // Register Link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setBackground(Color.WHITE);
        registerPanel.setMaximumSize(new Dimension(400, 40));
        registerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel registerText = new JLabel("Don't have an account? ");
        registerText.setFont(UIHelper.NORMAL_FONT);
        registerText.setForeground(UIHelper.TEXT_SECONDARY);
        
        JLabel registerLink = new JLabel("Register here");
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 18));
        registerLink.setForeground(UIHelper.PRIMARY_COLOR);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openRegistration();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerLink.setForeground(UIHelper.PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerLink.setForeground(UIHelper.PRIMARY_COLOR);
            }
        });
        
        registerPanel.add(registerText);
        registerPanel.add(registerLink);
        
        // Add components to form
        formPanel.add(typeLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(userTypeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(usernameLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(loginButton);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(registerPanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Enter key listener
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedType = (String) userTypeCombo.getSelectedItem();
        
        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please enter username and password");
            return;
        }
        
        User user = userDAO.authenticate(username, password);
        
        if (user != null) {
            // Check if user type matches selection
            if (selectedType.equals("Admin") && !user.isAdmin()) {
                UIHelper.showError(this, "Invalid admin credentials");
                return;
            }
            if (selectedType.equals("Customer") && !user.isCustomer()) {
                UIHelper.showError(this, "Invalid customer credentials");
                return;
            }
            
            // Login successful
            if (user.isAdmin()) {
                MainFrame adminFrame = new MainFrame(user);
                adminFrame.setVisible(true);
            } else {
                CustomerFrame customerFrame = new CustomerFrame(user);
                customerFrame.setVisible(true);
            }
            this.dispose();
        } else {
            UIHelper.showError(this, "Invalid username or password");
            passwordField.setText("");
        }
    }
    
    private void openRegistration() {
        RegistrationFrame regFrame = new RegistrationFrame(this);
        regFrame.setVisible(true);
        this.setVisible(false);
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