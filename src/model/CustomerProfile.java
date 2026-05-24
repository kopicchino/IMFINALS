package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerProfile {
    private int userId;
    private String segment;
    private String predictivePreferences;
    private String dynamicTags;
    private boolean consentDpa;
    private BigDecimal riskScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerProfile() {
        this.segment = "Standard Consumer";
        this.dynamicTags = "";
        this.consentDpa = false;
        this.riskScore = BigDecimal.ZERO;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getPredictivePreferences() {
        return predictivePreferences;
    }

    public void setPredictivePreferences(String predictivePreferences) {
        this.predictivePreferences = predictivePreferences;
    }

    public String getDynamicTags() {
        return dynamicTags;
    }

    public void setDynamicTags(String dynamicTags) {
        this.dynamicTags = dynamicTags;
    }

    public boolean isConsentDpa() {
        return consentDpa;
    }

    public void setConsentDpa(boolean consentDpa) {
        this.consentDpa = consentDpa;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
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
