package com.example.medication.model;

public class ChatMessage {

    public enum MessageType{
        TEXT,
        SHARE_YAKSOK
    }
    private String roomId;
    private Long senderParticipantId;
    private MessageType type;
    private String message;
    private Long yaksokId;
    private String senderNickname;
    private String createdAt;

    public ChatMessage(String roomId, Long senderParticipantId, MessageType type, String message) {
        this(roomId, senderParticipantId, type, message, null);
    }

    public ChatMessage(String roomId, Long senderParticipantId, MessageType type, String message, Long yaksokId) {
        this.roomId = roomId;
        this.senderParticipantId = senderParticipantId;
        this.type = type;
        this.message = message;
        this.yaksokId = yaksokId;
    }

    public String getRoomId() {
        return roomId;
    }

    public Long getSenderParticipantId() {
        return senderParticipantId;
    }

    public MessageType getType() {
        return type != null ? type : MessageType.TEXT;
    }

    public Long getYaksokId() {
        return yaksokId;
    }

    public String getSenderNickname() {
        return (senderNickname != null) ? senderNickname : "";
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
}
