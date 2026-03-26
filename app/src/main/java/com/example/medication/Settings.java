package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // 2. 하단바 상태 설정 (현재 '설정' 탭이 선택된 상태로 표시)
        bottomNav.setSelectedItemId(R.id.nav_settings);

        // 3. 클릭 이벤트 설정
        setupClickListeners();
    }

    private void setupClickListeners() {
        // 상단 로그아웃 아이콘 클릭
        ivLogout.setOnClickListener(v -> showLogOutDialog());

        // 내 정보 관리 클릭
        llMyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, MyInfoActivity.class);
            startActivity(intent);
        });

        // 공유 관리 클릭 (준비 중)
        llShare.setOnClickListener(v -> showToast("공유 관리 기능은 준비 중입니다."));

        // 일반 설정 클릭 (준비 중)
        llGeneral.setOnClickListener(v -> showToast("일반 설정 기능은 준비 중입니다."));

        // 하단 네비게이션 메뉴 전환
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // 홈(MainActivity)으로 전환
                Intent intent = new Intent(Settings.this, MainActivity.class);
                startActivity(intent);
                // 탭 전환 느낌을 위해 애니메이션 제거
                overridePendingTransition(0, 0);
                finish(); // 현재 설정 화면은 종료
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(Settings.this, WipActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true; // 현재 화면이므로 아무것도 안 함
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

                    // 2. 로그인 화면으로 이동하며 스택 제거
                    Intent intent = new Intent(Settings.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish(); // 현재 화면 종료
                })
                .setNegativeButton("취소", null)
                .show();
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
}