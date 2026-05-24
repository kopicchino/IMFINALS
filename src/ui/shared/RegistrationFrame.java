package ui.shared;

import dao.UserDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import util.UIHelper;

public class RegistrationFrame extends JFrame {
    private UserDAO userDAO = new UserDAO();
    private LoginFrame loginFrame;
    private JTextField usernameField, fullNameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JTextArea addressArea;
    
    public RegistrationFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("Customer Registration");
        setSize(550, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.CONTENT_BG);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.SUCCESS_COLOR);
        headerPanel.setPreferredSize(new Dimension(550, 100));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Register as a customer");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 252, 231));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // Full Name
        formPanel.add(createLabel("Full Name *"));
        fullNameField = createTextField();
        formPanel.add(fullNameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Username
        formPanel.add(createLabel("Username *"));
        usernameField = createTextField();
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Email
        formPanel.add(createLabel("Email"));
        emailField = createTextField();
        formPanel.add(emailField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Phone
        formPanel.add(createLabel("Phone"));
        phoneField = createTextField();
        formPanel.add(phoneField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Address
        formPanel.add(createLabel("Address"));
        addressArea = new JTextArea(3, 20);
        addressArea.setFont(UIHelper.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setMaximumSize(new Dimension(450, 70));
        addressScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(addressScroll);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Password
        formPanel.add(createLabel("Password *"));
        passwordField = createPasswordField();
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Confirm Password
        formPanel.add(createLabel("Confirm Password *"));
        confirmPasswordField = createPasswordField();
        formPanel.add(confirmPasswordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(450, 50));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton registerButton = UIHelper.createSuccessButton("Register");
        registerButton.setPreferredSize(new Dimension(180, 45));
        registerButton.addActionListener(e -> performRegistration());
        
        JButton cancelButton = UIHelper.createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(180, 45));
        cancelButton.addActionListener(e -> {
            loginFrame.setVisible(true);
            dispose();
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(UIHelper.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIHelper.NORMAL_FONT);
        field.setMaximumSize(new Dimension(450, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(UIHelper.NORMAL_FONT);
        field.setMaximumSize(new Dimension(450, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    private void performRegistration() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validation
        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please fill in all required fields (*)");
            return;
        }
        
        if (username.length() < 4) {
            UIHelper.showError(this, "Username must be at least 4 characters long");
            return;
        }
        
        if (password.length() < 6) {
            UIHelper.showError(this, "Password must be at least 6 characters long");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            UIHelper.showError(this, "Passwords do not match");
            return;
        }
        
        // Create user object
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserType("CUSTOMER");
        
        // Register
        if (userDAO.register(user)) {
            UIHelper.showSuccess(this, "Registration successful! Please login.");
            loginFrame.setVisible(true);
            dispose();
        } else {
            UIHelper.showError(this, "Registration failed. Username might already exist.");
        }
    }
}