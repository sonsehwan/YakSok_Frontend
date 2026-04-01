package com.example.medication.model.request;

public class ModifyInfoRequest {
    private String email;
    private String nickname;

    public ModifyInfoRequest(String email, String nickName){
        this.email = email;
        this.nickname = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
