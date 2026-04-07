package com.example.medication.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    private static final String BASE_URL = "http://54.116.63.204:8081/";
    private static final String MD_URL = "http://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService03/";

    private static Retrofit userRetrofit = null;
    private static Retrofit medicineRetrofit = null;

    private static OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    // 회원관련 api 통신
    public static UserApi getApi() {
        if (userRetrofit == null) {
            try {
                // 2. 설정된 OkHttpClient를 Retrofit에 적용
                userRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return userRetrofit.create(UserApi.class);
    }

    // 약 정보 관련 API 통신
    public static MedicineApi getMedicineApi() {
        if (medicineRetrofit == null) {
            try {
                // 2. 설정된 OkHttpClient를 Retrofit에 적용
                medicineRetrofit = new Retrofit.Builder()
                        .baseUrl(MD_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return medicineRetrofit.create(MedicineApi.class);
    }
}