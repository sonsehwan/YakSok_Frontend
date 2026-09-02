package com.example.medication.ui.sharedyaksok;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.adapter.ShareYaksokListAdapter;
import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.example.medication.util.YaksokEventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareYaksokList extends AppCompatActivity implements YaksokEventBus.Listener{

    private ImageView ivBack;
    private TextView tvEmpty;
    private TextView tvHeaderTitle;
    private RecyclerView rvShareYaksok;

    private final List<Yaksok> items = new ArrayList<>();
    private ShareYaksokListAdapter adapter;

    private Long senderId;
    private String senderNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_share_yaksok_list);
        InsetsUtil.applySystemBarPadding(findViewById(R.id.main_root));

        if (getIntent().hasExtra("senderId")) {
            senderId = getIntent().getLongExtra("senderId", -1);
        }
        senderNickname = getIntent().getStringExtra("senderNickname");

        initViews();

        if (senderNickname != null) {
            tvHeaderTitle.setText(senderNickname + "님이 공유한 약속");
        }

        adapter = new ShareYaksokListAdapter(items, new ShareYaksokListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Yaksok yaksok) {
                Intent intent = new Intent(ShareYaksokList.this, ShareYaksokDetail.class);
                intent.putExtra(ShareYaksokDetail.EXTRA_YAKSOK_ID, yaksok.getId());
                // 이미 저장한 약속이라 상세 화면의 저장 버튼을 숨긴다.
                intent.putExtra(ShareYaksokDetail.EXTRA_ALREADY_SAVED, true);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Yaksok yaksok) {
                showRemoveConfirmDialog(yaksok);
            }
        });

        rvShareYaksok.setLayoutManager(new LinearLayoutManager(this));
        rvShareYaksok.setAdapter(adapter);

        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart(){
        super.onStart();
        YaksokEventBus.get().subscribe(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 상대방의 진행률이 바뀔 수 있으므로 화면에 들어올 때마다 다시 가져온다.
        loadSharedYaksokList();
    }

    @Override
    protected void onStop(){
        super.onStop();
        YaksokEventBus.get().unsubscribe(this);
    }

    @Override
    public void onYaksokDataChanged(){
        loadSharedYaksokList();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvEmpty = findViewById(R.id.tv_empty);
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        rvShareYaksok = findViewById(R.id.rv_share_yaksok);
    }

    private void loadSharedYaksokList() {
        Long userId = SprefsManager.getUserId(this);

        NetworkClient.getYaksokApi().getSharedYaksokList(userId, senderId)
                .enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Yaksok>>> call,
                                           @NonNull Response<ApiResponse<List<Yaksok>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Yaksok> data = response.body().getData();

                            items.clear();
                            if (data != null) {
                                items.addAll(data);
                            }
                            adapter.notifyDataSetChanged();

                            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Log.e("공유약속목록", "조회 실패: " + response.code());
                            showToast("공유 약속 목록을 가져오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Yaksok>>> call, Throwable t) {
                        Log.e("공유약속목록", "통신 실패: " + t.getMessage());
                        showToast("네트워크 오류가 발생했습니다.");
                    }
                });
    }

    private void showRemoveConfirmDialog(Yaksok yaksok) {
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
                            showToast("목록에서 삭제.");
                            loadSharedYaksokList();
                        } else {
                            showToast("목록에서 삭제 실패.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("공유약속목록", "삭제 통신 실패: " + t.getMessage());
                        showToast("네트워크 오류가 발생했습니다.");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}