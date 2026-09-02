package com.example.medication.ui.finddrugstore;

import static com.example.medication.util.SprefsManager.getUser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.example.medication.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medication.LoadingDialog;
import com.example.medication.R;
import com.example.medication.adapter.FindDrugStoreAdapter;
import com.example.medication.databinding.ActivityFindDrugstoreBinding;
import com.example.medication.model.SearchDrugStore;
import com.example.medication.model.request.CreateDrugStoreRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.DrugStoreApi;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.myinfo.MyInfo;
import com.example.medication.util.SprefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindDrugStore extends BaseActivity {
    ActivityFindDrugstoreBinding binding;
    private FindDrugStoreAdapter adapter;
    private LoadingDialog loadingDialog;
    private String type;

    private static final int[] SIGUNGU_ARRAYS = {
            0,                         // 0: 시/도 선택
            R.array.sigungu_gangwon,   // 1: 강원특별자치도
            R.array.sigungu_gyeonggi,  // 2: 경기도
            R.array.sigungu_gyeongnam, // 3: 경상남도
            R.array.sigungu_gyeongbuk, // 4: 경상북도
            R.array.sigungu_gwangju,   // 5: 광주광역시
            R.array.sigungu_daegu,     // 6: 대구광역시
            R.array.sigungu_daejeon,   // 7: 대전광역시
            R.array.sigungu_busan,     // 8: 부산광역시
            R.array.sigungu_seoul,     // 9: 서울특별시
            R.array.sigungu_sejong,    // 10: 세종특별자치시
            R.array.sigungu_ulsan,     // 11: 울산광역시
            R.array.sigungu_incheon,   // 12: 인천광역시
            R.array.sigungu_jeonnam,   // 13: 전라남도
            R.array.sigungu_jeonbuk,   // 14: 전북특별자치도
            R.array.sigungu_jeju,      // 15: 제주특별자치도
            R.array.sigungu_chungnam,  // 16: 충청남도
            R.array.sigungu_chungbuk   // 17: 충청북도
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindDrugstoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        type = getIntent().getStringExtra("INTENT_TYPE");

        setRecyclerView();
        setupAddressSpinners();

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnFindDrugstore.setOnClickListener(v -> {
            // 회원가입 중에는 아직 로그인된 유저가 없으므로 역할 검사를 건너뛴다.
            if (!"sign_up".equals(type)) {
                UserResponse currentUser = getUser(this);

                // 약국 회원만 약국을 등록할 수 있다.
                if (currentUser == null || !Objects.equals(currentUser.getRole(), "DRUGSTORE")) {
                    showToast("유저회원은 약국 등록을 할 수 없습니다.");
                    return;
                }
            }

            int sidoPosition = binding.spFirstAddress.getSelectedItemPosition();

            if (sidoPosition == 0 || binding.spSecondAddress.getSelectedItem() == null) {
                showToast("지역을 선택해주세요.");
                return;
            }

            String firstAddress = binding.spFirstAddress.getSelectedItem().toString();
            String secondAddress = binding.spSecondAddress.getSelectedItem().toString();
            // 약국 이름은 선택 입력이다. 비우면 해당 지역 전체가 조회된다.
            String drugstoreName = binding.etDrugstoreName.getText().toString().trim();

            getSearchResult(firstAddress, secondAddress, drugstoreName);
        });
    }
    private void setRecyclerView(){
        adapter = new FindDrugStoreAdapter(new ArrayList<>(), this::showDialog);

        binding.rvDrugstoreList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDrugstoreList.setAdapter(adapter);
    }

    // 시/도를 고르면 그에 맞는 시군구 목록으로 두 번째 스피너를 다시 채운다.
    private void setupAddressSpinners(){
        ArrayAdapter<CharSequence> sidoAdapter = ArrayAdapter.createFromResource(
                this, R.array.sido_list, R.layout.item_spinner);
        sidoAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spFirstAddress.setAdapter(sidoAdapter);

        binding.spFirstAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    binding.spSecondAddress.setAdapter(null);
                    return;
                }

                ArrayAdapter<CharSequence> sigunguAdapter = ArrayAdapter.createFromResource(
                        FindDrugStore.this, SIGUNGU_ARRAYS[position], R.layout.item_spinner);
                sigunguAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                binding.spSecondAddress.setAdapter(sigunguAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
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
                    if ("sign_up".equals(type)) {
                        // 회원가입 중에는 계정이 아직 없으므로 API 호출 없이 선택 결과만 돌려준다.
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CHOiCE_DRUGSTORE", drugStore);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        choiceDrugStoreAndCallApi(drugStore);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void choiceDrugStoreAndCallApi(SearchDrugStore drugStore){
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show();

        Long userId = SprefsManager.getUserId(this);

        CreateDrugStoreRequest request = new CreateDrugStoreRequest(userId, drugStore);

        DrugStoreApi api = NetworkClient.getDrugStoreApi();
        api.modifyDrugStore(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                loadingDialog.dismiss();

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

                    Intent intent = new Intent(FindDrugStore.this, MyInfo.class);
                    startActivity(intent);
                    finish();
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
