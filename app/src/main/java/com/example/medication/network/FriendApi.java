package com.example.medication.network;

import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FriendListDto;

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
    @POST("/api/friend/request")
    Call<ApiResponse<Void>> createFriendRequest(@Body FriendRequestCreateDto request);

    @PATCH("/api/friend/request/{requestId}/answer")Call<ApiResponse<Void>> answerFriendRequest(
            @Path("requestId") Long requestId,
            @Body FriendRequestAnswerDto answer);


    /**
     *  친구 관계 API
     */

    @GET("/api/friend/list") Call<ApiResponse<FriendListDto>> getFriendList(@Query("loginUserId") Long loginUserId);
}
