package ui.admin;

import dao.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Product;
import model.Sale;
import util.UIHelper;

public class DashboardPanel extends JPanel {
    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();
    //private CategoryDAO categoryDAO = new CategoryDAO();
    
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.CONTENT_BG);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIHelper.CONTENT_BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.CONTENT_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = UIHelper.createHeaderLabel("Dashboard");
        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(UIHelper.NORMAL_FONT);
        dateLabel.setForeground(UIHelper.TEXT_SECONDARY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        mainPanel.add(headerPanel);
        
        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(UIHelper.CONTENT_BG);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Calculate stats
        List<Product> products = productDAO.findAll();
        List<Sale> sales = salesDAO.findAll();
        int totalProducts = products.size();
        int lowStockCount = productDAO.getLowStockProducts(10).size();
        //int totalCategories = categoryDAO.findAll().size();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Sale sale : sales) {
            totalRevenue = totalRevenue.add(sale.getTotal());
        }
        
        statsPanel.add(createStatCard("Total Products", String.valueOf(totalProducts), "", UIHelper.PRIMARY_COLOR));
        statsPanel.add(createStatCard("Total Sales", String.valueOf(sales.size()), "", UIHelper.SUCCESS_COLOR));
        statsPanel.add(createStatCard("Revenue", "₱" + totalRevenue.toString(), "", UIHelper.INFO_COLOR));
        statsPanel.add(createStatCard("Low Stock", String.valueOf(lowStockCount), "", UIHelper.WARNING_COLOR));
        
        mainPanel.add(statsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Recent activity and Low stock
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setBackground(UIHelper.CONTENT_BG);
        
        // Low stock products
        bottomPanel.add(createLowStockPanel());
        
        // Recent sales
        bottomPanel.add(createRecentSalesPanel());
        
        mainPanel.add(bottomPanel);
        
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }
    
    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout());
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLabel.setPreferredSize(new Dimension(60, 60));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(UIHelper.CARD_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIHelper.NORMAL_FONT);
        titleLabel.setForeground(UIHelper.TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(valueLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(Box.createRigidArea(new Dimension(15, 0)), BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createLowStockPanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout());
        
        JLabel title = UIHelper.createSubHeaderLabel("Low Stock Products");
        panel.add(title, BorderLayout.NORTH);
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.BEFORE_FIRST_LINE);
        
        List<Product> lowStockProducts = productDAO.getLowStockProducts(10);
        String[] columns = {"Product", "Stock"};
        Object[][] data = new Object[Math.min(lowStockProducts.size(), 5)][2];
        
        for (int i = 0; i < Math.min(lowStockProducts.size(), 5); i++) {
            Product p = lowStockProducts.get(i);
            data[i][0] = p.getName();
            data[i][1] = p.getStockQuantity();
        }
        
        JTable table = new JTable(data, columns);
        UIHelper.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRecentSalesPanel() {
        JPanel panel = UIHelper.createCard();
        panel.setLayout(new BorderLayout());
        
        JLabel title = UIHelper.createSubHeaderLabel("Recent Sales");
        panel.add(title, BorderLayout.NORTH);
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.BEFORE_FIRST_LINE);
        
        List<Sale> recentSales = salesDAO.findAll();
        String[] columns = {"Date", "Customer", "Total"};
        Object[][] data = new Object[Math.min(recentSales.size(), 5)][3];
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        for (int i = 0; i < Math.min(recentSales.size(), 5); i++) {
            Sale s = recentSales.get(i);
            data[i][0] = s.getSaleDate().format(formatter);
            data[i][1] = s.getCustomerName() != null ? s.getCustomerName() : "Walk-in";
            data[i][2] = "₱" + s.getTotal().toString();
        }
        
        JTable table = new JTable(data, columns);
        UIHelper.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
}