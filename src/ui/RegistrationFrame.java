package ui;

import dao.UserDAO;
import model.User;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * RegistrationFrame — Modern two-column registration form.
 * Matches the LoginFrame split-screen aesthetic.
 */
public class RegistrationFrame extends JFrame {
    private UserDAO userDAO = new UserDAO();
    private LoginFrame loginFrame;
    private JTextField usernameField, fullNameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JTextArea addressArea;
    private JCheckBox dpaConsentCheckbox;

    public RegistrationFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("ISMS — Create Account");
        setSize(960, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridLayout(1, 2));

        // ── LEFT: Branding ───────────────────────────────────────────────────
        root.add(buildBrandPanel());

        // ── RIGHT: Form ──────────────────────────────────────────────────────
        root.add(buildFormPanel());

        add(root);
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0,           new Color(15, 23, 42),
                    getWidth(), getHeight(), new Color(6, 95, 70)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-60, -60, 300, 300);
                g2.fillOval(getWidth() - 80, getHeight() - 100, 250, 250);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 50, 60, 50));

        panel.add(Box.createVerticalGlue());

        // Back to login link
        JLabel backLink = new JLabel("← Back to Login");
        backLink.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backLink.setForeground(new Color(148, 163, 184));
        backLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        backLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loginFrame.setVisible(true);
                dispose();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) { backLink.setForeground(Color.WHITE); }
            public void mouseExited(java.awt.event.MouseEvent e)  { backLink.setForeground(new Color(148, 163, 184)); }
        });
        panel.add(backLink);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Icon block
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.SUCCESS_COLOR);
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                String s = "+";
                g2.drawString(s, (56 - fm.stringWidth(s)) / 2, 56 / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(56, 56));
        iconCircle.setMaximumSize(new Dimension(56, 56));
        iconCircle.setMinimumSize(new Dimension(56, 56));
        iconCircle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(iconCircle);
        panel.add(Box.createRigidArea(new Dimension(0, 24)));

        JLabel title = new JLabel("Join ISMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);

        JLabel sub = new JLabel("Create your customer account");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(new Color(148, 163, 184));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sub);

        panel.add(Box.createRigidArea(new Dimension(0, 36)));

        String[] perks = {
            "\u2713  Personalized product recommendations",
            "\u2713  Secure payment card management",
            "\u2713  Complete order history tracking",
            "\u2713  360\u00B0 behavioral intelligence profile"
        };
        for (String p : perks) {
            JLabel pl = new JLabel(p);
            pl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pl.setForeground(new Color(148, 163, 184));
            pl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(pl);
            panel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        panel.add(Box.createVerticalGlue());

        JLabel version = new JLabel("PH Data Privacy Act 2012 compliant");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(71, 85, 105));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(version);

        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(36, 50, 36, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5, 6, 5, 6);

        // Heading
        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(UIHelper.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(heading, gbc);

        JLabel sub = UIHelper.createSecondaryLabel("Fill in the details below to register");
        gbc.gridy = 1; gbc.insets = new Insets(2, 6, 16, 6);
        form.add(sub, gbc);
        gbc.insets = new Insets(5, 6, 5, 6);

        // Row 1: Full Name | Username
        fullNameField = UIHelper.createTextField("Enter your full name");
        usernameField = UIHelper.createTextField("Choose a username");
        gbc.gridwidth = 1;
        addField(form, gbc, 2, 0, "Full Name *", fullNameField);
        addField(form, gbc, 2, 1, "Username *", usernameField);

        // Row 2: Email | Phone
        emailField = UIHelper.createTextField("your@email.com");
        phoneField = UIHelper.createTextField("+63 XXX XXX XXXX");
        addField(form, gbc, 3, 0, "Email", emailField);
        addField(form, gbc, 3, 1, "Phone", phoneField);

        // Row 3: Password | Confirm Password
        passwordField = UIHelper.createPasswordField();
        confirmPasswordField = UIHelper.createPasswordField();
        addField(form, gbc, 4, 0, "Password *", passwordField);
        addField(form, gbc, 4, 1, "Confirm Password *", confirmPasswordField);

        // Address — full width
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel addrLbl = makeFormLabel("Address");
        form.add(addrLbl, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        addressArea = new JTextArea(3, 20);
        addressArea.setFont(UIHelper.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setBorder(BorderFactory.createEmptyBorder());
        form.add(addrScroll, gbc);

        // DPA Consent
        gbc.gridy = 7; gbc.insets = new Insets(12, 6, 5, 6);
        JPanel consentPanel = new JPanel(new BorderLayout(10, 0));
        consentPanel.setBackground(new Color(240, 253, 244));
        consentPanel.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(new Color(187, 247, 208), 8, 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        dpaConsentCheckbox = new JCheckBox(
            "<html><body style='width:360px'>I consent to the collection of my purchase history for personalized recommendations " +
            "<b>(Philippine Data Privacy Act of 2012)</b></body></html>"
        );
        dpaConsentCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dpaConsentCheckbox.setBackground(new Color(240, 253, 244));
        dpaConsentCheckbox.setForeground(new Color(21, 128, 61));
        consentPanel.add(dpaConsentCheckbox);
        form.add(consentPanel, gbc);

        // Buttons
        gbc.gridy = 8; gbc.insets = new Insets(20, 6, 5, 6);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setBackground(Color.WHITE);

        JButton cancelBtn = UIHelper.createGhostButton("Back to Login");
        cancelBtn.addActionListener(e -> {
            loginFrame.setVisible(true);
            dispose();
        });

        JButton registerBtn = UIHelper.createSuccessButton("Create Account");
        registerBtn.addActionListener(e -> performRegistration());

        btnRow.add(cancelBtn);
        btnRow.add(registerBtn);
        form.add(btnRow, gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, JComponent field) {
        gbc.gridx = col; gbc.gridy = row * 2 - 2;
        panel.add(makeFormLabel(label), gbc);
        gbc.gridy = row * 2 - 1;
        panel.add(field, gbc);
    }

    private JLabel makeFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UIHelper.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private void performRegistration() {
        String username    = usernameField.getText().trim();
        String fullName    = fullNameField.getText().trim();
        String email       = emailField.getText().trim();
        String phone       = phoneField.getText().trim();
        String address     = addressArea.getText().trim();
        String password    = new String(passwordField.getPassword());
        String confirmPwd  = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please fill in all required fields (*).");
            return;
        }
        if (username.length() < 4) {
            UIHelper.showError(this, "Username must be at least 4 characters long.");
            return;
        }
        if (password.length() < 6) {
            UIHelper.showError(this, "Password must be at least 6 characters long.");
            return;
        }
        if (!password.equals(confirmPwd)) {
            UIHelper.showError(this, "Passwords do not match. Please re-enter.");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserType("CUSTOMER");

        if (userDAO.register(user)) {
            User registered = userDAO.authenticate(user.getUsername(), user.getPassword());
            if (registered != null) {
                new dao.CustomerProfileDAO().createProfile(registered.getId(), dpaConsentCheckbox.isSelected());
                new dao.AuditLogDAO().log("CUSTOMER_REGISTERED", registered.getId(),
                    "Registered new customer account with PH DPA 2012 Consent: " + dpaConsentCheckbox.isSelected(),
                    "Customer profile initialized.", "127.0.0.1");
            }
            UIHelper.showSuccess(this, "Registration successful! You can now log in.");
            loginFrame.setVisible(true);
            dispose();
        } else {
            UIHelper.showError(this, "Registration failed. The username may already be taken.");
        }
    }
}