package com.example.medication.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medication.InputView;
import com.example.medication.R;
import com.example.medication.model.request.ResetPwSendCodeRequest;
import com.example.medication.model.request.ResetPwVerifyRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.AuthApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPassword extends BaseActivity {

    private static final long RESEND_COOLDOWN_MS = 60_000L;

    private InputView inputLoginId, inputEmail, inputCode;
    private MaterialButton btnSendCode, btnVerify, btnGoLogin;
    private TextView tvResult;

    private String sentLoginId, sentEmail;
    private CountDownTimer cooldownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);

        initViews();

        btnSendCode.setOnClickListener(v -> sendCode());
        btnVerify.setOnClickListener(v -> verify());
        btnGoLogin.setOnClickListener(v -> goLogin());
    }

    private void initViews() {
        inputLoginId = findViewById(R.id.input_login_id);
        inputEmail = findViewById(R.id.input_email);
        inputCode = findViewById(R.id.input_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnVerify = findViewById(R.id.btn_verify);
        btnGoLogin = findViewById(R.id.btn_go_login);
        tvResult = findViewById(R.id.tv_result);
    }

    private void sendCode() {
        if (!inputLoginId.isValid() || !inputEmail.isValid()) {
            return;
        }
        String loginId = inputLoginId.getText();
        String email = inputEmail.getText();

        AuthApi api = NetworkClient.getAuthApi();
        api.sendResetPasswordCode(new ResetPwSendCodeRequest(loginId, email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isBusinessSuccess()) {
                    sentLoginId = loginId;
                    sentEmail = email;
                    inputCode.setVisibility(View.VISIBLE);
                    btnVerify.setVisibility(View.VISIBLE);
                    startCooldown();
                    showToast("인증코드를 이메일로 발송했습니다.");
                } else {
                    handleError(response, "인증코드 발송에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("FindPassword", "send-code 실패: " + t.getMessage());
            }
        });
    }

    private void verify() {
        String code = inputCode.getText();
        if (code.isEmpty()) {
            inputCode.showError("인증코드를 입력해주세요.");
            return;
        }

        AuthApi api = NetworkClient.getAuthApi();
        api.verifyResetPassword(new ResetPwVerifyRequest(sentLoginId, sentEmail, code)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isBusinessSuccess()) {
                    showResult();
                } else {
                    handleError(response, "인증에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("FindPassword", "verify 실패: " + t.getMessage());
            }
        });
    }

    private void showResult() {
        cancelCooldown();
        inputLoginId.setVisibility(View.GONE);
        inputEmail.setVisibility(View.GONE);
        btnSendCode.setVisibility(View.GONE);
        inputCode.setVisibility(View.GONE);
        btnVerify.setVisibility(View.GONE);

        tvResult.setText("임시 비밀번호를 이메일로 보냈습니다.\n로그인 후 비밀번호를 변경해주세요.");
        tvResult.setVisibility(View.VISIBLE);
        btnGoLogin.setVisibility(View.VISIBLE);
    }

    private void goLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startCooldown() {
        cancelCooldown();
        btnSendCode.setEnabled(false);
        cooldownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                btnSendCode.setText((msLeft / 1000) + "초 후 재발송");
            }

            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("인증코드 재발송");
            }
        }.start();
    }

    private void cancelCooldown() {
        if (cooldownTimer != null) {
            cooldownTimer.cancel();
            cooldownTimer = null;
        }
    }

    private void handleError(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                ApiResponse<?> body = new Gson().fromJson(json, ApiResponse.class);
                if (body != null && body.getMessage() != null) {
                    showToast(body.getMessage());
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("FindPassword", "에러 파싱 실패: " + e.getMessage());
        }
        showToast(fallback);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCooldown();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof android.widget.EditText) {
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
