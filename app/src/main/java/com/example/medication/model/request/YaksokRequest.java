package com.example.medication.model.request;

import java.time.LocalDate;
import java.util.List;

public class YaksokRequest {
    private String title;
    private LocalDate startDate;
    private int prescriptionDays;
    private boolean takeMorning;
    private boolean takeLunch;
    private boolean takeDinner;
    private List<PillRequest> pills;

    public YaksokRequest(String title, LocalDate startDate, int prescriptionDays, boolean takeMorning, boolean takeLunch, boolean takeDinner, List<PillRequest> pills) {
        this.title = title;
        this.startDate = startDate;
        this.prescriptionDays = prescriptionDays;
        this.takeMorning = takeMorning;
        this.takeLunch = takeLunch;
        this.takeDinner = takeDinner;
        this.pills = pills;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
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

    public List<PillRequest> getPills() {
        return pills;
    }

    public void setPills(List<PillRequest> pills) {
        this.pills = pills;
    }
}
