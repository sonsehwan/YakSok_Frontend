package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.NotificationMultiViewAdapter;
import com.example.medication.model.NotificationListItem;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvDate, tvGreeting, tvSummary, tvProgressPercent;
    private ProgressBar progressMain;
    private RecyclerView rvNotification;
    private FloatingActionButton fabScan;
    private BottomNavigationView bottomNav;

    private NotificationMultiViewAdapter adapter;
    private List<NotificationYaksok> notificationYaksokList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupDate();
        setNickName();

        notificationYaksokList = new ArrayList<>();
        rvNotification.setLayoutManager(new LinearLayoutManager(this));

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

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicationList();
    }

    private void setupDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN);
        tvDate.setText(sdf.format(new Date()));
    }

    private void setNickName(){
        String nickName = SprefsManager.getUserNickName(this);
        tvGreeting.setText("안녕하세요, "+ nickName +"님!");
    }

    private void setupRecyclerView(List<NotificationYaksok> notifications) {

        List<NotificationListItem> notiList = new ArrayList<>();

        List<NotificationYaksok> morning = new ArrayList<>();
        List<NotificationYaksok> lunch = new ArrayList<>();
        List<NotificationYaksok> dinner = new ArrayList<>();

        for(NotificationYaksok n : notifications){
            String category = n.getTimeCategory();
            if(category != null){
                if(category.equalsIgnoreCase("아침")){
                    morning.add(n);
                }else if(category.equalsIgnoreCase("점심")){
                    lunch.add(n);
                }else if(category.equalsIgnoreCase("저녁")){
                    dinner.add(n);
                }
            }
        }

        if(!morning.isEmpty()){
            notiList.add(new NotificationListItem.HeaderItem("아침", "아침"));
            for(NotificationYaksok n : morning) notiList.add(new NotificationListItem.NotificationItem(n));
        }
        if(!lunch.isEmpty()){
            notiList.add(new NotificationListItem.HeaderItem("점심", "점심"));
            for(NotificationYaksok n : lunch) notiList.add(new NotificationListItem.NotificationItem(n));
        }
        if(!dinner.isEmpty()){
            notiList.add(new NotificationListItem.HeaderItem("저녁", "저녁"));
            for(NotificationYaksok n : dinner) notiList.add(new NotificationListItem.NotificationItem(n));
        }

        adapter = new NotificationMultiViewAdapter(notiList, this::updateProgress);
        rvNotification.setAdapter(adapter);
    }

    private void loadMedicationList(){
        notificationYaksokList.clear();
        String userEmail = SprefsManager.getUserEmail(this); // 로그인한 유저의 이메일 정보

        NetworkClient.getYaksokApi().getNotifications(userEmail) // 해당 유저가 생성한 알림 정보를 DB에서 받아서 로컬에 저장
                .enqueue(new Callback<ApiResponse<List<NotificationYaksok>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<NotificationYaksok>>> call, Response<ApiResponse<List<NotificationYaksok>>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            ApiResponse<List<NotificationYaksok>> result = response.body();
                            List<NotificationYaksok> notifications = result.getData();
                            SprefsManager.setNotifications(MainActivity.this, notifications); //비동기이기 때문에 리스트를 성공적으로 저장해도 이미 리스트가 없을 때의 화면을 그려버려서 안보인다.
                            Log.d("메인화면", "성공적으로 알림 리스트를 가져왔습니다.");
                            Log.d("메인화면", "알림 리스트: " + result.getData());

                            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            if (notifications != null && !notifications.isEmpty()) {
                                for (NotificationYaksok item : notifications) {
                                    if (today.equals(item.getDate())) {
                                        notificationYaksokList.add(item);
                                    }
                                }
                            }

                            setupRecyclerView(notificationYaksokList);
                            updateProgress();

                        }else{
                            Log.e("메인화면 에러", "알림 리스트를 가져오는데 실패하였습니다." + response.code() + " 메시지: " + response.message());
                            showToast("알림 리스트를 가져오는데 실패하였습니다.");

                            loadFromLocalFallback();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<NotificationYaksok>>> call, Throwable t) {
                        Log.e("메인화면 에러","통신 실패: " + t.getMessage());

                        loadFromLocalFallback();
                    }
                });
    }

    private void loadFromLocalFallback() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 서버에서 가져오는 데 실패했으므로, 기기(로컬)에 마지막으로 저장되어 있던 리스트를 꺼냅니다.
        List<NotificationYaksok> savedList = SprefsManager.getNotificationList(MainActivity.this);

        if (savedList != null && !savedList.isEmpty()) {
            for (NotificationYaksok item : savedList) {
                if (today.equals(item.getDate())) {
                    notificationYaksokList.add(item);
                }
            }
        }

        // 로컬 데이터로 화면을 그립니다.
        setupRecyclerView(notificationYaksokList);
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

        List<NotificationYaksok> allSavedList = SprefsManager.getNotificationList(this);
        if (allSavedList != null) {
            for (NotificationYaksok todayItem : notificationYaksokList) {
                for (NotificationYaksok savedItem : allSavedList) {
                    // int와 Long 비교 주의 (entity는 int id, 서버 DTO 확인 필요)
                    if (savedItem.getId() == todayItem.getId()) {
                        savedItem.setTaken(todayItem.isTaken());
                        break;
                    }
                }
            }
            SprefsManager.setNotifications(this, allSavedList);
        }
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
        rvNotification = findViewById(R.id.rv_medication);
        fabScan = findViewById(R.id.fab_scan);
        bottomNav = findViewById(R.id.bottom_navigation);
    }
}