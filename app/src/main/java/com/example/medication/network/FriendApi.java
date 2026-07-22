package com.example.medication.network;

import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FriendListDto;
import com.example.medication.model.response.ReceivedFriendRequestDto;
import com.example.medication.model.response.UserSearchResultDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FriendApi {

    /**
     *  친구 요청 API
     */
    // 닉네임으로 사용자 검색
    @GET("/api/friend/search")
    Call<ApiResponse<UserSearchResultDto>> searchUser(
            @Query("nickname") String nickname,
            @Query("loginUserId") Long loginUserId);

    // 친구 요청 보내기
    @POST("/api/friend/request")
    Call<ApiResponse<Void>> createFriendRequest(@Body FriendRequestCreateDto request);

    // 내가 받은 친구 요청 목록
    @GET("/api/friend/request/received")
    Call<ApiResponse<List<ReceivedFriendRequestDto>>> getReceivedFriendRequests(
            @Query("loginUserId") Long loginUserId);

    // 친구 요청 수락/거절
    @PATCH("/api/friend/request/{requestId}/answer")Call<ApiResponse<Void>> answerFriendRequest(
            @Path("requestId") Long requestId,
            @Body FriendRequestAnswerDto answer);


    /**
     *  친구 관계 API
     */

    //  친구 목록 가져오기
    @GET("/api/friend/list") Call<ApiResponse<FriendListDto>> getFriendList(@Query("loginUserId") Long loginUserId);
}
