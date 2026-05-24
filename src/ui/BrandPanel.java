package ui;

import dao.BrandDAO;
import model.Brand;
import util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BrandPanel — Completely redesigned brand manager.
 * Features inline filtering, double-click edit, custom dialogs,
 * and a smooth empty-state visual fallback.
 */
public class BrandPanel extends JPanel {
    private BrandDAO brandDAO = new BrandDAO();
    private JTable brandTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    private JPanel centerContainer;
    private CardLayout cardLayout;
    private JScrollPane tableScrollPane;
    private JPanel emptyStatePanel;
    
    public BrandPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Page Header
        JPanel headerPanel = UIHelper.createPageHeader("Brands Management", "View and manage your product brands catalog.");
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
        String[] columns = {"ID", "Brand Name", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        brandTable = new JTable(tableModel);
        UIHelper.styleTable(brandTable);
        UIHelper.applyAlternatingRows(brandTable);
        
        brandTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        brandTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        brandTable.getColumnModel().getColumn(2).setPreferredWidth(400);
        
        // Double-click edit listener
        brandTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && brandTable.getSelectedRow() != -1) {
                    int row = brandTable.getSelectedRow();
                    int id = (int) tableModel.getValueAt(row, 0);
                    Brand brand = brandDAO.findById(id);
                    showAddEditDialog(brand);
                }
            }
        });
        
        tableScrollPane = new JScrollPane(brandTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Empty state view
        emptyStatePanel = UIHelper.createEmptyState("🏷️", "No Brands Found", "Create a brand to populate the catalog.");
        
        centerContainer.add(tableScrollPane, "table");
        centerContainer.add(emptyStatePanel, "empty");
        
        contentCard.add(centerContainer, BorderLayout.CENTER);
        add(contentCard, BorderLayout.CENTER);
        
        loadBrands();
    }
    
    private JPanel createToolbarPanel() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        // Filters/Search Left
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(Color.WHITE);
        
        searchField = UIHelper.createTextField("Search brands...");
        searchField.setPreferredSize(new Dimension(240, 36));
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void trigger() { loadBrands(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
        });
        
        JButton resetBtn = UIHelper.createGhostButton("Reset");
        resetBtn.setPreferredSize(new Dimension(80, 36));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadBrands();
        });
        
        leftPanel.add(UIHelper.createSecondaryLabel("Filter:"));
        leftPanel.add(searchField);
        leftPanel.add(resetBtn);
        
        // Actions Right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.WHITE);
        
        JButton addBtn = UIHelper.createSuccessButton("+ Add Brand");
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
        
        JButton deleteBtn = UIHelper.createDangerButton("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteBrand());
        
        rightPanel.add(addBtn);
        rightPanel.add(editBtn);
        rightPanel.add(deleteBtn);
        
        bar.add(leftPanel, BorderLayout.CENTER);
        bar.add(rightPanel, BorderLayout.EAST);
        
        return bar;
    }
    
    private void loadBrands() {
        tableModel.setRowCount(0);
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        
        List<Brand> brands = brandDAO.findAll();
        
        // Apply in-memory filtering
        if (!keyword.isEmpty()) {
            brands = brands.stream()
                .filter(b -> b.getName().toLowerCase().contains(keyword) || 
                             (b.getDescription() != null && b.getDescription().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
        }
        
        if (brands.isEmpty()) {
            cardLayout.show(centerContainer, "empty");
        } else {
            cardLayout.show(centerContainer, "table");
            for (Brand b : brands) {
                tableModel.addRow(new Object[]{b.getId(), b.getName(), b.getDescription()});
            }
        }
    }
    
    private void showAddEditDialog(Brand brand) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            brand == null ? "Add Brand" : "Edit Brand", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        UIHelper.styleDialog(dialog);
        
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = UIHelper.createDialogHeader(
            brand == null ? "Add New Brand" : "Edit Brand Details", 
            "Provide brand labels and descriptions for item categorizations.", 
            UIHelper.PRIMARY_COLOR
        );
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.CONTENT_BG);
        formPanel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        
        JTextField nameField = UIHelper.createTextField();
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(UIHelper.NORMAL_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundedBorder(UIHelper.BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setPreferredSize(new Dimension(240, 100));
        
        if (brand != null) {
            nameField.setText(brand.getName());
            descArea.setText(brand.getDescription());
        }
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formPanel.add(UIHelper.createLabel("Brand Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(UIHelper.createLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formPanel.add(descScroll, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        
        JButton saveBtn = UIHelper.createSuccessButton("Save Brand");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                UIHelper.showError(dialog, "Name is required");
                nameField.requestFocus();
                return;
            }
            
            Brand b = brand != null ? brand : new Brand();
            b.setName(name);
            b.setDescription(descArea.getText().trim());
            
            boolean success = brand == null ? brandDAO.create(b) : brandDAO.update(b);
            if (success) {
                UIHelper.showSuccess(this, "Brand saved successfully!");
                loadBrands();
                dialog.dispose();
            } else {
                UIHelper.showError(dialog, "Failed to save brand. Name might already exist.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
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
                    UIHelper.showError(this, "Failed to delete brand.");
                }
            }
        } else {
            UIHelper.showError(this, "Please select a brand to delete");
        }
    }
}