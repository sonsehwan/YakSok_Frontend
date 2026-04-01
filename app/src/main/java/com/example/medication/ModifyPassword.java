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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.model.request.ModifyPasswordRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModifyPassword extends AppCompatActivity {

    private InputView currentPassword;
    private InputView newPassword;
    private InputView checkPassword;
    private ImageView btnBack;
    private Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.modify_password);

        initViews();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        checkPassword.setOnValidateListener(text -> {
            if (newPassword == null) return null;
            String originalPw = newPassword.getText();
            if (!text.equals(originalPw)) {
                return "비밀번호가 일치하지 않습니다.";
            }
            return null;
        });

        btnFinish.setOnClickListener(v -> {startModify();});
    }

    private void startModify(){
        if (!currentPassword.isValid() || !newPassword.isValid() || !checkPassword.isValid()) {
            showToast("입력 정보를 다시 확인해주세요.");
            return;
        }

        String email = SprefsManager.getUserEmail(ModifyPassword.this);
        String currentPw = currentPassword.getText();
        String newPw = newPassword.getText();

        ModifyPasswordRequest request = new ModifyPasswordRequest(email, currentPw, newPw);

        UserApi api = NetworkClient.getApi();
        api.modifyPassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if(response.isSuccessful() && response.body() != null){

                    ApiResponse<Void> result = response.body();

                    if(result.isBusinessSuccess()){
                        SprefsManager.clearUserInfo(ModifyPassword.this);
                        showToast("수정 완료. 로그아웃합니다.");
                        Intent intent = new Intent(ModifyPassword.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Log.d("ModifyPw", result.getMessage());
                    }else{
                        //서버에서 회원가입에 실패 했을 경우.
                        Log.e("ModifyPwError", result.getMessage());
                        showToast(result.getMessage());
                    }
                }else{
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // 타임아웃, 와이파이 끊김 등 물리적 네트워크 연결 실패 시
                showToast("네트워크 연결을 확인해주세요.");
                // 에러 원인을 로그로 남기면 디버깅하기 좋습니다.
                android.util.Log.e("ModifyPw", "통신 실패: " + t.getMessage());
            }
        });
    }

    private void handleErrorResponse(Response<ApiResponse<Void>> response) {
        try{
            if(response.errorBody() != null){
                String errorJson = response.errorBody().string();

                Log.e("ModifyPwError", "서버 원본 응답: " + errorJson);

                ApiResponse errorResponse = new Gson().fromJson(errorJson, ApiResponse.class);

                String message = errorResponse.getMessage();
                showToast(message);

                Log.e("ModifyPwError", "서버 에러 메시지" + message);
            }else {
                showToast("서버 오류가 발생했습니다. (코드: " + response.code() + ")");
            }
        }catch (IOException e){
            showToast("에러 메시지 분석 중 오류가 발생했습니다.");
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                    if (imm != null && v.getWindowToken() != null) {
                        // 현재 포커스가 해제된 뷰의 윈도우 토큰을 이용해 키보드를 닫습니다.
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        // 9. 위에서 정의한 로직 외의 기본적인 터치 이벤트 처리는 부모 클래스로 넘깁니다.
        return super.dispatchTouchEvent(event);
    }

    private void initViews(){
        currentPassword = findViewById(R.id.input_current_pw);
        newPassword = findViewById(R.id.input_new_pw);
        checkPassword = findViewById(R.id.input_check_pw);
        btnBack = findViewById(R.id.btn_back);
        btnFinish = findViewById(R.id.btn_modify_pw);
    }
}