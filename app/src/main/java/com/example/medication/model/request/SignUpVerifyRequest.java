package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 회원가입 - 인증코드 검증 요청
@Getter
@AllArgsConstructor
public class SignUpVerifyRequest {
    private String email;
    private String code;
}
