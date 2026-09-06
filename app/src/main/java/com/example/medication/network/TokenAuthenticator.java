package com.example.medication.network;

import com.example.medication.MedicationApp;
import com.example.medication.model.request.RefreshTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.TokenResponse;
import com.example.medication.util.AuthEventBus;
import com.example.medication.util.SprefsManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {

    private static final String BASE_URL = "http://13.209.186.24:8080/";

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (responseCount(response) >= 2) {
            return null;
        }

        String refreshToken = SprefsManager.getRefreshToken(MedicationApp.getContext());
        if (refreshToken == null) {
            return null;
        }

        AuthApi authApi = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi.class);

        retrofit2.Response<ApiResponse<TokenResponse>> result =
                authApi.refresh(new RefreshTokenRequest(refreshToken)).execute();

        if (!result.isSuccessful() || result.body() == null || !result.body().isBusinessSuccess()) {
            forceLogout();
            return null;
        }

        TokenResponse newTokens = result.body().getData();

        SprefsManager.saveTokens(
                MedicationApp.getContext(),
                newTokens.getAccessToken(),
                newTokens.getRefreshToken()
        );

        return response.request().newBuilder()
                .header("Authorization", "Bearer " + newTokens.getAccessToken())
                .build();
    }

    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }

    private void forceLogout(){
        SprefsManager.clearUserInfo(MedicationApp.getContext());
        AuthEventBus.get().publish();
    }
}