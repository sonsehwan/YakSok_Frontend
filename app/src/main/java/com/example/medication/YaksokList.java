package com.example.medication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.ShareYaksokListAdapter;
import com.example.medication.adapter.SharedUserAdapter;
import com.example.medication.adapter.YaksokListAdapter;
import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SharedUser;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YaksokList extends AppCompatActivity {

    private TextView tvMyYaksok, tvSharedYaksok;
    private RecyclerView rvYaksokList, rvSharedUserList;
    private YaksokListAdapter adapter;
    private ShareYaksokListAdapter shareYaksokListAdapter;
    private SharedUserAdapter sharedUserAdapter;
    private Long currentSenderId;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabScan;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageView ivMenu;

    private final ActivityResultLauncher<Intent> detailActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 디테일 화면에서 RESULT_OK 신호를 보내며 종료했다면 (즉, 삭제가 발생했다면)
                if (result.getResultCode() == RESULT_OK) {
                    // 리스트를 다시 서버에서 불러오거나 갱신합니다.
                    fetchYaksokList();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_yaksok_list);
        InsetsUtil.applySystemBarPadding(findViewById(R.id.main));

        initViews();
        setupDrawer();
        selectMyYaksokTab();
        // setupDrawer 는 initViews 뒤에 호출해야 뷰가 준비된 상태가 된다.

        fabScan.setOnClickListener(v -> {
            ShowAddMedicationList bottomSheet = new ShowAddMedicationList();
            bottomSheet.show(getSupportFragmentManager(), "show_create_list");
        });
    }

    @SuppressLint("CutPasteId")
    private void initViews() {

        tvMyYaksok = findViewById(R.id.tv_my_yaksok);
        tvSharedYaksok = findViewById(R.id.tv_shared_yaksok);

        // 리사이클러뷰 설정
        rvYaksokList = findViewById(R.id.rv_yaksok_list);
        rvYaksokList.setLayoutManager(new LinearLayoutManager(this));

        rvSharedUserList = findViewById(R.id.rv_share_user_list);
        rvSharedUserList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        sharedUserAdapter = new SharedUserAdapter(new ArrayList<>(), (user, position) ->
                fetchSharedYaksokBySender(user.getUserId()));
        rvSharedUserList.setAdapter(sharedUserAdapter);

        tvMyYaksok.setOnClickListener(v -> selectMyYaksokTab());
        tvSharedYaksok.setOnClickListener(v -> selectSharedYaksokTab());

        bottomNav = findViewById(R.id.bottom_navigation);
        fabScan = findViewById(R.id.fab_scan);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        ivMenu = findViewById(R.id.iv_menu);

        // 어댑터 초기화 (클릭 리스너를 통해 다이얼로그 호출)
        adapter = new YaksokListAdapter(new ArrayList<>(), new YaksokListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Yaksok yaksok, int position) {
                Intent intent = new Intent(YaksokList.this, DetailYaksok.class);

                intent.putExtra("YAKSOK_DATA", yaksok);

                detailActivityLauncher.launch(intent);
            }
        });
        rvYaksokList.setAdapter(adapter);

        // 공유받은 약속(특정 공유자 선택 시) 어댑터 초기화
        shareYaksokListAdapter = new ShareYaksokListAdapter(new ArrayList<>(), new ShareYaksokListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Yaksok yaksok) {
                Intent intent = new Intent(YaksokList.this, ShareYaksokDetail.class);
                intent.putExtra(ShareYaksokDetail.EXTRA_YAKSOK_ID, yaksok.getId());
                // 이미 저장한 약속이라 상세 화면의 저장 버튼을 숨긴다.
                intent.putExtra(ShareYaksokDetail.EXTRA_ALREADY_SAVED, true);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Yaksok yaksok) {
                showRemoveSharedConfirmDialog(yaksok);
            }
        });

        // 하단 네비게이션 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_history);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(YaksokList.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                // 현재 화면이므로 아무 동작 안 함
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(YaksokList.this, Settings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }else if(itemId == R.id.nav_chat){
                Intent intent = new Intent(YaksokList.this, ChatRoomList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }else if (itemId == R.id.nav_friend){
                Intent intent = new Intent(YaksokList.this, FriendList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    // 상단 왼쪽 메뉴 버튼으로 사이드 메뉴를 열고, 뒤로가기로 닫는다.
    private void setupDrawer() {
        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            if (item.getItemId() == R.id.side_drugstore) {
                startActivity(new Intent(YaksokList.this, DrugStoreList.class));
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

    // 약속 리스트 데이터 불러오기
    private void fetchYaksokList() {
        YaksokApi api = NetworkClient.getYaksokApi();

        Long userId = SprefsManager.getUserId(this);

        api.getYaksokList(userId).enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Yaksok> yaksokList = response.body().getData();
                    if (yaksokList != null) {
                        adapter.updateData(yaksokList);
                    }
                } else {
                    Toast.makeText(YaksokList.this, "약속 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Yaksok>>> call, Throwable t) {
                Log.e("YaksokList", "API 통신 실패: " + t.getMessage());
                Toast.makeText(YaksokList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 나에게 공유해준 사람 목록 불러오기
    private void fetchSharedUserList() {
        Long userId = SprefsManager.getUserId(this);

        NetworkClient.getYaksokApi().getSharedUserList(userId).enqueue(new Callback<ApiResponse<List<SharedUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SharedUser>>> call, Response<ApiResponse<List<SharedUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SharedUser> userList = response.body().getData();
                    if (userList != null) {
                        sharedUserAdapter.updateData(userList);
                    }
                } else {
                    Log.e("YaksokList", "공유자 목록 조회 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SharedUser>>> call, Throwable t) {
                Log.e("YaksokList", "공유자 목록 API 통신 실패: " + t.getMessage());
            }
        });
    }

    // "내 약속" 탭 선택
    private void selectMyYaksokTab() {
        tvMyYaksok.setBackgroundResource(R.drawable.bg_touch_my_yaksok_list);
        tvSharedYaksok.setBackgroundResource(R.drawable.bg_black_border);

        rvSharedUserList.setVisibility(View.GONE);
        rvYaksokList.setVisibility(View.VISIBLE);

        rvYaksokList.setAdapter(adapter);
        fetchYaksokList();
    }

    // "공유 약속" 탭 선택
    private void selectSharedYaksokTab() {
        tvSharedYaksok.setBackgroundResource(R.drawable.bg_touch_shared_yaksok_list);
        tvMyYaksok.setBackgroundResource(R.drawable.bg_black_border);

        currentSenderId = null;

        // 내 약속 목록을 지우고, 공유자를 아직 선택하지 않았으니 약속 목록도 비운다.
        shareYaksokListAdapter.updateData(new ArrayList<>());
        rvYaksokList.setAdapter(shareYaksokListAdapter);

        rvSharedUserList.setVisibility(View.VISIBLE);
        fetchSharedUserList();
    }

    // 선택한 공유자가 나에게 공유한 약속 목록만 불러오기
    private void fetchSharedYaksokBySender(Long senderId) {
        currentSenderId = senderId;
        Long userId = SprefsManager.getUserId(this);

        NetworkClient.getYaksokApi().getSharedYaksokList(userId, senderId)
                .enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Yaksok> data = response.body().getData();
                            shareYaksokListAdapter.updateData(data != null ? data : new ArrayList<>());
                        } else {
                            Log.e("YaksokList", "공유 약속 목록 조회 실패: " + response.code());
                            Toast.makeText(YaksokList.this, "공유 약속 목록을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Yaksok>>> call, Throwable t) {
                        Log.e("YaksokList", "공유 약속 목록 통신 실패: " + t.getMessage());
                        Toast.makeText(YaksokList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRemoveSharedConfirmDialog(Yaksok yaksok) {
        new AlertDialog.Builder(this)
                .setTitle("공유 목록에서 빼기")
                .setMessage("'" + yaksok.getTitle() + "'을(를) 목록에서 뺄까요?\n원본 약속은 삭제되지 않습니다.")
                .setPositiveButton("빼기", (dialog, which) -> removeSharedYaksok(yaksok.getId()))
                .setNegativeButton("취소", null)
                .show();
    }

    private void removeSharedYaksok(Long yaksokId) {
        Long userId = SprefsManager.getUserId(this);

        NetworkClient.getYaksokApi().deleteSharedYaksok(yaksokId, userId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(YaksokList.this, "목록에서 삭제.", Toast.LENGTH_SHORT).show();
                            if (currentSenderId != null) {
                                fetchSharedYaksokBySender(currentSenderId);
                            }
                        } else {
                            Toast.makeText(YaksokList.this, "목록에서 삭제 실패.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("YaksokList", "공유 약속 삭제 통신 실패: " + t.getMessage());
                        Toast.makeText(YaksokList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_history);
    }
}