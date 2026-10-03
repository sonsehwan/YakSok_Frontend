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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.InputView;
import com.example.medication.R;
import com.example.medication.adapter.AddMedicationSettingAdapter;
import com.example.medication.databinding.ActivityCreatePrescriptionBinding;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.CreateYakSokRequest;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SaveYaksokResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.ui.main.MainActivity;
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

public class CreateDirectSchedule extends BaseActivity {

    private ActivityCreatePrescriptionBinding binding;

    private AddMedicationSettingAdapter settingAdapter;
    private final List<PillRequest> selectedPills = new ArrayList<>();

    private ActivityResultLauncher<Intent> searchLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearchLauncher();
        setupTimePickerLogic();

        binding.ivBack.setOnClickListener(v -> finish());
        binding.inputStartDate.setOnClickListener(v -> showDatePicker());

        binding.btnAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(CreateDirectSchedule.this, MedicineSearchActivity.class);
            searchLauncher.launch(intent);
        });

        binding.btnRegister.setOnClickListener(v -> {
            if(!validateInput()) return;
            startCreateYaksok();
        });
    }

    private void setupTimePickerLogic(){

        binding.cbMorning.setOnCheckedChangeListener((button, isChecked) -> {
            binding.inputSetMorningTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.inputSetMorningTime.setOnClickListener(v -> showTimePicker(binding.inputSetMorningTime, 8, 0));

        binding.cbLunch.setOnCheckedChangeListener((button, isChecked) -> {
            binding.inputSetLunchTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.inputSetLunchTime.setOnClickListener(v -> showTimePicker(binding.inputSetLunchTime, 12, 0));

        binding.cbDinner.setOnCheckedChangeListener((button, isChecked) -> {
            binding.inputSetDinnerTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.inputSetDinnerTime.setOnClickListener(v -> showTimePicker(binding.inputSetDinnerTime, 18, 0));
    }

    private void showTimePicker(InputView targetInputView, int defaultHour, int defaultMinute) {
        int hour = defaultHour;
        int minute = defaultMinute;

        String currentTimeStr = targetInputView.getText();

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

    private void startCreateYaksok(){
        String startDate = binding.inputStartDate.getText();
        String title = binding.inputCardTitle.getText();
        int prescriptionDays = Integer.parseInt(binding.inputPrescriptionDays.getText());

        boolean takeMorning = binding.cbMorning.isChecked();
        boolean takeLunch = binding.cbLunch.isChecked();
        boolean takeDinner = binding.cbDinner.isChecked();

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

        api.saveYaksok(request).enqueue(new Callback<ApiResponse<SaveYaksokResponse>>() {
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

                            SprefsManager.addYaksok(CreateDirectSchedule.this, yaksok);

                            showToast("약속이 성공적으로 등록되었습니다.");

                            Intent intent = new Intent(CreateDirectSchedule.this, MainActivity.class);
                            //intent.putExtra("newNotifications", (ArrayList<NotificationYaksok>)newNotifications);
                            startActivity(intent);
                            finish();
                        }else{
                            showToast("서버와의 통신 중에 문제가 발생하였습니다. 죄송합니다.");
                            Log.e("CreateYaksokError", "약속ID를 가져오는데 실패하였습니다.");
                            Log.e("CreateYaksokError", "yaksokId: " + yaksokId);
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
            binding.btnRegister.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.p600));
            binding.btnRegister.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            binding.btnRegister.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.g100));
            binding.btnRegister.setTextColor(ContextCompat.getColor(this, R.color.g400));
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