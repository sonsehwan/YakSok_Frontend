package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.FriendListAdapter;
import com.example.medication.model.response.UserResponse;

import java.util.ArrayList;

public class FriendList extends AppCompatActivity {

    private RecyclerView rvYaksokList;
    private FriendListAdapter adapter;

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_list);

        initViews();
        setRecyclerView();
//        fetchFriendList();
    }

    private void setRecyclerView() {
        rvYaksokList = findViewById(R.id.rv_yaksok_list);
        rvYaksokList.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 초기화 (클릭 리스너를 통해 다이얼로그 호출)
        adapter = new FriendListAdapter(new ArrayList<>(), new FriendListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserResponse user, int position) {
                Intent intent = new Intent(FriendList.this, WipActivity.class);
                startActivity(intent);
            }
        });
        rvYaksokList.setAdapter(adapter);
    }

    private void initViews() {
        btnBack = findViewById(R.id.iv_back);
    }


        // 약속 리스트 데이터 불러오기
//    private void fetchFriendList() {
//        YaksokApi api = NetworkClient.getFriendApi();
//
//        String userEmail = SprefsManager.getUserEmail(this);
//
//        api.getYaksokList(userEmail).enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
//            @Override
//            public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Yaksok> yaksokList = response.body().getData();
//                    if (yaksokList != null) {
//                        adapter.updateData(yaksokList);
//                    }
//                } else {
//                    Toast.makeText(FriendList.this, "약속 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse<List<Yaksok>>> call, Throwable t) {
//                Log.e("YaksokList", "API 통신 실패: " + t.getMessage());
//                Toast.makeText(FriendList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}