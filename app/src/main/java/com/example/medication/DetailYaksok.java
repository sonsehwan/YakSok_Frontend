package com.example.medication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.AddMedicationSettingAdapter;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailYaksok extends AppCompatActivity {

    private ImageView ivBack, ivMenu;
    private InputView inputStartDate, inputTitle, inputPrescriptionDays;
    private InputView inputSetMorningTime, inputSetLunchTime, inputSetDinnerTime;
    private LinearLayout llDasage;
    private CheckBox cbMorning, cbLunch, cbDinner;
    private RadioGroup rgDosageTime;

    // 선택된 약 목록 리사이클러뷰 관련
    private RecyclerView rvSelectedPills;
    private AddMedicationSettingAdapter settingAdapter;
    private final List<PillRequest> selectedPills = new ArrayList<>();
    private ActivityResultLauncher<Intent> searchLauncher;
    private Yaksok originalYaksok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_yaksok);

        initViews();
        setupRecyclerView();
        setupTimePickerLogic();

        originalYaksok = (Yaksok)getIntent().getSerializableExtra("YAKSOK_DATA");

        ivBack.setOnClickListener(v -> finish());

        ivMenu.setOnClickListener(view -> {
            showMenu(view, originalYaksok);
        });

        if(originalYaksok != null){
            populateViews(originalYaksok);
        }else{
            showToast("약속 정보를 불러올 수 없습니다.");
        }
    }

    private void populateViews(Yaksok yaksok){
        if(yaksok.getTitle() != null) inputTitle.setText(yaksok.getTitle());
        if (yaksok.getStartDate() != null) inputStartDate.setText(yaksok.getStartDate());
        inputPrescriptionDays.setText(String.valueOf(yaksok.getPrescriptionDays()));

        if(yaksok.getPills() != null && !yaksok.getPills().isEmpty()) {
            selectedPills.clear();
            selectedPills.addAll(yaksok.getPills());
            settingAdapter.notifyDataSetChanged();
        }

        if (yaksok.isTakeMorning()) {
            cbMorning.setChecked(true);
            inputSetMorningTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeMorning() != null) inputSetMorningTime.setText(yaksok.getTimeMorning());
        }
        if (yaksok.isTakeLunch()) {
            cbLunch.setChecked(true);
            inputSetLunchTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeLunch() != null) inputSetLunchTime.setText(yaksok.getTimeLunch());
        }
        if (yaksok.isTakeDinner()) {
            cbDinner.setChecked(true);
            inputSetDinnerTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeDinner() != null) inputSetDinnerTime.setText(yaksok.getTimeDinner());
        }

        String dosageTime = yaksok.getDosageTime();
        if (dosageTime != null) {
            if (dosageTime.equals("식전 30분")) {
                rgDosageTime.check(R.id.rb_before);
            } else if (dosageTime.equals("식후 30분")) {
                rgDosageTime.check(R.id.rb_after);
            } else { // 직후
                rgDosageTime.check(R.id.rb_anytime);
            }
        }
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


    private void setupRecyclerView() {
        settingAdapter = new AddMedicationSettingAdapter(selectedPills);
        rvSelectedPills.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedPills.setAdapter(settingAdapter);
    }

    private void showDeleteConfirmDialog(Yaksok yaksok) {
        Long id = yaksok.getId();
        new AlertDialog.Builder(this)
                .setTitle("약속 삭제")
                .setMessage("'" + yaksok.getTitle() + "' 약속을 삭제하시겠습니까?\n관련된 모든 정보(복약, 알림)가 함께 삭제됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteYaksokFromServer(id);
                    Intent intent = new Intent(DetailYaksok.this, YaksokList.class);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteYaksokFromServer(Long yaksokId) {
        YaksokApi api = NetworkClient.getYaksokApi();

        api.deleteYaksok(yaksokId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailYaksok.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetailYaksok.this, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("YaksokList", "삭제 통신 실패: " + t.getMessage());
                Toast.makeText(DetailYaksok.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMenu(View view, Yaksok yaksok){
        PopupMenu menu = new PopupMenu(this, view);

        menu.getMenuInflater().inflate(R.menu.yaksok_menu, menu.getMenu());

        menu.setOnMenuItemClickListener(item ->{
            int id = item.getItemId();

            if(id == R.id.yaksok_share){
                showToast("구현 예정입니다.");
                return true;
            }
            else if(id == R.id.yaksok_delete){
                showDeleteConfirmDialog(yaksok);
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivMenu = findViewById(R.id.iv_menu);
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
        rvSelectedPills = findViewById(R.id.rv_selected_pills);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}