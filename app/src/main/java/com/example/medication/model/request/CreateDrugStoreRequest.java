package com.example.medication.model.request;

import com.example.medication.model.SearchDrugStore;

public class CreateDrugStoreRequest {
    Long userId;
    SearchDrugStore drugStore;

    public CreateDrugStoreRequest(Long userId, SearchDrugStore store) {
        this.userId = userId;
        this.drugStore = store;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public SearchDrugStore getStore() {
        return drugStore;
    }

    public void setStore(SearchDrugStore store) {
        this.drugStore = store;
    }
}
