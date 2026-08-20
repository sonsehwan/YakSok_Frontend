package com.example.medication.network;

import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.CreateYakSokRequest;
import com.example.medication.model.request.ShareYaksokRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SaveYaksokResponse;
import com.example.medication.model.response.SharedUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YaksokApi {

    @POST("/api/yaksok")
    Call<ApiResponse<SaveYaksokResponse>> saveYaksok(@Body CreateYakSokRequest request);

    @PUT("/api/yaksok/{id}")
    Call<ApiResponse<SaveYaksokResponse>> updateYaksok(
            @Path("id") Long id,
            @Body CreateYakSokRequest request
    );

    @PATCH("api/yaksok/notifications/{notificationId}/status")
    Call<ApiResponse<Void>> updateNotificationStatus(
            @Path("notificationId") Long notificationId,
            @Query("isTaken") boolean isTaken
    );

    @GET("api/yaksok/{userId}/notifications")
    Call<ApiResponse<List<NotificationYaksok>>> getNotifications(@Path("userId") Long userId);

    @GET("api/yaksok/list/{userId}")
    Call<ApiResponse<List<Yaksok>>> getYaksokList(@Path("userId") Long userId);

    @DELETE("api/yaksok/{id}")
    Call<ApiResponse<Void>> deleteYaksok(@Path("id") Long id);

    // 약속 단일 조회 (채팅에서 공유받은 약속 확인용)
    @GET("api/yaksok/{yaksokId}")
    Call<ApiResponse<Yaksok>> getYaksok(@Path("yaksokId") Long yaksokId);


    /***
     *  공유 약속 API
     */

    // 공유받은 약속을 내 목록에 저장
    @POST("/api/shared-yaksok")
    Call<ApiResponse<Void>> saveSharedYaksok(@Body ShareYaksokRequest request);

    // 공유자 목록
    @GET("/api/shared-yaksok/sender")
    Call<ApiResponse<List<SharedUser>>> getSharedUserList(@Query("userId") Long userId);

    // 저장한 공유 약속 목록
    @GET("/api/shared-yaksok")
    Call<ApiResponse<List<Yaksok>>> getSharedYaksokList(
            @Query("userId") Long userId,
            @Query("senderId") Long senderId);

    // 공유 목록에서 제외
    @DELETE("/api/shared-yaksok/{yaksokId}")
    Call<ApiResponse<Void>> deleteSharedYaksok(
            @Path("yaksokId") Long yaksokId,
            @Query("userId") Long userId);
}
