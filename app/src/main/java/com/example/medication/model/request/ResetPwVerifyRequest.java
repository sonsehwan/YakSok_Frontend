package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 비밀번호 재설정 - 인증코드 검증 요청 (성공 시 서버가 임시 비밀번호를 메일로 발송)
@Getter
@AllArgsConstructor
public class ResetPwVerifyRequest {
    private String loginId;
    private String email;
    private String code;
}
