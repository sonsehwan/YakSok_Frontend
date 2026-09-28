package com.example.medication.model.response;

import com.example.medication.model.Yaksok;
import com.google.gson.annotations.SerializedName;

public class SaveYaksokResponse {
    @SerializedName("yaksok")
    Yaksok yaksok;

    public Yaksok getYaksok() {
        return yaksok;
    }

    public void setYaksok(Yaksok yaksok) {
        this.yaksok = yaksok;
    }

    public SaveYaksokResponse(Yaksok yaksok) {
        this.yaksok = yaksok;
    }
}
