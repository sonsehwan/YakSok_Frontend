package com.example.medication.model.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class LoginResponse {
    @SerializedName("user")
    private UserResponse user;
    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("refreshToken")
    private String refreshToken;
}
