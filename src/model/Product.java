package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product {
    private int id;
    private String name;
    private int categoryId;
    private int brandId;
    private int supplierId;
    private BigDecimal costPrice;
    private BigDecimal markupPercentage;
    private BigDecimal sellingPrice;
    private int stockQuantity;
    private String description;
    
    private String categoryName;
    private String brandName;
    private String supplierName;
    
    public Product() {}
    
    public Product(int id, String name, int categoryId, int brandId, int supplierId,
                   BigDecimal costPrice, BigDecimal markupPercentage, int stockQuantity, String description) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.supplierId = supplierId;
        this.costPrice = costPrice;
        this.markupPercentage = markupPercentage;
        this.stockQuantity = stockQuantity;
        this.description = description;
        calculateSellingPrice();
    }
    
    public void calculateSellingPrice() {
        if (costPrice != null && markupPercentage != null) {
            BigDecimal markup = costPrice.multiply(markupPercentage).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            this.sellingPrice = costPrice.add(markup).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public int getBrandId() { return brandId; }
    public void setBrandId(int brandId) { this.brandId = brandId; }
    
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { 
        this.costPrice = costPrice;
        calculateSellingPrice();
    }
    
    public BigDecimal getMarkupPercentage() { return markupPercentage; }
    public void setMarkupPercentage(BigDecimal markupPercentage) { 
        this.markupPercentage = markupPercentage;
        calculateSellingPrice();
    }
    
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    @Override
    public String toString() {
        return name;
    }
}
