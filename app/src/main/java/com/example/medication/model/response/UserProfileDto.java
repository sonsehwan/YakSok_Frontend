package com.example.medication.model.response;

import lombok.Getter;

@Getter
public class UserProfileDto {
    private Long userId;
    private String nickname;
    private String email;
    private RelationStatus relation;
    private Long requestId;
    public enum RelationStatus {
        SELF, FRIEND, SENT, RECEIVED, NONE
    }
}
