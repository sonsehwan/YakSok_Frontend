package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendChatRoomRequest {
    private String userEmail;
    private Long friendId;
}
