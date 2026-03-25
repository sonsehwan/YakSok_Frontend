package com.example.medication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.model.request.LoginRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private InputView inputEmail;
    private InputView inputPw;
    private Button btnLogin;
    private TextView tvGoSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 로그인 유지 여부를 확인
        if(SprefsManager.isLoggedIn(this)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

            return;
        }

        // 로그인이 안되어 있을 때만 로그인 화면을 보여주기 시작
        setContentView(R.layout.activity_login);

        initViews();

        btnLogin.setOnClickListener(v -> startLogin());

        tvGoSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp_Step1.class);
            startActivity(intent);
        });
    }

    private void startLogin(){
        if(!inputEmail.isValid() || !inputPw.isValid()){
            return;
        }
        String email = inputEmail.getText();
        String password = inputPw.getText();

        LoginRequest request = new LoginRequest(email, password);

        UserApi api = NetworkClient.getApi();

        api.login(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if(response.isSuccessful() && response.body() != null){

                    ApiResponse<UserResponse> result = response.body();

                    if(result.isBusinessSuccess()){

                        // 로그인한 유저의 정보를 가져온다(이메일, 비밀번호, 닉네임)
                        UserResponse user = result.getData();
                        //저장소에 저장
                        SprefsManager.setUserInfo(Login.this, user);

                        Log.d("Login", result.getMessage());

                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        showToast(result.getMessage());
                        Log.e("Login", result.getMessage());
                    }
                }else{
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                showToast("네트워크 연결 실패: " + t.getMessage());
                Log.e("Login", "Failure: " + t.getMessage());
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiResponse errorResponse = new Gson().fromJson(errorJson, ApiResponse.class);
                String message = errorResponse.getMessage();

                // 비밀번호가 틀렸거나 이메일이 없는 경우 처리
                if (message.contains("비밀번호")) {
                    inputPw.showError(message);
                } else if (message.contains("이메일") || message.contains("사용자")) {
                    inputEmail.showError(message);
                } else {
                    showToast(message);
                }
            } else {
                showToast("오류가 발생했습니다. (코드: " + response.code() + ")");
            }
        } catch (IOException e) {
            showToast("데이터 분석 중 오류가 발생했습니다.");
        }
    }

    private void initViews() {
        inputEmail = findViewById(R.id.input_email);
        inputPw = findViewById(R.id.input_pw);
        btnLogin = findViewById(R.id.btn_login);
        tvGoSignUp = findViewById(R.id.tv_go_signup);
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