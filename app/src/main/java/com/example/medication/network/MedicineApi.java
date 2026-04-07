package com.example.medication.network;

import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.MedicineSearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MedicineApi {

    @GET("/api/medicine/search")
    Call<ApiResponse<List<MedicineSearchResponse>>> searchMedicine(
            @Query("keyword") String keyword,
            @Query("pageNo") int pageNo
    );
}
