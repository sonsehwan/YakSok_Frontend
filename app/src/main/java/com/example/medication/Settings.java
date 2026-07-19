package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Settings extends AppCompatActivity {

    private ImageView ivLogout;
    private LinearLayout llMyInfo, llShare, llGeneral;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // 1. 뷰 초기화
        initViews();

        bottomNav.setSelectedItemId(R.id.nav_settings);

        setupClickListeners();
    }

    private void setupClickListeners() {
        ivLogout.setOnClickListener(v -> {
            showLogOutDialog();
            deleteToken();
        });

        llMyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, MyInfoActivity.class);
            startActivity(intent);
        });

        llShare.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, SharingPage.class);
            startActivity(intent);
        });

        llGeneral.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this,WipActivity.class);
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
            }else if(itemId == R.id.nav_drugstore){
                Intent intent = new Intent(Settings.this, DrugStoreList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
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
                    // 1. 세션 데이터(Sprefs) 삭제
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
        String email = SprefsManager.getUserEmail(this);
        FirebaseTokenRequest request = new FirebaseTokenRequest(null);
        UserApi api = NetworkClient.getApi();

        api.updateFcmToken(email, request).enqueue(new Callback<ApiResponse<Void>>(){
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
        llShare = findViewById(R.id.ll_share);
        llGeneral = findViewById(R.id.ll_general);
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_settings);
    }
}