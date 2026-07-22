package com.example.medication.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    private static final String BASE_URL = "http://13.209.186.24:8080/";

    private static Retrofit userRetrofit = null;
    private static Retrofit friendRetrofit = null;
    private static Retrofit pillRetrofit = null;
    private static Retrofit yaksokRetrofit = null;
    private static Retrofit drugStoreRetrofit = null;
    private static Retrofit chatRetrofit = null;

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

    //친구관련 api 통신
    public static FriendApi getFriendApi(){
        if (friendRetrofit == null) {
            try {
                // 2. 설정된 OkHttpClient를 Retrofit에 적용
                friendRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return friendRetrofit.create(FriendApi.class);
    }

    // 약 정보 관련 API 통신
    public static PillApi getMedicineApi() {
        if (pillRetrofit == null) {
            try {
                // 2. 설정된 OkHttpClient를 Retrofit에 적용
                pillRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return pillRetrofit.create(PillApi.class);
    }

    public static YaksokApi getYaksokApi() {
        if (yaksokRetrofit == null) {
            try {
                yaksokRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return yaksokRetrofit.create(YaksokApi.class);
    }

    public static DrugStoreApi getDrugStoreApi() {
        if (drugStoreRetrofit == null) {
            try {
                drugStoreRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return drugStoreRetrofit.create(DrugStoreApi.class);
    }

    public static ChatApi getChatApi() {
        if (chatRetrofit == null) {
            try {
                chatRetrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                android.util.Log.e("NetworkClient", "Retrofit 초기화 실패: " + e.getMessage());
            }
        }
        return chatRetrofit.create(ChatApi.class);
    }
}