package model;

import java.time.LocalDateTime;

public class CustomerCard {
    private int id;
    private int userId;
    private String cardNumberToken;
    private String cardNumberMasked;
    private String status; // 'Active', 'Reported', 'Locked', 'Under Review', 'Resolved/Replaced'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerCard() {
        this.status = "Active";
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

    public String getCardNumberToken() {
        return cardNumberToken;
    }

    public void setCardNumberToken(String cardNumberToken) {
        this.cardNumberToken = cardNumberToken;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
