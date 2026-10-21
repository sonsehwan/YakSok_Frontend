package com.example.medication.model.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class KakaoLoginResponse {
    @SerializedName("newUser")
    private boolean newUser;

    @SerializedName("login")
    private LoginResponse login;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("email") private String email;
}
