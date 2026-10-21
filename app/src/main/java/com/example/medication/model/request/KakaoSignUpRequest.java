package com.example.medication.model.request;

import com.example.medication.model.SearchDrugStore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoSignUpRequest {
    private final String accessToken;
    private final String role;
    private final String nickname;
    private final SearchDrugStore drugStore;

}
