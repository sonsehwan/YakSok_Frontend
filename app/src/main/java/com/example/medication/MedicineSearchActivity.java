package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.MedicineAdapter;
import com.example.medication.databinding.MedicineSearchBinding;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.MedicineSearchResponse;
import com.example.medication.network.MedicineApi;
import com.example.medication.network.NetworkClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicineSearchActivity extends AppCompatActivity {

    private MedicineSearchBinding binding;
    private MedicineAdapter medicineAdapter;

    private String lastSearchedKeyword = "";
    private int currentPage = 1;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = MedicineSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        // [핵심] 화면 진입 시 검색어 없이 첫 페이지 데이터를 즉시 요청합니다.
        searchMedicine("", true);

        // 검색 버튼 클릭 이벤트 처리
        binding.btnSearch.setOnClickListener(v -> {
            String keyword = binding.etSearch.getText().toString();
            searchMedicine(keyword, true);
        });
    }

    private void setupRecyclerView() {
        medicineAdapter = new MedicineAdapter();
        binding.rvMedicine.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMedicine.setAdapter(medicineAdapter);

        medicineAdapter.setOnItemClickListener(medicine -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_MEDICINE_NAME", medicine.getName());
            resultIntent.putExtra("SELECTED_MEDICINE_IMAGE", medicine.getImage());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 무한 스크롤 리스너 추가
        binding.rvMedicine.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(layoutManager == null) return;

                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                // 리스트 바닥에서 5개 전 아이템이 보일 때 다음 페이지 로드
                if(!isLoading && lastVisibleItemPosition >= totalItemCount - 5){
                    searchMedicine(lastSearchedKeyword, false);
                }
            }
        });
    }

    private void searchMedicine(String keyword, Boolean isNewSearch){
        if(isLoading) return;
        isLoading = true;

        if(isNewSearch){
            currentPage = 1;
            lastSearchedKeyword = keyword;
            medicineAdapter.clearItems();
        } else {
            currentPage++;
        }

        // NetworkClient를 통해 생성된 API 인터페이스 가져오기
        MedicineApi api = NetworkClient.getMedicineApi();

        // 백엔드 서버에 요청 보내기
        api.searchMedicine(lastSearchedKeyword, currentPage)
                .enqueue(new Callback<ApiResponse<List<MedicineSearchResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<MedicineSearchResponse>>> call,
                                           @NonNull Response<ApiResponse<List<MedicineSearchResponse>>> response) {
                        isLoading = false;
                        if (response.isSuccessful() && response.body() != null) {
                            List<MedicineSearchResponse> data = response.body().getData();
                            if (data != null && !data.isEmpty()) {
                                medicineAdapter.addItems(data);
                            } else if (isNewSearch) {
                                Toast.makeText(MedicineSearchActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<MedicineSearchResponse>>> call, @NonNull Throwable t) {
                        isLoading = false;
                        Toast.makeText(MedicineSearchActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}