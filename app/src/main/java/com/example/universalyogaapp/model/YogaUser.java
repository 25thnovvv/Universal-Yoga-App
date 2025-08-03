package com.example.universalyogaapp.model;

import java.io.Serializable;

public class YogaUser implements Serializable {
    // User identification
    private String userId;
    private String userEmail;
    private String userFullName;
    private String userPhoneNumber;
    private String cloudUserId;
    private boolean cloudSyncStatus;

    // Default constructor for Firebase serialization
    public YogaUser() {
        this.cloudSyncStatus = false;
    }

    // Basic user constructor
    public YogaUser(String userEmail, String userFullName, String userPhoneNumber) {
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.userPhoneNumber = userPhoneNumber;
        this.cloudSyncStatus = false;
    }

    // Comprehensive constructor
    public YogaUser(String userId, String userEmail, String userFullName, String userPhoneNumber, 
                String cloudUserId, boolean cloudSyncStatus) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.userPhoneNumber = userPhoneNumber;
        this.cloudUserId = cloudUserId;
        this.cloudSyncStatus = cloudSyncStatus;
    }

    // Primary getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserPhoneNumber() { return userPhoneNumber; }
    public void setUserPhoneNumber(String userPhoneNumber) { this.userPhoneNumber = userPhoneNumber; }

    public String getCloudUserId() { return cloudUserId; }
    public void setCloudUserId(String cloudUserId) { this.cloudUserId = cloudUserId; }

    public boolean getCloudSyncStatus() { return cloudSyncStatus; }
    public void setCloudSyncStatus(boolean cloudSyncStatus) { this.cloudSyncStatus = cloudSyncStatus; }

    // Legacy getters for backward compatibility
    public String getId() { return userId; }
    public void setId(String id) { this.userId = id; }

    public String getEmail() { return userEmail; }
    public void setEmail(String email) { this.userEmail = email; }

    public String getFullName() { return userFullName; }
    public void setFullName(String fullName) { this.userFullName = fullName; }

    public String getPhoneNumber() { return userPhoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.userPhoneNumber = phoneNumber; }

    public String getFirebaseId() { return cloudUserId; }
    public void setFirebaseId(String firebaseId) { this.cloudUserId = firebaseId; }

    public boolean isSynced() { return cloudSyncStatus; }
    public void setSynced(boolean synced) { this.cloudSyncStatus = synced; }
} 