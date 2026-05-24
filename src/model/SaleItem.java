package model;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    public SaleItem() {}
    
    public SaleItem(int productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    public void calculateTotal() {
        BigDecimal price = Objects.requireNonNullElse(unitPrice, BigDecimal.ZERO);
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        calculateTotal();
    }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    public BigDecimal getUnitPriceSafe() { return Objects.requireNonNullElse(unitPrice, BigDecimal.ZERO); }
    public BigDecimal getTotalPriceSafe() { return Objects.requireNonNullElse(totalPrice, BigDecimal.ZERO); }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
