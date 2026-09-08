package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetPwConfirmRequest {
    private String resetToken;
    private String newPassword;
}
