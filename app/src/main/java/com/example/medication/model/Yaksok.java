package com.example.medication.model;

import com.example.medication.model.request.PillRequest;

import java.util.List;

public class Yaksok {
    private Long id; // 약속 ID
    private String title; // 약속 이름
    private String startDate; // 약속 시작 날짜
    private int prescriptionDays; // 복용 기간
    private boolean takeMorning; // 아침 복용 여부
    private boolean takeLunch; // 점심 복용 여부
    private boolean takeDinner; // 저녁 복용 여부
    private String timeMorning; // 아침 복용 시간
    private String timeLunch; // 점심 복용 시간
    private String timeDinner; // 저녁 복용 시간
    private String dosageTime; // 투약 시간대
    private List<PillRequest> pills; //복약 목록
    private String status; // 약속 상태(복용 중, 복용 완료)

    public Yaksok(String title, String startDate, int prescriptionDays, boolean takeMorning, boolean takeLunch, boolean takeDinner, String dosageTime, String timeMorning, String timeLunch, String timeDinner, List<PillRequest> pills, String stauts) {
        this.title = title;
        this.startDate = startDate;
        this.prescriptionDays = prescriptionDays;
        this.takeMorning = takeMorning;
        this.takeLunch = takeLunch;
        this.takeDinner = takeDinner;
        this.timeMorning = timeMorning;
        this.timeLunch = timeLunch;
        this.timeDinner = timeDinner;
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

    public String getTimeMorning() {return timeMorning;}

    public void setTimeMorning(String timeMorning) {this.timeMorning = timeMorning;}

    public String getTimeLunch() {return timeLunch;}

    public void setTimeLunch(String timeLunch) {this.timeLunch = timeLunch;}

    public String getTimeDinner() {return timeDinner;}

    public void setTimeDinner(String timeDinner) {this.timeDinner = timeDinner;}

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
