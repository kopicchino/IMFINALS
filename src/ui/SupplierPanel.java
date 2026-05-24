package ui;

import dao.SupplierDAO;
import model.Supplier;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SupplierPanel — Completely redesigned supplier manager.
 * Features inline filtering, double-click edit, custom dialogs,
 * and a smooth empty-state visual fallback.
 */
public class SupplierPanel extends JPanel {
    private SupplierDAO supplierDAO = new SupplierDAO();
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    private JPanel centerContainer;
    private CardLayout cardLayout;
    private JScrollPane tableScrollPane;
    private JPanel emptyStatePanel;
    
    public SupplierPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Page Header
        JPanel headerPanel = UIHelper.createPageHeader("Suppliers Management", "View and manage item suppliers.");
        add(headerPanel, BorderLayout.NORTH);
        
        // Content container card
        JPanel contentCard = UIHelper.createCard();
        contentCard.setLayout(new BorderLayout(0, 16));
        
        // Top Toolbar
        JPanel toolbarPanel = createToolbarPanel();
        contentCard.add(toolbarPanel, BorderLayout.NORTH);
        
        // Center layout with CardLayout for empty states
        cardLayout = new CardLayout();
        centerContainer = new JPanel(cardLayout);
        centerContainer.setBackground(Color.WHITE);
        
        // Table view
        String[] columns = {"ID", "Supplier Name", "Contact Number", "Email Address", "Office Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierTable = new JTable(tableModel);
        UIHelper.styleTable(supplierTable);
        UIHelper.applyAlternatingRows(supplierTable);
        
        supplierTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        supplierTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        supplierTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        supplierTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        supplierTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        
        // Double-click edit listener
        supplierTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && supplierTable.getSelectedRow() != -1) {
                    int row = supplierTable.getSelectedRow();
                    int id = (int) tableModel.getValueAt(row, 0);
                    Supplier supplier = supplierDAO.findById(id);
                    showAddEditDialog(supplier);
                }
            }
        });
        
        tableScrollPane = new JScrollPane(supplierTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Empty state view
        emptyStatePanel = UIHelper.createEmptyState("🏢", "No Suppliers Found", "Register a supplier to begin catalog association.");
        
        centerContainer.add(tableScrollPane, "table");
        centerContainer.add(emptyStatePanel, "empty");
        
        contentCard.add(centerContainer, BorderLayout.CENTER);
        add(contentCard, BorderLayout.CENTER);
        
        loadSuppliers();
    }
    
    private JPanel createToolbarPanel() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        // Filters/Search Left
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(Color.WHITE);
        
        searchField = UIHelper.createTextField("Search suppliers...");
        searchField.setPreferredSize(new Dimension(240, 36));
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void trigger() { loadSuppliers(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
        });
        
        JButton resetBtn = UIHelper.createGhostButton("Reset");
        resetBtn.setPreferredSize(new Dimension(80, 36));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadSuppliers();
        });
        
        leftPanel.add(UIHelper.createSecondaryLabel("Filter:"));
        leftPanel.add(searchField);
        leftPanel.add(resetBtn);
        
        // Actions Right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.WHITE);
        
        JButton addBtn = UIHelper.createSuccessButton("+ Add Supplier");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        
        JButton editBtn = UIHelper.createPrimaryButton("Edit");
        editBtn.addActionListener(e -> {
            int row = supplierTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Supplier supplier = supplierDAO.findById(id);
                showAddEditDialog(supplier);
            } else {
                UIHelper.showError(this, "Please select a supplier to edit");
            }
        });
        
        JButton deleteBtn = UIHelper.createDangerButton("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteSupplier());
        
        rightPanel.add(addBtn);
        rightPanel.add(editBtn);
        rightPanel.add(deleteBtn);
        
        bar.add(leftPanel, BorderLayout.CENTER);
        bar.add(rightPanel, BorderLayout.EAST);
        
        return bar;
    }
    
    private void loadSuppliers() {
        tableModel.setRowCount(0);
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        
        List<Supplier> suppliers = supplierDAO.findAll();
        
        // Apply in-memory filtering
        if (!keyword.isEmpty()) {
            suppliers = suppliers.stream()
                .filter(s -> s.getName().toLowerCase().contains(keyword) || 
                             (s.getContact() != null && s.getContact().toLowerCase().contains(keyword)) ||
                             (s.getEmail() != null && s.getEmail().toLowerCase().contains(keyword)) ||
                             (s.getAddress() != null && s.getAddress().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
        }
        
        if (suppliers.isEmpty()) {
            cardLayout.show(centerContainer, "empty");
        } else {
            cardLayout.show(centerContainer, "table");
            for (Supplier s : suppliers) {
                tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getContact(), s.getEmail(), s.getAddress()});
            }
        }
    }
    
    private void showAddEditDialog(Supplier supplier) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            supplier == null ? "Add Supplier" : "Edit Supplier", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        UIHelper.styleDialog(dialog);
        
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = UIHelper.createDialogHeader(
            supplier == null ? "Add New Supplier" : "Edit Supplier Details", 
            "Provide vendor contact information, emails and corporate office location.", 
            UIHelper.PRIMARY_COLOR
        );
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.CONTENT_BG);
        formPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        
        JTextField nameField = UIHelper.createTextField();
        JTextField contactField = UIHelper.createTextField();
        JTextField emailField = UIHelper.createTextField();
        
        JTextArea addressArea = new JTextArea(3, 20);
        addressArea.setFont(UIHelper.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createEmptyBorder());
        addressScroll.setPreferredSize(new Dimension(240, 70));
        
        if (supplier != null) {
            nameField.setText(supplier.getName());
            contactField.setText(supplier.getContact());
            emailField.setText(supplier.getEmail());
            addressArea.setText(supplier.getAddress());
        }
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(UIHelper.createLabel("Vendor Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formPanel.add(nameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(UIHelper.createLabel("Contact No:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formPanel.add(contactField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(UIHelper.createLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formPanel.add(emailField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(UIHelper.createLabel("Office Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formPanel.add(addressScroll, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        JButton saveBtn = UIHelper.createSuccessButton("Save Supplier");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                UIHelper.showError(dialog, "Name is required");
                nameField.requestFocus();
                return;
            }
            
            Supplier s = supplier != null ? supplier : new Supplier();
            s.setName(name);
            s.setContact(contactField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setAddress(addressArea.getText().trim());
            
            boolean success = supplier == null ? supplierDAO.create(s) : supplierDAO.update(s);
            if (success) {
                UIHelper.showSuccess(this, "Supplier saved successfully!");
                loadSuppliers();
                dialog.dispose();
            } else {
                UIHelper.showError(dialog, "Failed to save supplier.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void deleteSupplier() {
        int row = supplierTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            
            if (supplierDAO.isUsedByProducts(id)) {
                UIHelper.showError(this, "Cannot delete supplier. It is being used by products.");
                return;
            }
            
            if (UIHelper.showConfirm(this, "Are you sure you want to delete this supplier?")) {
                if (supplierDAO.delete(id)) {
                    UIHelper.showSuccess(this, "Supplier deleted successfully!");
                    loadSuppliers();
                } else {
                    UIHelper.showError(this, "Failed to delete supplier.");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a supplier to delete");
        }
    }
}