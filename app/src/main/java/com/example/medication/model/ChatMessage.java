package com.example.medication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessage {

    public enum MessageType{
        TEXT,
        SHARE_YAKSOK
    }
    private Long id;
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
}
