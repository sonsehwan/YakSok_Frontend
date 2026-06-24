package com.example.medication.network;

import com.example.medication.model.DrugStore;
import com.example.medication.model.SearchDrugStore;
import com.example.medication.model.request.CreateDrugStoreRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DrugStoreApi {

    @GET("/api/drugstore/closelist")
    Call<ApiResponse<List<DrugStore>>> getCloseDrugstores(
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("page") int page
    );

    @GET("/api/drugstore/searchlist")
    Call<ApiResponse<List<SearchDrugStore>>> getSearchDrugstores(
            @Query("firstAddress") String firstAddress,
            @Query("secondAddress") String secondAddress,
            @Query("name") String name
    );

    @POST("/api/drugstore")
    Call<ApiResponse<UserResponse>> createDrugStore(@Body CreateDrugStoreRequest request);
}
