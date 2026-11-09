package com.example.medication.ui.myinfo;

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

import androidx.activity.result.ActivityResultLauncher;

import com.example.medication.R;
import com.example.medication.databinding.ActivityMyInfoBinding;
import com.example.medication.model.request.KakaoLoginRequest;
import com.example.medication.model.request.ModifyInfoRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.base.BaseActivity;
import com.example.medication.ui.common.AppDialog;
import com.example.medication.ui.drugstore.FindDrugStore;
import com.example.medication.ui.login.Login;
import com.example.medication.ui.main.MainActivity;
import com.example.medication.util.KakaoLoginHelper;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;
import com.kakao.sdk.user.UserApiClient;

import java.util.Objects;

import kotlin.Unit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyInfo extends BaseActivity {

    private ActivityMyInfoBinding binding;

    private ActivityResultLauncher<Intent> findDrugStoreLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnWithdraw.setOnClickListener(v -> showWithdrawConfirmDialog());

        String currentNickname = SprefsManager.getUserNickName(this);
        binding.inputNickname.setText(currentNickname);

        UserResponse currentUser = SprefsManager.getUser(this);

        String email = currentUser != null ? currentUser.getEmail() : null;
        if (email == null || email.isEmpty()) {
            binding.tvLabelEmail.setVisibility(View.GONE);
            binding.tvEmail.setVisibility(View.GONE);
        } else {
            binding.tvEmail.setText(email);
        }

        if (currentUser != null && currentUser.getSocialType() != null && !currentUser.isHasPassword()) {
            binding.tvLabelAccount.setVisibility(View.GONE);
            binding.llAccount.setVisibility(View.GONE);
        }
        updateKakaoStatus(currentUser);
        binding.btnLinkKakao.setOnClickListener(v -> {
            UserResponse user = SprefsManager.getUser(this);
            if (user != null && user.getSocialType() != null) showUnlinkConfirmDialog();
            else startKakaoLink();
        });

        if (currentUser != null && Objects.equals(currentUser.getRole(), "DRUGSTORE")) {
            binding.tvLabelDrugstore.setVisibility(View.VISIBLE);
            binding.llFindDrugstore.setVisibility(View.VISIBLE);

            if (currentUser.getMyDrugStore() != null) {
                binding.tvDrugstore.setText(currentUser.getMyDrugStore().getDutyName());
            }
        }

        binding.ivBack.setOnClickListener(v -> finish());

        binding.btnSaveInfo.setOnClickListener(v -> {
            startModify();
        });

        binding.btnChangePw.setOnClickListener(v->{
            Intent intent = new Intent(this, ModifyPassword.class);
            startActivity(intent);
        });

        binding.btnFindDrugstore.setOnClickListener(view -> {
            Intent intent = new Intent(this, FindDrugStore.class);
            intent.putExtra("INTENT_TYPE", "modify");
            startActivity(intent);
        });
    }

    // 연결된 상태에서 누르면 해제. 카카오로만 가입한 회원은 해제할 수 없어 비활성
    private void updateKakaoStatus(UserResponse user) {
        boolean linked = user != null && user.getSocialType() != null;
        boolean kakaoOnly = linked && !user.isHasPassword();
        binding.tvKakaoStatus.setText(kakaoOnly ? "카카오로 가입한 계정" : linked ? "연결됨 · 해제하기" : "연결하기");
        binding.btnLinkKakao.setEnabled(!kakaoOnly);
    }

    private void showUnlinkConfirmDialog() {
        AppDialog.confirm(this, "카카오 연동 해제",
                "연동을 해제하면 카카오로 로그인할 수 없습니다. 아이디와 비밀번호로 로그인해주세요.",
                "해제", true, this::requestKakaoUnlink);
    }

    private void requestKakaoUnlink() {
        NetworkClient.getAuthApi().kakaoUnlink()
                .enqueue(busy(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call,
                                           Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isBusinessSuccess()) {
                            UserResponse user = response.body().getData();
                            SprefsManager.setUserInfo(MyInfo.this, user);
                            updateKakaoStatus(user);
                            // 다음 연동 때 다른 카카오 계정을 고를 수 있게 SDK 세션도 정리
                            UserApiClient.getInstance().logout(error -> Unit.INSTANCE);
                            showToast("카카오 연동이 해제되었습니다.");
                        } else {
                            handleErrorResponse(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        showToast("네트워크 연결 실패: " + t.getMessage());
                    }
                }, binding.btnLinkKakao));
    }

    private void startKakaoLink() {
        KakaoLoginHelper.login(this, new KakaoLoginHelper.Listener() {
            @Override public void onToken(String kakaoToken) { requestKakaoLink(kakaoToken); }
            @Override public void onError(Throwable error) {
                Log.e("KakaoLink", "카카오 로그인 실패", error);
                showToast("카카오 로그인에 실패했습니다.");
            }
            @Override public void onCancel() { }
        });
    }

    private void requestKakaoLink(String kakaoToken) {
        NetworkClient.getAuthApi().kakaoInterlock(new KakaoLoginRequest(kakaoToken))
                .enqueue(busy(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call,
                                           Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isBusinessSuccess()) {
                            UserResponse user = response.body().getData();
                            SprefsManager.setUserInfo(MyInfo.this, user);
                            updateKakaoStatus(user);
                            showToast("카카오 계정이 연결되었습니다.");
                        } else {
                            // 실패한 카카오 세션을 남기지 않아야 다음 시도에서 다른 카카오 계정을 고를 수 있다
                            UserApiClient.getInstance().logout(error -> Unit.INSTANCE);
                            handleErrorResponse(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        showToast("네트워크 연결 실패: " + t.getMessage());
                    }
                }, binding.btnLinkKakao));
    }

    private void showWithdrawConfirmDialog() {
        AppDialog.confirm(this, "회원 탈퇴",
                "정말로 탈퇴하시겠습니까?\n모든 데이터가 삭제되며 복구할 수 없습니다.",
                "탈퇴", true, this::deleteUserFromServer);
    }

    private void deleteUserFromServer() {
        UserApi api = NetworkClient.getApi();
        Long userId = SprefsManager.getUserId(this);

        Log.d("MyInfoActivity", "탈퇴 요청 id: " + userId);

        api.deleteUser().enqueue(busy(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    showToast("회원 탈퇴가 완료되었습니다.");

                    SprefsManager.clearUserInfo(MyInfo.this);

                    navigateToLoginScreen();
                } else {
                    int statusCode = response.code();
                    String errorMsg = "알 수 없는 에러";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                        Log.e("MyInfoActivity", "탈퇴 실패 코드: " + statusCode + ", 에러 본문: " + errorMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showToast("탈퇴 실패 (코드: " + statusCode + ")");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("MyInfoActivity", "탈퇴 API 통신 실패: " + t.getMessage());
                showToast("네트워크 오류가 발생했습니다.");
            }
        }, binding.btnWithdraw));
    }

    private void navigateToLoginScreen() {
        Intent intent = new Intent(MyInfo.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void startModify(){
        if(!binding.inputNickname.isValid()){
            return;
        }

        Long userId = SprefsManager.getUserId(MyInfo.this);
        String nickname = binding.inputNickname.getText();

        ModifyInfoRequest request = new ModifyInfoRequest(userId, nickname);

        UserApi api = NetworkClient.getApi();

        api.modifyNickname(request).enqueue(busy(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if(response.isSuccessful() && response.body() != null){

                    ApiResponse<UserResponse> result = response.body();

                    if(result.isBusinessSuccess()){
                        UserResponse user = result.getData();
                        SprefsManager.setUserInfo(MyInfo.this, user);
                        Log.d("ModifyNickName", result.getMessage());
                        showToast("회원정보 수정완료.");

                        Intent intent = new Intent(MyInfo.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(MainActivity.EXTRA_TARGET_TAB, R.id.nav_settings);
                        startActivity(intent);
                    }else{
                        showToast(result.getMessage());
                        Log.e("ModifiyNickName", result.getMessage());
                    }
                }else{
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                showToast("네트워크 연결 실패: " + t.getMessage());
                Log.e("ModifyNickName", "Failure: " + t.getMessage());
            }
        }, binding.btnSaveInfo));
    }

    private void handleErrorResponse(Response<?> response) {
        try {

            Log.e("ModifyInfoError", "HTTP 상태 코드: " + response.code());
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();

                Log.e("ModifyInfoError", "서버 원본 응답: " + errorJson);

                if (errorJson == null || errorJson.trim().isEmpty()) {

                    showToast("수정에 실패했습니다. (응답 없음)");
                    return;
                }

                ApiResponse errorResponse = new Gson().fromJson(errorJson, ApiResponse.class);

                if (errorResponse != null && errorResponse.getMessage() != null) {
                    String message = errorResponse.getMessage();
                    showToast(message);
                } else {
                    showToast("수정 정보를 다시 확인해주세요.");
                }
            }
        } catch (Exception e) {
            showToast("서버 응답을 분석할 수 없습니다.");
        }
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

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}