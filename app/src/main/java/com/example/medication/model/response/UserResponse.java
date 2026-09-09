package com.example.medication.model.response;

import com.example.medication.model.DrugStore;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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

    public Long getId(){ return id;}
    public void setId(Long id){ this.id = id; }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }
    public DrugStore getMyDrugStore() {
        return myDrugStore;
    }
}
