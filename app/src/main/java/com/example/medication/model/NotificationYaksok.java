package com.example.medication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NotificationYaksok implements Serializable {
    private Long id;
    private String title;
    @SerializedName("timeCategory")
    private String timeCategory;
    private String date;
    private String time;
    private String instruction;
    private boolean isTaken;

    public NotificationYaksok(String title, String timeCategory, String date, String time, String instruction, boolean isTaken) {
        this.time = time;
        this.title = title;
        this.timeCategory = timeCategory;
        this.date = date;
        this.instruction = instruction;
        this.isTaken = isTaken;
    }

    public Long getId() { return id; }

    public void setId(Long id){ this.id = id; }

    public String getTitle() {
        return title;
    }

    public void settitle(String title) {
        this.title = title;
    }

    public String getTimeCategory() {
        return timeCategory;
    }

    public void setTimeCategory(String timeCategory) {
        this.timeCategory = timeCategory;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public boolean isTaken() {
        return isTaken;
    }

    public void setTaken(boolean taken) {
        isTaken = taken;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
