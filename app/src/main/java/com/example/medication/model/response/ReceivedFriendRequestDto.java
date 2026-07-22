package com.example.medication.model.response;

import lombok.Getter;

@Getter
public class ReceivedFriendRequestDto {
    private Long requestId;
    private Long requesterId;
    private String nickname;
    private String email;
    private String requestedAt;
}
