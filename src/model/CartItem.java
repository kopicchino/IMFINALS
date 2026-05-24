package model;

import java.math.BigDecimal;
import java.util.Objects;

public class CartItem {
    private int id;
    private int userId;
    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private int availableStock;
    
    public CartItem() {
    }
    
    public CartItem(int productId, String productName, int quantity, BigDecimal unitPrice, int availableStock) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.availableStock = availableStock;
        calculateTotal();
    }
    
    /**
     * Null-safe total calculation
     * Uses BigDecimal.ZERO if unitPrice is null
     */
    public void calculateTotal() {
        BigDecimal price = Objects.requireNonNullElse(unitPrice, BigDecimal.ZERO);
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Get unit price with null-safe default to ZERO
     * @return unitPrice or BigDecimal.ZERO if null
     */
    public BigDecimal getUnitPriceSafe() {
        return Objects.requireNonNullElse(unitPrice, BigDecimal.ZERO);
    }
    
    /**
     * Get total price with null-safe default to ZERO
     * @return totalPrice or BigDecimal.ZERO if null
     */
    public BigDecimal getTotalPriceSafe() {
        return Objects.requireNonNullElse(totalPrice, BigDecimal.ZERO);
    }
    
    /**
     * Check if product has a valid price
     * @return true if unitPrice is not null and greater than zero
     */
    public boolean hasPriceError() {
        return unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public int getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }
}