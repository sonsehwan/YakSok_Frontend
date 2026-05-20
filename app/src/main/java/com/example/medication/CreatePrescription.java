package com.example.medication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.AddMedicationSettingAdapter;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.CreateYakSokRequest;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SaveYaksokResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.util.SprefsManager;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.java.GenerativeModelFutures;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePrescription extends AppCompatActivity {

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> searchLauncher;

    // UI 컴포넌트들
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_direct_schedule);

        // 1. UI 및 컴포넌트 초기화
        initViews();
        setupRecyclerView();
        setupSearchLauncher();
        setupTimePickerLogic();

        // 2. 기본 클릭 이벤트 설정
        ivBack.setOnClickListener(v -> finish());
        inputStartDate.setOnClickListener(v -> showDatePicker());

        btnAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePrescription.this, MedicineSearchActivity.class);
            searchLauncher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> {
            if(!validateInput()) return;
            startCreateYaksok();
        });

        // 3. 갤러리 런처 세팅 및 즉시 실행 (처방전 스캔 시작)
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d("MLKit", "사진 주소 가져오기 성공: " + uri);
                        recognizeTextFromImage(uri);
                    } else {
                        Toast.makeText(this, "사진을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                        finish(); // 사진 안 고르면 액티비티 종료
                    }
                }
        );

        galleryLauncher.launch("image/*");
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

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            inputStartDate.setText(dateStr);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private boolean validateInput() {
        if (!inputStartDate.isValid() || !inputTitle.isValid() || !inputPrescriptionDays.isValid()) {
            Toast.makeText(this, "필수 항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!cbMorning.isChecked() && !cbLunch.isChecked() && !cbDinner.isChecked()){
            llDasage.setBackgroundResource(R.drawable.bg_error_border);
            Toast.makeText(this, "투약 횟수를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            llDasage.setBackgroundResource(R.drawable.bg_yellow_border);
        }

        if(rgDosageTime.getCheckedRadioButtonId() == -1){
            rgDosageTime.setBackgroundResource(R.drawable.bg_error_border);
            Toast.makeText(this, "투약 시간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            rgDosageTime.setBackgroundResource(R.drawable.bg_yellow_border);
        }

        if (selectedPills.isEmpty()) {
            Toast.makeText(this, "최소 한 개 이상의 약을 추가해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        for(PillRequest pill : selectedPills){
            if(pill.getDosage() == null || pill.getDosage().isEmpty()){
                Toast.makeText(this, pill.getName() +"의 투약량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
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

    // =========================================================================
    // ML Kit + Firebase AI 처방전 분석 로직 시작
    // =========================================================================

    private void recognizeTextFromImage(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        Log.d("MLKit", "🎉 온디바이스 1차 텍스트 추출 완료!");
                        String alignedText = processExtractedDataWithLocation(visionText);

                        try {
                            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            analyzeTextWithGemini(imageBitmap, alignedText);
                        } catch (IOException e) {
                            Log.e("Image_Error", "비트맵 변환 실패", e);
                            Toast.makeText(this, "이미지 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MLKit", "텍스트 인식 실패", e);
                        Toast.makeText(this, "글자를 읽어내는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 파일을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeTextWithGemini(Bitmap imageBitmap, String alignedText) {
        String promptText = "너는 처방전 도우미 AI야. 첨부된 '처방전 이미지 원본'과 1차 정렬된 '텍스트'를 교차 검증해서 정확한 약물 정보를 추출해줘. 오직 JSON 리스트 형식으로만 반환해.\n\n" +
                "[1차 정렬된 텍스트]\n" + alignedText + "\n\n" +
                "포맷 예시: [{\"name\":\"써스펜정\",\"dose\":\"1\",\"frequency\":\"3\",\"days\":\"3\"}]";

        try {
            GenerativeModel gm = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash");

            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(promptText)
                    .addImage(imageBitmap)
                    .build();

            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);

            Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    parseAndDisplayGeminiResult(resultText);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("Firebase_AI", "AI 분석 실패", t);
                    Toast.makeText(CreatePrescription.this, "AI 분석 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            Log.e("Firebase_Setup_Error", "모델 초기화 실패", e);
            Toast.makeText(this, "AI 모델을 준비하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseAndDisplayGeminiResult(String aiResponseText) {
        try {
            String cleanJson = aiResponseText.replace("```json", "").replace("```", "").trim();
            JSONArray medicineArray = new JSONArray(cleanJson);

            if (medicineArray.length() > 0) {
                int maxDays = 0;
                int maxFreq = 0;
                String firstMedName = "";

                // 기존에 수동으로 등록된 약이 있다면 초기화 (처방전 스캔 기준 덮어쓰기)
                selectedPills.clear();

                // 모든 약 데이터를 돌며 가장 큰 days(투약일 수)와 frequency 추출 & 리스트에 등록
                for(int i = 0; i < medicineArray.length(); i++) {
                    JSONObject med = medicineArray.getJSONObject(i);
                    String medName = med.getString("name");

                    if (i == 0) firstMedName = medName;

                    // ✨ 핵심: AI가 찾은 약을 리사이클러뷰(약 목록)에 자동 추가!
                    selectedPills.add(new PillRequest("", medName));

                    try {
                        int days = Integer.parseInt(med.getString("days"));
                        if(days > maxDays) maxDays = days;

                        int freq = Integer.parseInt(med.getString("frequency"));
                        if(freq > maxFreq) maxFreq = freq;
                    } catch (NumberFormatException e) {
                        Log.e("Parsing", "숫자 변환 오류", e);
                    }
                }

                // 어댑터 새로고침 및 버튼 활성화 상태 업데이트
                if (settingAdapter != null) settingAdapter.notifyDataSetChanged();
                updateRegisterButtonState();

                String finalTitle = firstMedName;
                if (medicineArray.length() > 1) {
                    finalTitle += " 외 " + (medicineArray.length() - 1) + "건";
                }

                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // 메인 스레드에서 UI 동적 업데이트
                if (inputTitle != null) inputTitle.setText(finalTitle);
                if (inputPrescriptionDays != null) inputPrescriptionDays.setText(String.valueOf(maxDays > 0 ? maxDays : 1));
                if (inputStartDate != null) inputStartDate.setText(currentDate);

                // frequency 로직에 따른 체크박스 자동 배분
                if (cbMorning != null && cbLunch != null && cbDinner != null) {
                    cbMorning.setChecked(false);
                    cbLunch.setChecked(false);
                    cbDinner.setChecked(false);

                    if (maxFreq == 1) {
                        cbMorning.setChecked(true);
                    } else if (maxFreq == 2) {
                        cbMorning.setChecked(true);
                        cbDinner.setChecked(true);
                    } else if (maxFreq >= 3) {
                        cbMorning.setChecked(true);
                        cbLunch.setChecked(true);
                        cbDinner.setChecked(true);
                    }
                }

                Toast.makeText(this, "처방전 정보 자동 입력 완료!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("Gemini_Parsing_Error", "응답 데이터 파싱 실패", e);
            Toast.makeText(this, "AI가 처방전을 명확히 식별하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String processExtractedDataWithLocation(Text visionText) {
        List<Text.Line> allLines = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            allLines.addAll(block.getLines());
        }

        Collections.sort(allLines, (l1, l2) -> {
            if (l1.getBoundingBox() == null || l2.getBoundingBox() == null) return 0;
            return Integer.compare(l1.getBoundingBox().centerY(), l2.getBoundingBox().centerY());
        });

        List<List<Text.Line>> rows = new ArrayList<>();
        List<Text.Line> currentRow = new ArrayList<>();
        int currentY = -1;

        for (Text.Line line : allLines) {
            if (line.getBoundingBox() == null) continue;
            int y = line.getBoundingBox().centerY();

            if (currentY == -1 || Math.abs(y - currentY) < 30) {
                currentRow.add(line);
                currentY = (currentY == -1) ? y : (currentY + y) / 2;
            } else {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                currentRow.add(line);
                currentY = y;
            }
        }
        if (!currentRow.isEmpty()) rows.add(currentRow);

        for (List<Text.Line> row : rows) {
            Collections.sort(row, (l1, l2) -> {
                if (l1.getBoundingBox() == null || l2.getBoundingBox() == null) return 0;
                return Integer.compare(l1.getBoundingBox().left, l2.getBoundingBox().left);
            });
        }

        StringBuilder alignedTextBuilder = new StringBuilder();
        for (List<Text.Line> row : rows) {
            StringBuilder rowTextBuilder = new StringBuilder();
            for (Text.Line line : row) {
                rowTextBuilder.append(line.getText().trim()).append(" | ");
            }
            alignedTextBuilder.append(rowTextBuilder.toString()).append("\n");
        }
        return alignedTextBuilder.toString();
    }

    private void startCreateYaksok(){
        String startDate = inputStartDate.getText();
        String title = inputTitle.getText();
        int prescriptionDays = Integer.parseInt(inputPrescriptionDays.getText());

        boolean takeMorning = cbMorning.isChecked();
        boolean takeLunch = cbLunch.isChecked();
        boolean takeDinner = cbDinner.isChecked();

        String timeMorning = takeMorning ? inputSetMorningTime.getText() : null;
        String timeLunch = takeLunch ? inputSetLunchTime.getText() : null;
        String timeDinner = takeDinner ? inputSetDinnerTime.getText() : null;

        int selectedId = rgDosageTime.getCheckedRadioButtonId();
        String dosageTime = "";
        if(selectedId == R.id.rb_before) dosageTime = "식전 30분";
        else if(selectedId == R.id.rb_after) dosageTime = "식후 30분";
        else if(selectedId == R.id.rb_anytime) dosageTime = "직후";

        CreateYakSokRequest request = new CreateYakSokRequest(
                SprefsManager.getUserEmail(this),
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
                            SprefsManager.addYaksok(CreatePrescription.this, yaksok);

                            List<NotificationYaksok> allNotifications = saveYaksokResponse.getNotifications();
                            SprefsManager.setNotifications(CreatePrescription.this, allNotifications);

                            Toast.makeText(CreatePrescription.this, "약속이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(CreatePrescription.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CreatePrescription.this, "서버와의 통신 오류: 약속 ID 없음", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreatePrescription.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SaveYaksokResponse>> call, Throwable t) {
                Toast.makeText(CreatePrescription.this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorJson);
                String message = jsonObject.optString("message", errorJson);
                Toast.makeText(this, "서버 에러: " + message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "네트워크 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
}