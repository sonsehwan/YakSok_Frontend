package com.example.medication.model.response;

import com.example.medication.model.DrugStore;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

public class UserResponse {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("nickname")
    private String nickname;

    private String gender;

    private LocalDate birthdate;


    private Boolean penaltyEnable = false;

    private Boolean isLocked = false;

    private String role;

    private DrugStore myDrugStore;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }
    public Boolean getPenaltyEnable() {
        return penaltyEnable;
    }
    public Boolean getIsLocked() {
        return isLocked;
    }
    public String getRole() {
        return role;
    }
    public DrugStore getMyDrugStore() {
        return myDrugStore;
    }
}
