package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 회원가입 - 인증코드 발송 요청
@Getter
@AllArgsConstructor
public class SignUpSendCodeRequest {
    private String email;
}
