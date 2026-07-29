package com.example.medication.model.response;

public class ChatRoomListDto {
    private Long roomId;
    private String roomName;
    private String lastMessage;
    private String lastMessageAt;   // 서버가 ISO 문자열로 내려준다 (예: 2026-07-28T11:05:00)

    public Long getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageAt() { return lastMessageAt; }
}
