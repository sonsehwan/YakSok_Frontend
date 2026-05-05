package com.example.medication.network;

import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.request.LoginRequest;
import com.example.medication.model.request.ModifyInfoRequest;
import com.example.medication.model.request.ModifyPasswordRequest;
import com.example.medication.model.request.UserRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserApi {

    @POST("/api/users/signup")
    Call<ApiResponse<Void>> signUp(@Body UserRequest request);

    @POST("/api/users/login")
    Call<ApiResponse<UserResponse>> login(@Body LoginRequest request);

    @PATCH("/api/users/info")
    Call<ApiResponse<UserResponse>> modifyNickname(@Body ModifyInfoRequest request);

    @PATCH("/api/users/password")
    Call<ApiResponse<Void>> modifyPassword(@Body ModifyPasswordRequest request);

    @DELETE("api/users/{email}")
    Call<ApiResponse<Void>> deleteUser(@Path("email") String email);

    @POST("/api/Users/fcm-token")
    Call<ApiResponse<Void>> updateFcmToken(@Body FirebaseTokenRequest request);
}