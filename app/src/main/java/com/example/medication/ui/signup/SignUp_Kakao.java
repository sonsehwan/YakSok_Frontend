package com.example.medication.ui.signup;

import static com.example.medication.util.AuthMethod.onLoginSuccess;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.medication.R;
import com.example.medication.databinding.ActivitySignUpKakaoBinding;
import com.example.medication.model.SearchDrugStore;
import com.example.medication.model.request.KakaoSignUpRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.LoginResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.base.BaseActivity;
import com.example.medication.ui.drugstore.FindDrugStore;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp_Kakao extends BaseActivity {

    public static final String EXTRA_KAKAO_TOKEN = "KAKAO_TOKEN";
    public static final String EXTRA_NICKNAME = "NICKNAME";
    public static final String EXTRA_EMAIL = "EMAIL";

    private ActivitySignUpKakaoBinding binding;
    private ActivityResultLauncher<Intent> findDrugStoreLauncher;
    private SearchDrugStore drugStore;
    private boolean isDrugStore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpKakaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String kakaoToken = getIntent().getStringExtra(EXTRA_KAKAO_TOKEN);
        String nickname = getIntent().getStringExtra(EXTRA_NICKNAME);
        String email = getIntent().getStringExtra(EXTRA_EMAIL);


        binding.inputNickname.setText(nickname);
        binding.inputEmail.setText(email);

        findDrugStoreLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        drugStore = (SearchDrugStore) result.getData().getSerializableExtra("CHOiCE_DRUGSTORE");
                        if (drugStore == null) {
                            showToast("약국 정보를 가져오는데 실패하였습니다.");
                        } else {
                            hideDrugStoreError();
                            binding.tvDrugstore.setText(drugStore.getDutyName());
                        }
                    }
                });

        binding.tvRoleNormal.setOnClickListener(v -> selectRole(false));
        binding.tvRoleDrugstore.setOnClickListener(v -> selectRole(true));
        selectRole(false);

        binding.btnFindDrugstore.setOnClickListener(v -> {
            Intent intent = new Intent(this, FindDrugStore.class);
            intent.putExtra("INTENT_TYPE", "sign_up");
            findDrugStoreLauncher.launch(intent);
        });

        binding.ivBack.setOnClickListener(v -> finish());

        binding.btnSignUp.setOnClickListener(v -> {
            // TODO: POST /api/auth/kakao/signup (토큰·role·nickname·drugStore) 후 로그인 후처리
            if(kakaoToken == null){
                showToast("잘못된 접근 입니다. 다시 로그인 해주세요.");
                finish();
                return;
            }

            if (!checkValid()) {
                return;
            }

            String role = isDrugStore ? "DRUGSTORE" : "NORMAL";
            String nickName = binding.inputNickname.getText();
            SearchDrugStore selected = isDrugStore ? drugStore : null;

            startKakaoSignUp(new KakaoSignUpRequest(kakaoToken, role, nickName, selected));
        });
    }

    private void startKakaoSignUp(KakaoSignUpRequest request) {
        NetworkClient.getAuthApi().kakaoSignUp(request)
                .enqueue(busy(new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResponse>> call,
                                           Response<ApiResponse<LoginResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isBusinessSuccess()) {
                            onLoginSuccess(SignUp_Kakao.this, response.body().getData());
                            return;
                        }
                        showSignUpError(response);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                        showToast("네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
                        Log.e("KakaoSignUp", "Failure: " + t.getMessage());
                    }
                }, binding.btnSignUp));
    }

    private void selectRole(boolean drugStoreRole) {
        isDrugStore = drugStoreRole;
        binding.tvRoleNormal.setSelected(!drugStoreRole);
        binding.tvRoleDrugstore.setSelected(drugStoreRole);
        binding.llDrugstore.setVisibility(drugStoreRole ? View.VISIBLE : View.GONE);

        if (!drugStoreRole) {
            drugStore = null;
            binding.tvDrugstore.setText("");
            hideDrugStoreError();
        }
    }

    private boolean checkValid() {
        boolean isNicknameValid = binding.inputNickname.isValid();

        boolean isDrugStoreValid = true;
        if (isDrugStore) {
            if (drugStore == null) {
                showDrugStoreError("약국을 선택해주세요.");
                isDrugStoreValid = false;
            } else {
                hideDrugStoreError();
            }
        }

        if (!isNicknameValid || !isDrugStoreValid) {
            showToast("입력 정보를 다시 확인해주세요.");
            return false;
        }
        return true;
    }

    private void showSignUpError(Response<?> response) {
        String message = "회원가입에 실패했습니다.";
        try {
            if (response.errorBody() != null) {
                ApiResponse<?> err = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (err != null && err.getMessage() != null) {
                    message = err.getMessage();
                }
            }
        } catch (Exception e) {
            Log.e("KakaoSignUp", "에러 응답 파싱 실패", e);
        }

        // 약국 관련 실패는 약국 선택 영역에, 나머지는 토스트로
        if (message.contains("약국")) {
            showDrugStoreError(message);
        } else {
            showToast(message);
        }
    }

    private void showDrugStoreError(String message) {
        binding.tvDrugstoreError.setText(message);
        binding.tvDrugstoreError.setVisibility(View.VISIBLE);
        binding.mcvFindDrugstore.setStrokeColor(Color.parseColor("#FF0000"));
    }

    private void hideDrugStoreError() {
        binding.tvDrugstoreError.setVisibility(View.GONE);
        binding.mcvFindDrugstore.setStrokeColor(ContextCompat.getColor(this, R.color.g100));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
