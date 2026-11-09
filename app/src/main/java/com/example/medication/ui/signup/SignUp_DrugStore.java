package com.example.medication.ui.signup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.medication.R;
import com.example.medication.databinding.ActivitySignUpDrugstoreBinding;
import com.example.medication.model.SearchDrugStore;
import com.example.medication.model.request.SignUpSendCodeRequest;
import com.example.medication.model.request.SignUpVerifyRequest;
import com.example.medication.model.request.UserRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SignUpVerifyResponse;
import com.example.medication.network.AuthApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.base.BaseActivity;
import com.example.medication.ui.drugstore.FindDrugStore;
import com.example.medication.ui.login.Login;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp_DrugStore extends BaseActivity {

    private ActivitySignUpDrugstoreBinding binding;

    private ActivityResultLauncher<Intent> findDrugStoreLauncher;

    private SearchDrugStore drugStore;

    private String sentEmail;
    private String emailVerifyToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpDrugstoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFindDrugStoreLauncher();

        binding.inputCheckPw.setOnValidateListener(text ->{
            String originalPw = binding.inputPw.getText();
            if(!text.equals(originalPw)){
                return "비밀번호가 일치하지 않습니다.";
            }
            return null;
        });

        binding.btnFindDrugstore.setOnClickListener(v -> {
            Intent intent = new Intent(this, FindDrugStore.class);
            intent.putExtra("INTENT_TYPE", "sign_up");
            findDrugStoreLauncher.launch(intent);
        });

        binding.verificationCode.setOnSendListener(email -> sendCode(email, false));
        binding.verificationCode.setOnResendListener(email -> sendCode(email, true));
        binding.verificationCode.setOnVerifyListener(this::verify);

        binding.ivBack.setOnClickListener(v -> finish());

        Intent getIntent = getIntent();
        String type = getIntent.getStringExtra("SignUp_Type");

        binding.btnSignUp.setOnClickListener(v -> {
            if(!checkValid()){
                return;
            }
            startSignUp(type, drugStore);
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 1. 사용자가 화면을 손가락으로 '눌렀을 때'만 이벤트를 가로챕니다.
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // 2. 현재 화면에서 포커스(커서)를 가지고 있는 뷰를 가져옵니다.
            View v = getCurrentFocus();

            // 3. 만약 포커스된 뷰가 입력창(EditText)이라면 로직을 수행합니다.
            if (v instanceof EditText) {

                // 4. 입력창의 현재 화면상 위치를 담을 사각형(Rect) 객체를 생성합니다.
                Rect outRect = new Rect();

                // 5. 현재 포커스된 입력창의 실제 좌표와 크기를 outRect에 저장합니다.
                v.getGlobalVisibleRect(outRect);

                // 6. 사용자가 터치한 위치(event.getRawX, getRawY)가
                //    입력창의 영역(outRect) 내부에 포함되지 않는지(!) 확인합니다.
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {

                    // 7. 입력창 바깥을 눌렀으므로 포커스(커서)를 제거합니다.
                    v.clearFocus();

                    // 8. 소프트 키보드를 화면에서 숨기기 위해 InputMethodManager를 호출합니다.
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // 현재 포커스가 해제된 뷰의 윈도우 토큰을 이용해 키보드를 닫습니다.
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        // 9. 위에서 정의한 로직 외의 기본적인 터치 이벤트 처리는 부모 클래스로 넘깁니다.
        return super.dispatchTouchEvent(event);
    }

    private boolean checkValid(){
        boolean isLoginIdValid = binding.inputLoginId.isValid();
        boolean isPwVaild = binding.inputPw.isValid();
        boolean isCheckPw = binding.inputCheckPw.isValid();
        boolean isNickName = binding.inputNickname.isValid();

        boolean isEmailVerified = emailVerifyToken != null;
        if(!isEmailVerified){
            binding.verificationCode.showEmailError("이메일 인증을 완료해주세요.");
        }else if(!binding.verificationCode.getEmail().equals(sentEmail)){
            binding.verificationCode.showEmailError("인증받은 이메일과 다릅니다. 인증코드를 다시 받아주세요.");
            isEmailVerified = false;
        }

        boolean isDrugStoreValid = true;
        if(drugStore == null){
            showDrugStoreError("약국을 선택해주세요.");
            isDrugStoreValid = false;
        }else{
            hideDrugStoreError();
        }

        if(!isLoginIdValid || !isEmailVerified || !isPwVaild || !isCheckPw || !isNickName || !isDrugStoreValid){
            showToast("입력 정보를 다시 확인해주세요.");
            return false;
        }
        return true;
    }

    private void setupFindDrugStoreLauncher() {
        findDrugStoreLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        drugStore = (SearchDrugStore) result.getData().getSerializableExtra("CHOiCE_DRUGSTORE");

                        if(drugStore == null){
                            showToast("약국 정보를 가져오는데 실패하였습니다.");
                        }else{
                            hideDrugStoreError();
                            binding.tvDrugstore.setText(drugStore.getDutyName());
                        }
                    }
                }
        );
    }

    private void startSignUp(String type, SearchDrugStore drugStore){
        String loginId = binding.inputLoginId.getText();
        String pw = binding.inputPw.getText();
        String nickName = binding.inputNickname.getText();
        String role = type;

        // 서버는 인증받은 이메일(sentEmail)과 토큰을 대조한다
        UserRequest request = new UserRequest(loginId, sentEmail, pw, nickName, drugStore, role, emailVerifyToken);

        UserApi api = NetworkClient.getApi();
        api.signUp(request).enqueue(busy(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if(response.isSuccessful() && response.body() != null){

                    ApiResponse<Void> result = response.body();

                    if(result.isBusinessSuccess()){
                        Log.d("SignUp", result.getMessage());
                        showToast("회원가입 완료");
                        Intent intent = new Intent(SignUp_DrugStore.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        //서버에서 회원가입에 실패 했을 경우.
                        Log.e("SignUpError", result.getMessage());
                        showToast(result.getMessage());
                    }
                }else{
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("SignUp", "통신 실패: " + t.getMessage());
            }
        }, binding.btnSignUp));
    }

    private void handleErrorResponse(Response<ApiResponse<Void>> response) {
        try{
            if(response.errorBody() != null){
                String errorJson = response.errorBody().string();
                ApiResponse errorResponse = new Gson().fromJson(errorJson, ApiResponse.class);

                String message = errorResponse.getMessage();

                // 토큰 자체가 만료·불일치일 때만 인증을 다시 받게 한다(다른 실패는 토큰이 살아 있다)
                if(message.contains("이메일 인증")){
                    resetEmailVerification();
                }

                if(message.contains("아이디")){
                    binding.inputLoginId.showError(message);
                }else if(message.contains("이메일")){
                    binding.verificationCode.showEmailError(message);
                }else if(message.contains("닉네임")){
                    binding.inputNickname.showError(message);
                }else if(message.contains("약국")){
                    showDrugStoreError(message);
                }else{
                    showToast(message);
                }

                Log.e("SignUp", "서버 에러 메시지" + message);
            }else {
                showToast("서버 오류가 발생했습니다. (코드: " + response.code() + ")");
            }
        }catch (IOException e){
            showToast("에러 메시지 분석 중 오류가 발생했습니다.");
        }
    }

    private void sendCode(String email, boolean resend) {
        AuthApi api = NetworkClient.getAuthApi();
        api.sendSignUpCode(new SignUpSendCodeRequest(email)).enqueue(busy(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isBusinessSuccess()) {
                    // 새 코드를 받는 순간 이전 인증은 버린다(발송 실패 땐 유지)
                    sentEmail = email;
                    emailVerifyToken = null;
                    binding.verificationCode.start();
                    showToast("인증코드를 이메일로 발송했습니다.");
                } else {
                    if (resend) binding.verificationCode.onResendFailed();
                    binding.verificationCode.showEmailError(serverMessage(response, "인증코드 발송에 실패했습니다."));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (resend) binding.verificationCode.onResendFailed();
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("SignUp", "send-code 실패: " + t.getMessage());
            }
        }, binding.verificationCode.findViewById(R.id.btn_send_code), binding.verificationCode.findViewById(R.id.btn_resend)));
    }

    private void verify(String code) {
        if (sentEmail == null) {
            binding.verificationCode.showError("먼저 인증코드를 발송해주세요.");
            return;
        }
        AuthApi api = NetworkClient.getAuthApi();
        api.verifySignUp(new SignUpVerifyRequest(sentEmail, code)).enqueue(busy(new Callback<ApiResponse<SignUpVerifyResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SignUpVerifyResponse>> call, Response<ApiResponse<SignUpVerifyResponse>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isBusinessSuccess() && response.body().getData() != null) {
                    emailVerifyToken = response.body().getData().getEmailVerifyToken();
                    binding.verificationCode.markVerified();
                    showToast("이메일 인증이 완료되었습니다.");
                } else {
                    binding.verificationCode.showError(serverMessage(response, "인증에 실패했습니다."));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SignUpVerifyResponse>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
                Log.e("SignUp", "verify 실패: " + t.getMessage());
            }
        }, binding.verificationCode.findViewById(R.id.btn_verify)));
    }

    private void resetEmailVerification() {
        sentEmail = null;
        emailVerifyToken = null;
        binding.verificationCode.reset();
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
            Log.e("SignUp", "에러 파싱 실패: " + e.getMessage());
        }
        return fallback;
    }

    private void showDrugStoreError(String message){
        binding.tvDrugstoreError.setText(message);
        binding.tvDrugstoreError.setVisibility(View.VISIBLE);
        binding.mcvFindDrugstore.setStrokeColor(Color.parseColor("#FF0000"));
    }

    private void hideDrugStoreError(){
        binding.tvDrugstoreError.setVisibility(View.GONE);
        binding.mcvFindDrugstore.setStrokeColor(ContextCompat.getColor(this, R.color.g100));
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}