package com.example.medication;

import static com.example.medication.util.SprefsManager.getUser;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medication.adapter.FindDrugStoreAdapter;
import com.example.medication.databinding.ActivityFindDrugstoreBinding;
import com.example.medication.model.SearchDrugStore;
import com.example.medication.model.request.CreateDrugStoreRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.DrugStoreApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.SprefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindDrugStore extends AppCompatActivity {
    ActivityFindDrugstoreBinding binding;
    private FindDrugStoreAdapter adapter;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindDrugstoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setRecyclerView();

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnFindDrugstore.setOnClickListener(v -> {
            String firstAddress = binding.etFirstAddress.getText().toString().trim();
            String secondAddress = binding.etSecondAddress.getText().toString().trim();
            String drugstoreName = binding.etDrugstoreName.getText().toString().trim();

            if(!firstAddress.isEmpty() && !secondAddress.isEmpty() && !drugstoreName.isEmpty()){
                getSearchResult(firstAddress, secondAddress, drugstoreName);
            }else{
                showToast("모든 정보를 입력하세요.");
            }
        });
    }
    private void setRecyclerView(){
        adapter = new FindDrugStoreAdapter(new ArrayList<>(), this::showDialog);

        binding.rvDrugstoreList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDrugstoreList.setAdapter(adapter);
    }
    private void getSearchResult(String firstAddress, String secondAddress, String drugstoreName){
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show();

        DrugStoreApi api = NetworkClient.getDrugStoreApi();
        api.getSearchDrugstores(firstAddress, secondAddress, drugstoreName).enqueue(new Callback<ApiResponse<List<SearchDrugStore>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SearchDrugStore>>> call, Response<ApiResponse<List<SearchDrugStore>>> response) {
                loadingDialog.dismiss(); // 통신이 끝나면 로딩창 닫기

                Log.d("DrugStoreAPI", "요청 URL: " + call.request().url());
                Log.d("DrugStoreAPI", "응답 코드: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<SearchDrugStore>> apiResponse = response.body();
                    List<SearchDrugStore> drugStores = apiResponse.getData(); // 응답 구조에서 List 꺼내기

                    if (drugStores != null && !drugStores.isEmpty()) {
                        adapter.updateData(drugStores); // 어댑터에 데이터 전달하여 리스트 갱신
                    } else {
                        showToast("조건에 맞는 약국을 찾을 수 없습니다.");
                        adapter.updateData(new ArrayList<>()); // 목록 비우기
                    }
                } else {
                    showToast("서버로부터 결과를 가져오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SearchDrugStore>>> call, Throwable t) {
                loadingDialog.dismiss(); // 통신 실패 시 로딩창 닫기
                showToast("네트워크 통신 오류: " + t.getMessage());
            }
        });
    }

    private void showDialog(SearchDrugStore drugStore) {
        new AlertDialog.Builder(this)
                .setTitle(drugStore.getDutyName() + " 선택")
                .setMessage(drugStore.getDutyName() +"을 선택하시겠습니까?")
                .setPositiveButton("선택", (dialog, which) -> {
                    choiceDrugStoreAndCallApi(drugStore);
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void choiceDrugStoreAndCallApi(SearchDrugStore drugStore){
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show();

        String email = SprefsManager.getUserEmail(this);

        CreateDrugStoreRequest request = new CreateDrugStoreRequest(email, drugStore);

        DrugStoreApi api = NetworkClient.getDrugStoreApi();
        api.createDrugStore(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                loadingDialog.dismiss(); // 통신이 끝나면 로딩창 닫기

                Log.d("DrugStoreAPI", "요청 URL: " + call.request().url());
                Log.d("DrugStoreAPI", "응답 코드: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserResponse> apiResponse = response.body();
                    UserResponse user = apiResponse.getData(); // 응답 구조에서 유저 꺼내기
                    if(user == null){
                        Log.d("DrugStoreAPI", "유저 정보가 없습니다.");
                        return;
                    }

                    // 로컬에 유저 정보 갱신
                    SprefsManager.setUserInfo(FindDrugStore.this, user);
                    UserResponse saveUser = getUser(FindDrugStore.this);
                    String userHdip = saveUser.getMyDrugStore().getHpid();
                    Log.d("새로 저장한 유저", userHdip);
                } else {
                    showToast("서버로부터 결과를 가져오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                loadingDialog.dismiss(); // 통신 실패 시 로딩창 닫기
                showToast("네트워크 통신 오류: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
