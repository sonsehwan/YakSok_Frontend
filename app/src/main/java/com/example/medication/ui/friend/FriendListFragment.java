package com.example.medication.ui.friend;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.FriendListAdapter;
import com.example.medication.adapter.ReceivedRequestAdapter;
import com.example.medication.databinding.DialogAddFriendBinding;
import com.example.medication.databinding.DialogReceivedRequestBinding;
import com.example.medication.databinding.FragmentFriendListBinding;
import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FriendListDto;
import com.example.medication.model.response.FriendResponseDto;
import com.example.medication.model.response.ReceivedFriendRequestDto;
import com.example.medication.model.response.UserResponse;
import com.example.medication.model.response.UserSearchResultDto;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.common.WipActivity;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendListFragment extends Fragment {

    private FragmentFriendListBinding binding;
    private FriendListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsUtil.applySystemBarPadding(binding.main);

        UserResponse user = SprefsManager.getUser(requireContext());
        if (user == null || user.getId() == null) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        initViews();
        setRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter == null) {
            return; // onViewCreated에서 로그인 정보 없이 return한 경우
        }
        fetchFriendList();
        updateRequestCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViews() {
        binding.tvAddFriend.setOnClickListener(v -> showAddFriendDialog());
        binding.layoutFriendRequest.setOnClickListener(v -> showReceivedRequestDialog());
    }

    private void setRecyclerView() {
        binding.rvYaksokList.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FriendListAdapter(new ArrayList<>(), (friend, position) -> {
            // 약속 공유 기능 완성 후 연결 예정
            Intent intent = new Intent(requireContext(), WipActivity.class);
            startActivity(intent);
        }, this::showFriendActionsDialog);
        binding.rvYaksokList.setAdapter(adapter);
    }

    private void fetchFriendList() {
        NetworkClient.getFriendApi().getFriendList()
                .enqueue(new Callback<ApiResponse<FriendListDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendListDto>> call,
                                           Response<ApiResponse<FriendListDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            bindFriendList(response.body().getData().getFriends());
                        } else {
                            showError(response, "친구 목록을 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FriendListDto>> call, Throwable t) {
                        Log.e("FriendList", "친구 목록 통신 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 친구 목록과 개수, 빈 목록 안내 문구를 한 번에 갱신한다.
    private void bindFriendList(List<FriendResponseDto> friends) {
        adapter.updateData(friends);

        int count = (friends == null) ? 0 : friends.size();
        binding.tvFriendCount.setText("내 친구 " + count);
        binding.tvEmptyFriend.setVisibility(count == 0 ? TextView.VISIBLE : TextView.GONE);
    }

    // 친구 항목을 길게 누르면 액션 메뉴를 띄운다.
    private void showFriendActionsDialog(FriendResponseDto friend) {
        String[] actions = {"삭제"};
        new AlertDialog.Builder(requireContext())
                .setTitle(friend.getNickname())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showDeleteConfirmDialog(friend);
                    }
                })
                .show();
    }

    private void showDeleteConfirmDialog(FriendResponseDto friend) {
        new AlertDialog.Builder(requireContext())
                .setTitle("친구 삭제")
                .setMessage("'" + friend.getNickname() + "' 님을 내 친구 목록에서 삭제할까요?\n"
                        + "상대방 목록에는 내가 그대로 남습니다.")
                .setPositiveButton("삭제", (dialog, which) -> deleteFriend(friend))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteFriend(FriendResponseDto friend) {
        NetworkClient.getFriendApi().deleteFriend(friend.getFriendId())
                .enqueue(new Callback<ApiResponse<FriendListDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendListDto>> call,
                                           Response<ApiResponse<FriendListDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            Toast.makeText(requireContext(), "친구를 삭제했습니다.", Toast.LENGTH_SHORT).show();

                            bindFriendList(response.body().getData().getFriends());
                        } else {
                            showError(response, "친구를 삭제하지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FriendListDto>> call, Throwable t) {
                        Log.e("FriendList", "친구 삭제 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRequestCount() {
        NetworkClient.getFriendApi().getReceivedFriendRequests()
                .enqueue(new Callback<ApiResponse<List<ReceivedFriendRequestDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call,
                                           Response<ApiResponse<List<ReceivedFriendRequestDto>>> response) {
                        int count = 0;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            count = response.body().getData().size();
                        }
                        binding.tvRequestCount.setText(count + "개 받음");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call, Throwable t) {
                        Log.e("FriendList", "요청 개수 조회 실패: " + t.getMessage());
                    }
                });
    }

    private void showAddFriendDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogAddFriendBinding dialogBinding = DialogAddFriendBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(dialogBinding.getRoot());
        if (dialog.getWindow() != null) {
            // 기본 창 배경을 없애야 둥근 모서리 밖으로 검은 모서리가 보이지 않는다
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etNickname = dialogBinding.etNickname;
        LinearLayout layoutResult = dialogBinding.layoutResult;
        TextView tvResultAvatar = dialogBinding.tvResultAvatar;
        TextView tvResultNickname = dialogBinding.tvResultNickname;
        TextView tvResultEmail = dialogBinding.tvResultEmail;

        dialogBinding.btnSearch.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            if (TextUtils.isEmpty(nickname)) {
                Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutResult.setVisibility(LinearLayout.GONE);

            NetworkClient.getFriendApi().searchUser(nickname)
                    .enqueue(new Callback<ApiResponse<UserSearchResultDto>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UserSearchResultDto>> call,
                                               Response<ApiResponse<UserSearchResultDto>> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().getData() != null) {
                                UserSearchResultDto found = response.body().getData();

                                String nick = found.getNickname();
                                tvResultAvatar.setText(
                                        (nick == null || nick.isEmpty()) ? "?" : nick.substring(0, 1));
                                tvResultNickname.setText(nick);
                                tvResultEmail.setText(found.getEmail());
                                layoutResult.setVisibility(LinearLayout.VISIBLE);

                                dialogBinding.btnSendRequest.setOnClickListener(b ->
                                        sendFriendRequest(found.getUserId(), dialog));
                            } else {
                                showError(response, "사용자를 찾을 수 없습니다.");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UserSearchResultDto>> call, Throwable t) {
                            Log.e("FriendList", "사용자 검색 실패: " + t.getMessage());
                            Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private void sendFriendRequest(Long friendId, Dialog dialog) {
        FriendRequestCreateDto request = new FriendRequestCreateDto(friendId);

        NetworkClient.getFriendApi().createFriendRequest(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            showError(response, "친구 요청에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("FriendList", "친구 요청 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReceivedRequestDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogReceivedRequestBinding dialogBinding = DialogReceivedRequestBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(dialogBinding.getRoot());
        if (dialog.getWindow() != null) {
            // 기본 창 배경을 없애야 둥근 모서리 밖으로 검은 모서리가 보이지 않는다
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvEmpty = dialogBinding.tvEmpty;
        RecyclerView rv = dialogBinding.rvReceivedRequest;
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        ReceivedRequestAdapter requestAdapter = new ReceivedRequestAdapter(
                new ArrayList<>(),
                (request, accept) -> answerFriendRequest(request, accept, dialog));
        rv.setAdapter(requestAdapter);

        NetworkClient.getFriendApi().getReceivedFriendRequests()
                .enqueue(new Callback<ApiResponse<List<ReceivedFriendRequestDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call,
                                           Response<ApiResponse<List<ReceivedFriendRequestDto>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReceivedFriendRequestDto> requests = response.body().getData();

                            requestAdapter.updateData(requests);

                            boolean isEmpty = (requests == null || requests.isEmpty());
                            tvEmpty.setVisibility(isEmpty ? TextView.VISIBLE : TextView.GONE);
                            rv.setVisibility(isEmpty ? RecyclerView.GONE : RecyclerView.VISIBLE);
                        } else {
                            showError(response, "받은 요청을 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call, Throwable t) {
                        Log.e("FriendList", "받은 요청 조회 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

    private void answerFriendRequest(ReceivedFriendRequestDto request, boolean accept, Dialog dialog) {
        FriendRequestAnswerDto answer = new FriendRequestAnswerDto(accept);

        NetworkClient.getFriendApi().answerFriendRequest(request.getRequestId(), answer)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(),
                                    accept ? "친구 요청을 수락했습니다." : "친구 요청을 거절했습니다.",
                                    Toast.LENGTH_SHORT).show();

                            dialog.dismiss();
                            fetchFriendList();      // 친구 목록 갱신
                            updateRequestCount();   // 요청 개수 갱신
                        } else {
                            showError(response, "요청 처리에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("FriendList", "친구 요청 응답 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showError(Response<?> response, String fallback) {
        String message = fallback;
        try {
            if (response.errorBody() != null) {
                ApiResponse<?> error = new Gson()
                        .fromJson(response.errorBody().string(), ApiResponse.class);
                if (error != null && error.getMessage() != null) {
                    message = error.getMessage();
                }
            } else if (response.body() instanceof ApiResponse
                    && ((ApiResponse<?>) response.body()).getMessage() != null) {
                message = ((ApiResponse<?>) response.body()).getMessage();
            }
        } catch (Exception ignored) {
            // 파싱 실패 시 기본 메시지 사용
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
