package com.example.medication.ui.yaksok;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.adapter.ShareYaksokListAdapter;
import com.example.medication.adapter.SharedUserAdapter;
import com.example.medication.adapter.YaksokListAdapter;
import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SharedUser;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.ui.sharedyaksok.ShareYaksokDetail;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.example.medication.util.YaksokEventBus;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YaksokListFragment extends Fragment implements YaksokEventBus.Listener {

    private TextView tvMyYaksok, tvSharedYaksok;
    private RecyclerView rvYaksokList, rvSharedUserList;
    private YaksokListAdapter adapter;
    private ShareYaksokListAdapter shareYaksokListAdapter;
    private SharedUserAdapter sharedUserAdapter;
    private FloatingActionButton fabScan;
    private ImageView ivMenu;

    private Long currentSenderId;
    private boolean isMyYaksokTab = true;

    private final ActivityResultLauncher<Intent> detailActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fetchYaksokList();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yaksok_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsUtil.applySystemBarPadding(view.findViewById(R.id.main));

        initViews(view);
        setupDrawerButton();
        selectMyYaksokTab();

        fabScan.setOnClickListener(v -> {
            ShowAddMedicationList bottomSheet = new ShowAddMedicationList();
            bottomSheet.show(getParentFragmentManager(), "show_create_list");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCurrentTab();
    }

    @Override
    public void onStart() {
        super.onStart();
        YaksokEventBus.get().subscribe(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        YaksokEventBus.get().unsubscribe(this);
    }

    @Override
    public void onYaksokDataChanged() {
        refreshCurrentTab();
    }

    @SuppressLint("CutPasteId")
    private void initViews(View root) {
        tvMyYaksok = root.findViewById(R.id.tv_my_yaksok);
        tvSharedYaksok = root.findViewById(R.id.tv_shared_yaksok);

        rvYaksokList = root.findViewById(R.id.rv_yaksok_list);
        rvYaksokList.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvSharedUserList = root.findViewById(R.id.rv_share_user_list);
        rvSharedUserList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        sharedUserAdapter = new SharedUserAdapter(new ArrayList<>(), (user, position) ->
                fetchSharedYaksokBySender(user.getUserId()));
        rvSharedUserList.setAdapter(sharedUserAdapter);

        tvMyYaksok.setOnClickListener(v -> selectMyYaksokTab());
        tvSharedYaksok.setOnClickListener(v -> selectSharedYaksokTab());

        fabScan = root.findViewById(R.id.fab_scan);
        ivMenu = root.findViewById(R.id.iv_menu);

        adapter = new YaksokListAdapter(new ArrayList<>(), new YaksokListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Yaksok yaksok, int position) {
                Intent intent = new Intent(requireContext(), YaksokDetail.class);
                intent.putExtra("YAKSOK_DATA", yaksok);
                detailActivityLauncher.launch(intent);
            }
        });
        rvYaksokList.setAdapter(adapter);

        shareYaksokListAdapter = new ShareYaksokListAdapter(new ArrayList<>(), new ShareYaksokListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Yaksok yaksok) {
                Intent intent = new Intent(requireContext(), ShareYaksokDetail.class);
                intent.putExtra(ShareYaksokDetail.EXTRA_YAKSOK_ID, yaksok.getId());
                intent.putExtra(ShareYaksokDetail.EXTRA_ALREADY_SAVED, true);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Yaksok yaksok) {
                showRemoveSharedConfirmDialog(yaksok);
            }
        });
    }

    private void setupDrawerButton() {
        ivMenu.setOnClickListener(v ->
                ((DrawerLayout) requireActivity().findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START));
    }

    private void fetchYaksokList() {
        YaksokApi api = NetworkClient.getYaksokApi();

        api.getYaksokList().enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Yaksok> yaksokList = response.body().getData();
                    if (yaksokList != null) {
                        adapter.updateData(yaksokList);
                    }
                } else {
                    Toast.makeText(requireContext(), "약속 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Yaksok>>> call, Throwable t) {
                Log.e("YaksokList", "API 통신 실패: " + t.getMessage());
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSharedUserList() {
        NetworkClient.getYaksokApi().getSharedUserList().enqueue(new Callback<ApiResponse<List<SharedUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SharedUser>>> call, Response<ApiResponse<List<SharedUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SharedUser> userList = response.body().getData();
                    if (userList != null) {
                        sharedUserAdapter.updateData(userList);
                    }
                } else {
                    Log.e("YaksokList", "공유자 목록 조회 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SharedUser>>> call, Throwable t) {
                Log.e("YaksokList", "공유자 목록 API 통신 실패: " + t.getMessage());
            }
        });
    }

    private void selectMyYaksokTab() {
        isMyYaksokTab = true;
        tvMyYaksok.setBackgroundResource(R.drawable.bg_touch_my_yaksok_list);
        tvSharedYaksok.setBackgroundResource(R.drawable.bg_black_border);

        rvSharedUserList.setVisibility(View.GONE);
        rvYaksokList.setVisibility(View.VISIBLE);

        rvYaksokList.setAdapter(adapter);
        fetchYaksokList();
    }

    private void selectSharedYaksokTab() {
        isMyYaksokTab = false;
        tvSharedYaksok.setBackgroundResource(R.drawable.bg_touch_shared_yaksok_list);
        tvMyYaksok.setBackgroundResource(R.drawable.bg_black_border);

        currentSenderId = null;

        shareYaksokListAdapter.updateData(new ArrayList<>());
        rvYaksokList.setAdapter(shareYaksokListAdapter);

        rvSharedUserList.setVisibility(View.VISIBLE);
        fetchSharedUserList();
    }

    private void refreshCurrentTab() {
        if (isMyYaksokTab) {
            fetchYaksokList();
        } else if (currentSenderId != null) {
            fetchSharedYaksokBySender(currentSenderId);
        } else {
            fetchSharedUserList();
        }
    }

    private void fetchSharedYaksokBySender(Long senderId) {
        currentSenderId = senderId;

        NetworkClient.getYaksokApi().getSharedYaksokList(senderId)
                .enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Yaksok> data = response.body().getData();
                            shareYaksokListAdapter.updateData(data != null ? data : new ArrayList<>());
                        } else {
                            Log.e("YaksokList", "공유 약속 목록 조회 실패: " + response.code());
                            Toast.makeText(requireContext(), "공유 약속 목록을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Yaksok>>> call, Throwable t) {
                        Log.e("YaksokList", "공유 약속 목록 통신 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRemoveSharedConfirmDialog(Yaksok yaksok) {
        new AlertDialog.Builder(requireContext())
                .setTitle("공유 목록에서 빼기")
                .setMessage("'" + yaksok.getTitle() + "'을(를) 목록에서 뺄까요?\n원본 약속은 삭제되지 않습니다.")
                .setPositiveButton("빼기", (dialog, which) -> removeSharedYaksok(yaksok.getId()))
                .setNegativeButton("취소", null)
                .show();
    }

    private void removeSharedYaksok(Long yaksokId) {
        NetworkClient.getYaksokApi().deleteSharedYaksok(yaksokId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "목록에서 삭제.", Toast.LENGTH_SHORT).show();
                            if (currentSenderId != null) {
                                fetchSharedYaksokBySender(currentSenderId);
                            }
                        } else {
                            Toast.makeText(requireContext(), "목록에서 삭제 실패.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("YaksokList", "공유 약속 삭제 통신 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
