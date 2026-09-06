package com.example.medication.network;

import com.example.medication.MedicationApp;
import com.example.medication.util.SprefsManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException{
        Request original = chain.request();

        String accessToken = SprefsManager.getAccessToken(MedicationApp.getContext());

        if(accessToken == null){
            return chain.proceed(original);
        }

        Request authorized = original.newBuilder()
                .header("Authorization", "Bearer" + accessToken)
                .build();

        return chain.proceed(authorized);
    }
}
