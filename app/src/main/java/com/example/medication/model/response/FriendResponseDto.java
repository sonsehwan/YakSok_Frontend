package com.example.medication.model.response;

import lombok.Getter;

@Getter
public class FriendResponseDto {
    private Long friendId;
    private String nickname;
    private String email;
    private String friendedAt;
}
