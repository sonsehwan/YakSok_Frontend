package com.example.medication.model.request;

public class PillRequest {

    private String name;
    private String image;
    private int dayFrequncy;
    private int duration;
    private int dayDosage;

    public PillRequest(String name, String image, int dayFrequncy, int duration, int dayDosage) {
        this.name = name;
        this.image = image;
        this.dayFrequncy = dayFrequncy;
        this.duration = duration;
        this.dayDosage = dayDosage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getDayFrequncy() {
        return dayFrequncy;
    }

    public void setDayFrequncy(int dayFrequncy) {
        this.dayFrequncy = dayFrequncy;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDayDosage() {
        return dayDosage;
    }

    public void setDayDosage(int dayDosage) {
        this.dayDosage = dayDosage;
    }
}
