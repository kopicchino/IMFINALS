package ui;

import dao.UserDAO;
import model.User;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginFrame — Modern split-screen login.
 * Left: Branding panel with gradient background and application identity.
 * Right: Clean white form panel with focus-aware inputs.
 */
public class LoginFrame extends JFrame {
    private UserDAO userDAO = new UserDAO();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;

    public LoginFrame() {
        setTitle("ISMS — Inventory & Security Management System");
        setSize(960, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UIHelper.SIDEBAR_BG);

        // ── LEFT: Branding Panel ─────────────────────────────────────────────
        root.add(buildBrandPanel());

        // ── RIGHT: Form Panel ────────────────────────────────────────────────
        root.add(buildFormPanel());

        add(root);
    }

    // ── Branding Panel ───────────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Diagonal gradient: deep navy to indigo
                GradientPaint gp = new GradientPaint(
                    0, 0,           new Color(15, 23, 42),
                    getWidth(), getHeight(), new Color(67, 56, 202)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle decorative circles
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-60, -60, 300, 300);
                g2.fillOval(getWidth() - 100, getHeight() - 120, 280, 280);
                g2.fillOval(getWidth() / 2 - 80, getHeight() / 2, 160, 160);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 50, 60, 50));

        panel.add(Box.createVerticalGlue());

        // Logo / Icon block
        JPanel logoBlock = new JPanel();
        logoBlock.setOpaque(false);
        logoBlock.setLayout(new BoxLayout(logoBlock, BoxLayout.X_AXIS));
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String s = "IS";
                g2.drawString(s, (56 - fm.stringWidth(s)) / 2, 56 / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(56, 56));
        iconCircle.setMaximumSize(new Dimension(56, 56));
        iconCircle.setMinimumSize(new Dimension(56, 56));
        logoBlock.add(iconCircle);
        logoBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(logoBlock);
        panel.add(Box.createRigidArea(new Dimension(0, 28)));

        // App name
        JLabel appName = new JLabel("ISMS");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 42));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(appName);

        JLabel fullName = new JLabel("Inventory & Security");
        fullName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fullName.setForeground(new Color(148, 163, 184));
        fullName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fullName);

        JLabel fullName2 = new JLabel("Management System");
        fullName2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fullName2.setForeground(new Color(148, 163, 184));
        fullName2.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fullName2);

        panel.add(Box.createRigidArea(new Dimension(0, 32)));

        // Feature bullets
        String[] features = {
            "\u2713  Enterprise Admin Console",
            "\u2713  Real-time Analytics Dashboard",
            "\u2713  Compliance & Fraud Protocol",
            "\u2713  Customer Intelligence Portal"
        };
        for (String f : features) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fl.setForeground(new Color(148, 163, 184));
            fl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(fl);
            panel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        panel.add(Box.createVerticalGlue());

        // Version footer
        JLabel version = new JLabel("v1.0  •  Enterprise Edition");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(71, 85, 105));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(version);

        return panel;
    }

    // ── Form Panel ───────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 60, 0, 60));

        panel.add(Box.createVerticalGlue());

        // Welcome header
        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcome.setForeground(UIHelper.TEXT_SECONDARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(welcome);

        JLabel signIn = new JLabel("Sign in to your account");
        signIn.setFont(new Font("Segoe UI", Font.BOLD, 26));
        signIn.setForeground(UIHelper.TEXT_PRIMARY);
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(signIn);

        panel.add(Box.createRigidArea(new Dimension(0, 36)));

        // Login as
        panel.add(makeFieldLabel("Login As"));
        userTypeCombo = new JComboBox<>(new String[]{"Customer", "Admin"});
        userTypeCombo.setFont(UIHelper.NORMAL_FONT);
        userTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        userTypeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        userTypeCombo.setBackground(Color.WHITE);
        panel.add(userTypeCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Username
        panel.add(makeFieldLabel("Username"));
        usernameField = UIHelper.createTextField("Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Password
        panel.add(makeFieldLabel("Password"));
        passwordField = UIHelper.createPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 28)));

        // Login button
        JButton loginBtn = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isRollover() ? UIHelper.PRIMARY_DARK : UIHelper.PRIMARY_COLOR;
                Color bot = getModel().isRollover() ? new Color(29, 78, 216) : UIHelper.PRIMARY_DARK;
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setOpaque(false);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> performLogin());
        panel.add(loginBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(UIHelper.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);

        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Register link
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        regRow.setBackground(Color.WHITE);
        regRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        regRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcct = new JLabel("Don't have an account?  ");
        noAcct.setFont(UIHelper.SMALL_FONT);
        noAcct.setForeground(UIHelper.TEXT_SECONDARY);
        JLabel regLink = new JLabel("Create account");
        regLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        regLink.setForeground(UIHelper.PRIMARY_COLOR);
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { openRegistration(); }
            public void mouseEntered(java.awt.event.MouseEvent e) { regLink.setForeground(UIHelper.PRIMARY_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { regLink.setForeground(UIHelper.PRIMARY_COLOR); }
        });
        regRow.add(noAcct);
        regRow.add(regLink);
        panel.add(regRow);

        panel.add(Box.createVerticalGlue());

        // Enter key on password
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });

        return panel;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UIHelper.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    // ── Business Logic ───────────────────────────────────────────────────────
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedType = (String) userTypeCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please enter your username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            if (selectedType.equals("Admin") && !user.isAdmin()) {
                UIHelper.showError(this, "These credentials are not linked to an admin account.");
                return;
            }
            if (selectedType.equals("Customer") && !user.isCustomer()) {
                UIHelper.showError(this, "These credentials are not linked to a customer account.");
                return;
            }
            if (user.isLocked()) {
                UIHelper.showError(this,
                    "Your account has been temporarily restricted due to a cascading security lock.\n" +
                    "Please contact the Compliance Team to resolve this.");
                return;
            }

            if (user.isAdmin()) {
                MainFrame adminFrame = new MainFrame(user);
                adminFrame.setVisible(true);
            } else {
                CustomerFrame customerFrame = new CustomerFrame(user);
                customerFrame.setVisible(true);
            }
            this.dispose();
        } else {
            UIHelper.showError(this, "Invalid username or password. Please try again.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void openRegistration() {
        RegistrationFrame regFrame = new RegistrationFrame(this);
        regFrame.setVisible(true);
        this.setVisible(false);
    }
}