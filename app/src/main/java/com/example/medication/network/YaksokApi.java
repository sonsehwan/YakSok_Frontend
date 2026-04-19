package com.example.medication.network;

import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SaveYaksokResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface YaksokApi {

    @POST("/api/yaksok")
    Call<ApiResponse<SaveYaksokResponse>> saveYaksok(@Body Yaksok request);
}
