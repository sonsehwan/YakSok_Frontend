package com.example.medication.network;

import com.example.medication.model.DrugStore;
import com.example.medication.model.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DrugStoreApi {

    @GET("/api/drugstore/closelist")
    Call<ApiResponse<List<DrugStore>>> getCloseDrugstores(
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("page") int page
    );
}
