package com.example.medication.model.request;

public class FirebaseTokenRequest {
    private String fcmToken;

    public FirebaseTokenRequest() {}

    public FirebaseTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    public String getFcmToken() {
        return fcmToken;
    }
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
