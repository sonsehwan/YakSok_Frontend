package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.MedicationAdapter;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvDate, tvGreeting, tvSummary, tvProgressPercent;
    private ProgressBar progressMain;
    private RecyclerView rvMedication;
    private FloatingActionButton fabScan;
    private BottomNavigationView bottomNav;

    private MedicationAdapter adapter;
    private List<NotificationYaksok> notificationYaksokList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupDate();
        setNickName();
        setupRecyclerView();

        fabScan.setOnClickListener(v -> {
            ShowAddMedicationList bottomSheet = new ShowAddMedicationList();
            bottomSheet.show(getSupportFragmentManager(), "show_create_list");
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(MainActivity.this, WipActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

    }

    private void setupDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN);
        tvDate.setText(sdf.format(new Date()));
    }

    private void setNickName(){
        String nickName = SprefsManager.getUserNickName(this);
        tvGreeting.setText("안녕하세요, "+ nickName +"님!");
    }

    private void setupRecyclerView() {
        notificationYaksokList = new ArrayList<>();
        // 예시 데이터
        notificationYaksokList.add(new NotificationYaksok("종합 비타민", "오전 08:30", "식후 30분", true));
        notificationYaksokList.add(new NotificationYaksok("오메가 3", "오후 01:00", "식후 즉시", false));
        notificationYaksokList.add(new NotificationYaksok("혈압약", "오후 07:00", "식전 30분", false));

        adapter = new MedicationAdapter(notificationYaksokList, (position, isDone) -> {
            updateProgress(); // 체크 상태 변경 시 상단 UI 갱신
        });

        rvMedication.setLayoutManager(new LinearLayoutManager(this));
        rvMedication.setAdapter(adapter);

        updateProgress();
    }

    private void updateProgress() {
        int total = notificationYaksokList.size();
        int done = 0;
        for (NotificationYaksok m : notificationYaksokList) {
            if (m.isTaken()) done++;
        }

        // 수정: total이 0일 때 발생할 수 있는 0으로 나누기 오류 방지
        if (total > 0) {
            int percent = (int) (((float) done / total) * 100);
            progressMain.setProgress(percent);
            tvProgressPercent.setText(percent + "%");
        } else {
            progressMain.setProgress(0);
            tvProgressPercent.setText("0%");
        }

        int remain = total - done;
        tvSummary.setText("오늘 약속은 " + remain + "건 남았어요.");
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tv_date);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvSummary = findViewById(R.id.tv_summary);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        progressMain = findViewById(R.id.progress_main);
        rvMedication = findViewById(R.id.rv_medication);
        fabScan = findViewById(R.id.fab_scan);
        bottomNav = findViewById(R.id.bottom_navigation);
    }
}