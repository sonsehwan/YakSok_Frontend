package com.example.medication.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.medication.databinding.ActivityResetPasswordBinding;
import com.example.medication.model.request.ResetPwConfirmRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.AuthApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.base.BaseActivity;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPassword extends BaseActivity {

    public static final String EXTRA_RESET_TOKEN = "resetToken";

    private ActivityResetPasswordBinding binding;
    private String resetToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetToken = getIntent().getStringExtra(EXTRA_RESET_TOKEN);
        if (resetToken == null || resetToken.isEmpty()) {
            showToast("잘못된 접근입니다.");
            goLogin();
            return;
        }

        binding.inputCheckPw.setOnValidateListener(text -> {
            if (!text.equals(binding.inputNewPw.getText())) {
                return "비밀번호가 일치하지 않습니다.";
            }
            return null;
        });

        binding.btnConfirm.setOnClickListener(v -> confirm());
    }

    private void confirm() {
        if (!binding.inputNewPw.isValid() || !binding.inputCheckPw.isValid()) {
            return;
        }
        String newPassword = binding.inputNewPw.getText();

        AuthApi api = NetworkClient.getAuthApi();
        api.confirmResetPassword(new ResetPwConfirmRequest(resetToken, newPassword)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isBusinessSuccess()) {
                    showToast("비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.");
                    goLogin();
                } else {
                    // 토큰 만료 등은 되돌릴 수 없으므로 로그인 화면으로 보내 처음부터 다시 하게 한다
                    showToast(serverMessage(response, "비밀번호 변경에 실패했습니다."));
                    goLogin();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("ResetPassword", "confirm 실패: " + t.getMessage());
            }
        });
    }

    private void goLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String serverMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                ApiResponse<?> body = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (body != null && body.getMessage() != null) {
                    return body.getMessage();
                }
            }
        } catch (Exception e) {
            Log.e("ResetPassword", "에러 파싱 실패: " + e.getMessage());
        }
        return fallback;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
