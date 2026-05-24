package ui.admin;

import dao.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.*;
import util.UIHelper;

public class ReportsPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private JTextArea reportArea;
    private String currentReportType = ""; // Track current report type for export
    private String currentReportPeriod = ""; // Track period for sales reports
    private LocalDate currentSpecificDate = null; // Track specific date if selected
    
    public ReportsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.CONTENT_BG);
        
        JLabel headerLabel = UIHelper.createHeaderLabel("Reports");
        add(headerLabel, BorderLayout.NORTH);
        
        // Left panel with report buttons
        JPanel leftPanel = createReportButtonsPanel();
        
        // Right panel with report display
        JPanel rightPanel = UIHelper.createCard();
        rightPanel.setLayout(new BorderLayout());
        
        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        reportArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add export button at the bottom
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        exportPanel.setBackground(Color.WHITE);
        
        JButton exportBtn = UIHelper.createSuccessButton("Export to CSV");
        exportBtn.addActionListener(e -> exportCurrentReport());
        exportPanel.add(exportBtn);
        
        rightPanel.add(exportPanel, BorderLayout.SOUTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createReportButtonsPanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(250, 0));
        
        JLabel title = UIHelper.createSubHeaderLabel("Available Reports");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton productReportBtn = createReportButton("Product Report");
        productReportBtn.addActionListener(e -> generateProductReport());
        
        JButton salesReportBtn = createReportButton("Sales Report");
        salesReportBtn.addActionListener(e -> showSalesReportOptions());
        
        JButton inventoryReportBtn = createReportButton("Inventory Report");
        inventoryReportBtn.addActionListener(e -> generateInventoryReport());
        
        panel.add(productReportBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(salesReportBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(inventoryReportBtn);
        
        return panel;
    }
    
    private JButton createReportButton(String text) {
        JButton btn = UIHelper.createMenuButton(text);
        btn.setFont(UIHelper.BUTTON_FONT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(230, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }
    
    private void generateProductReport() {
        currentReportType = "Product";
        currentReportPeriod = "";
        currentSpecificDate = null;
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("PRODUCT REPORT\n");
        report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        report.append("=".repeat(80)).append("\n\n");
        
        List<Product> products = productDAO.findAll();
        
        report.append(String.format("%-5s %-30s %-12s %-10s %-10s %-10s %-10s\n",
            "ID", "Name", "Category", "Stock", "Cost", "Markup%", "Price"));
        report.append("-".repeat(80)).append("\n");
        
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Product p : products) {
            report.append(String.format("%-5d %-30s %-12s %-10d ₱%-9.2f %-9.1f%% ₱%-9.2f\n",
                p.getId(),
                p.getName().length() > 30 ? p.getName().substring(0, 27) + "..." : p.getName(),
                p.getCategoryName(),
                p.getStockQuantity(),
                p.getCostPrice(),
                p.getMarkupPercentage(),
                p.getSellingPrice()));
            BigDecimal price = Objects.requireNonNullElse(p.getSellingPrice(), BigDecimal.ZERO);
            totalValue = totalValue.add(price.multiply(BigDecimal.valueOf(p.getStockQuantity())));
        }
        
        report.append("-".repeat(80)).append("\n");
        report.append(String.format("Total Products: %d\n", products.size()));
        report.append(String.format("Total Inventory Value: ₱%.2f\n", totalValue));
        
        reportArea.setText(report.toString());
    }
    
    private void showSalesReportOptions() {
        String[] options = {"Today", "This Month", "This Year", "Specific Date", "All Time"};
        String choice = (String) JOptionPane.showInputDialog(
            this,
            "Select time period:",
            "Sales Report",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == null) return;
        
        if ("Specific Date".equals(choice)) {
            String defaultDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String input = JOptionPane.showInputDialog(this, "Enter date (YYYY-MM-DD):", defaultDate);
            if (input == null || input.trim().isEmpty()) return;
            try {
                LocalDate specific = LocalDate.parse(input.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                generateSalesReport(choice, specific);
            } catch (Exception ex) {
                UIHelper.showError(this, "Invalid date format. Please use YYYY-MM-DD.");
            }
        } else {
            generateSalesReport(choice, null);
        }
    }
    
    private void generateSalesReport(String period, LocalDate specificDate) {
        currentReportType = "Sales";
        currentReportPeriod = period;
        currentSpecificDate = specificDate;
        
        StringBuilder report = new StringBuilder();
        String headerPeriod = ("Specific Date".equals(period) && specificDate != null)
            ? specificDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            : period.toUpperCase();
        report.append("=".repeat(80)).append("\n");
        report.append("SALES REPORT - ").append(headerPeriod).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        report.append("=".repeat(80)).append("\n\n");
        
        List<Sale> allSales = salesDAO.findAll();
        LocalDate today = LocalDate.now();
        
        List<Sale> filteredSales = new ArrayList<>();
        for (Sale sale : allSales) {
            LocalDate saleDate = sale.getSaleDate().toLocalDate();
            boolean include = false;
            
            switch (period) {
                case "Today":
                    include = saleDate.equals(today);
                    break;
                case "This Month":
                    include = saleDate.getMonth() == today.getMonth() && saleDate.getYear() == today.getYear();
                    break;
                case "This Year":
                    include = saleDate.getYear() == today.getYear();
                    break;
                case "Specific Date":
                    include = (specificDate != null) && saleDate.equals(specificDate);
                    break;
                case "All Time":
                    include = true;
                    break;
            }
            
            if (include) {
                filteredSales.add(sale);
            }
        }
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalTransactions = filteredSales.size();
        Map<String, Integer> productSales = new HashMap<>();
        
        for (Sale sale : filteredSales) {
            totalRevenue = totalRevenue.add(sale.getTotal());
            for (SaleItem item : sale.getItems()) {
                productSales.merge(item.getProductName(), item.getQuantity(), Integer::sum);
            }
        }
        
        report.append(String.format("Total Transactions: %d\n", totalTransactions));
        report.append(String.format("Total Revenue: ₱%.2f\n\n", totalRevenue));
        
        if (!productSales.isEmpty()) {
            report.append("TOP SELLING PRODUCTS:\n");
            report.append("-".repeat(80)).append("\n");
            report.append(String.format("%-50s %s\n", "Product", "Quantity Sold"));
            report.append("-".repeat(80)).append("\n");
            
            productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    report.append(String.format("%-50s %d\n", entry.getKey(), entry.getValue()));
                });
        }
        
        reportArea.setText(report.toString());
    }
    
    private void generateInventoryReport() {
        currentReportType = "Inventory";
        currentReportPeriod = "";
        currentSpecificDate = null;
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("INVENTORY ACTIVITY REPORT\n");
        report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        report.append("=".repeat(80)).append("\n\n");
        
        List<Product> products = productDAO.findAll();
        List<Product> lowStock = productDAO.getLowStockProducts(10);
        
        report.append("STOCK SUMMARY:\n");
        report.append("-".repeat(80)).append("\n");
        report.append(String.format("Total Products: %d\n", products.size()));
        report.append(String.format("Low Stock Items: %d\n", lowStock.size()));
        
        int outOfStock = 0;
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        
        for (Product p : products) {
            if (p.getStockQuantity() == 0) outOfStock++;
            BigDecimal price = Objects.requireNonNullElse(p.getSellingPrice(), BigDecimal.ZERO);
            totalInventoryValue = totalInventoryValue.add(price.multiply(BigDecimal.valueOf(p.getStockQuantity())));
        }
        
        report.append(String.format("Out of Stock Items: %d\n", outOfStock));
        report.append(String.format("Total Inventory Value: ₱%.2f\n\n", totalInventoryValue));
        
        if (!lowStock.isEmpty()) {
            report.append("LOW STOCK ALERTS:\n");
            report.append("-".repeat(80)).append("\n");
            report.append(String.format("%-5s %-40s %-15s %s\n", "ID", "Product", "Category", "Stock"));
            report.append("-".repeat(80)).append("\n");
            
            for (Product p : lowStock) {
                report.append(String.format("%-5d %-40s %-15s %d\n",
                    p.getId(),
                    p.getName().length() > 40 ? p.getName().substring(0, 37) + "..." : p.getName(),
                    p.getCategoryName(),
                    p.getStockQuantity()));
            }
        }
        
        reportArea.setText(report.toString());
    }
    
    private void exportCurrentReport() {
        if (reportArea.getText().isEmpty()) {
            UIHelper.showError(this, "Please generate a report first before exporting");
            return;
        }
        
        // File chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = currentReportType + "_Report_" + timestamp + ".csv";
        fileChooser.setSelectedFile(new File(fileName));
        
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File file = fileChooser.getSelectedFile();
        
        try {
            switch (currentReportType) {
                case "Product":
                    exportProductReportToCSV(file);
                    break;
                case "Sales":
                    exportSalesReportToCSV(file);
                    break;
                case "Inventory":
                    exportInventoryReportToCSV(file);
                    break;
                default:
                    UIHelper.showError(this, "Unknown report type");
                    return;
            }
            
            UIHelper.showSuccess(this, "Report exported successfully!\n\nSaved to: " + file.getAbsolutePath());
            
            // Ask if user wants to open the file
            int open = JOptionPane.showConfirmDialog(this, 
                "Report exported successfully! Do you want to open it?", 
                "Export Complete", 
                JOptionPane.YES_NO_OPTION);
            
            if (open == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(file);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            UIHelper.showError(this, "Failed to export report: " + ex.getMessage());
        }
    }
    
    private void exportProductReportToCSV(File file) throws Exception {
        List<Product> products = productDAO.findAll();
        
        FileWriter writer = new FileWriter(file);
        
        // Header
        writer.append("Product ID,Product Name,Category,Brand,Supplier,Cost Price,Markup %,Selling Price,Stock Quantity,Inventory Value\n");
        
        // Data
        for (Product p : products) {
            BigDecimal inventoryValue = p.getSellingPrice().multiply(BigDecimal.valueOf(p.getStockQuantity()));
            
            writer.append(String.valueOf(p.getId())).append(",");
            writer.append("\"").append(p.getName()).append("\",");
            writer.append("\"").append(p.getCategoryName()).append("\",");
            writer.append("\"").append(p.getBrandName()).append("\",");
            writer.append("\"").append(p.getSupplierName()).append("\",");
            writer.append(p.getCostPrice().toString()).append(",");
            writer.append(p.getMarkupPercentage().toString()).append(",");
            writer.append(p.getSellingPrice().toString()).append(",");
            writer.append(String.valueOf(p.getStockQuantity())).append(",");
            writer.append(inventoryValue.toString()).append("\n");
        }
        
        writer.flush();
        writer.close();
    }
    
    private void exportSalesReportToCSV(File file) throws Exception {
        List<Sale> allSales = salesDAO.findAll();
        LocalDate today = LocalDate.now();
        
        // Filter sales based on current period
        List<Sale> filteredSales = new ArrayList<>();
        for (Sale sale : allSales) {
            LocalDate saleDate = sale.getSaleDate().toLocalDate();
            boolean include = false;
            
            switch (currentReportPeriod) {
                case "Today":
                    include = saleDate.equals(today);
                    break;
                case "This Month":
                    include = saleDate.getMonth() == today.getMonth() && saleDate.getYear() == today.getYear();
                    break;
                case "This Year":
                    include = saleDate.getYear() == today.getYear();
                    break;
                case "Specific Date":
                    include = (currentSpecificDate != null) && saleDate.equals(currentSpecificDate);
                    break;
                case "All Time":
                    include = true;
                    break;
            }
            
            if (include) {
                filteredSales.add(sale);
            }
        }
        
        FileWriter writer = new FileWriter(file);
        
        // Header
        writer.append("Order ID,Date,Customer Name,Address,Phone,Subtotal,Tax,Delivery Fee,Total\n");
        
        // Data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Sale sale : filteredSales) {
            writer.append(String.valueOf(sale.getId())).append(",");
            writer.append(sale.getSaleDate().format(formatter)).append(",");
            writer.append("\"").append(sale.getCustomerName() != null ? sale.getCustomerName() : "").append("\",");
            writer.append("\"").append(sale.getDeliveryAddress() != null ? sale.getDeliveryAddress() : "").append("\",");
            writer.append("\"").append(sale.getDeliveryPhone() != null ? sale.getDeliveryPhone() : "").append("\",");
            writer.append(sale.getSubtotal().toString()).append(",");
            writer.append(sale.getTax().toString()).append(",");
            writer.append(sale.getDeliveryFee() != null ? sale.getDeliveryFee().toString() : "0").append(",");
            writer.append(sale.getTotal().toString()).append("\n");
        }
        
        writer.flush();
        writer.close();
    }
    
    private void exportInventoryReportToCSV(File file) throws Exception {
        List<Product> products = productDAO.findAll();
        //List<Product> lowStock = productDAO.getLowStockProducts(10);
        
        FileWriter writer = new FileWriter(file);
        
        // Header
        writer.append("Product ID,Product Name,Category,Brand,Stock Quantity,Selling Price,Inventory Value,Status\n");
        
        // Data
        for (Product p : products) {
            String status = p.getStockQuantity() == 0 ? "Out of Stock" :
                          p.getStockQuantity() < 10 ? "Low Stock" : "In Stock";
            BigDecimal inventoryValue = p.getSellingPrice().multiply(BigDecimal.valueOf(p.getStockQuantity()));
            
            writer.append(String.valueOf(p.getId())).append(",");
            writer.append("\"").append(p.getName()).append("\",");
            writer.append("\"").append(p.getCategoryName()).append("\",");
            writer.append("\"").append(p.getBrandName()).append("\",");
            writer.append(String.valueOf(p.getStockQuantity())).append(",");
            writer.append(p.getSellingPrice().toString()).append(",");
            writer.append(inventoryValue.toString()).append(",");
            writer.append(status).append("\n");
        }
        
        writer.flush();
        writer.close();
    }
}