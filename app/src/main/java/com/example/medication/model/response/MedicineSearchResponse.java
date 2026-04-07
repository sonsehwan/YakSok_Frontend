package com.example.medication.model.response;

public class MedicineSearchResponse {

    private String image;
    private String name;

    public MedicineSearchResponse(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }
}
