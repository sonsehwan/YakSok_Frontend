package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.util.SprefsManager;

public class SharingPage extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout llFriend, llShareYaksok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_page);

        // 1. 뷰 초기화
        initViews();

        setupClickListeners();
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> {
            finish();
        });

        llFriend.setOnClickListener(v -> {
            Intent intent = new Intent(SharingPage.this, WipActivity.class);
            startActivity(intent);
        });

        llShareYaksok.setOnClickListener(v -> {
            Intent intent = new Intent(SharingPage.this, WipActivity.class);
            startActivity(intent);
        });
    }

    private void showLogOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    // 1. 세션 데이터(Sprefs) 삭제
                    SprefsManager.clearUserInfo(SharingPage.this);

                    Intent intent = new Intent(SharingPage.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        llFriend = findViewById(R.id.ll_friend);
        llShareYaksok = findViewById(R.id.ll_share_yaksok);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}