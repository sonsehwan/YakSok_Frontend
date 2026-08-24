package com.example.medication.model.request;

import com.example.medication.model.SearchDrugStore;

//서버에 회원가입을 요청할 때 보내는 데이터 바구니 (DTO)
public class UserRequest {
    private String email;
    private String password;
    private String nickname;
    private SearchDrugStore drugStore;
    private String fcmToken;
    private String role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public SearchDrugStore getDrugStore(){ return drugStore; }

    public void setDrugStore(SearchDrugStore drugStore) { this.drugStore = drugStore; }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }


    public UserRequest(String email, String password, String nickname, String role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.drugStore = null;
        this.role = role;
    }

    public UserRequest(String email, String password, String nickname, SearchDrugStore drugStore, String role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.drugStore = drugStore;
        this.role = role;
    }
}