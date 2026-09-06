package com.example.medication.network;

import com.example.medication.model.request.RefreshTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/refresh")
    Call<ApiResponse<TokenResponse>> refresh(@Body RefreshTokenRequest request);

    @POST("/api/auth/logout")
    Call<ApiResponse<Void>> logout(@Body RefreshTokenRequest request);
}
