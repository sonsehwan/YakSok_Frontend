package com.example.medication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.YaksokListAdapter;
import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.util.SprefsManager;

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        fetchYaksokList();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish()); // 뒤로가기 버튼 동작

        rvYaksokList = findViewById(R.id.rv_yaksok_list);
        rvYaksokList.setLayoutManager(new LinearLayoutManager(this));

        // 빈 리스트로 초기 어댑터 설정
        adapter = new YaksokListAdapter(new ArrayList<>());
        rvYaksokList.setAdapter(adapter);
    }

    private void fetchYaksokList() {
        YaksokApi api = NetworkClient.getYaksokApi();

        // SprefsManager에서 이메일 가져오기 (실제 구현된 메서드명에 맞춰 수정 필요)
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
}