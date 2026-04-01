package com.example.medication.model.request;

public class ModifyPasswordRequest {
    private String email;
    private String currentPassword;
    private String newPassword;

    public ModifyPasswordRequest(String email, String currentPassword, String newPassword) {
        this.email = email;
        this.newPassword = newPassword;
        this.currentPassword = currentPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
