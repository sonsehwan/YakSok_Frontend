package com.example.medication.model.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

// 아이디 찾기 검증 성공 응답
@Getter
public class FindIdResponse {
    @SerializedName("loginId")
    private String loginId;
}
