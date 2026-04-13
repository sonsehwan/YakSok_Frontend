package com.example.medication.model.request;

import java.util.List;

public class YaksokRequest {
    private String date;
    private String name;

    private List<PillRequest> pills;

    public YaksokRequest(String date, String name, List<PillRequest> pills) {
        this.date = date;
        this.name = name;
        this.pills = pills;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PillRequest> getPills() {
        return pills;
    }

    public void setPills(List<PillRequest> pills) {
        this.pills = pills;
    }
}
