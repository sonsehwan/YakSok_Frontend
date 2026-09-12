package com.example.medication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    private boolean isReadOnly = false;
    private int validationType = 0;
    private OnValidateListener onValidateListener;
    private String helperText;

    // 마지막 글자만 노출하는 마스크와 전체를 가리는 마스크. 둘을 번갈아 적용해 노출 여부를 바꾼다.
    private final LastCharVisibleTransformation lastCharMask = new LastCharVisibleTransformation();
    private final TransformationMethod fullMask = PasswordTransformationMethod.getInstance();

    // 마지막 글자를 노출해 두는 시간(마지막 입력 기준)
    private static final long REVEAL_DURATION_MS = 2000L;
    private final Handler revealHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideLastCharRunnable = () -> applyMask(false);

    private boolean lastChangeWasInsertion = false;  // 삭제 시엔 노출하지 않기 위한 판정값
    private boolean isUpdatingMask = false;          // 마스크 교체가 유발한 TextWatcher 재진입 무시용

    // 상태별 색상 상수
    private final int COLOR_ERROR = Color.parseColor("#FF0000");      // 에러 시 빨간색
    private final int COLOR_GUIDE = Color.parseColor("#999999");      // 평상시 가이드 회색
    private final int COLOR_DEFAULT_STROKE = ContextCompat.getColor(getContext(), R.color.brand_icon);

    private final String idPattern = "^[a-z0-9]{8,20}$";
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
                isReadOnly = a.getBoolean(R.styleable.InputView_isReadOnly, false);
                validationType = a.getInt(R.styleable.InputView_validationType, 0);
                helperText = a.getString(R.styleable.InputView_helperText);

                if(iconRes != 0){
                    icon.setImageResource(iconRes);
                    icon.setVisibility(View.VISIBLE);
                }else{
                    icon.setVisibility(View.GONE);
                }

                if (isReadOnly) {
                    editText.setFocusable(false);
                    editText.setFocusableInTouchMode(false);
                    editText.setClickable(true);
                    editText.setCursorVisible(false);
                    // 에디트텍스트 클릭 시 부모(InputView)의 클릭 이벤트가 발생하도록 중계
                    editText.setOnClickListener(v -> callOnClick());
                }

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
                    // 포커스가 빠지면 마지막 글자도 즉시 가린다.
                    cancelHideLastChar();
                    applyMask(false);
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
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        
        editText.setTransformationMethod(fullMask);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingMask) return;
                lastChangeWasInsertion = count > before;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingMask) return;

                // 삭제이거나, 비었거나, 사용자가 입력 중이 아니면(setText 등) 노출하지 않는다.
                if (!lastChangeWasInsertion || s.length() == 0 || !editText.hasFocus()) {
                    cancelHideLastChar();
                    applyMask(false);
                    return;
                }
                applyMask(true);
                scheduleHideLastChar();
            }
        });

        imgToggle.setVisibility(View.VISIBLE);
        imgToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;

            cancelHideLastChar();
            editText.setTransformationMethod(isPasswordVisible ? null : fullMask);
            imgToggle.setImageResource(isPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
            editText.setSelection(editText.getText().length());
        });
    }

    /**
     * 마지막 글자 노출 여부를 적용한다.
     * setTransformationMethod()는 내부에서 setText()를 호출해 TextWatcher를 재진입시키므로
     * isUpdatingMask로 감싸 그 호출을 무시하게 한다.
     *
     * @param revealLast true면 마지막 글자 노출, false면 전체 마스킹
     */
    private void applyMask(boolean revealLast) {
        if (!isPasswordType || isPasswordVisible) return;

        TransformationMethod target = revealLast ? lastCharMask : fullMask;
        if (editText.getTransformationMethod() == target) return;

        // 마스크를 교체하면 커서 위치가 초기화되므로 복원한다.
        int selStart = editText.getSelectionStart();
        int selEnd = editText.getSelectionEnd();
        isUpdatingMask = true;
        try {
            editText.setTransformationMethod(target);
            if (selStart >= 0 && selEnd >= 0) {
                editText.setSelection(selStart, selEnd);
            }
        } finally {
            isUpdatingMask = false;
        }
    }

    private void scheduleHideLastChar() {
        cancelHideLastChar();
        revealHandler.postDelayed(hideLastCharRunnable, REVEAL_DURATION_MS);
    }

    private void cancelHideLastChar() {
        revealHandler.removeCallbacks(hideLastCharRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelHideLastChar();
        super.onDetachedFromWindow();
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
                showError("아이디: 8~20자의 영문 소문자와 숫자만 사용 가능합니다.");
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

    public void setText(String text){
        editText.setText(text);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return editText.requestFocus(direction, previouslyFocusedRect);
    }

    public void requestInputFocus() {
        editText.requestFocus();
        editText.post(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getContext()
                            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}