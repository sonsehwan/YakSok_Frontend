package com.example.medication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.AddMedicationSettingAdapter;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.util.SprefsManager;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePrescription extends AppCompatActivity {

    private ImageView ivBack;
    private InputView inputStartDate, inputTitle, inputPrescriptionDays;
    private InputView inputSetMorningTime, inputSetLunchTime, inputSetDinnerTime;
    private LinearLayout llDasage;
    private CheckBox cbMorning, cbLunch, cbDinner;
    private RadioGroup rgDosageTime;
    private FrameLayout btnAddPill;
    private Button btnRegister;

    // 선택된 약 목록 리사이클러뷰 관련
    private RecyclerView rvSelectedPills;
    private AddMedicationSettingAdapter settingAdapter;
    private final List<PillRequest> selectedPills = new ArrayList<>();

    private ActivityResultLauncher<Intent> searchLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_prescription);

        initViews();
        setupRecyclerView();
        setupSearchLauncher();
        setupTimePickerLogic();

        ivBack.setOnClickListener(v -> finish());
        inputStartDate.setOnClickListener(v -> showDatePicker());

        // 약 추가 버튼 클릭 시 검색 화면 이동
        btnAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePrescription.this, MedicineSearchActivity.class);
            searchLauncher.launch(intent);
        });

        // 약속 등록 버튼
        btnRegister.setOnClickListener(v -> {
            if(!validateInput()) return;
            startCreateYaksok();
        });
    }

    private void setupTimePickerLogic(){
        // 아침 체크박스 로직
        cbMorning.setOnCheckedChangeListener((button, isChecked) -> {
            inputSetMorningTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        inputSetMorningTime.setOnClickListener(v -> showTimePicker(inputSetMorningTime, 8, 0));

        // 점심 체크박스 로직
        cbLunch.setOnCheckedChangeListener((button, isChecked) -> {
            inputSetLunchTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        inputSetLunchTime.setOnClickListener(v -> showTimePicker(inputSetLunchTime, 12, 0));

        // 저녁 체크박스 로직
        cbDinner.setOnCheckedChangeListener((button, isChecked) -> {
            inputSetDinnerTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        inputSetDinnerTime.setOnClickListener(v -> showTimePicker(inputSetDinnerTime, 18, 0));
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
        if (!inputStartDate.isValid() || !inputTitle.isValid() || !inputPrescriptionDays.isValid()) {
            showToast("필수 항목을 모두 입력해주세요.");
            return false;
        }

        if(!cbMorning.isChecked() && !cbLunch.isChecked() && !cbDinner.isChecked()){
            llDasage.setBackgroundResource(R.drawable.bg_error_border);
            showToast("투약 횟수를 선택해주세요.");
            return false;
        } else {
            llDasage.setBackgroundResource(R.drawable.bg_yellow_border);
        }

        if(rgDosageTime.getCheckedRadioButtonId() == -1){
            rgDosageTime.setBackgroundResource(R.drawable.bg_error_border);
            showToast("투약 시간을 선택해주세요.");
            return false;
        } else {
            rgDosageTime.setBackgroundResource(R.drawable.bg_yellow_border);
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
        String startDate = inputStartDate.getText();
        String name = inputTitle.getText();
        int prescriptionDays = Integer.parseInt(inputPrescriptionDays.getText());

        boolean takeMorning = cbMorning.isChecked();
        boolean takeLunch = cbLunch.isChecked();
        boolean takeDinner = cbDinner.isChecked();

        // 체크되지 않은 알림 시간은 null로 전송
        String timeMorning = takeMorning ? inputSetMorningTime.getText() : null;
        String timeLunch = takeLunch ? inputSetLunchTime.getText() : null;
        String timeDinner = takeDinner ? inputSetDinnerTime.getText() : null;

        int selectedId = rgDosageTime.getCheckedRadioButtonId();
        String dosageTime = "";
        if(selectedId == R.id.rb_before) dosageTime = "식전 30분";
        else if(selectedId == R.id.rb_after) dosageTime = "식후 30분";
        else if(selectedId == R.id.rb_anytime) dosageTime = "직후";

        Yaksok request = new Yaksok(name, startDate, prescriptionDays, takeMorning, takeLunch, takeDinner, dosageTime, timeMorning, timeLunch, timeDinner, selectedPills, "TAKING");

        YaksokApi api = NetworkClient.getYaksokApi();
        String finalDosageTime = dosageTime;

        api.saveYaksok(request).enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if(response.isSuccessful() && response.body() != null){
                    ApiResponse<Long> result = response.body();

                    if(result.isBusinessSuccess()){
                        Long yaksokId = result.getData();

                        if(yaksokId != null) {
                            request.setId(yaksokId);
                            // 1. 전체 약속 리스트에 저장
                            SprefsManager.addYaksok(CreatePrescription.this, request);

                            // 2. 알림용 NotificationYaksok 리스트 생성
                            // (투약일 수 * 투약 횟수만큼 생성)
                            ArrayList<NotificationYaksok> newNotifications = new ArrayList<>();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Calendar cal = Calendar.getInstance();

                            try {
                                if (startDate != null && !startDate.isEmpty()) {
                                    cal.setTime(sdf.parse(startDate));
                                }
                            } catch (ParseException e) {
                                Log.e("DateParse", "날짜 파싱 실패: " + e.getMessage());
                            }

                            // 투약 기간만큼 반복하며 각 날짜별 알림 객체 생성
                            for (int i = 0; i < prescriptionDays; i++) {
                                String currentDateStr = sdf.format(cal.getTime());

                                if (takeMorning) {
                                    String t = (timeMorning != null && !timeMorning.isEmpty()) ? timeMorning : "오전 08:00";
                                    newNotifications.add(new NotificationYaksok(name, currentDateStr, t, finalDosageTime, false));
                                }
                                if (takeLunch) {
                                    String t = (timeLunch != null && !timeLunch.isEmpty()) ? timeLunch : "오후 12:00";
                                    newNotifications.add(new NotificationYaksok(name, currentDateStr, t, finalDosageTime, false));
                                }
                                if (takeDinner) {
                                    String t = (timeDinner != null && !timeDinner.isEmpty()) ? timeDinner : "오후 06:00";
                                    newNotifications.add(new NotificationYaksok(name, currentDateStr, t, finalDosageTime, false));
                                }

                                // 다음 날짜로 이동
                                cal.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            // 3. SharedPreferences에 알림 리스트 누적 저장
                            SprefsManager.addNotifications(CreatePrescription.this, newNotifications);

                            showToast("약속이 성공적으로 등록되었습니다.");

                            // 4. 메인 화면으로 이동하며 새로 생성된 리스트 전달
                            Intent intent = new Intent(CreatePrescription.this, MainActivity.class);
                            intent.putExtra("newNotifications", newNotifications);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        showToast(result.getMessage());
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                showToast("네트워크 연결을 확인해주세요.");
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if(response.errorBody() != null) {
                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                showToast(jsonObject.getString("message"));
            } else {
                showToast("서버 오류: " + response.code());
            }
        } catch (Exception e) {
            showToast("오류가 발생했습니다.");
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
        rvSelectedPills.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedPills.setAdapter(settingAdapter);

        settingAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() { updateRegisterButtonState(); }
            @Override
            public void onItemRangeRemoved(int start, int count) { updateRegisterButtonState(); }
        });
    }

    private void updateRegisterButtonState() {
        if (!selectedPills.isEmpty()) {
            btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFEB3B));
            btnRegister.setTextColor(0xFF000000);
        } else {
            btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
            btnRegister.setTextColor(0xFFFFFFFF);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            inputStartDate.setText(dateStr);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        inputStartDate = findViewById(R.id.input_start_date);
        inputTitle = findViewById(R.id.input_card_title);
        inputPrescriptionDays = findViewById(R.id.input_prescriptionDays);
        llDasage = findViewById(R.id.ll_dasage);
        cbMorning = findViewById(R.id.cb_morning);
        cbLunch = findViewById(R.id.cb_lunch);
        cbDinner = findViewById(R.id.cb_dinner);
        inputSetMorningTime = findViewById(R.id.input_set_morning_time);
        inputSetLunchTime = findViewById(R.id.input_set_lunch_time);
        inputSetDinnerTime = findViewById(R.id.input_set_dinner_time);
        rgDosageTime = findViewById(R.id.rg_dosage_time);
        btnAddPill = findViewById(R.id.btn_add_pill);
        btnRegister = findViewById(R.id.btn_register);
        rvSelectedPills = findViewById(R.id.rv_selected_pills);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}