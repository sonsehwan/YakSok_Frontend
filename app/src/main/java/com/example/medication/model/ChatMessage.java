package com.example.medication.model;

public class ChatMessage {

    public enum MessageType{
        TEXT,
        SHARE_YAKSOK
    }
    private String roomId;
    private String sender;
    private MessageType type;
    private String message;
    private Long yaksokId;

    public ChatMessage(String roomId, String sender, MessageType type, String message) {
        this(roomId, sender, type, message, null);
    }

    public ChatMessage(String roomId, String sender, MessageType type, String message, Long yaksokId) {
        this.roomId = roomId;
        this.sender = sender;
        this.type = type;
        this.message = message;
        this.yaksokId = yaksokId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSender() {
        return sender;
    }
    public MessageType getType() {
        return type != null ? type : MessageType.TEXT;
    }

    public Long getYaksokId() {
        return yaksokId;
    }

    public String getMessage() {
        return message;
    }
}