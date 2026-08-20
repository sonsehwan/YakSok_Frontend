package com.example.medication.model.request;

public class ChatRoomRequest {
    private Long userId;
    private String hpid;

    public ChatRoomRequest(Long userId, String hpid) {
        this.userId = userId;
        this.hpid = hpid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getHpid() {
        return hpid;
    }

    public void setHpid(String hpid) {
        this.hpid = hpid;
    }
}