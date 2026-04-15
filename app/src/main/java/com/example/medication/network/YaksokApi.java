package com.example.medication.network;

import com.example.medication.model.request.YaksokRequest;
import com.example.medication.model.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface YaksokApi {

    @POST("/api/yaksok")
    Call<ApiResponse<Long>> saveYaksok(@Body YaksokRequest request);
}
