package com.example.medication.network;

import com.example.medication.model.request.LoginRequest;
import com.example.medication.model.request.UserRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {
    /**
     * 회원가입 API 호출
     * @param request 안드로이드에서 모은 회원 정보 객체
     * @return 서버로부터의 응답(성공 여부, 메시지 등)
     */
    @POST("/api/users/signup")
    Call<ApiResponse<Void>> signUp(@Body UserRequest request);

    @POST("/api/users/login")
    Call<ApiResponse<UserResponse>> login(@Body LoginRequest request);
}