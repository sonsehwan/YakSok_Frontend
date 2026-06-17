package com.example.medication.model.response;

import com.google.gson.annotations.SerializedName;

public class ChatRoomResponse {

    @SerializedName("roomId")
    private Long roomId;

    @SerializedName("isNew")
    private boolean isNew;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}