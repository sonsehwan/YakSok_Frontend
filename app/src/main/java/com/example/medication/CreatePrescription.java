package com.example.medication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class CreatePrescription extends AppCompatActivity {

    private ImageView ivBack;
    private InputView inputDate, inputPharmacy, inputHospital;
    private FrameLayout btnAddPill;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_prescription);

        initViews();

        // 뒤로가기
        ivBack.setOnClickListener(v -> finish());

        // 날짜 선택 로직
        inputDate.setOnClickListener(v -> showDatePicker());

        // 약 추가 클릭 이벤트
        btnAddPill.setOnClickListener(v -> {
            showToast("약 추가 화면으로 이동합니다.");
        });

        // 등록 버튼 클릭 이벤트
        btnRegister.setOnClickListener(v -> {
            showToast("등록되었습니다.");
            finish();
        });
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
        inputPharmacy = findViewById(R.id.input_pharmacy);
        inputHospital = findViewById(R.id.input_hospital);
        btnAddPill = findViewById(R.id.btn_add_pill);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}