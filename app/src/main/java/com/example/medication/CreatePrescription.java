package com.example.medication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.AddMedicationSettingAdapter;
import com.example.medication.model.MedicationSetting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreatePrescription extends AppCompatActivity {

    private ImageView ivBack;
    private InputView inputDate;
    private InputView inputName;
    private FrameLayout btnAddPill;
    private Button btnRegister;

    // 리사이클러 뷰 관련 추가
    private RecyclerView rvSelectedPills;
    private AddMedicationSettingAdapter settingAdapter;
    private final List<MedicationSetting> selectedPills = new ArrayList<>();

    private ActivityResultLauncher<Intent> searchLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_prescription);

        initViews();
        setupRecyclerView();

        setupSearchLauncher();

        ivBack.setOnClickListener(v -> finish());
        inputDate.setOnClickListener(v -> showDatePicker());

        // 약 추가 버튼 클릭 시 리스트에 데이터 추가
        btnAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePrescription.this, MedicineSearchActivity.class);
            searchLauncher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> {
            if (selectedPills.isEmpty()) {
                showToast("최소 한 개 이상의 약을 추가해주세요.");
                return;
            }

            finish();
        });
    }

    private void startCreateYaksok(){
        String date = inputDate.getText();
        String yaksokName = inputName.getText();

    }
    private void setupSearchLauncher() {
        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 결과가 RESULT_OK이고 데이터가 존재할 때만 실행
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        String medicineName = result.getData().getStringExtra("SELECTED_MEDICINE_NAME");
                        String medicineImage = result.getData().getStringExtra("SELECTED_MEDICINE_IMAGE");

                        if (medicineName != null) {
                            selectedPills.add(new MedicationSetting(medicineImage, medicineName));

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

        // 데이터가 변할 때마다 등록 버튼 상태 업데이트를 위한 Observer 등록 (선택사항)
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
            inputDate.setText(dateStr);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        inputDate = findViewById(R.id.input_date);
        inputName = findViewById(R.id.input_card_name);
        btnAddPill = findViewById(R.id.btn_add_pill);
        btnRegister = findViewById(R.id.btn_register);
        rvSelectedPills = findViewById(R.id.rv_selected_pills);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}