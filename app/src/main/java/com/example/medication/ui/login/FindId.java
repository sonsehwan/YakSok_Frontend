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

import com.example.medication.databinding.ActivityFindIdBinding;
import com.example.medication.model.request.FindIdSendCodeRequest;
import com.example.medication.model.request.FindIdVerifyRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FindIdResponse;
import com.example.medication.network.AuthApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.base.BaseActivity;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindId extends BaseActivity {

    private ActivityFindIdBinding binding;

    private String sentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindIdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSendCode.setOnClickListener(v -> sendCode(false));
        binding.verificationCode.setOnResendListener(() -> sendCode(true));
        binding.verificationCode.setOnVerifyListener(this::verify);
        binding.btnGoLogin.setOnClickListener(v -> goLogin());
    }

    private void sendCode(boolean resend) {
        if (!binding.inputEmail.isValid()) {
            return;
        }
        String email = binding.inputEmail.getText();

        AuthApi api = NetworkClient.getAuthApi();
        api.sendFindIdCode(new FindIdSendCodeRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isBusinessSuccess()) {
                    sentEmail = email;
                    if (!resend) binding.btnSendCode.setVisibility(View.GONE);
                    binding.verificationCode.start();
                    showToast("인증코드를 이메일로 발송했습니다.");
                } else {
                    if (resend) binding.verificationCode.onResendFailed();
                    showToast(serverMessage(response, "인증코드 발송에 실패했습니다."));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (resend) binding.verificationCode.onResendFailed();
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("FindId", "send-code 실패: " + t.getMessage());
            }
        });
    }

    private void verify(String code) {
        AuthApi api = NetworkClient.getAuthApi();
        api.verifyFindId(new FindIdVerifyRequest(sentEmail, code)).enqueue(new Callback<ApiResponse<FindIdResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FindIdResponse>> call, Response<ApiResponse<FindIdResponse>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isBusinessSuccess() && response.body().getData() != null) {
                    showResult(response.body().getData().getLoginId());
                } else {
                    binding.verificationCode.showError(serverMessage(response, "인증에 실패했습니다."));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FindIdResponse>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("FindId", "verify 실패: " + t.getMessage());
            }
        });
    }

    private void showResult(String loginId) {
        binding.verificationCode.stop();
        binding.inputEmail.setVisibility(View.GONE);
        binding.btnSendCode.setVisibility(View.GONE);
        binding.verificationCode.setVisibility(View.GONE);

        binding.tvResult.setText("회원님의 아이디는\n[ " + loginId + " ] 입니다.");
        binding.tvResult.setVisibility(View.VISIBLE);
        binding.btnGoLogin.setVisibility(View.VISIBLE);
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
            Log.e("FindId", "에러 파싱 실패: " + e.getMessage());
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
