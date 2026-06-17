package com.example.medication.model.request;

public class ChatRoomRequest {
    private String userEmail;
    private String hpid;

    public ChatRoomRequest(String userEmail, String hpid) {
        this.userEmail = userEmail;
        this.hpid = hpid;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getHpid() {
        return hpid;
    }

    public void setHpid(String hpid) {
        this.hpid = hpid;
    }
}