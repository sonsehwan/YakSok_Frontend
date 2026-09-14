package com.example.medication.ui.main;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.medication.R;
import com.example.medication.adapter.NotificationMultiViewAdapter;
import com.example.medication.model.NotificationListItem;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.example.medication.util.YaksokEventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements YaksokEventBus.Listener {

    private TextView tvDate, tvGreeting, tvSummary, tvProgressPercent;
    private ProgressBar progressMain;
    private RecyclerView rvNotification;
    private ImageView ivMenu, ivPrevDate, ivNextDate, ivCalendar;
    private Calendar selectedCalendar;

    private NotificationMultiViewAdapter adapter;

    private List<NotificationYaksok> notificationYaksokList;
    private List<NotificationYaksok> allNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        InsetsUtil.applySystemBarPadding(view.findViewById(R.id.main_root));

        selectedCalendar = Calendar.getInstance();
        updateDateHeader();
        setupDateNavigation();
        setNickName();

        allNotifications = new ArrayList<>();
        notificationYaksokList = new ArrayList<>();
        rvNotification.setLayoutManager(new LinearLayoutManager(requireContext()));

        RecyclerView.ItemAnimator animator = rvNotification.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        ivMenu.setOnClickListener(v ->
                ((DrawerLayout) requireActivity().findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START));
    }

    @Override
    public void onResume() {
        super.onResume();
        selectedCalendar = Calendar.getInstance();
        updateDateHeader();
        loadNotificationList();
    }

    @Override
    public void onStart() {
        super.onStart();
        YaksokEventBus.get().subscribe(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        YaksokEventBus.get().unsubscribe(this);
    }

    @Override
    public void onYaksokDataChanged() {
        loadNotificationList();
    }

    private void setNickName() {
        String nickName = SprefsManager.getUserNickName(requireContext());
        tvGreeting.setText(nickName + "님!");
    }

    private void setupDateNavigation() {
        ivPrevDate.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1);
            onDateChanged();
        });

        ivNextDate.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, +1);
            onDateChanged();
        });

        ivCalendar.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(year, month, dayOfMonth);
                onDateChanged();
            }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void onDateChanged() {
        updateDateHeader();
        renderForSelectedDate();
    }

    private void updateDateHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN);
        tvDate.setText(sdf.format(selectedCalendar.getTime()));
    }

    private String getSelectedDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
    }

    private void loadNotificationList() {
        notificationYaksokList.clear();

        NetworkClient.getYaksokApi().getNotifications()
                .enqueue(new Callback<ApiResponse<List<NotificationYaksok>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<NotificationYaksok>>> call, Response<ApiResponse<List<NotificationYaksok>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<NotificationYaksok> notifications = response.body().getData();
                            SprefsManager.setNotifications(requireContext(), notifications);
                            allNotifications = notifications != null ? notifications : new ArrayList<>();
                            renderForSelectedDate();
                        } else {
                            Log.e("메인화면 에러", "알림 리스트를 가져오는데 실패하였습니다." + response.code() + " 메시지: " + response.message());
                            showToast("알림 리스트를 가져오는데 실패하였습니다.");
                            loadFromLocalFallback();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<NotificationYaksok>>> call, Throwable t) {
                        Log.e("메인화면 에러", "통신 실패: " + t.getMessage());
                        loadFromLocalFallback();
                    }
                });
    }

    private void loadFromLocalFallback() {
        List<NotificationYaksok> savedList = SprefsManager.getNotificationList(requireContext());
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

        for (NotificationYaksok n : notifications) {
            String category = n.getTimeCategory();
            if (category != null) {
                if (category.equalsIgnoreCase("아침")) morning.add(n);
                else if (category.equalsIgnoreCase("점심")) lunch.add(n);
                else if (category.equalsIgnoreCase("저녁")) dinner.add(n);
            }
        }

        if (!morning.isEmpty()) {
            notiList.add(new NotificationListItem.HeaderItem("아침", "아침"));
            for (NotificationYaksok n : morning) notiList.add(new NotificationListItem.NotificationItem(n));
        }
        if (!lunch.isEmpty()) {
            notiList.add(new NotificationListItem.HeaderItem("점심", "점심"));
            for (NotificationYaksok n : lunch) notiList.add(new NotificationListItem.NotificationItem(n));
        }
        if (!dinner.isEmpty()) {
            notiList.add(new NotificationListItem.HeaderItem("저녁", "저녁"));
            for (NotificationYaksok n : dinner) notiList.add(new NotificationListItem.NotificationItem(n));
        }

        if (adapter == null) {
            adapter = new NotificationMultiViewAdapter(notiList, this::updateProgress);
            rvNotification.setAdapter(adapter);
        } else {
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
        if (total > 0) {
            percent = (int) (((float) done / total) * 100);
            visualPercent = percent;
        }
        if (percent == 0) visualPercent = 100;

        progressMain.setProgress(visualPercent);
        tvProgressPercent.setText(percent + "%");

        if (percent == 0) {
            progressMain.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_missed)));
        } else if (percent == 100) {
            progressMain.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_done)));
        } else {
            progressMain.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_pending)));
        }

        int remain = total - done;
        String todayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isToday = todayString.equals(getSelectedDateString());
        tvSummary.setText((isToday ? "오늘" : "이 날의") + " 약속은 " + remain + "건 남았어요.");

        List<NotificationYaksok> allSavedList = SprefsManager.getNotificationList(requireContext());
        if (allSavedList != null) {
            for (NotificationYaksok todayItem : notificationYaksokList) {
                for (NotificationYaksok savedItem : allSavedList) {
                    if (Objects.equals(savedItem.getId(), todayItem.getId())) {
                        savedItem.setTaken(todayItem.isTaken());
                        break;
                    }
                }
            }
            SprefsManager.setNotifications(requireContext(), allSavedList);
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void initViews(View root) {
        tvDate = root.findViewById(R.id.tv_date);
        tvGreeting = root.findViewById(R.id.tv_greeting);
        tvSummary = root.findViewById(R.id.tv_summary);
        tvProgressPercent = root.findViewById(R.id.tv_progress_percent);
        progressMain = root.findViewById(R.id.progress_main);
        rvNotification = root.findViewById(R.id.rv_medication);
        ivMenu = root.findViewById(R.id.iv_menu);
        ivPrevDate = root.findViewById(R.id.iv_prev_date);
        ivNextDate = root.findViewById(R.id.iv_next_date);
        ivCalendar = root.findViewById(R.id.iv_calendar);
    }
}