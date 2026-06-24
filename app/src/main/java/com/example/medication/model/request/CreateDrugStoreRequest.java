package com.example.medication.model.request;

import com.example.medication.model.SearchDrugStore;

public class CreateDrugStoreRequest {
    String email;
    SearchDrugStore drugStore;

    public CreateDrugStoreRequest(String email, SearchDrugStore store) {
        this.email = email;
        this.drugStore = store;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SearchDrugStore getStore() {
        return drugStore;
    }

    public void setStore(SearchDrugStore store) {
        this.drugStore = store;
    }
}
