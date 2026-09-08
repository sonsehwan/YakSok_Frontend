package com.example.medication.model.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class ResetPwVerifyResponse {
    @SerializedName("resetToken")
    private String resetToken;
}
