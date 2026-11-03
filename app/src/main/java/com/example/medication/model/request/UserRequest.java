package com.example.medication.model.request;

import com.example.medication.model.SearchDrugStore;

import lombok.Getter;

//서버에 회원가입을 요청할 때 보내는 데이터 바구니 (DTO)
@Getter
public class UserRequest {
    private String loginId;
    private String email;
    private String password;
    private String nickname;
    private SearchDrugStore drugStore;
    private String role;
    private String emailVerifyToken;

    public UserRequest(String loginId, String email, String password, String nickname, String role, String emailVerifyToken) {
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.drugStore = null;
        this.role = role;
        this.emailVerifyToken = emailVerifyToken;
    }

    public UserRequest(String loginId, String email, String password, String nickname, SearchDrugStore drugStore, String role, String emailVerifyToken) {
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.drugStore = drugStore;
        this.role = role;
        this.emailVerifyToken = emailVerifyToken;
    }
}
