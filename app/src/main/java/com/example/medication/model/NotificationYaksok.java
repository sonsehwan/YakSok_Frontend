package com.example.medication.model;

import java.io.Serializable;

public class NotificationYaksok implements Serializable {
    private Long id;
    private String name;
    private String time;
    private String instruction;
    private boolean isTaken;

    public NotificationYaksok(String name, String time, String instruction, boolean isTaken) {
        this.time = time;
        this.name = name;
        this.instruction = instruction;
        this.isTaken = isTaken;
    }

    public Long getId() { return id; }

    public void setId(Long id){ this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
