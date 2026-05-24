package ui.admin;

import dao.CategoryDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Category;
import util.UIHelper;

public class CategoryPanel extends JPanel {
    private CategoryDAO categoryDAO = new CategoryDAO();
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    
    public CategoryPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("Categories Management");
        add(headerLabel, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadCategories();
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
        
        categoryTable = new JTable(tableModel);
        UIHelper.styleTable(categoryTable);
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(UIHelper.CONTENT_BG);
        
        JButton addBtn = UIHelper.createSuccessButton("Add Category");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        
        JButton editBtn = UIHelper.createPrimaryButton("Edit");
        editBtn.addActionListener(e -> {
            int row = categoryTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Category category = categoryDAO.findById(id);
                showAddEditDialog(category);
            } else {
                UIHelper.showError(this, "Please select a category to edit");
            }
        });
        
        JButton deleteBtn = UIHelper.createDangerButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteCategory());
        
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        
        return panel;
    }
    
    private void loadCategories() {
        tableModel.setRowCount(0);
        List<Category> categories = categoryDAO.findAll();
        for (Category c : categories) {
            tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getDescription()});
        }
    }
    
    private void showAddEditDialog(Category category) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            category == null ? "Add Category" : "Edit Category", true);
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
        
        if (category != null) {
            nameField.setText(category.getName());
            descField.setText(category.getDescription());
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
            
            Category c = category != null ? category : new Category();
            c.setName(nameField.getText().trim());
            c.setDescription(descField.getText().trim());
            
            boolean success = category == null ? categoryDAO.create(c) : categoryDAO.update(c);
            if (success) {
                UIHelper.showSuccess(this, "Category saved successfully!");
                loadCategories();
                dialog.dispose();
            } else {
                UIHelper.showError(this, "Failed to save category. Name might already exist.");
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
    
    private void deleteCategory() {
        int row = categoryTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            
            if (categoryDAO.isUsedByProducts(id)) {
                UIHelper.showError(this, "Cannot delete category. It is being used by products.");
                return;
            }
            
            if (UIHelper.showConfirm(this, "Are you sure you want to delete this category?")) {
                if (categoryDAO.delete(id)) {
                    UIHelper.showSuccess(this, "Category deleted successfully!");
                    loadCategories();
                } else {
                    UIHelper.showError(this, "Failed to delete category");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a category to delete");
        }
    }
}