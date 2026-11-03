package com.example.medication.model.response;

import com.example.medication.model.DrugStore;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class UserResponse implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("email")
    private String email;

    @SerializedName("nickname")
    private String nickname;

    private String role;

    @SerializedName("myDrugStore")
    private DrugStore myDrugStore;

    @SerializedName("notificationEnabled")
    private boolean notificationEnabled;

    @SerializedName("socialType")
    private String socialType;

    @SerializedName("hasPassword")
    private boolean hasPassword;
}
