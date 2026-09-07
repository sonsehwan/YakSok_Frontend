package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 비밀번호 재설정 - 인증코드 발송 요청
@Getter
@AllArgsConstructor
public class ResetPwSendCodeRequest {
    private String loginId;
    private String email;
}
