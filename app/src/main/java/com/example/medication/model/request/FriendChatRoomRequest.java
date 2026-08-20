package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendChatRoomRequest {
    private Long userId;
    private Long friendId;
}
