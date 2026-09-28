package com.example.medication.model.response;

import lombok.Getter;

@Getter
public class SharedUser {
    Long userId;
    String nickName;

    public SharedUser(Long userId, String nickName) {
        this.userId = userId;
        this.nickName = nickName;
    }
}
