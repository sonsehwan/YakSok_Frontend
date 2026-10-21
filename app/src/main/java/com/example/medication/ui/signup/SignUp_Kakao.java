package com.example.medication.ui.signup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.medication.R;
import com.example.medication.databinding.ActivitySignUpKakaoBinding;
import com.example.medication.model.SearchDrugStore;
import com.example.medication.ui.base.BaseActivity;
import com.example.medication.ui.drugstore.FindDrugStore;

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

        binding.inputNickname.setText(getIntent().getStringExtra(EXTRA_NICKNAME));
        binding.inputEmail.setText(getIntent().getStringExtra(EXTRA_EMAIL));

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
            if (!checkValid()) {
                return;
            }
            // TODO: POST /api/auth/kakao/signup (토큰·role·nickname·drugStore) 후 로그인 후처리
        });
    }

    private void selectRole(boolean drugStoreRole) {
        isDrugStore = drugStoreRole;
        binding.tvRoleNormal.setSelected(!drugStoreRole);
        binding.tvRoleDrugstore.setSelected(drugStoreRole);
        binding.llDrugstore.setVisibility(drugStoreRole ? View.VISIBLE : View.GONE);
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
