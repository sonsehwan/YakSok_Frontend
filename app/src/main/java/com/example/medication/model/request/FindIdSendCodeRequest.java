package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 아이디 찾기 - 인증코드 발송 요청
@Getter
@AllArgsConstructor
public class FindIdSendCodeRequest {
    private String email;
}
