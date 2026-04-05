package com.example.medication.network;

import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.MedicineSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MedicineApi {

    @GET("/getMdcinGrnldtfclnfoList03")
    Call<ApiResponse<MedicineSearchResponse>> searchAllList(
            @Query("serviceKey") String key);

    @GET("/getMdcinGrnldtfclnfoList03")
    Call<ApiResponse<MedicineSearchResponse>> searchAllList(
            @Query("serviceKey") String key,
            @Query("keyword") String keyword);
}
