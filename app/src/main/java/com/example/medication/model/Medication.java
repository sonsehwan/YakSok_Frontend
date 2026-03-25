package com.example.medication.model;

public class Medication {
    private String name;
    private String time;
    private String instruction;
    private boolean isTaken;

    public Medication(String name, String time, String instruction, boolean isTaken) {
        this.time = time;
        this.name = name;
        this.instruction = instruction;
        this.isTaken = isTaken;
    }

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
