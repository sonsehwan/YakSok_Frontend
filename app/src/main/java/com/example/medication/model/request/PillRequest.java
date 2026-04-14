package com.example.medication.model.request;

public class PillRequest {

    private String image;
    private String name;           // 약 이름
    private int dailyFrequency;    // 1일 투여 횟수
    private String dosage;         // 1회 투약량 (사용자 입력값)

    public PillRequest(String image, String name) {
        this.image = image;
        this.name = name;
        this.dailyFrequency = 3;    // 기본값 설정
        this.dosage = "";       // 초기 입력값
    }

    // Getter 및 Setter 메서드
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDailyFrequency() { return dailyFrequency; }
    public void setDailyFrequency(int dailyFrequency) { this.dailyFrequency = dailyFrequency; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
}