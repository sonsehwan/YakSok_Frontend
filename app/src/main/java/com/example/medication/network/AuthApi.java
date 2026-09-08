package com.example.medication.network;

import com.example.medication.model.request.FindIdSendCodeRequest;
import com.example.medication.model.request.FindIdVerifyRequest;
import com.example.medication.model.request.RefreshTokenRequest;
import com.example.medication.model.request.ResetPwConfirmRequest;
import com.example.medication.model.request.ResetPwSendCodeRequest;
import com.example.medication.model.request.ResetPwVerifyRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FindIdResponse;
import com.example.medication.model.response.ResetPwVerifyResponse;
import com.example.medication.model.response.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/refresh")
    Call<ApiResponse<TokenResponse>> refresh(@Body RefreshTokenRequest request);

    @POST("/api/auth/logout")
    Call<ApiResponse<Void>> logout(@Body RefreshTokenRequest request);

    @POST("/api/auth/find-id/send-code")
    Call<ApiResponse<Void>> sendFindIdCode(@Body FindIdSendCodeRequest request);

    @POST("/api/auth/find-id/verify")
    Call<ApiResponse<FindIdResponse>> verifyFindId(@Body FindIdVerifyRequest request);

    @POST("/api/auth/reset-password/send-code")
    Call<ApiResponse<Void>> sendResetPasswordCode(@Body ResetPwSendCodeRequest request);

    @POST("/api/auth/reset-password/verify")
    Call<ApiResponse<ResetPwVerifyResponse>> verifyResetPassword(@Body ResetPwVerifyRequest request);

    @POST("/api/auth/reset-password/confirm")
    Call<ApiResponse<Void>> confirmResetPassword(@Body ResetPwConfirmRequest request);
}
