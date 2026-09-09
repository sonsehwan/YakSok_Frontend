package com.example.medication.model;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class SearchDrugStore implements Serializable{

    private String hpid; // 약국id
    private String dutyName; // 약국 이름
    private String dutyAddr; // 약국 주소
    private String dutyTel1; // 약국 전화번호

    //영업 종료 시간(월~일 + 공휴일(8))
    private String dutyTime1c;
    private String dutyTime2c;
    private String dutyTime3c;
    private String dutyTime4c;
    private String dutyTime5c;
    private String dutyTime6c;
    private String dutyTime7c;
    private String dutyTime8c;

    //영업 시작 시간(월~일 + 공휴일(8))
    private String dutyTime1s;
    private String dutyTime2s;
    private String dutyTime3s;
    private String dutyTime4s;
    private String dutyTime5s;
    private String dutyTime6s;
    private String dutyTime7s;
    private String dutyTime8s;

    // 우편번호
    private String postCdn1;
    private String postCdn2;

    private String wgs84Lon; // 경도
    private String wgs84Lat; // 위도

    public SearchDrugStore(String dutyTime3c, String hpid, String dutyName, String dutyAddr, String dutyTel1, String dutyTime1c, String dutyTime2c, String dutyTime4c, String dutyTime5c, String dutyTime6c, String dutyTime7c, String dutyTime8c, String dutyTime1s, String dutyTime2s, String dutyTime3s, String dutyTime5s, String dutyTime4s, String dutyTime6s, String dutyTime7s, String dutyTime8s, String postCdn1, String postCdn2, String wgs84Lon, String wgs84Lat) {
        this.dutyTime3c = dutyTime3c;
        this.hpid = hpid;
        this.dutyName = dutyName;
        this.dutyAddr = dutyAddr;
        this.dutyTel1 = dutyTel1;
        this.dutyTime1c = dutyTime1c;
        this.dutyTime2c = dutyTime2c;
        this.dutyTime4c = dutyTime4c;
        this.dutyTime5c = dutyTime5c;
        this.dutyTime6c = dutyTime6c;
        this.dutyTime7c = dutyTime7c;
        this.dutyTime8c = dutyTime8c;
        this.dutyTime1s = dutyTime1s;
        this.dutyTime2s = dutyTime2s;
        this.dutyTime3s = dutyTime3s;
        this.dutyTime5s = dutyTime5s;
        this.dutyTime4s = dutyTime4s;
        this.dutyTime6s = dutyTime6s;
        this.dutyTime7s = dutyTime7s;
        this.dutyTime8s = dutyTime8s;
        this.postCdn1 = postCdn1;
        this.postCdn2 = postCdn2;
        this.wgs84Lon = wgs84Lon;
        this.wgs84Lat = wgs84Lat;
    }
}