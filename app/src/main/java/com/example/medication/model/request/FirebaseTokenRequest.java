package com.example.medication.model.request;

public class FirebaseTokenRequest {
    private String email;
    private String fcmToken;

    public FirebaseTokenRequest(String email, String fcmToken) {
        this.email = email;
        this.fcmToken = fcmToken;
    }

    public String getEmail() {
        return email;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}
