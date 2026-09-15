package com.example.medication.ui.sharedyaksok;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.example.medication.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medication.R;
import com.example.medication.adapter.DetailYaksokMedicationAdapter;
import com.example.medication.databinding.ActivityShareYaksokDetailBinding;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.request.ShareYaksokRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareYaksokDetail extends BaseActivity {

    public static final String EXTRA_YAKSOK_ID = "SHARED_YAKSOK_ID";
    // 이미 저장한 경우
    public static final String EXTRA_ALREADY_SAVED = "ALREADY_SAVED";

    private ActivityShareYaksokDetailBinding binding;

    private DetailYaksokMedicationAdapter pillAdapter;
    private final List<PillRequest> pills = new ArrayList<>();

    private long yaksokId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityShareYaksokDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        yaksokId = getIntent().getLongExtra(EXTRA_YAKSOK_ID, -1);
        if (yaksokId == -1) {
            showToast("약속 정보를 불러올 수 없습니다.");
            finish();
            return;
        }

        binding.ivBack.setOnClickListener(v -> finish());

        if (getIntent().getBooleanExtra(EXTRA_ALREADY_SAVED, false)) {
            binding.btnSave.setVisibility(View.GONE);
        }

        binding.btnSave.setOnClickListener(v -> saveSharedYaksok());

        loadYaksok();


    }

    private void setupRecyclerView() {
        pillAdapter = new DetailYaksokMedicationAdapter(pills);
        binding.rvSelectedPills.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSelectedPills.setAdapter(pillAdapter);
    }

    private void loadYaksok() {
        NetworkClient.getYaksokApi().getYaksok(yaksokId)
                .enqueue(new Callback<ApiResponse<Yaksok>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Yaksok>> call, Response<ApiResponse<Yaksok>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            populateViews(response.body().getData());
                        } else {
                            showToast(parseErrorMessage(response, "약속 정보를 불러오지 못했습니다."));
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Yaksok>> call, Throwable t) {
                        Log.e("공유약속", "통신 실패: " + t.getMessage());
                        showToast("네트워크 오류가 발생했습니다.");
                        finish();
                    }
                });
    }

    // DetailYaksok 과 같은 방식으로 채우되, 이 화면은 처음부터 읽기 전용이라 잠금 처리가 따로 없다.
    private void populateViews(Yaksok yaksok) {
        String owner = yaksok.getOwnerNickname();
        binding.tvOwner.setText(owner != null ? owner + "님이 공유한 약속입니다." : "공유받은 약속입니다.");

        if (yaksok.getTitle() != null) binding.inputCardTitle.setText(yaksok.getTitle());
        if (yaksok.getStartDate() != null) binding.inputStartDate.setText(yaksok.getStartDate());
        binding.inputPrescriptionDays.setText(String.valueOf(yaksok.getPrescriptionDays()));

        if (yaksok.getPills() != null && !yaksok.getPills().isEmpty()) {
            pills.clear();
            pills.addAll(yaksok.getPills());
            pillAdapter.notifyDataSetChanged();
        }

        if (yaksok.isTakeMorning()) {
            binding.cbMorning.setChecked(true);
            binding.inputSetMorningTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeMorning() != null) binding.inputSetMorningTime.setText(yaksok.getTimeMorning());
        }
        if (yaksok.isTakeLunch()) {
            binding.cbLunch.setChecked(true);
            binding.inputSetLunchTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeLunch() != null) binding.inputSetLunchTime.setText(yaksok.getTimeLunch());
        }
        if (yaksok.isTakeDinner()) {
            binding.cbDinner.setChecked(true);
            binding.inputSetDinnerTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeDinner() != null) binding.inputSetDinnerTime.setText(yaksok.getTimeDinner());
        }

        String dosageTime = yaksok.getDosageTime();
        if (dosageTime != null) {
            if (dosageTime.equals("식전 30분")) {
                binding.rgDosageTime.check(R.id.rb_before);
            } else if (dosageTime.equals("식후 30분")) {
                binding.rgDosageTime.check(R.id.rb_after);
            } else {
                binding.rgDosageTime.check(R.id.rb_anytime);
            }
        }
    }

    private void saveSharedYaksok() {
        Long userId = SprefsManager.getUserId(this);
        binding.btnSave.setEnabled(false);   // 연타로 중복 요청이 나가지 않게 막는다

        NetworkClient.getYaksokApi()
                .saveSharedYaksok(new ShareYaksokRequest(userId, yaksokId))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            showToast("공유 약속을 저장했습니다.");
                            finish();
                        } else {
                            binding.btnSave.setEnabled(true);
                            showToast(parseErrorMessage(response, "저장에 실패했습니다."));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        binding.btnSave.setEnabled(true);
                        Log.e("공유약속", "저장 통신 실패: " + t.getMessage());
                        showToast("네트워크 오류가 발생했습니다.");
                    }
                });
    }

    // 서버가 "이미 저장한 공유 약속입니다." 같은 메시지를 내려주므로 그대로 보여준다.
    private String parseErrorMessage(Response<?> response, String defaultMessage) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : null;
            Log.e("공유약속", "서버 에러 상세: " + errorBody);

            if (errorBody != null) {
                ApiResponse<?> error = new Gson().fromJson(errorBody, ApiResponse.class);
                if (error != null && error.getMessage() != null) {
                    return error.getMessage();
                }
            }
        } catch (Exception e) {
            Log.e("공유약속", "에러 응답 파싱 실패", e);
        }
        return defaultMessage;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}