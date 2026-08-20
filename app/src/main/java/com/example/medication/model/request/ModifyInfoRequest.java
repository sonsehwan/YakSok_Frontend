package com.example.medication.model.request;

public class ModifyInfoRequest {
    private Long userId;
    private String nickname;

    public ModifyInfoRequest(Long userId, String nickName){
        this.userId = userId;
        this.nickname = nickName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
