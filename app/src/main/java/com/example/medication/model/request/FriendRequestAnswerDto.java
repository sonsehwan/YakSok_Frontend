package com.example.medication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRequestAnswerDto {
    private Long loginUserId;
    private Boolean answer;
}
