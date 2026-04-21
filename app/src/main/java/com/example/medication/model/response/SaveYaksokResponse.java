package com.example.medication.model.response;

import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.Yaksok;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveYaksokResponse {
    @SerializedName("yaksok")
    Yaksok yaksok;

    @SerializedName("notifications")
    List<NotificationYaksok> notifications;

    public Yaksok getYaksok() {
        return yaksok;
    }

    public void setYaksok(Yaksok yaksok) {
        this.yaksok = yaksok;
    }

    public List<NotificationYaksok> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationYaksok> notifications) {
        this.notifications = notifications;
    }

    public SaveYaksokResponse(Yaksok yaksok, List<NotificationYaksok> notifications) {
        this.yaksok = yaksok;
        this.notifications = notifications;
    }
}
