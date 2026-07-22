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
    // 아래 두 값은 보낼 때는 비어 있고 서버가 채워준다
    private String senderNickname;
    private String createdAt;

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

    // 서버가 채워주는 값. 없으면 이메일로 대체한다.
    public String getSenderNickname() {
        return (senderNickname != null && !senderNickname.isEmpty()) ? senderNickname : sender;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
}