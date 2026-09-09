package com.example.medication.model.request;

public class ChatRoomRequest {
    private String hpid;

    public ChatRoomRequest(String hpid) {
        this.hpid = hpid;
    }

    public String getHpid() {
        return hpid;
    }

    public void setHpid(String hpid) {
        this.hpid = hpid;
    }
}
