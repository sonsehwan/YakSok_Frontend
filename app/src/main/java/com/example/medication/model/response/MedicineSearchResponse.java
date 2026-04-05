package com.example.medication.model.response;

import com.example.medication.model.SimpleMedicine;

import java.util.List;

import retrofit2.http.Body;

public class MedicineSearchResponse {

    private Body body;

    private class body{
        private List<SimpleMedicine> list;
    }
}
