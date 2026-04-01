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

import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.model.request.ModifyInfoRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyInfoActivity extends AppCompatActivity {

    private ImageView ivBack;
    private InputView inputNickname;
    private Button btnChangePw;
    private Button btnSaveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        ivBack = findViewById(R.id.iv_back);
        inputNickname = findViewById(R.id.input_nickname);
        btnChangePw = findViewById(R.id.btn_change_pw);
        btnSaveInfo = findViewById(R.id.btn_save_info);

        // 기존에 저장된 닉네임을 불러와서 미리 채워둡니다.
        String currentNickname = SprefsManager.getUserNickName(this);
        inputNickname.setText(currentNickname);

        // 뒤로가기 버튼 클릭 이벤트
        ivBack.setOnClickListener(v -> finish());

        // 수정하기 버튼 클릭 이벤트
        btnSaveInfo.setOnClickListener(v -> {
            startModify();
        });

        btnChangePw.setOnClickListener(v->{
            Intent intent = new Intent(this, ModifyPassword.class);
            startActivity(intent);
        });
    }

    private void startModify(){
        if(!inputNickname.isValid()){
            return;
        }

        String email = SprefsManager.getUserEmail(MyInfoActivity.this);
        String nickname = inputNickname.getText();

        ModifyInfoRequest request = new ModifyInfoRequest(email, nickname);

        UserApi api = NetworkClient.getApi();

        api.modifyNickname(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                // 응답을 성공적으로 받고 내용이 있을 때
                if(response.isSuccessful() && response.body() != null){

                    ApiResponse<UserResponse> result = response.body();

                    if(result.isBusinessSuccess()){

                        // 로그인한 유저의 정보를 가져온다(이메일, 비밀번호, 닉네임)
                        UserResponse user = result.getData();
                        //저장소에 저장
                        SprefsManager.setUserInfo(MyInfoActivity.this, user);

                        Log.d("ModifyNickName", result.getMessage());

                        Intent intent = new Intent(MyInfoActivity.this, Settings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{// 서버와 연결은 성공했지만 요청을 승인할 수 없을 때
                        showToast(result.getMessage());
                        Log.e("ModifiyNickName", result.getMessage());
                    }
                }else{// 서버와 연결이 실패 했을 때
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                showToast("네트워크 연결 실패: " + t.getMessage());
                Log.e("ModifyNickName", "Failure: " + t.getMessage());
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {

            Log.e("ModifyInfoError", "HTTP 상태 코드: " + response.code());
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();

                Log.e("ModifyInfoError", "서버 원본 응답: " + errorJson);
                // 1. 서버가 빈 값을 보냈을 때 방어
                if (errorJson == null || errorJson.trim().isEmpty()) {

                    showToast("수정에 실패했습니다. (응답 없음)");
                    return;
                }

                ApiResponse errorResponse = new Gson().fromJson(errorJson, ApiResponse.class);

                // 2. 파싱은 성공했지만 안에 Message가 없을 때 방어
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    String message = errorResponse.getMessage();
                    showToast(message);
                } else {
                    // JSON 형태가 아니거나 메세지가 없는 경우
                    showToast("수정 정보를 다시 확인해주세요.");
                }
            }
        } catch (Exception e) {
            // IOException 및 Gson 파싱 에러(JsonSyntaxException)를 모두 잡아서 앱 강제종료 방지
            showToast("서버 응답을 분석할 수 없습니다.");
        }
    }

    // 빈 공간 터치 시 키보드 내리기
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

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}