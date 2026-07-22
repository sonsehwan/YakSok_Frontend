package com.example.medication.model.response;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class FriendResponseDto {
    private Long friendId;
    private String nickname;
    private String email;
    private LocalDateTime friendedAt;
}
