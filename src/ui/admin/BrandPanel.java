package ui.admin;

import dao.BrandDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Brand;
import util.UIHelper;

public class BrandPanel extends JPanel {
    private BrandDAO brandDAO = new BrandDAO();
    private JTable brandTable;
    private DefaultTableModel tableModel;
    
    public BrandPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("Brands Management");
        add(headerLabel, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadBrands();
    }
    
    private JPanel createTablePanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout());
        
        String[] columns = {"ID", "Name", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        brandTable = new JTable(tableModel);
        UIHelper.styleTable(brandTable);
        
        JScrollPane scrollPane = new JScrollPane(brandTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JButton addBtn = UIHelper.createSuccessButton("Add Brand");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        
        JButton editBtn = UIHelper.createPrimaryButton("Edit");
        editBtn.addActionListener(e -> {
            int row = brandTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Brand brand = brandDAO.findById(id);
                showAddEditDialog(brand);
            } else {
                UIHelper.showError(this, "Please select a brand to edit");
            }
        });
        
        JButton deleteBtn = UIHelper.createDangerButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteBrand());
        
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        
        return panel;
    }
    
    private void loadBrands() {
        tableModel.setRowCount(0);
        List<Brand> brands = brandDAO.findAll();
        for (Brand b : brands) {
            tableModel.addRow(new Object[]{b.getId(), b.getName(), b.getDescription()});
        }
    }
    
    private void showAddEditDialog(Brand brand) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            brand == null ? "Add Brand" : "Edit Brand", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField nameField = UIHelper.createTextField();
        JTextField descField = UIHelper.createTextField();
        
        if (brand != null) {
            nameField.setText(brand.getName());
            descField.setText(brand.getDescription());
        }
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(UIHelper.createLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(UIHelper.createLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(descField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createSuccessButton("Save");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                UIHelper.showError(this, "Name is required");
                return;
            }
            
            Brand b = brand != null ? brand : new Brand();
            b.setName(nameField.getText().trim());
            b.setDescription(descField.getText().trim());
            
            boolean success = brand == null ? brandDAO.create(b) : brandDAO.update(b);
            if (success) {
                UIHelper.showSuccess(this, "Brand saved successfully!");
                loadBrands();
                dialog.dispose();
            } else {
                UIHelper.showError(this, "Failed to save brand. Name might already exist.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteBrand() {
        int row = brandTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            
            if (brandDAO.isUsedByProducts(id)) {
                UIHelper.showError(this, "Cannot delete brand. It is being used by products.");
                return;
            }
            
            if (UIHelper.showConfirm(this, "Are you sure you want to delete this brand?")) {
                if (brandDAO.delete(id)) {
                    UIHelper.showSuccess(this, "Brand deleted successfully!");
                    loadBrands();
                } else {
                    UIHelper.showError(this, "Failed to delete brand");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a brand to delete");
        }
    }
}