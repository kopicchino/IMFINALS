package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private Integer userId;
    private LocalDateTime saleDate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private BigDecimal deliveryFee;
    private String customerName;
    private String deliveryAddress;
    private String deliveryPhone;
    private String status;
    private List<SaleItem> items;
    
    public Sale() {
        this.items = new ArrayList<>();
        this.saleDate = LocalDateTime.now();
        this.deliveryFee = BigDecimal.ZERO;
        this.status = "PENDING";
    }
    
    public void calculateTotals() {
        subtotal = BigDecimal.ZERO;
        for (SaleItem item : items) {
            subtotal = subtotal.add(item.getTotalPrice());
        }
        if (tax == null) tax = BigDecimal.ZERO;
        if (deliveryFee == null) deliveryFee = BigDecimal.ZERO;
        total = subtotal.add(tax).add(deliveryFee);
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
    public void addItem(SaleItem item) { this.items.add(item); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
