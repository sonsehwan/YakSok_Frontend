package com.example.medication.model.response;

import lombok.Getter;

@Getter
public class ChatRoomListDto {
    private Long roomId;
    private Long myParticipantId;
    private String roomName;
    private String lastMessage;
    private Long unreadCount;
    private String lastMessageAt;
}
