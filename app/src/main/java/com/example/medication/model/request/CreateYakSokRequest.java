package com.example.medication.model.request;

import com.example.medication.model.Yaksok;


public class CreateYakSokRequest {
    private String userEmail;
    private Yaksok yaksok;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Yaksok getYaksok() {
        return yaksok;
    }

    public void setYaksok(Yaksok yaksok) {
        this.yaksok = yaksok;
    }

    public CreateYakSokRequest(String userEmail, Yaksok yaksok) {
        this.userEmail = userEmail;
        this.yaksok = yaksok;
    }
}
