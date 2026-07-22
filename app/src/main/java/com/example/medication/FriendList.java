package com.example.medication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.FriendListAdapter;
import com.example.medication.adapter.ReceivedRequestAdapter;
import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FriendListDto;
import com.example.medication.model.response.FriendResponseDto;
import com.example.medication.model.response.ReceivedFriendRequestDto;
import com.example.medication.model.response.UserResponse;
import com.example.medication.model.response.UserSearchResultDto;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendList extends AppCompatActivity {

    private RecyclerView rvFriendList;
    private FriendListAdapter adapter;

    private ImageView btnBack;
    private TextView btnAddFriend;
    private LinearLayout layoutFriendRequest;
    private TextView tvRequestCount;
    private TextView tvFriendCount;
    private TextView tvEmptyFriend;

    private Long loginUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_list);

        UserResponse user = SprefsManager.getUser(this);
        if (user == null || user.getId() == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loginUserId = user.getId();

        initViews();
        setRecyclerView();
        fetchFriendList();
        updateRequestCount();
    }

    private void initViews() {
        btnBack = findViewById(R.id.iv_back);
        btnAddFriend = findViewById(R.id.tv_add_friend);
        layoutFriendRequest = findViewById(R.id.layout_friend_request);
        tvRequestCount = findViewById(R.id.tv_request_count);
        tvFriendCount = findViewById(R.id.tv_friend_count);
        tvEmptyFriend = findViewById(R.id.tv_empty_friend);

        btnBack.setOnClickListener(v -> finish());
        btnAddFriend.setOnClickListener(v -> showAddFriendDialog());
        layoutFriendRequest.setOnClickListener(v -> showReceivedRequestDialog());
    }

    private void setRecyclerView() {
        rvFriendList = findViewById(R.id.rv_yaksok_list);
        rvFriendList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendListAdapter(new ArrayList<>(), (friend, position) -> {
            // 약속 공유 기능 완성 후 연결 예정
            Intent intent = new Intent(FriendList.this, WipActivity.class);
            startActivity(intent);
        });
        rvFriendList.setAdapter(adapter);
    }

    private void fetchFriendList() {
        NetworkClient.getFriendApi().getFriendList(loginUserId)
                .enqueue(new Callback<ApiResponse<FriendListDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendListDto>> call,
                                           Response<ApiResponse<FriendListDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            List<FriendResponseDto> friends = response.body().getData().getFriends();

                            adapter.updateData(friends);

                            int count = (friends == null) ? 0 : friends.size();
                            tvFriendCount.setText("내 친구 " + count);
                            tvEmptyFriend.setVisibility(count == 0 ? TextView.VISIBLE : TextView.GONE);
                        } else {
                            showError(response, "친구 목록을 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FriendListDto>> call, Throwable t) {
                        Log.e("FriendList", "친구 목록 통신 실패: " + t.getMessage());
                        Toast.makeText(FriendList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRequestCount() {
        NetworkClient.getFriendApi().getReceivedFriendRequests(loginUserId)
                .enqueue(new Callback<ApiResponse<List<ReceivedFriendRequestDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call,
                                           Response<ApiResponse<List<ReceivedFriendRequestDto>>> response) {
                        int count = 0;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            count = response.body().getData().size();
                        }
                        tvRequestCount.setText(count + "개 받음");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call, Throwable t) {
                        Log.e("FriendList", "요청 개수 조회 실패: " + t.getMessage());
                    }
                });
    }

    private void showAddFriendDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_friend);
        if (dialog.getWindow() != null) {
            // 기본 창 배경을 없애야 둥근 모서리 밖으로 검은 모서리가 보이지 않는다
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etNickname = dialog.findViewById(R.id.et_nickname);
        Button btnSearch = dialog.findViewById(R.id.btn_search);
        LinearLayout layoutResult = dialog.findViewById(R.id.layout_result);
        TextView tvResultAvatar = dialog.findViewById(R.id.tv_result_avatar);
        TextView tvResultNickname = dialog.findViewById(R.id.tv_result_nickname);
        TextView tvResultEmail = dialog.findViewById(R.id.tv_result_email);
        Button btnSendRequest = dialog.findViewById(R.id.btn_send_request);

        btnSearch.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            if (TextUtils.isEmpty(nickname)) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutResult.setVisibility(LinearLayout.GONE);

            NetworkClient.getFriendApi().searchUser(nickname, loginUserId)
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

                                btnSendRequest.setOnClickListener(b ->
                                        sendFriendRequest(found.getUserId(), dialog));
                            } else {
                                showError(response, "사용자를 찾을 수 없습니다.");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UserSearchResultDto>> call, Throwable t) {
                            Log.e("FriendList", "사용자 검색 실패: " + t.getMessage());
                            Toast.makeText(FriendList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private void sendFriendRequest(Long friendId, Dialog dialog) {
        FriendRequestCreateDto request = new FriendRequestCreateDto(loginUserId, friendId);

        NetworkClient.getFriendApi().createFriendRequest(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(FriendList.this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            showError(response, "친구 요청에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("FriendList", "친구 요청 실패: " + t.getMessage());
                        Toast.makeText(FriendList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReceivedRequestDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_received_request);
        if (dialog.getWindow() != null) {
            // 기본 창 배경을 없애야 둥근 모서리 밖으로 검은 모서리가 보이지 않는다
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvEmpty = dialog.findViewById(R.id.tv_empty);
        RecyclerView rv = dialog.findViewById(R.id.rv_received_request);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ReceivedRequestAdapter requestAdapter = new ReceivedRequestAdapter(
                new ArrayList<>(),
                (request, accept) -> answerFriendRequest(request, accept, dialog));
        rv.setAdapter(requestAdapter);

        NetworkClient.getFriendApi().getReceivedFriendRequests(loginUserId)
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
                        Toast.makeText(FriendList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

    private void answerFriendRequest(ReceivedFriendRequestDto request, boolean accept, Dialog dialog) {
        FriendRequestAnswerDto answer = new FriendRequestAnswerDto(loginUserId, accept);

        NetworkClient.getFriendApi().answerFriendRequest(request.getRequestId(), answer)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(FriendList.this,
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
                        Toast.makeText(FriendList.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}