package com.example.medication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.request.YaksokRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePrescription extends AppCompatActivity {

    private ImageView ivBack;
    private InputView inputStartDate;
    private InputView inputTitle;
    private InputView inputPrescriptionDays;
    private LinearLayout llDasage;
    private CheckBox cbMorning, cbLunch, cbDinner;
    private RadioGroup rgDosageTime;
    private FrameLayout btnAddPill;
    private Button btnRegister;

    // 리사이클러 뷰 관련 추가
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




        ivBack.setOnClickListener(v -> finish());
        inputStartDate.setOnClickListener(v -> showDatePicker());

        // 약 추가 버튼 클릭 시 리스트에 데이터 추가
        btnAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePrescription.this, MedicineSearchActivity.class);
            searchLauncher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> {
            boolean isDateValid = inputStartDate.isValid(); // (주의: 사용하신 유효성 검사 메서드 이름이 validate()가 맞는지 확인해 주세요!)
            boolean isTitleValid = inputTitle.isValid();
            boolean isDaysValid = inputPrescriptionDays.isValid();
            int selectedId = rgDosageTime.getCheckedRadioButtonId();

            if (!isDateValid || !isTitleValid || !isDaysValid) {
                showToast("필수 항목을 모두 입력해주세요.");
                return;
            }

            if(!cbMorning.isChecked() && !cbLunch.isChecked() && !cbDinner.isChecked()){
                llDasage.setBackgroundResource(R.drawable.bg_error_border);
                showToast("투약 횟수를 선택해주세요.");
                return;
            }else{
                llDasage.setBackgroundResource(R.drawable.bg_yellow_border);
            }

            if(selectedId == -1){
                rgDosageTime.setBackgroundResource(R.drawable.bg_error_border);
                showToast("투약 시간을 선택해주세요.");
                return;
            }else{
                rgDosageTime.setBackgroundResource(R.drawable.bg_yellow_border);
            }

            if (selectedPills.isEmpty()) {
                showToast("최소 한 개 이상의 약을 추가해주세요.");
                return;
            }
            for(PillRequest pill : selectedPills){
                if(pill.getDosage().isEmpty()){
                    showToast(pill.getName() +"의 투약량을 입력해주세요.");
                    return;
                }
            }
            startCreateYaksok();

        });
    }

    private void startCreateYaksok(){
        if(inputStartDate.getText() != null && inputTitle.getText() != null && !selectedPills.isEmpty()){
            String startDate = inputStartDate.getText();
            String name = inputTitle.getText();
            int prescriptionDays = Integer.parseInt(inputPrescriptionDays.getText());
            boolean takeMorning  = cbMorning.isChecked();
            boolean takeLunch = cbLunch.isChecked();
            boolean takeDinner = cbDinner.isChecked();

            int selectedId = rgDosageTime.getCheckedRadioButtonId();
            String dosageTime = "";
            if(selectedId == R.id.rb_before){
                dosageTime = "식전 30분";
            }
            else if(selectedId == R.id.rb_after){
                dosageTime = "식후 30분";
            }
            else if(selectedId == R.id.rb_anytime){
                dosageTime = "직후";
            }

            YaksokRequest request = new YaksokRequest(name, startDate, prescriptionDays, takeMorning, takeLunch, takeDinner, dosageTime, selectedPills);

            YaksokApi api = NetworkClient.getYaksokApi();

            api.saveYaksok(request).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if(response.isSuccessful() && response.body() != null){
                        ApiResponse<Void> result = response.body();

                        if(result.isBusinessSuccess()){
                            Log.d("CreateYaksok", result.getMessage());
                            Intent intent = new Intent(CreatePrescription.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Log.e("CreateYaksokError", result.getMessage());
                            showToast(result.getMessage());
                        }
                    }else{
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showToast("네트워크 연결을 확인해주세요.");
                    Log.e("NetwordError", "통신 실패: " + t.getMessage());
                }
            });
        }
    }

    private void handleErrorResponse(Response<?> response) {
        try{
            if(response.errorBody()!= null){
                String errorBodyString = response.errorBody().string();

                JSONObject jsonObject = new JSONObject(errorBodyString);

                String errorMessage = jsonObject.getString("message");

                showToast("errorMessage");
            }else{
                showToast("서버 오류가 발생했습니다. (코드: " + response.code() + ")");
            }
        } catch (Exception e) {
            Log.d("CreateYaksokError", "JSON 파싱 실패: " + e.getMessage());
            showToast("알 수 없는 오류가 발생했습니다.");
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
            btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFEB3B)); // 노란색
            btnRegister.setTextColor(0xFF000000); // 검은색
        } else {
            btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0)); // 회색
            btnRegister.setTextColor(0xFFFFFFFF); // 흰색
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
        rgDosageTime = findViewById(R.id.rg_dosage_time);
        btnAddPill = findViewById(R.id.btn_add_pill);
        btnRegister = findViewById(R.id.btn_register);
        rvSelectedPills = findViewById(R.id.rv_selected_pills);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}