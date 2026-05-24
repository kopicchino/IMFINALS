package model;

import java.time.LocalDateTime;

public class InventoryLog {
    private int id;
    private int productId;
    private String productName;
    private String changeType;
    private int quantity;
    private int previousStock;
    private int newStock;
    private String notes;
    private LocalDateTime logDate;
    
    public InventoryLog() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }
    
    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }
}
