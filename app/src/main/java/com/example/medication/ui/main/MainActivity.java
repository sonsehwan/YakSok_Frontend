package com.example.medication.ui.main;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.medication.ChatRoomList;
import com.example.medication.DrugStoreList;
import com.example.medication.FriendList;
import com.example.medication.R;
import com.example.medication.Settings;
import com.example.medication.ui.yaksoklist.YaksokList;
import com.example.medication.adapter.NotificationMultiViewAdapter;
import com.example.medication.model.NotificationListItem;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.example.medication.util.YaksokEventBus;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.kakao.sdk.common.util.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements YaksokEventBus.Listener {

    private TextView tvDate, tvGreeting, tvSummary, tvProgressPercent;
    private ProgressBar progressMain;
    private RecyclerView rvNotification;
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageView ivMenu, ivPrevDate, ivNextDate, ivCalendar;
    private Calendar selectedCalendar;

    private NotificationMultiViewAdapter adapter;

    private List<NotificationYaksok> notificationYaksokList; //현재 날짜에 해당하는 알림 목록
    private List<NotificationYaksok> allNotifications; // 전체 알림 목록

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        InsetsUtil.applySystemBarPadding(findViewById(R.id.main_root));

        initViews();
        selectedCalendar = Calendar.getInstance();
        updateDateHeader();
        setupDateNavigation();
        setNickName();

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("KEY_HASH i9nQaOt3ocAUy+P1UAWmDGU7niY=", keyHash);

        allNotifications = new ArrayList<>();
        notificationYaksokList = new ArrayList<>();
        rvNotification.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.ItemAnimator animator = rvNotification.getItemAnimator();
        if(animator instanceof SimpleItemAnimator){
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        setupDrawer();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(MainActivity.this, YaksokList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }else if(itemId == R.id.nav_chat){
                Intent intent = new Intent(MainActivity.this, ChatRoomList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }else if (itemId == R.id.nav_friend){
                Intent intent = new Intent(MainActivity.this, FriendList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        selectedCalendar = Calendar.getInstance();
        updateDateHeader();
        loadNotificationList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        YaksokEventBus.get().subscribe(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        YaksokEventBus.get().unsubscribe(this);
    }

    @Override
    public void onYaksokDataChanged(){
        loadNotificationList();
    }

    private void setNickName(){
        String nickName = SprefsManager.getUserNickName(this);
        tvGreeting.setText(nickName +"님!");
    }

    private void setupDateNavigation() {
        ivPrevDate.setOnClickListener(v->{
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1);
            onDateChanged();
        });

        ivNextDate.setOnClickListener(v->{
            selectedCalendar.add(Calendar.DAY_OF_MONTH, +1);
            onDateChanged();
        });

        ivCalendar.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(year, month, dayOfMonth);
                onDateChanged();
            }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void onDateChanged(){
        updateDateHeader();
        renderForSelectedDate();
    }

    private void updateDateHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN);
        tvDate.setText(sdf.format(selectedCalendar.getTime()));
    }

    private String getSelectedDateString(){
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
    }

    private void loadNotificationList(){
        notificationYaksokList.clear();
        Long userId = SprefsManager.getUserId(this); // 로그인한 유저의 id 정보

        NetworkClient.getYaksokApi().getNotifications(userId) // 해당 유저가 생성한 알림 정보를 DB에서 받아서 로컬에 저장
                .enqueue(new Callback<ApiResponse<List<NotificationYaksok>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<NotificationYaksok>>> call, Response<ApiResponse<List<NotificationYaksok>>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            ApiResponse<List<NotificationYaksok>> result = response.body();
                            List<NotificationYaksok> notifications = result.getData();
                            SprefsManager.setNotifications(MainActivity.this, notifications); //비동기이기 때문에 리스트를 성공적으로 저장해도 이미 리스트가 없을 때의 화면을 그려버려서 안보인다.
                            Log.d("메인화면", "성공적으로 알림 리스트를 가져왔습니다.");
                            Log.d("메인화면", "알림 리스트: " + result.getData());

                            allNotifications = notifications != null ? notifications : new ArrayList<>();

                            renderForSelectedDate();
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
        List<NotificationYaksok> savedList = SprefsManager.getNotificationList(MainActivity.this);
        allNotifications = savedList != null ? savedList : new ArrayList<>();
        renderForSelectedDate();
    }

    private void renderForSelectedDate() {
        String selectedDate = getSelectedDateString();
        notificationYaksokList.clear();

        for (NotificationYaksok item : allNotifications) {
            if (selectedDate.equals(item.getDate())) {
                notificationYaksokList.add(item);
            }
        }

        setupRecyclerView(notificationYaksokList);
        updateProgress();
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

        if(adapter == null){
            adapter = new NotificationMultiViewAdapter(notiList, this::updateProgress);
            rvNotification.setAdapter(adapter);
        }else{
            adapter.updateData(notiList);
        }
    }

    private void updateProgress() {
        int total = notificationYaksokList.size();
        int done = 0;
        for (NotificationYaksok m : notificationYaksokList) {
            if (m.isTaken()) done++;
        }
        int percent = 0;
        int visualPercent = 0;

        // 수정: total이 0일 때 발생할 수 있는 0으로 나누기 오류 방지
        if (total > 0) {
            percent = (int) (((float) done / total) * 100);
            visualPercent = percent;
        }

        if (percent == 0) {
            visualPercent = 100;
        }

        progressMain.setProgress(visualPercent);
        tvProgressPercent.setText(percent + "%");

        if (percent == 0) {
            progressMain.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_missed)));
        } else if (percent == 100) {
            progressMain.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_done)));
        } else {
            progressMain.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_pending)));
        }

        int remain = total - done;
        String todayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isToday = todayString.equals(getSelectedDateString());
        tvSummary.setText((isToday ? "오늘" : "이 날의") + " 약속은 " + remain + "건 남았어요.");

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
        bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        ivMenu = findViewById(R.id.iv_menu);
        ivPrevDate = findViewById(R.id.iv_prev_date);
        ivNextDate = findViewById(R.id.iv_next_date);
        ivCalendar = findViewById(R.id.iv_calendar);
    }

    // 상단 왼쪽 메뉴 버튼으로 사이드 메뉴를 열고, 뒤로가기로 닫는다.
    private void setupDrawer() {
        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            if (item.getItemId() == R.id.side_drugstore) {
                startActivity(new Intent(MainActivity.this, DrugStoreList.class));
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}