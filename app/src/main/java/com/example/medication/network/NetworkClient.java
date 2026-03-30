package com.example.medication.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    private static final String BASE_URL = "http://54.116.63.204:8081/";

    private static Retrofit retrofit = null;

    public static UserApi getApi() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS) // 서버 연결 대기 시간 (15초)
                    .readTimeout(15, TimeUnit.SECONDS)    // 데이터 읽기 대기 시간 (15초)
                    .writeTimeout(15, TimeUnit.SECONDS)   // 데이터 쓰기 대기 시간 (15초)
                    .build();

            try {
                // 2. 설정된 OkHttpClient를 Retrofit에 적용
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return retrofit.create(UserApi.class);
    }
}