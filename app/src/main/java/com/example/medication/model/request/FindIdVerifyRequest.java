package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 아이디 찾기 - 인증코드 검증 요청
@Getter
@AllArgsConstructor
public class FindIdVerifyRequest {
    private String email;
    private String code;
}
