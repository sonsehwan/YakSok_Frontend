package com.example.medication.model.response;

import lombok.Getter;

@Getter
public class UserSearchResultDto {
    private Long userId;
    private String nickname;
    private String email;
}
