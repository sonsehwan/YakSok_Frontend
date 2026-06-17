package com.example.medication.model;

public class ChatMessage {
    private String roomId;
    private String sender;
    private String message;

    public ChatMessage(String roomId, String sender, String message) {
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
