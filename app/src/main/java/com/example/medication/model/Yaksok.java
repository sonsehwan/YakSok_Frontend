package com.example.medication.model;

import com.example.medication.model.request.PillRequest;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Yaksok implements Serializable {
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
    private String ownerNickname; // 약속을 공유한 유저 이름(공유 약속 이외에는 null)
    private int totalNotifications;
    private int currentClearNotifications;

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
}
