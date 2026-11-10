package com.example.medication.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.LoginResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.main.MainActivity;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthMethod {

    public static void onLoginSuccess(Activity activity, LoginResponse loginResponse){
        UserResponse user = loginResponse.getUser();

        SprefsManager.saveTokens(activity, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        SprefsManager.setUserInfo(activity, user);

        getAndSendFcmToken(user.getId());

        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    public static void sendTokenToServer(Long userId, String token){
        FirebaseTokenRequest request = new FirebaseTokenRequest(token);
        UserApi api = NetworkClient.getApi();

        api.updateFcmToken(request).enqueue(new Callback<ApiResponse<Void>>(){
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response){
                if(response.isSuccessful()) {
                    Log.d("FcmToken", "토큰을 저장적으로 저장하였습니다.");
                }else{
                    Log.e("FcmToken", "토큰 저장에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("FcmToken", "네트워크 통신 실패: " + t.getMessage());
            }
        });
    }

    public static void getAndSendFcmToken(Long userId){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FcmToken", "FCM 토큰 획득 실패", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FcmToken", "획득한 토큰: " + token);
                    sendTokenToServer(userId, token);
                });
    }

    public static void deleteToken(Activity activity, Runnable onDone) {
        Long userId = SprefsManager.getUserId(activity);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FcmToken", "로그아웃 중 토큰 조회 실패", task.getException());
                        onDone.run();
                        return;
                    }
                    sendTokenDeleteToServer(userId, task.getResult(), onDone);
                });
    }

    public static void sendTokenDeleteToServer(Long userId, String token, Runnable onDone) {
        FirebaseTokenRequest request = new FirebaseTokenRequest(token);
        UserApi api = NetworkClient.getApi();

        api.deleteFcmToken(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d("FcmToken", "토큰을 저장적으로 삭제하였습니다.");
                } else {
                    Log.e("FcmToken", "토큰 삭제에 실패했습니다.");
                }
                onDone.run();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("FcmToken", "네트워크 통신 실패: " + t.getMessage());
                onDone.run();
            }
        });
    }
}
