package com.example.medication;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class VerificationCode extends LinearLayout {

    public interface OnSendListener { void onSend(String email); }
    public interface OnResendListener { void onResend(String email); }
    public interface OnVerifyListener { void onVerify(String code); }

    private static final long EXPIRY_MS = 5 * 60 * 1000L;
    private static final long RESEND_COOLDOWN_MS = 60 * 1000L;
    private static final long WARN_THRESHOLD_MS = 60 * 1000L;   // 남은 시간 1분 이하면 빨강

    private InputView inputEmail;
    private MaterialButton btnSendCode;
    private View rowExpiry;
    private TextView tvExpiry;
    private InputView inputCode;
    private MaterialButton btnVerify;
    private MaterialButton btnResend;
    private TextView tvCooldown;

    private final int colorNormal;
    private final int colorWarn = Color.parseColor("#FF0000");

    private CountDownTimer expiryTimer;
    private CountDownTimer cooldownTimer;
    private boolean expired = false;

    private OnSendListener sendListener;
    private OnResendListener resendListener;
    private OnVerifyListener verifyListener;

    public VerificationCode(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.component_verification_code, this, true);

        inputEmail = findViewById(R.id.input_email);
        btnSendCode = findViewById(R.id.btn_send_code);
        rowExpiry = findViewById(R.id.row_expiry);
        tvExpiry = findViewById(R.id.tv_expiry);
        inputCode = findViewById(R.id.input_code);
        btnVerify = findViewById(R.id.btn_verify);
        btnResend = findViewById(R.id.btn_resend);
        tvCooldown = findViewById(R.id.tv_cooldown);

        colorNormal = ContextCompat.getColor(context, R.color.p700);

        btnSendCode.setOnClickListener(v -> {
            if (!inputEmail.isValid()) return;
            if (sendListener != null) sendListener.onSend(inputEmail.getText());
        });

        btnResend.setOnClickListener(v -> {
            if (!inputEmail.isValid()) return;
            setResendEnabled(false);                  // 응답 올 때까지 중복 클릭 방지
            if (resendListener != null) resendListener.onResend(inputEmail.getText());
        });

        btnVerify.setOnClickListener(v -> {
            String code = inputCode.getText();
            if (code.isEmpty()) {
                inputCode.showError("인증코드를 입력해주세요.");
                return;
            }
            if (expired) {
                inputCode.showError("인증 시간이 만료되었어요. 재발송해주세요.");
                return;
            }
            if (verifyListener != null) verifyListener.onVerify(code);
        });
    }

    public void setOnSendListener(OnSendListener l) { this.sendListener = l; }

    public void setOnResendListener(OnResendListener l) { this.resendListener = l; }

    public void setOnVerifyListener(OnVerifyListener l) { this.verifyListener = l; }

    public String getCode() { return inputCode.getText(); }

    public void showError(String message) { inputCode.showError(message); }

    public void onSendFailed() {
        // 발송 실패 시 입력 화면 그대로 유지 (버튼은 계속 보이는 상태)
    }

    /** 발송이 성공했을 때: 발송 버튼을 재발송 버튼으로 바꾸고 타이머를 (다시) 시작한다. */
    public void start() {
        btnSendCode.setVisibility(GONE);
        btnResend.setVisibility(VISIBLE);
        inputCode.setText("");
        inputCode.hideError();
        expired = false;
        btnVerify.setEnabled(true);
        startExpiryTimer();
        startCooldownTimer();
        inputCode.requestInputFocus();
    }

    public void onResendFailed() {
        setResendEnabled(true);
    }

    private void setResendEnabled(boolean enabled) {
        btnResend.setEnabled(enabled);
        btnResend.setTextColor(enabled ? colorNormal : ContextCompat.getColor(getContext(), R.color.g300));
    }

    public void stop() {
        cancelExpiryTimer();
        cancelCooldownTimer();
    }

    private void startExpiryTimer() {
        cancelExpiryTimer();
        rowExpiry.setVisibility(VISIBLE);
        expiryTimer = new CountDownTimer(EXPIRY_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                long sec = msLeft / 1000;
                tvExpiry.setText(String.format(Locale.KOREA, "%d:%02d", sec / 60, sec % 60));
                tvExpiry.setTextColor(msLeft <= WARN_THRESHOLD_MS ? colorWarn : colorNormal);
            }

            @Override
            public void onFinish() {
                expired = true;
                tvExpiry.setText("만료됨");
                tvExpiry.setTextColor(colorWarn);
                btnVerify.setEnabled(false);
                cancelCooldownTimer();
                setResendEnabled(true);
                tvCooldown.setVisibility(GONE);
            }
        }.start();
    }

    private void startCooldownTimer() {
        cancelCooldownTimer();
        setResendEnabled(false);
        tvCooldown.setVisibility(VISIBLE);
        cooldownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                tvCooldown.setText((msLeft / 1000) + "초 후 재발송 가능");
            }

            @Override
            public void onFinish() {
                cooldownTimer = null;
                setResendEnabled(true);
                tvCooldown.setVisibility(GONE);
            }
        }.start();
    }

    private void cancelExpiryTimer() {
        if (expiryTimer != null) {
            expiryTimer.cancel();
            expiryTimer = null;
        }
    }

    private void cancelCooldownTimer() {
        if (cooldownTimer != null) {
            cooldownTimer.cancel();
            cooldownTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();                                  // 액티비티 onDestroy 에서 안 지워도 안전
    }
}
