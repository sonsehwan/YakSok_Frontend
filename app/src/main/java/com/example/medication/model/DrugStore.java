package com.example.medication.model;

public class DrugStore {

    private String dutyName; // 약국 이름
    private String dutyAddr; // 약국 주소
    private String dutyTel1; // 약국 전화번호
    private String startTime; // 약국 영업 시작
    private String endTime; // 약국 영업 종료

    private String latitude; // 약국 위도
    private String longitude; // 약국 경도
    public DrugStore(String dutyName, String dutyAddr, String dutyTel1, String startTime, String endTime, String latitude, String longitude) {
        this.dutyName = dutyName;
        this.dutyAddr = dutyAddr;
        this.dutyTel1 = dutyTel1;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDutyName() {
        return dutyName;
    }

    public void setDutyName(String dutyName) {
        this.dutyName = dutyName;
    }

    public String getDutyAddr() {
        return dutyAddr;
    }

    public void setDutyAddr(String dutyAddr) {
        this.dutyAddr = dutyAddr;
    }

    public String getDutyTel1() {
        return dutyTel1;
    }

    public void setDutyTel1(String dutyTel1) {
        this.dutyTel1 = dutyTel1;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}