package com.example.medication.model.request;

import java.util.List;

public class YaksokRequest {
    private Long id;
    private String title;
    private String startDate;
    private int prescriptionDays;
    private boolean takeMorning;
    private boolean takeLunch;
    private boolean takeDinner;
    private String dosageTime;
    private List<PillRequest> pills;
    private String status;

    public YaksokRequest(String title, String startDate, int prescriptionDays, boolean takeMorning, boolean takeLunch, boolean takeDinner, String dosageTime, List<PillRequest> pills, String stauts) {
        this.title = title;
        this.startDate = startDate;
        this.prescriptionDays = prescriptionDays;
        this.takeMorning = takeMorning;
        this.takeLunch = takeLunch;
        this.takeDinner = takeDinner;
        this.dosageTime = dosageTime;
        this.pills = pills;
        this.status = stauts;
    }

    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getPrescriptionDays() {
        return prescriptionDays;
    }

    public void setPrescriptionDays(int prescriptionDays) {
        this.prescriptionDays = prescriptionDays;
    }

    public boolean isTakeMorning() {
        return takeMorning;
    }

    public void setTakeMorning(boolean takeMorning) {
        this.takeMorning = takeMorning;
    }

    public boolean isTakeLunch() {
        return takeLunch;
    }

    public void setTakeLunch(boolean takeLunch) {
        this.takeLunch = takeLunch;
    }

    public boolean isTakeDinner() {
        return takeDinner;
    }

    public void setTakeDinner(boolean takeDinner) {
        this.takeDinner = takeDinner;
    }

    public String getDosageTime() {
        return dosageTime;
    }

    public void setDosageTime(String dosageTime) {
        this.dosageTime = dosageTime;
    }

    public List<PillRequest> getPills() {
        return pills;
    }

    public void setPills(List<PillRequest> pills) {
        this.pills = pills;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
