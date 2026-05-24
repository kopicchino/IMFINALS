package ui;

import model.Product;
import util.UIHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AddProductDialog extends JDialog {
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField priceField;
    private JTextField qtyField;
    private JTextArea descArea;

    private Product createdProduct = null;

    public AddProductDialog(Frame parent) {
        super(parent, "Add New Product", true);
        initUI();
    }

    private void initUI() {
        Color bg = new Color(245, 247, 249);
        Color card = Color.WHITE;
        Color label = new Color(55, 65, 81);
        //Color muted = new Color(120, 130, 140);
        Color green = new Color(39, 174, 96);
        Color greenHover = new Color(29, 144, 76);

        setUndecorated(false);
        setSize(520, 520);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(bg);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(card);
        cardPanel.setBorder(new EmptyBorder(18, 20, 18, 20));
        cardPanel.setPreferredSize(new Dimension(460, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Add New Product");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(label);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        cardPanel.add(title, gbc);

        nameField = createRoundedField();
        addField(cardPanel, gbc, 1, "Product Name", nameField);

        categoryField = createRoundedField();
        addField(cardPanel, gbc, 2, "Category", categoryField);

        priceField = createRoundedField();
        qtyField = createRoundedField();
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(card);
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(0, 0, 0, 8); r.fill = GridBagConstraints.HORIZONTAL; r.weightx = 0.6;
        rowPanel.add(priceField, r);
        r.insets = new Insets(0, 8, 0, 0); r.weightx = 0.4;
        rowPanel.add(qtyField, r);
        addField(cardPanel, gbc, 3, "Price", rowPanel, "Quantity", null);

        descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new CompoundRoundedBorder());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setPreferredSize(new Dimension(420, 100));
        addField(cardPanel, gbc, 4, "Description", descScroll);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(192, 57, 43));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(6,10,0,10);
        cardPanel.add(errorLabel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setBackground(card);

        JButton addBtn = new JButton("Add Product");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(green);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(new RoundedCornerBorder(12, green));
        addBtn.setPreferredSize(new Dimension(160, 42));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { addBtn.setBackground(greenHover); addBtn.setBorder(new RoundedCornerBorder(12, greenHover)); }
            @Override public void mouseExited(MouseEvent e) { addBtn.setBackground(green); addBtn.setBorder(new RoundedCornerBorder(12, green)); }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setForeground(new Color(45, 55, 72));
        cancelBtn.setBackground(new Color(235, 237, 240));
        cancelBtn.setBorder(new RoundedCornerBorder(10, new Color(235,237,240)));
        cancelBtn.setPreferredSize(new Dimension(110, 42));

        btnPanel.add(cancelBtn);
        btnPanel.add(addBtn);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.insets = new Insets(14,10,6,10);
        cardPanel.add(btnPanel, gbc);

        cancelBtn.addActionListener(e -> {
            createdProduct = null;
            dispose();
        });

        addBtn.addActionListener(e -> {
            // validation
            String name = nameField.getText().trim();
            String cat = categoryField.getText().trim();
            String priceText = priceField.getText().trim();
            String qtyText = qtyField.getText().trim();

            if (name.isEmpty() || cat.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
                errorLabel.setText("Please fill in all required fields.");
                return;
            }

            try {
                java.math.BigDecimal price = new java.math.BigDecimal(priceText);
                int qty = Integer.parseInt(qtyText);

                Product p = new Product();
                p.setName(name);
                p.setDescription("Category: " + cat + "\n" + descArea.getText());
                p.setCostPrice(price);
                p.setStockQuantity(qty);

                createdProduct = p;
                dispose();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Price must be a valid number and Quantity an integer.");
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        root.add(cardPanel, gbc);

        getContentPane().add(root);
    }

    private JTextField createRoundedField() {
        JTextField f = new JTextField();
        f.setBorder(new CompoundRoundedBorder());
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(420, 36));
        return f;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.insets = new Insets(8,10,6,10);
        panel.add(UIHelper.createLabel(labelText), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8,10,6,10);
        panel.add(field, gbc);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelTextLeft, Component leftComp, String labelTextRight, Component rightComp) {
        // left label
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.insets = new Insets(8,10,6,10);
        panel.add(UIHelper.createLabel(labelTextLeft), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8,10,6,10);
        panel.add(leftComp, gbc);
    }

    public Product getCreatedProduct() { return createdProduct; }

    public static Product showDialog(Frame parent) {
        AddProductDialog dlg = new AddProductDialog(parent);
        dlg.setVisible(true);
        return dlg.getCreatedProduct();
    }

    private static class CompoundRoundedBorder implements Border {
       // private final Border inner = BorderFactory.createEmptyBorder(8,12,8,12);
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(220, 224, 230));
            g2.fillRoundRect(x, y, width-1, height-1, 12, 12);
            g2.setColor(new Color(200, 204, 210));
            g2.drawRoundRect(x, y, width-1, height-1, 12, 12);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(8,12,8,12); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    private static class RoundedCornerBorder implements Border {
        private final int radius; private final Color bg;
        public RoundedCornerBorder(int radius, Color bg) { this.radius = radius; this.bg = bg; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4,8,4,8); }
        @Override public boolean isBorderOpaque() { return true; }
    }
}
