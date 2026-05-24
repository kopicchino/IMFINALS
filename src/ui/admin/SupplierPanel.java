package ui.admin;

import dao.SupplierDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Supplier;
import util.UIHelper;

public class SupplierPanel extends JPanel {
    private SupplierDAO supplierDAO = new SupplierDAO();
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    
    public SupplierPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("Suppliers Management");
        add(headerLabel, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadSuppliers();
    }
    
    private JPanel createTablePanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout());
        
        String[] columns = {"ID", "Name", "Contact", "Email", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierTable = new JTable(tableModel);
        UIHelper.styleTable(supplierTable);
        
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JButton addBtn = UIHelper.createSuccessButton("Add Supplier");
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
        
        JButton deleteBtn = UIHelper.createDangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteSupplier());
        
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        
        return panel;
    }
    
    private void loadSuppliers() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierDAO.findAll();
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getContact(), s.getEmail(), s.getAddress()});
        }
    }
    
    private void showAddEditDialog(Supplier supplier) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            supplier == null ? "Add Supplier" : "Edit Supplier", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField nameField = UIHelper.createTextField();
        JTextField contactField = UIHelper.createTextField();
        JTextField emailField = UIHelper.createTextField();
        JTextField addressField = UIHelper.createTextField();
        
        if (supplier != null) {
            nameField.setText(supplier.getName());
            contactField.setText(supplier.getContact());
            emailField.setText(supplier.getEmail());
            addressField.setText(supplier.getAddress());
        }
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(UIHelper.createLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(UIHelper.createLabel("Contact:"), gbc);
        gbc.gridx = 1;
        panel.add(contactField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(UIHelper.createLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(UIHelper.createLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createSuccessButton("Save");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                UIHelper.showError(this, "Name is required");
                return;
            }
            
            Supplier s = supplier != null ? supplier : new Supplier();
            s.setName(nameField.getText().trim());
            s.setContact(contactField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setAddress(addressField.getText().trim());
            
            boolean success = supplier == null ? supplierDAO.create(s) : supplierDAO.update(s);
            if (success) {
                UIHelper.showSuccess(this, "Supplier saved successfully!");
                loadSuppliers();
                dialog.dispose();
            } else {
                UIHelper.showError(this, "Failed to save supplier");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
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
                    UIHelper.showError(this, "Failed to delete supplier");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a supplier to delete");
        }
    }
}