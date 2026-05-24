package model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String userType;
    private boolean isActive;
    private boolean isLocked;
    private LocalDateTime createdAt;
    
    public User() {
        this.isActive = true;
        this.isLocked = false;
    }
    
    public User(int id, String username, String fullName, String email, String userType) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.isActive = true;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(userType);
    }
    
    public boolean isCustomer() {
        return "CUSTOMER".equals(userType);
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}