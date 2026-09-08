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

import com.example.medication.databinding.ActivityFindPasswordBinding;
import com.example.medication.model.request.ResetPwSendCodeRequest;
import com.example.medication.model.request.ResetPwVerifyRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ResetPwVerifyResponse;
import com.example.medication.network.AuthApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.base.BaseActivity;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPassword extends BaseActivity {

    private ActivityFindPasswordBinding binding;

    private String sentLoginId, sentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSendCode.setOnClickListener(v -> sendCode(false));
        binding.verificationCode.setOnResendListener(() -> sendCode(true));
        binding.verificationCode.setOnVerifyListener(this::verify);
    }

    private void sendCode(boolean resend) {
        if (!binding.inputLoginId.isValid() || !binding.inputEmail.isValid()) {
            return;
        }
        String loginId = binding.inputLoginId.getText();
        String email = binding.inputEmail.getText();

        AuthApi api = NetworkClient.getAuthApi();
        api.sendResetPasswordCode(new ResetPwSendCodeRequest(loginId, email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isBusinessSuccess()) {
                    sentLoginId = loginId;
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
                Log.e("FindPassword", "send-code 실패: " + t.getMessage());
            }
        });
    }

    private void verify(String code) {
        AuthApi api = NetworkClient.getAuthApi();
        api.verifyResetPassword(new ResetPwVerifyRequest(sentLoginId, sentEmail, code))
                .enqueue(new Callback<ApiResponse<ResetPwVerifyResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ResetPwVerifyResponse>> call, Response<ApiResponse<ResetPwVerifyResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isBusinessSuccess() && response.body().getData() != null) {
                            binding.verificationCode.stop();
                            Intent intent = new Intent(FindPassword.this, ResetPassword.class);
                            intent.putExtra(ResetPassword.EXTRA_RESET_TOKEN, response.body().getData().getResetToken());
                            startActivity(intent);
                            finish();
                        } else {
                            binding.verificationCode.showError(serverMessage(response, "인증에 실패했습니다."));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ResetPwVerifyResponse>> call, Throwable t) {
                        showToast("네트워크 연결을 확인해주세요.");
                        Log.e("FindPassword", "verify 실패: " + t.getMessage());
                    }
                });
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
            Log.e("FindPassword", "에러 파싱 실패: " + e.getMessage());
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
