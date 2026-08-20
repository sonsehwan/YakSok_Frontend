package com.example.medication.model.request;

public class ModifyPasswordRequest {
    private Long userId;
    private String currentPassword;
    private String newPassword;

    public ModifyPasswordRequest(Long userId, String currentPassword, String newPassword) {
        this.userId = userId;
        this.newPassword = newPassword;
        this.currentPassword = currentPassword;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
