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

    public static UserProfileDto fromFriend(FriendResponseDto friend) {
        UserProfileDto profile = new UserProfileDto();
        profile.userId = friend.getFriendId();
        profile.nickname = friend.getNickname();
        profile.email = friend.getEmail();
        profile.relation = RelationStatus.FRIEND;
        return profile;
    }
}
