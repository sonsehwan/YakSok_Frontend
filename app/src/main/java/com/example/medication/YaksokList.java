package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.YaksokListAdapter;
import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YaksokList extends AppCompatActivity {

    private RecyclerView rvYaksokList;
    private YaksokListAdapter adapter;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_yaksok_list);

        initViews();
        fetchYaksokList();
    }

    private void initViews() {
        // 뒤로가기 버튼 설정
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 리사이클러뷰 설정
        rvYaksokList = findViewById(R.id.rv_yaksok_list);
        rvYaksokList.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 초기화 (클릭 리스너를 통해 다이얼로그 호출)
        adapter = new YaksokListAdapter(new ArrayList<>(), (yaksok, position) -> {
            showDeleteConfirmDialog(yaksok, position);
        });
        rvYaksokList.setAdapter(adapter);

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
            }
            return false;
        });
    }

    // 약속 리스트 데이터 불러오기
    private void fetchYaksokList() {
        YaksokApi api = NetworkClient.getYaksokApi();

        String userEmail = SprefsManager.getUserEmail(this);

        api.getYaksokList(userEmail).enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
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

    // 아이템 삭제 확인 다이얼로그 띄우기
    private void showDeleteConfirmDialog(Yaksok yaksok, int position) {
        new AlertDialog.Builder(this)
                .setTitle("약속 삭제")
                .setMessage("'" + yaksok.getTitle() + "' 약속을 삭제하시겠습니까?\n관련된 모든 정보(복약, 알림)가 함께 삭제됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteYaksokFromServer(yaksok.getId(), position);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 서버에 삭제 요청 후 UI 갱신
    private void deleteYaksokFromServer(Long yaksokId, int position) {
        YaksokApi api = NetworkClient.getYaksokApi();

        api.deleteYaksok(yaksokId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    adapter.removeItem(position); // 성공 시 리스트 UI에서 바로 제거
                    Toast.makeText(YaksokList.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(YaksokList.this, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("YaksokList", "삭제 통신 실패: " + t.getMessage());
                Toast.makeText(YaksokList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}