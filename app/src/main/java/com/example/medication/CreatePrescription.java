package com.example.medication;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePrescription extends AppCompatActivity {

    private ActivityResultLauncher<String> galleryLauncher;
    private EditText etMedicineName;
    private EditText etPrescriptionDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_direct_schedule);

//        initViews();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d("MLKit", "사진 주소 가져오기 성공: " + uri);
                        recognizeTextFromImage(uri);
                    } else {
                        Toast.makeText(this, "사진을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );

        galleryLauncher.launch("image/*");
    }

    private void initViews() {
        etMedicineName = findViewById(R.id.input_card_title);
        etPrescriptionDays = findViewById(R.id.input_prescriptionDays);
    }

    private void recognizeTextFromImage(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

            recognizer.process(image)
                    // 주의: 이제 문자열(String)이 아니라 Text 객체 전체를 넘깁니다! (좌표를 쓰기 위해)
                    .addOnSuccessListener(visionText -> {
                        Log.d("MLKit", "🎉 텍스트 인식 성공!");
                        Toast.makeText(this, "처방전 분석을 시작합니다.", Toast.LENGTH_SHORT).show();

                        processExtractedDataWithLocation(visionText);
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

    private void processExtractedDataWithLocation(Text visionText) {
        // 1. 사진 안의 모든 '줄(Line)' 데이터 모으기
        List<Text.Line> allLines = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            allLines.addAll(block.getLines());
        }

        // 2. Y좌표(위치)를 기준으로 위에서 아래로 정렬하기
        Collections.sort(allLines, (l1, l2) -> {
            if (l1.getBoundingBox() == null || l2.getBoundingBox() == null) return 0;
            return Integer.compare(l1.getBoundingBox().centerY(), l2.getBoundingBox().centerY());
        });

        // 3. Y좌표가 비슷한(오차 30픽셀 이내) 줄끼리 묶어서 가로 행(Row) 만들기
        List<List<Text.Line>> rows = new ArrayList<>();
        List<Text.Line> currentRow = new ArrayList<>();
        int currentY = -1;

        for (Text.Line line : allLines) {
            if (line.getBoundingBox() == null) continue;
            int y = line.getBoundingBox().centerY();

            if (currentY == -1 || Math.abs(y - currentY) < 30) {
                // 같은 행에 속함
                currentRow.add(line);
                currentY = (currentY == -1) ? y : (currentY + y) / 2; // Y값 평균 업데이트
            } else {
                // 새로운 행 시작
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                currentRow.add(line);
                currentY = y;
            }
        }
        if (!currentRow.isEmpty()) rows.add(currentRow);

        // 4. 각 행 내에서 X좌표(가로 위치) 기준으로 왼쪽부터 오른쪽으로 정렬
        for (List<Text.Line> row : rows) {
            Collections.sort(row, (l1, l2) -> {
                if (l1.getBoundingBox() == null || l2.getBoundingBox() == null) return 0;
                return Integer.compare(l1.getBoundingBox().left, l2.getBoundingBox().left);
            });
        }

        // 5. 복원된 표 데이터를 바탕으로 약품 및 투약 정보 추출
        int medicineCount = 0;
        StringBuilder titleBuilder = new StringBuilder();
        String globalDays = "";

        Pattern planAPattern = Pattern.compile("\\[?(급여|비급여)\\]?\\s*\\[?[A-Za-z0-9-]*\\]?\\s*([^\\(]+)");
        Pattern daysPattern = Pattern.compile("교부일로부터\\s*\\(?(\\d+)\\)?\\s*일간");

        for (List<Text.Line> row : rows) {
            StringBuilder rowTextBuilder = new StringBuilder();

            // 이 가로줄의 모든 텍스트를 " | " 로 구분해서 합칩니다.
            for (Text.Line line : row) {
                rowTextBuilder.append(line.getText().trim()).append(" | ");
            }
            String rowText = rowTextBuilder.toString();
            Log.d("MLKit_Row", "재구성된 행: " + rowText);

            // 해당 행이 약품명 행인지 검사
            Matcher matcherA = planAPattern.matcher(rowText);
            if (matcherA.find()) {
                String medName = matcherA.group(2).trim();

                // 약품명 뒤에 나오는 나머지 문자열만 자르기 (예: "(에페리손염산 1 | 3 | 7 |")
                String afterMed = rowText.substring(matcherA.end());

                // 공백 기준으로 잘라서 "순수 숫자"만 리스트에 수집
                List<String> numbersInRow = new ArrayList<>();
                for (String token : afterMed.split("[\\s|]+")) {
                    if (token.matches("^\\d+(\\.\\d+)?$")) { // 숫자(또는 소수점)만 있는 경우
                        numbersInRow.add(token);
                    }
                }

                // 순서대로 1회 투약량, 1일 투여횟수, 총 투약일수에 매핑
                String dose = numbersInRow.size() > 0 ? numbersInRow.get(0) : "1";
                String freq = numbersInRow.size() > 1 ? numbersInRow.get(1) : "1";
                String days = numbersInRow.size() > 2 ? numbersInRow.get(2) : "1";

                Log.d("MLKit_Parsed", "💊 추출완료 -> 약품: " + medName + " | 1회: " + dose + " | 1일: " + freq + "번 | 총: " + days + "일");

                if (medicineCount == 0) titleBuilder.append(medName);
                medicineCount++;

                // TODO: 여기서 뽑아낸 개별 dose, freq를 바탕으로 Yaksok 객체의 Pill 리스트를 만들면 됩니다!
            }

            // 약품 행이 아닐 경우 전역 투약일수 검사 (예: 교부일로부터 7일간)
            Matcher daysMatcher = daysPattern.matcher(rowText);
            if (daysMatcher.find() && globalDays.isEmpty()) {
                globalDays = daysMatcher.group(1);
            }
        }

//        // 결과 UI 세팅
//        String finalTitle = "";
//        if (medicineCount > 1) {
//            finalTitle = titleBuilder.toString() + " 외 " + (medicineCount - 1) + "건";
//        } else if (medicineCount == 1) {
//            finalTitle = titleBuilder.toString();
//        }
//
//        if (!finalTitle.isEmpty()) {
//            etMedicineName.setText(finalTitle);
//        } else {
//            etMedicineName.setHint("약 이름을 직접 입력해주세요");
//        }
//
//        if (!globalDays.isEmpty()) {
//            etPrescriptionDays.setText(globalDays);
//        }
    }
}