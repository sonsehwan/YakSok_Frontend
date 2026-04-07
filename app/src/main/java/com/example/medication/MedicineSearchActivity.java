package com.example.medication;

import android.os.Bundle;
import android.widget.Toast;

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

        binding = MedicineSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        setupRecyclerView();
    }

    private void setupRecyclerView() {
        medicineAdapter = new MedicineAdapter();

        binding.rvMedicine.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMedicine.setAdapter(medicineAdapter);

        binding.rvMedicine.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(layoutManager == null) return;

                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

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
        }else{
            currentPage++;
        }

        MedicineApi api = NetworkClient.getMedicineApi();
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
                        Toast.makeText(MedicineSearchActivity.this, "네트워크 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}