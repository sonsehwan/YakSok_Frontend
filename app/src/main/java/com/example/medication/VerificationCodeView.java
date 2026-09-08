package com.example.medication;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class VerificationCodeView extends LinearLayout {

    public interface OnResendListener { void onResend(); }
    public interface OnVerifyListener { void onVerify(String code); }

    private static final long EXPIRY_MS = 5 * 60 * 1000L;
    private static final long RESEND_COOLDOWN_MS = 60 * 1000L;
    private static final long WARN_THRESHOLD_MS = 60 * 1000L;   // 남은 시간 1분 이하면 빨강

    private InputView inputCode;
    private TextView tvExpiry;
    private MaterialButton btnResend;
    private MaterialButton btnVerify;

    private int colorNormal;
    private final int colorWarn = Color.parseColor("#FF0000");

    private CountDownTimer expiryTimer;
    private CountDownTimer cooldownTimer;
    private boolean expired = false;

    private OnResendListener resendListener;
    private OnVerifyListener verifyListener;

    public VerificationCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_verification_code, this, true);

        inputCode = findViewById(R.id.input_code);
        tvExpiry = findViewById(R.id.tv_expiry);
        btnResend = findViewById(R.id.btn_resend);
        btnVerify = findViewById(R.id.btn_verify);

        colorNormal = ContextCompat.getColor(context, R.color.brand_icon);
        tvExpiry.setTextColor(colorNormal);

        btnResend.setOnClickListener(v -> {
            btnResend.setEnabled(false);              // 응답 올 때까지 중복 클릭 방지
            if (resendListener != null) resendListener.onResend();
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

    public void setOnResendListener(OnResendListener l) { this.resendListener = l; }

    public void setOnVerifyListener(OnVerifyListener l) { this.verifyListener = l; }

    public String getCode() { return inputCode.getText(); }

    public void showError(String message) { inputCode.showError(message); }

    /** 인증코드 발송/재발송 성공 시 호출. 뷰를 보이게 하고 두 타이머를 (재)시작한다. */
    public void start() {
        setVisibility(VISIBLE);
        inputCode.setText("");
        inputCode.hideError();
        expired = false;
        btnVerify.setEnabled(true);
        startExpiryTimer();
        startCooldownTimer();
    }

    /** 재발송 요청이 실패했을 때 호출 — 버튼을 다시 누를 수 있게 되돌린다. */
    public void onResendFailed() {
        btnResend.setEnabled(true);
        btnResend.setText("인증코드 재발송");
    }

    /** 인증 성공 등으로 타이머가 더 필요 없을 때. */
    public void stop() {
        cancelExpiryTimer();
        cancelCooldownTimer();
    }

    private void startExpiryTimer() {
        cancelExpiryTimer();
        tvExpiry.setVisibility(VISIBLE);
        expiryTimer = new CountDownTimer(EXPIRY_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                long sec = msLeft / 1000;
                tvExpiry.setText(String.format(Locale.KOREA, "남은 시간 %d:%02d", sec / 60, sec % 60));
                tvExpiry.setTextColor(msLeft <= WARN_THRESHOLD_MS ? colorWarn : colorNormal);
            }

            @Override
            public void onFinish() {
                expired = true;
                tvExpiry.setText("인증 시간이 만료되었어요. 재발송해주세요.");
                tvExpiry.setTextColor(colorWarn);
                btnVerify.setEnabled(false);
                cancelCooldownTimer();          // 만료되면 재발송을 바로 열어준다
                btnResend.setEnabled(true);
                btnResend.setText("인증코드 재발송");
            }
        }.start();
    }

    private void startCooldownTimer() {
        cancelCooldownTimer();
        btnResend.setEnabled(false);
        cooldownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                btnResend.setText((msLeft / 1000) + "초 후 재발송");
            }

            @Override
            public void onFinish() {
                cooldownTimer = null;
                btnResend.setEnabled(true);
                btnResend.setText("인증코드 재발송");
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
