package com.example.medication.model;

public class MedicationSetting {
    private String name;           // 약 이름
    private int prescriptionDays;  // 처방 일수
    private int dailyFrequency;    // 1일 투여 횟수
    private String dosage;         // 1회 투약량 (사용자 입력값)

    public MedicationSetting(String name) {
        this.name = name;
        this.prescriptionDays = 3; // 기본값 설정
        this.dailyFrequency = 3;    // 기본값 설정
        this.dosage = "";       // 초기 입력값
    }

    // Getter 및 Setter 메서드
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrescriptionDays() { return prescriptionDays; }
    public void setPrescriptionDays(int prescriptionDays) { this.prescriptionDays = prescriptionDays; }

    public int getDailyFrequency() { return dailyFrequency; }
    public void setDailyFrequency(int dailyFrequency) { this.dailyFrequency = dailyFrequency; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
}