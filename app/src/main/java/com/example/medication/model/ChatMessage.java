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
    public ChatMessage(String roomId, String sender,MessageType type, String message) {
        this.roomId = roomId;
        this.sender = sender;
        this.type = type;
        this.message = message;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSender() {
        return sender;
    }

    public MessageType getType() { return type; }

    public String getMessage() {
        return message;
    }
}
