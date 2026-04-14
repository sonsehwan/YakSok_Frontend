package com.example.medication.model.request;

import java.util.Date;
import java.util.List;


public class CreateYakSokRequest {
    private Date date;
    private String name;
    private List<PillRequest> list;

    public CreateYakSokRequest(Date date, String name, List<PillRequest> list) {
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

    public List<PillRequest> getList() {
        return list;
    }

    public void setList(List<PillRequest> list) {
        this.list = list;
    }
}
