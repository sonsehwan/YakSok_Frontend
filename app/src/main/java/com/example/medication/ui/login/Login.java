package com.example.medication.ui.login;

import static com.example.medication.util.LoginMethod.onLoginSuccess;

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

import com.example.medication.databinding.ActivityLoginBinding;
import com.example.medication.model.request.KakaoLoginRequest;
import com.example.medication.model.request.LoginRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.KakaoLoginResponse;
import com.example.medication.model.response.LoginResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.base.BaseActivity;
import com.example.medication.ui.main.MainActivity;
import com.example.medication.ui.signup.SignUpTypeBottomSheet;
import com.example.medication.ui.signup.SignUp_Kakao;
import com.example.medication.util.KakaoLoginHelper;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends BaseActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(SprefsManager.isLoggedIn(this)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

            return;
        }

        // 로그인이 안되어 있을 때만 로그인 화면을 보여주기 시작
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> startLogin());

        binding.btnKakaoLogin.setOnClickListener(v -> startKakaoLogin());

        binding.tvGoSignup.setOnClickListener(v -> {
            SignUpTypeBottomSheet sheet = new SignUpTypeBottomSheet();
            sheet.show(getSupportFragmentManager(), "sign_up_type");
        });

        binding.tvFindId.setOnClickListener(v -> startActivity(new Intent(this, FindId.class)));
        binding.tvFindPw.setOnClickListener(v -> startActivity(new Intent(this, FindPassword.class)));
    }

    private void startLogin(){
        if(!binding.inputLoginId.isValid() || !binding.inputPw.isValid()){
            return;
        }
        String loginId = binding.inputLoginId.getText();
        String password = binding.inputPw.getText();

        LoginRequest request = new LoginRequest(loginId, password);

        UserApi api = NetworkClient.getApi();

        api.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                // 응답을 성공적으로 받고 내용이 있을 때
                if(response.isSuccessful() && response.body() != null){

                    ApiResponse<LoginResponse> result = response.body();

                    if(result.isBusinessSuccess()){
                        onLoginSuccess(Login.this, result.getData());
                    }else{// 서버와 연결은 성공했지만 요청을 승인할 수 없을 때
                        showToast(result.getMessage());
                        Log.e("Login", result.getMessage());
                    }
                }else{// 서버와 연결이 실패 했을 때
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                showToast("네트워크 연결 실패: " + t.getMessage());
                Log.e("Login", "Failure: " + t.getMessage());
            }
        });
    }

    private void setKakaoLoading(boolean loading) {
        binding.overlayLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (binding != null) setKakaoLoading(false);
    }

    private void startKakaoLogin() {
        setKakaoLoading(true);
        KakaoLoginHelper.login(this, new KakaoLoginHelper.Listener() {
            @Override public void onToken(String kakaoToken) { requestKakaoLogin(kakaoToken); }
            @Override public void onError(Throwable error) {
                setKakaoLoading(false);
                Log.e("KakaoLogin", "카카오 로그인 실패", error);
                showToast("카카오 로그인에 실패했습니다.");
            }
            @Override public void onCancel() { setKakaoLoading(false); }
        });
    }

    private void requestKakaoLogin(String kakaoToken) {
        NetworkClient.getAuthApi().kakaoLogin(new KakaoLoginRequest(kakaoToken))
                .enqueue(new Callback<ApiResponse<KakaoLoginResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<KakaoLoginResponse>> call,
                                           Response<ApiResponse<KakaoLoginResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isBusinessSuccess()) {
                            KakaoLoginResponse data = response.body().getData();

                            if(data.isNewUser()){
                                Intent intent = new Intent(Login.this, SignUp_Kakao.class);
                                intent.putExtra(SignUp_Kakao.EXTRA_KAKAO_TOKEN, kakaoToken);
                                intent.putExtra(SignUp_Kakao.EXTRA_NICKNAME, data.getNickname());
                                intent.putExtra(SignUp_Kakao.EXTRA_EMAIL, data.getEmail());
                                startActivity(intent);
                            }else {
                                onLoginSuccess(Login.this, data.getLogin());
                            }

                        } else {
                            setKakaoLoading(false);
                            showKakaoError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<KakaoLoginResponse>> call, Throwable t) {
                        setKakaoLoading(false);
                        showToast("네트워크 연결 실패: " + t.getMessage());
                    }
                });
    }

    private void showKakaoError(Response<?> response) {
        try {
            String errorJson = response.errorBody() != null ? response.errorBody().string() : "";
            ApiResponse<?> err = new Gson().fromJson(errorJson, ApiResponse.class);
            showToast(err != null && err.getMessage() != null
                    ? err.getMessage() : "카카오 로그인에 실패했습니다.");
        } catch (Exception e) {
            showToast("서버 응답을 분석할 수 없습니다.");
        }
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            Log.e("LoginError", "HTTP 상태 코드: " + response.code());
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();

                Log.e("LoginError", "서버 원본 응답: " + errorJson);
                // 1. 서버가 빈 값을 보냈을 때 방어
                if (errorJson == null || errorJson.trim().isEmpty()) {

                    showToast("로그인에 실패했습니다. (응답 없음)");
                    return;
                }

                ApiResponse errorResponse = new Gson().fromJson(errorJson, ApiResponse.class);

                // 2. 파싱은 성공했지만 안에 Message가 없을 때 방어
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    String message = errorResponse.getMessage();

                    if (message.contains("비밀번호")) binding.inputPw.showError(message);
                    else if (message.contains("아이디") || message.contains("사용자")) binding.inputLoginId.showError(message);
                    else showToast(message);
                } else {
                    // JSON 형태가 아니거나 메세지가 없는 경우
                    showToast("로그인 정보를 다시 확인해주세요.");
                }
            }
        } catch (Exception e) {
            // IOException 및 Gson 파싱 에러(JsonSyntaxException)를 모두 잡아서 앱 강제종료 방지
            showToast("서버 응답을 분석할 수 없습니다.");
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            View v = getCurrentFocus();
            if( v instanceof EditText){
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())){
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}