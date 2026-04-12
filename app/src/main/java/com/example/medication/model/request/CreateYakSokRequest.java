package com.example.medication.model.request;

import com.example.medication.model.MedicationSetting;

import java.util.Date;
import java.util.List;


public class CreateYakSokRequest {
    private Date date;
    private String name;
    private List<MedicationSetting> list;

    public CreateYakSokRequest(Date date, String name, List<MedicationSetting> list) {
        this.date = date;
        this.name = name;
        this.list = list;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MedicationSetting> getList() {
        return list;
    }

    public void setList(List<MedicationSetting> list) {
        this.list = list;
    }
}
