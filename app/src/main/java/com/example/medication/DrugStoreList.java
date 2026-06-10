package com.example.medication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.DrugStoreAdapter;
import com.example.medication.model.DrugStore;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.DrugStoreApi;
import com.example.medication.network.NetworkClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrugStoreList extends AppCompatActivity {

    private RecyclerView rvDrugstoreList;
    private DrugStoreAdapter adapter;
    private ImageView ivBack;
    private BottomNavigationView bottomNavigationView;
    private LoadingDialog loadingDialog;

    private int currentPage = 1;
    private boolean isLoading = false;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // 하드코딩된 내 위치 (예: 서울시청)
    private double currentLat;
    private double currentLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.drug_store_list);

        initViews();
        setupListeners();
        loadingDialog = new LoadingDialog(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        registerPermissionLauncher();
        checkPermissionAndFetchLocation();

        // 화면이 켜지면 하드코딩된 위치 값으로 즉시 약국 목록을 가져옵니다.
        loadingDialog.show();

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

    private void checkPermissionAndFetchLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fetchCurrentLocation();
        }else{
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void registerPermissionLauncher(){
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    if(fineLocationGranted != null && fineLocationGranted){
                        fetchCurrentLocation();
                    }else{
                        Toast.makeText(DrugStoreList.this, "위치 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                }
        );
    }

    private void fetchCurrentLocation(){
        loadingDialog.show();

        try{
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();

                            Log.d("위치 정보", "위도: " + currentLat + ", 경도: " + currentLng);

                            fetchDrugStoresFromServer(true);
                        }else{
                            loadingDialog.dismiss();
                            Toast.makeText(this, "위치 신호를 잡을 수 없습니다. 탁 트인 곳으로 이동해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(this,e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "위치 신호를 잡을 수 없습니다. 탁 트인 곳으로 이동해주세요.", Toast.LENGTH_SHORT).show();
                    });
        }catch (SecurityException e){
            e.printStackTrace();
            loadingDialog.dismiss();
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvDrugstoreList = findViewById(R.id.rv_drugstore_list);
        rvDrugstoreList.setLayoutManager(new LinearLayoutManager(this));

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvDrugstoreList.setLayoutManager(layoutManager);

        adapter = new DrugStoreAdapter(currentLat, currentLng);
        adapter.setOnItemClickListener(drugStore -> {
            Intent intent = new Intent(DrugStoreList.this, DrugStoreDetail.class);
            intent.putExtra("drugStore", drugStore);
            startActivity(intent);
        });
        rvDrugstoreList.setAdapter(adapter);

        rvDrugstoreList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();

                    // 리스트 바닥에서 5개 전 아이템이 보일 때 다음 페이지 로드
                    if (!isLoading && lastVisibleItemPosition >= totalItemCount - 5) {
                        fetchDrugStoresFromServer(false);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void fetchDrugStoresFromServer(Boolean isInitialLoad) {
        if(isLoading) return;
        isLoading = true;

        if(isInitialLoad){
            currentPage = 1;
            adapter.clearItems();
        } else {
            currentPage++;
        }

        DrugStoreApi api = NetworkClient.getDrugStoreApi();

        api.getCloseDrugstores(String.valueOf(currentLat), String.valueOf(currentLng), currentPage)
                .enqueue(new Callback<ApiResponse<List<DrugStore>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<DrugStore>>> call, @NonNull Response<ApiResponse<List<DrugStore>>> response) {
                        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();

                        isLoading = false;
                        if (response.isSuccessful() && response.body() != null) {
                            List<DrugStore> apiList = response.body().getData();
                            if (apiList != null && !apiList.isEmpty()) {

                                adapter.updateLocation(currentLat, currentLng);
                                adapter.addItems(apiList);
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
                        isLoading = false;
                        Log.e("DrugStoreList", "API Error: " + t.getMessage());
                        Toast.makeText(DrugStoreList.this, "네트워크 통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_drugstore);
    }
}