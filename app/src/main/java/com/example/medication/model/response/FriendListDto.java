package com.example.medication.model.response;

import java.util.List;

import lombok.Getter;

@Getter
public class FriendListDto {
    private List<FriendResponseDto> friends;
    private int totalCount;
}
