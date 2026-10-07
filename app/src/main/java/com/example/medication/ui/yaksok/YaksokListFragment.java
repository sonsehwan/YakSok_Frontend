package com.example.medication.ui.yaksok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medication.R;
import com.example.medication.adapter.ShareYaksokListAdapter;
import com.example.medication.adapter.SharedUserAdapter;
import com.example.medication.adapter.YaksokListAdapter;
import com.example.medication.databinding.FragmentYaksokListBinding;
import com.example.medication.model.Yaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.SharedUser;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.ui.common.AppDialog;
import com.example.medication.ui.sharedyaksok.ShareYaksokDetail;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.YaksokEventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YaksokListFragment extends Fragment implements YaksokEventBus.Listener {

    private FragmentYaksokListBinding binding;
    private YaksokListAdapter adapter;
    private ShareYaksokListAdapter shareYaksokListAdapter;
    private SharedUserAdapter sharedUserAdapter;

    private Long currentSenderId;
    private boolean sharedFilterSelected = false;
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
        binding = FragmentYaksokListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsUtil.applySystemBarPadding(binding.getRoot());

        initViews();
        setupDrawerButton();
        selectMyYaksokTab();

        binding.fabScan.setOnClickListener(v -> {
            BottomSheetAddYaksok bottomSheet = new BottomSheetAddYaksok();
            bottomSheet.show(getParentFragmentManager(), "show_create_list");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private void initViews() {
        binding.rvYaksokList.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.rvShareUserList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        sharedUserAdapter = new SharedUserAdapter(new ArrayList<>(), (user, position) -> {
            if (user.getUserId() == null) {
                binding.tvSharedOwnerLabel.setVisibility(View.GONE);
            } else {
                showSharedOwnerLabel(user.getNickName());
            }
            fetchSharedYaksokBySender(user.getUserId());
        });
        binding.rvShareUserList.setAdapter(sharedUserAdapter);

        binding.tvMyYaksok.setOnClickListener(v -> selectMyYaksokTab());
        binding.tvSharedYaksok.setOnClickListener(v -> selectSharedYaksokTab());

        adapter = new YaksokListAdapter(new ArrayList<>(), new YaksokListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Yaksok yaksok, int position) {
                Intent intent = new Intent(requireContext(), YaksokDetail.class);
                intent.putExtra("YAKSOK_DATA", yaksok);
                detailActivityLauncher.launch(intent);
            }
        });
        binding.rvYaksokList.setAdapter(adapter);

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
        binding.ivMenu.setOnClickListener(v ->
                ((DrawerLayout) requireActivity().findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START));
    }

    private void fetchYaksokList() {
        YaksokApi api = NetworkClient.getYaksokApi();

        api.getYaksokList().enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
                if (binding == null) return; // 응답이 늦게 와서 View가 이미 파괴된 경우
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
                if (binding == null) return;
                Log.e("YaksokList", "API 통신 실패: " + t.getMessage());
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSharedUserList() {
        NetworkClient.getYaksokApi().getSharedUserList().enqueue(new Callback<ApiResponse<List<SharedUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SharedUser>>> call, Response<ApiResponse<List<SharedUser>>> response) {
                if (binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<SharedUser> userList = response.body().getData();
                    if (userList != null && !userList.isEmpty()) {
                        List<SharedUser> displayList = new ArrayList<>();
                        displayList.add(new SharedUser(null, "전체"));
                        displayList.addAll(userList);
                        sharedUserAdapter.updateData(displayList);
                        selectSharedUser(displayList, currentSenderId);
                    } else if (userList != null) {
                        sharedUserAdapter.updateData(userList);
                        binding.tvSharedOwnerLabel.setVisibility(View.GONE);
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

    private void selectSharedUser(List<SharedUser> displayList, Long targetSenderId) {
        int index = 0;
        for (int i = 0; i < displayList.size(); i++) {
            if (java.util.Objects.equals(displayList.get(i).getUserId(), targetSenderId)) {
                index = i;
                break;
            }
        }
        SharedUser selected = displayList.get(index);
        sharedUserAdapter.setSelectedPosition(index);
        if (selected.getUserId() == null) {
            binding.tvSharedOwnerLabel.setVisibility(View.GONE);
        } else {
            showSharedOwnerLabel(selected.getNickName());
        }
        fetchSharedYaksokBySender(selected.getUserId());
    }

    private void showSharedOwnerLabel(String nickname) {
        binding.tvSharedOwnerLabel.setText(nickname + "님이 공유한 약속");
        binding.tvSharedOwnerLabel.setVisibility(View.VISIBLE);
    }

    private void selectMyYaksokTab() {
        isMyYaksokTab = true;
        setTabSelected(binding.tvMyYaksok, binding.viewUnderlineMy, true);
        setTabSelected(binding.tvSharedYaksok, binding.viewUnderlineShared, false);

        binding.rvShareUserList.setVisibility(View.GONE);
        binding.tvSharedOwnerLabel.setVisibility(View.GONE);
        binding.rvYaksokList.setVisibility(View.VISIBLE);

        binding.rvYaksokList.setAdapter(adapter);
        fetchYaksokList();
    }

    private void selectSharedYaksokTab() {
        isMyYaksokTab = false;
        setTabSelected(binding.tvSharedYaksok, binding.viewUnderlineShared, true);
        setTabSelected(binding.tvMyYaksok, binding.viewUnderlineMy, false);

        currentSenderId = null;
        sharedFilterSelected = false;

        shareYaksokListAdapter.updateData(new ArrayList<>());
        binding.rvYaksokList.setAdapter(shareYaksokListAdapter);

        binding.rvShareUserList.setVisibility(View.VISIBLE);
        fetchSharedUserList();
    }

    // 선택된 탭은 진하게 + 밑줄, 아닌 탭은 옅게 + 밑줄 없음
    private void setTabSelected(android.widget.TextView tab, View underline, boolean selected) {
        tab.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.g900 : R.color.g400));
        underline.setBackgroundColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.p600 : android.R.color.transparent));
    }

    private void refreshCurrentTab() {
        if (isMyYaksokTab) {
            fetchYaksokList();
        } else if (sharedFilterSelected) {
            fetchSharedYaksokBySender(currentSenderId);
        } else {
            fetchSharedUserList();
        }
    }

    private void fetchSharedYaksokBySender(Long senderId) {
        currentSenderId = senderId;
        sharedFilterSelected = true;

        NetworkClient.getYaksokApi().getSharedYaksokList(senderId)
                .enqueue(new Callback<ApiResponse<List<Yaksok>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Yaksok>>> call, Response<ApiResponse<List<Yaksok>>> response) {
                        if (binding == null) return;
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
                        if (binding == null) return;
                        Log.e("YaksokList", "공유 약속 목록 통신 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRemoveSharedConfirmDialog(Yaksok yaksok) {
        AppDialog.confirm(requireContext(), "공유 목록에서 빼기",
                "'" + yaksok.getTitle() + "'을(를) 목록에서 뺄까요?\n원본 약속은 삭제되지 않습니다.",
                "빼기", true, () -> removeSharedYaksok(yaksok.getId()));
    }

    private void removeSharedYaksok(Long yaksokId) {
        NetworkClient.getYaksokApi().deleteSharedYaksok(yaksokId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "목록에서 삭제.", Toast.LENGTH_SHORT).show();
                            fetchSharedUserList();
                        } else {
                            Toast.makeText(requireContext(), "목록에서 삭제 실패.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        if (binding == null) return;
                        Log.e("YaksokList", "공유 약속 삭제 통신 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
