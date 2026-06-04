package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.DrugStoreAdapter;
import com.example.medication.model.DrugStore;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.DrugStoreApi;
import com.example.medication.network.NetworkClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrugStoreList extends AppCompatActivity {

    private RecyclerView rvDrugstoreList;
    private DrugStoreAdapter adapter;
    private ImageView ivBack;
    BottomNavigationView bottomNavigationView;

    private LoadingDialog loadingDialog;

    // 하드코딩된 내 위치 (예: 서울시청)
    private double currentLat = 37.5220056247758;
    private double currentLng = 126.83028754332358;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.drug_store_list);

        initViews();
        setupListeners();

        loadingDialog = new LoadingDialog(this);

        // 화면이 켜지면 하드코딩된 위치 값으로 즉시 약국 목록을 가져옵니다.
        loadingDialog.show();
        fetchDrugStoresFromServer();

        bottomNavigationView.setSelectedItemId(R.id.nav_drugstore);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(DrugStoreList.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(DrugStoreList.this, YaksokList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(DrugStoreList.this, Settings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }else return itemId == R.id.nav_drugstore;
        });
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvDrugstoreList = findViewById(R.id.rv_drugstore_list);
        rvDrugstoreList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DrugStoreAdapter(new ArrayList<>(), (drugStore, position) -> {
            Toast.makeText(DrugStoreList.this, drugStore.getDutyName() + " 약국을 선택했습니다.", Toast.LENGTH_SHORT).show();
        });
        rvDrugstoreList.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void fetchDrugStoresFromServer() {
        DrugStoreApi api = NetworkClient.getDrugStoreApi();

        api.getCloseDrugstores(String.valueOf(currentLat), String.valueOf(currentLng))
                .enqueue(new Callback<ApiResponse<List<DrugStore>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<DrugStore>>> call, @NonNull Response<ApiResponse<List<DrugStore>>> response) {
                        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            List<DrugStore> apiList = response.body().getData();
                            if (apiList != null && !apiList.isEmpty()) {
                                displayDrugStores(apiList); // 리사이클러뷰에 데이터 채워넣기
                            } else {
                                Toast.makeText(DrugStoreList.this, "주변에 문을 연 약국이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DrugStoreList.this, "서버에서 약국 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<DrugStore>>> call, @NonNull Throwable t) {
                        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                        Log.e("DrugStoreList", "API Error: " + t.getMessage());
                        Toast.makeText(DrugStoreList.this, "네트워크 통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayDrugStores(List<DrugStore> apiList) {
        if (adapter != null) {
            adapter.updateData(apiList);
        }
    }
}