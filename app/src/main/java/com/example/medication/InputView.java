package com.example.medication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

public class InputView extends LinearLayout {

    public interface OnValidateListener{
        String onValidate(String text);
    }

    private ImageView icon;
    private EditText editText;
    private ImageView imgToggle;
    private TextView tvError;
    private MaterialCardView inputView;

    private boolean isPasswordType = false;
    private boolean isPasswordVisible = false;
    private int validationType = 0;
    private OnValidateListener onValidateListener;
    private String helperText;

    // 상태별 색상 상수
    private final int COLOR_ERROR = Color.parseColor("#FF0000");      // 에러 시 빨간색
    private final int COLOR_GUIDE = Color.parseColor("#999999");      // 평상시 가이드 회색
    private final int COLOR_DEFAULT_STROKE = Color.parseColor("#FFEB3B"); // 기본 테두리 레몬 노랑

    private final String idPattern = "^[a-z0-9_-]{5,20}$";
    private final String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,16}$";
    private final String nicknamePattern = "^[a-zA-Z0-9가-힣]{2,10}$";
    private final String birthPattern = "^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])$";
    private final String phonePattern = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$";

    public InputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setOnValidateListener(OnValidateListener listener){
        this.onValidateListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.input, this, true);

        icon = findViewById(R.id.img_icon);
        editText = findViewById(R.id.et_input);
        imgToggle = findViewById(R.id.img_password_toggle);
        tvError = findViewById(R.id.tv_error);
        inputView = findViewById(R.id.mcv_input);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.InputView, 0, 0);
            try {
                String hint = a.getString(R.styleable.InputView_hintText);
                int iconRes = a.getResourceId(R.styleable.InputView_iconSrc, 0);
                isPasswordType = a.getBoolean(R.styleable.InputView_isPassword, false);
                validationType = a.getInt(R.styleable.InputView_validationType, 0);
                helperText = a.getString(R.styleable.InputView_helperText);

                int inputType = a.getInt(R.styleable.InputView_android_inputType, InputType.TYPE_CLASS_TEXT);
                editText.setInputType(inputType);

                int imeOptions = a.getInt(R.styleable.InputView_android_imeOptions, EditorInfo.IME_ACTION_NEXT);
                editText.setImeOptions(imeOptions);

                if (hint != null) editText.setHint(hint);
                if (iconRes != 0) icon.setImageResource(iconRes);

                if(helperText != null){
                    tvError.setText(helperText);
                    tvError.setTextColor(COLOR_GUIDE);
                    tvError.setVisibility(View.VISIBLE);
                }

                if (isPasswordType) {
                    setupPasswordMode();
                }
            } finally {
                a.recycle();
            }

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    validateInput();
                }
            });

            editText.setOnEditorActionListener((v, actionId, event)->{
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    editText.clearFocus();
                }
                // false를 반환하면 키보드가 자동으로 닫힌다.
                return false;
            });
        }
    }

    private void setupPasswordMode() {
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgToggle.setVisibility(View.VISIBLE);
        imgToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgToggle.setImageResource(R.drawable.ic_visibility);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgToggle.setImageResource(R.drawable.ic_visibility_off);
            }
            editText.setSelection(editText.getText().length());
        });
    }

    public void showError(String message) {
        tvError.setText(message);
        tvError.setTextColor(COLOR_ERROR);
        tvError.setVisibility(View.VISIBLE);
        inputView.setStrokeColor(COLOR_ERROR);
    }

    public void hideError() {
        if (helperText != null) {
            tvError.setText(helperText);
            tvError.setTextColor(COLOR_GUIDE);
            tvError.setVisibility(View.VISIBLE);
        } else {
            tvError.setVisibility(View.GONE);
        }
        inputView.setStrokeColor(COLOR_DEFAULT_STROKE);
    }

    public boolean isValid() {
        return validateInput();
    }

    private boolean validateInput() {
        String text = getText();

        if (text.isEmpty()) {
            showError("필수 입력 항목입니다.");
            return false;
        }

        if (this.isPasswordType) {
            if (!text.matches(passwordPattern)) {
                showError("비밀번호: 8~16자의 영문, 숫자, 특수문자를 포함해야 합니다.");
                return false;
            }
        } else if (validationType == 1) { // email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                showError("이메일 형식이 올바르지 않습니다.");
                return false;
            }
        } else if (validationType == 2) { // id
            if (!text.matches(idPattern)) {
                showError("아이디: 5~20자의 영문 소문자, 숫자, _, -만 사용 가능합니다.");
                return false;
            }
        } else if (validationType == 4) { // nickname
            if (!text.matches(nicknamePattern)) {
                showError("닉네임: 2~10자의 영문, 한글, 숫자만 사용 가능합니다.");
                return false;
            }
        } else if (validationType == 5) { // birth
            if (!text.matches(birthPattern)) {
                showError("생년월일: YYYYMMDD 형태로 입력하세요.");
                return false;
            }
        } else if (validationType == 6) { // phone
            if (!text.matches(phonePattern)) { // birthPattern -> phonePattern으로 수정
                showError("전화번호: 올바른 형식으로 입력하세요.(숫자만)");
                return false;
            }
        }

        if(onValidateListener != null){
            String customErrorMessage = onValidateListener.onValidate(text);
            if(customErrorMessage != null){
                showError(customErrorMessage);
                return false;
            }
        }

        hideError();
        return true;
    }

    public String getText() {
        return editText.getText().toString().trim();
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return editText.requestFocus(direction, previouslyFocusedRect);
    }
}