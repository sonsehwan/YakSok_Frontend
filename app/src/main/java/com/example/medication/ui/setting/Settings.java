package com.example.medication.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.ChatRoomList;
import com.example.medication.FriendList;
import com.example.medication.R;
import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.login.Login;
import com.example.medication.ui.main.MainActivity;
import com.example.medication.ui.myinfo.MyInfo;
import com.example.medication.ui.yaksok.YaksokList;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Settings extends AppCompatActivity {

    private ImageView ivLogout;
    private LinearLayout llMyInfo;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        InsetsUtil.applySystemBarPadding(findViewById(R.id.main));

        // 1. 뷰 초기화
        initViews();

        bottomNav.setSelectedItemId(R.id.nav_settings);

        setupClickListeners();
    }

    private void setupClickListeners() {
        ivLogout.setOnClickListener(v -> {
            showLogOutDialog();
        });

        llMyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, MyInfo.class);
            startActivity(intent);
        });

        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(Settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(Settings.this, YaksokList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true; // 현재 화면이므로 아무것도 안 함
            }else if(itemId == R.id.nav_chat) {
                Intent intent = new Intent(Settings.this, ChatRoomList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }else if (itemId == R.id.nav_friend){
                Intent intent = new Intent(Settings.this, FriendList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void showLogOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    deleteToken();
                    SprefsManager.clearUserInfo(Settings.this);
                    finishAffinity();
                    Intent intent = new Intent(Settings.this, Login.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteToken(){
        Long userId = SprefsManager.getUserId(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()){
                        Log.w("FcmToken", "로그아웃 중 토큰 조회 실패", task.getException());
                        return;
                    }
                    sendTokenDeleteToServer(userId, task.getResult());
                });
    }

    private void sendTokenDeleteToServer(Long userId, String token){
        FirebaseTokenRequest request = new FirebaseTokenRequest(token);
        UserApi api = NetworkClient.getApi();

        api.deleteFcmToken(userId, request).enqueue(new Callback<ApiResponse<Void>>(){
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response){
                if(response.isSuccessful()) {
                    Log.d("FcmToken", "토큰을 저장적으로 삭제하였습니다.");
                }else{
                    Log.e("FcmToken", "토큰 삭제에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("FcmToken", "네트워크 통신 실패: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        ivLogout = findViewById(R.id.iv_logout);
        llMyInfo = findViewById(R.id.ll_my_info);
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_settings);
    }
}