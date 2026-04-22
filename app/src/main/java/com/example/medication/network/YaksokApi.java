package com.example.medication.network;

import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.request.CreateYakSokRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SaveYaksokResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YaksokApi {

    @POST("/api/yaksok")
    Call<ApiResponse<SaveYaksokResponse>> saveYaksok(@Body CreateYakSokRequest request);

    @PATCH("api/yaksok/notifications/{notificationId}/status")
    Call<ApiResponse<Void>> updateNotificationStatus(
            @Path("notificationId") Long notificationId,
            @Query("isTaken") boolean isTaken
    );

    @GET("api/yaksok/{userEmail}/notifications")
    Call<ApiResponse<List<NotificationYaksok>>> getNotifications(@Path("userEmail") String userEmail);
}
