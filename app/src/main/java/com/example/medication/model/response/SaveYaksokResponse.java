package com.example.medication.model.response;

import com.example.medication.model.NotificationYaksok;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveYaksokResponse {
    @SerializedName("id")
    Long id;

    @SerializedName("notifications")
    List<NotificationYaksok> notifications;

    public SaveYaksokResponse(Long id, List<NotificationYaksok> notifications) {
        this.id = id;
        this.notifications = notifications;
    }

    // Getter 메서드
    public Long getId() {
        return id;
    }

    public List<NotificationYaksok> getNotifications() {
        return notifications;
    }

    // Setter 메서드
    public void setId(Long id) {
        this.id = id;
    }

    public void setNotifications(List<NotificationYaksok> notifications) {
        this.notifications = notifications;
    }
}
