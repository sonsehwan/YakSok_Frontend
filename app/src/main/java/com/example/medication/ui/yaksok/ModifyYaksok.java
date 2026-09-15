package com.example.medication.ui.yaksok;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.example.medication.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.InputView;
import com.example.medication.R;
import com.example.medication.adapter.AddMedicationSettingAdapter;
import com.example.medication.databinding.ActivityModifyYaksokBinding;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.CreateYakSokRequest;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SaveYaksokResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.ui.medicine.MedicineSearchActivity;
import com.example.medication.util.SprefsManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModifyYaksok extends BaseActivity {

    private ActivityModifyYaksokBinding binding;

    // 선택된 약 목록 리사이클러뷰 관련
    private AddMedicationSettingAdapter settingAdapter;
    private final List<PillRequest> selectedPills = new ArrayList<>();
    private ActivityResultLauncher<Intent> searchLauncher;
    private Yaksok originalYaksok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModifyYaksokBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearchLauncher();
        setupTimePickerLogic();

        binding.ivBack.setOnClickListener(v -> finish());
        binding.inputStartDate.setOnClickListener(v -> showDatePicker());

        // 약 추가 버튼 클릭 시 검색 화면 이동
        binding.btnAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(ModifyYaksok.this, MedicineSearchActivity.class);
            searchLauncher.launch(intent);
        });

        // 약속 수정 버튼
        binding.btnRegister.setOnClickListener(v -> {
            if(!validateInput()) return;
            startModifyYaksok();
        });

        originalYaksok = (Yaksok)getIntent().getSerializableExtra("YAKSOK_DATA");


        if(originalYaksok != null){
            populateViews(originalYaksok);
        }else{
            showToast("약속 정보를 불러올 수 없습니다.");
        }
    }

    private void populateViews(Yaksok yaksok){
        if(yaksok.getTitle() != null) binding.inputCardTitle.setText(yaksok.getTitle());
        if (yaksok.getStartDate() != null) binding.inputStartDate.setText(yaksok.getStartDate());
        binding.inputPrescriptionDays.setText(String.valueOf(yaksok.getPrescriptionDays()));

        if(yaksok.getPills() != null && !yaksok.getPills().isEmpty()) {
            selectedPills.clear();
            selectedPills.addAll(yaksok.getPills());
            settingAdapter.notifyDataSetChanged();
            updateRegisterButtonState();
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
            } else { // 직후
                binding.rgDosageTime.check(R.id.rb_anytime);
            }
        }
    }

    private void setupTimePickerLogic(){
        // 아침 체크박스 로직
        binding.cbMorning.setOnCheckedChangeListener((button, isChecked) -> {
            binding.inputSetMorningTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.inputSetMorningTime.setOnClickListener(v -> showTimePicker(binding.inputSetMorningTime, 8, 0));

        // 점심 체크박스 로직
        binding.cbLunch.setOnCheckedChangeListener((button, isChecked) -> {
            binding.inputSetLunchTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.inputSetLunchTime.setOnClickListener(v -> showTimePicker(binding.inputSetLunchTime, 12, 0));

        // 저녁 체크박스 로직
        binding.cbDinner.setOnCheckedChangeListener((button, isChecked) -> {
            binding.inputSetDinnerTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.inputSetDinnerTime.setOnClickListener(v -> showTimePicker(binding.inputSetDinnerTime, 18, 0));
    }

    private void showTimePicker(InputView targetInputView, int defaultHour, int defaultMinute) {
        int hour = defaultHour;
        int minute = defaultMinute;

        String currentTimeStr = targetInputView.getText();

        // 현재 입력된 텍스트가 있으면 파싱하여 다이얼로그 초기값으로 설정
        if (currentTimeStr != null && !currentTimeStr.isEmpty()) {
            try {
                String[] parts = currentTimeStr.split(" ");
                if(parts.length == 2) {
                    String amPm = parts[0];
                    String[] timeParts = parts[1].split(":");

                    int parsedHour = Integer.parseInt(timeParts[0]);
                    int parsedMinute = Integer.parseInt(timeParts[1]);

                    if (amPm.equals("오후") && parsedHour < 12) parsedHour += 12;
                    else if (amPm.equals("오전") && parsedHour == 12) parsedHour = 0;

                    hour = parsedHour;
                    minute = parsedMinute;
                }
            } catch (Exception e) {
                Log.e("TimePicker", "기존 시간 파싱 실패: " + e.getMessage());
            }
        }

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String amPm = hourOfDay < 12 ? "오전" : "오후";
            int displayHour = hourOfDay % 12;
            if (displayHour == 0) displayHour = 12;

            String timeStr = String.format(Locale.getDefault(), "%s %02d:%02d", amPm, displayHour, minuteOfHour);
            targetInputView.setText(timeStr);
        }, hour, minute, false);

        dialog.show();
    }

    private boolean validateInput() {
        if (!binding.inputStartDate.isValid() || !binding.inputCardTitle.isValid() || !binding.inputPrescriptionDays.isValid()) {
            showToast("필수 항목을 모두 입력해주세요.");
            return false;
        }

        if(!binding.cbMorning.isChecked() && !binding.cbLunch.isChecked() && !binding.cbDinner.isChecked()){
            binding.llDasage.setBackgroundResource(R.drawable.bg_error_border);
            showToast("투약 횟수를 선택해주세요.");
            return false;
        } else {
            binding.llDasage.setBackgroundResource(R.drawable.bg_yellow_border);
        }

        if(binding.rgDosageTime.getCheckedRadioButtonId() == -1){
            binding.rgDosageTime.setBackgroundResource(R.drawable.bg_error_border);
            showToast("투약 시간을 선택해주세요.");
            return false;
        } else {
            binding.rgDosageTime.setBackgroundResource(R.drawable.bg_yellow_border);
        }

        if (selectedPills.isEmpty()) {
            showToast("최소 한 개 이상의 약을 추가해주세요.");
            return false;
        }

        for(PillRequest pill : selectedPills){
            if(pill.getDosage() == null || pill.getDosage().isEmpty()){
                showToast(pill.getName() +"의 투약량을 입력해주세요.");
                return false;
            }
        }
        return true;
    }

    private void startModifyYaksok(){
        String startDate = binding.inputStartDate.getText();
        String title = binding.inputCardTitle.getText();
        int prescriptionDays = Integer.parseInt(binding.inputPrescriptionDays.getText());

        boolean takeMorning = binding.cbMorning.isChecked();
        boolean takeLunch = binding.cbLunch.isChecked();
        boolean takeDinner = binding.cbDinner.isChecked();

        // 체크되지 않은 알림 시간은 null로 전송
        String timeMorning = takeMorning ? binding.inputSetMorningTime.getText() : null;
        String timeLunch = takeLunch ? binding.inputSetLunchTime.getText() : null;
        String timeDinner = takeDinner ? binding.inputSetDinnerTime.getText() : null;

        int selectedId = binding.rgDosageTime.getCheckedRadioButtonId();
        String dosageTime = "";
        if(selectedId == R.id.rb_before) dosageTime = "식전 30분";
        else if(selectedId == R.id.rb_after) dosageTime = "식후 30분";
        else if(selectedId == R.id.rb_anytime) dosageTime = "직후";

        CreateYakSokRequest request = new CreateYakSokRequest(
                SprefsManager.getUserId(this),
                title,
                startDate,
                prescriptionDays,
                takeMorning,
                takeLunch,
                takeDinner,
                dosageTime,
                timeMorning,
                timeLunch,
                timeDinner,
                selectedPills);

        YaksokApi api = NetworkClient.getYaksokApi();

        api.updateYaksok(originalYaksok.getId(), request).enqueue(new Callback<ApiResponse<SaveYaksokResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SaveYaksokResponse>> call, @NonNull Response<ApiResponse<SaveYaksokResponse>> response) {
                if(response.isSuccessful() && response.body() != null){
                    ApiResponse<SaveYaksokResponse> result = response.body();

                    SaveYaksokResponse saveYaksokResponse = result.getData();

                    if(result.isBusinessSuccess()){
                        Yaksok yaksok = saveYaksokResponse.getYaksok();
                        Long yaksokId = yaksok.getId();

                        if(yaksokId != null) {
                            yaksok.setId(yaksokId);

                            SprefsManager.updateYaksok(ModifyYaksok.this, yaksok);

                            List<NotificationYaksok> allNotifications = saveYaksokResponse.getNotifications();

                            SprefsManager.setNotifications(ModifyYaksok.this, allNotifications);

                            showToast("약속이 성공적으로 수정되었습니다.");

                            Intent intent = new Intent();
                            intent.putExtra("UPDATED_YAKSOK", yaksok);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            showToast("서버와의 통신 중에 문제가 발생하였습니다. 죄송합니다.");
                            Log.e("ModifyYaksokError", "약속ID를 가져오는데 실패하였습니다.");
                        }
                    } else {
                        showToast(result.getMessage());
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SaveYaksokResponse>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorJson);
                // message 키가 있으면 출력, 없으면 전체 에러 내용 출력
                String message = jsonObject.optString("message", errorJson);
                showToast("서버 에러: " + message);
                Log.e("서버 에러: ", message);
            } else {
                showToast("서버 오류: " + response.code());
            }
        } catch (Exception e) {
            showToast("네트워크 오류 발생");
        }
    }
    private void setupSearchLauncher() {
        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String medicineName = result.getData().getStringExtra("SELECTED_MEDICINE_NAME");
                        String medicineImage = result.getData().getStringExtra("SELECTED_MEDICINE_IMAGE");

                        if (medicineName != null) {
                            selectedPills.add(new PillRequest(medicineImage, medicineName));
                            settingAdapter.notifyItemInserted(selectedPills.size() - 1);
                            updateRegisterButtonState();
                        }
                    }
                }
        );
    }

    private void setupRecyclerView() {
        settingAdapter = new AddMedicationSettingAdapter(selectedPills);
        binding.rvSelectedPills.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSelectedPills.setAdapter(settingAdapter);

        settingAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() { updateRegisterButtonState(); }
            @Override
            public void onItemRangeRemoved(int start, int count) { updateRegisterButtonState(); }
        });
    }

    private void updateRegisterButtonState() {
        if (!selectedPills.isEmpty()) {
            binding.btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFEB3B));
            binding.btnRegister.setTextColor(0xFF000000);
        } else {
            binding.btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
            binding.btnRegister.setTextColor(0xFFFFFFFF);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.inputStartDate.setText(dateStr);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}