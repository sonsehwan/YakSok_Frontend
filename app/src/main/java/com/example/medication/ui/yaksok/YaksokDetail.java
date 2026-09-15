package com.example.medication.ui.yaksok;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import com.example.medication.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medication.ui.chattingroom.ChattingRoom;
import com.example.medication.R;
import com.example.medication.adapter.DetailYaksokMedicationAdapter;
import com.example.medication.adapter.FriendListAdapter;
import com.example.medication.databinding.ActivityYaksokDetailBinding;
import com.example.medication.databinding.DialogSelectFriendBinding;
import com.example.medication.model.Yaksok;
import com.example.medication.model.request.FriendChatRoomRequest;
import com.example.medication.model.request.PillRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomResponse;
import com.example.medication.model.response.FriendListDto;
import com.example.medication.model.response.FriendResponseDto;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.YaksokApi;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YaksokDetail extends BaseActivity {

    private ActivityYaksokDetailBinding binding;

    // 선택된 약 목록 리사이클러뷰 관련
    private DetailYaksokMedicationAdapter settingAdapter;
    private final List<PillRequest> selectedPills = new ArrayList<>();
    private Yaksok originalYaksok;

    private ActivityResultLauncher<Intent> modifyLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYaksokDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        modifyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Yaksok updatedYaksok = (Yaksok) result.getData().getSerializableExtra("UPDATED_YAKSOK");
                        if (updatedYaksok != null) {
                            originalYaksok = updatedYaksok;
                            populateViews(originalYaksok); // 화면 내용 갱신
                            disableAllInteractions(); // 다시 읽기 전용 모드로 잠금
                            setResult(RESULT_OK); // 홈 화면의 리스트도 갱신될 수 있도록 RESULT_OK 설정
                        }
                    }
                }
        );

        originalYaksok = (Yaksok)getIntent().getSerializableExtra("YAKSOK_DATA");

        binding.ivBack.setOnClickListener(v -> finish());

        binding.ivMenu.setOnClickListener(view -> {
            showMenu(view, originalYaksok);
        });

        if(originalYaksok != null){
            populateViews(originalYaksok);
            disableAllInteractions();
        }else{
            showToast("약속 정보를 불러올 수 없습니다.");
        }
    }

    private void populateViews(Yaksok yaksok){
        if(yaksok.getTitle() != null) binding.inputCardTitle.setText(yaksok.getTitle());
        if (yaksok.getStartDate() != null) binding.inputStartDate.setText(yaksok.getStartDate());
        binding.inputPrescriptionDays.setText(String.valueOf(yaksok.getPrescriptionDays()));

        if(yaksok.getPills() != null && !yaksok.getPills().isEmpty()) {
            selectedPills.clear();
            selectedPills.addAll(yaksok.getPills());
            settingAdapter.notifyDataSetChanged();
        }

        if (yaksok.isTakeMorning()) {
            binding.cbMorning.setChecked(true);
            binding.inputSetMorningTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeMorning() != null) binding.inputSetMorningTime.setText(yaksok.getTimeMorning());
        }
        if (yaksok.isTakeLunch()) {
            binding.cbLunch.setChecked(true);
            binding.inputSetLunchTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeLunch() != null) binding.inputSetLunchTime.setText(yaksok.getTimeLunch());
        }
        if (yaksok.isTakeDinner()) {
            binding.cbDinner.setChecked(true);
            binding.inputSetDinnerTime.setVisibility(View.VISIBLE);
            if (yaksok.getTimeDinner() != null) binding.inputSetDinnerTime.setText(yaksok.getTimeDinner());
        }

        String dosageTime = yaksok.getDosageTime();
        if (dosageTime != null) {
            if (dosageTime.equals("식전 30분")) {
                binding.rgDosageTime.check(R.id.rb_before);
            } else if (dosageTime.equals("식후 30분")) {
                binding.rgDosageTime.check(R.id.rb_after);
            } else { // 직후
                binding.rgDosageTime.check(R.id.rb_anytime);
            }
        }
    }

    private void disableAllInteractions() {
        binding.cbMorning.setEnabled(false);
        binding.cbLunch.setEnabled(false);
        binding.cbDinner.setEnabled(false);

        binding.inputSetMorningTime.setClickable(false);
        binding.inputSetMorningTime.setFocusable(false);
        binding.inputSetLunchTime.setClickable(false);
        binding.inputSetLunchTime.setFocusable(false);
        binding.inputSetDinnerTime.setClickable(false);
        binding.inputSetDinnerTime.setFocusable(false);

        for (int i = 0; i < binding.rgDosageTime.getChildCount(); i++) {
            binding.rgDosageTime.getChildAt(i).setEnabled(false);
        }
    }

    private void setupRecyclerView() {
        settingAdapter = new DetailYaksokMedicationAdapter(selectedPills);
        binding.rvSelectedPills.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSelectedPills.setAdapter(settingAdapter);
    }

    private void showDeleteConfirmDialog(Yaksok yaksok) {
        Long id = yaksok.getId();
        new AlertDialog.Builder(this)
                .setTitle("약속 삭제")
                .setMessage("'" + yaksok.getTitle() + "' 약속을 삭제하시겠습니까?\n관련된 모든 정보(복약, 알림)가 함께 삭제됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteYaksokFromServer(id);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteYaksokFromServer(Long yaksokId) {
        YaksokApi api = NetworkClient.getYaksokApi();

        api.deleteYaksok(yaksokId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d("약속 삭제", "약속을 성공적으로 삭제하였습니다.");
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    Toast.makeText(YaksokDetail.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(YaksokDetail.this, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("YaksokList", "삭제 통신 실패: " + t.getMessage());
                Toast.makeText(YaksokDetail.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 공유할 친구를 고르는 다이얼로그
    private void showFriendPickerDialog(Yaksok yaksok) {
        UserResponse me = SprefsManager.getUser(this);
        if (me == null || me.getId() == null) {
            showToast("로그인 정보를 불러올 수 없습니다.");
            return;
        }

        DialogSelectFriendBinding dialogBinding = DialogSelectFriendBinding.inflate(getLayoutInflater());
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            // 기본 창 배경을 없애야 둥근 모서리 밖으로 검은 모서리가 보이지 않는다
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        FriendListAdapter adapter = new FriendListAdapter(new ArrayList<>(), (friend, position) -> {
            dialog.dismiss();
            openChatRoomAndShare(friend, yaksok);
        });

        dialogBinding.rvSelectFriend.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.rvSelectFriend.setAdapter(adapter);

        NetworkClient.getFriendApi().getFriendList()
                .enqueue(new Callback<ApiResponse<FriendListDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendListDto>> call,
                                           Response<ApiResponse<FriendListDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            List<FriendResponseDto> friends = response.body().getData().getFriends();
                            adapter.updateData(friends);
                            dialogBinding.tvEmpty.setVisibility(
                                    (friends == null || friends.isEmpty()) ? View.VISIBLE : View.GONE);
                        } else {
                            showToast("친구 목록을 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FriendListDto>> call, Throwable t) {
                        showToast("네트워크 오류가 발생했습니다.");
                    }
                });

        dialog.show();
    }

    // 친구와의 채팅방을 열고, 공유 메시지를 실어 보낸다
    private void openChatRoomAndShare(FriendResponseDto friend, Yaksok yaksok) {
        if (yaksok.getId() == null) {
            showToast("약속 정보를 불러올 수 없습니다.");
            return;
        }

        FriendChatRoomRequest request = new FriendChatRoomRequest(friend.getFriendId());

        NetworkClient.getChatApi().enterFriendChatRoom(request)
                .enqueue(new Callback<ApiResponse<ChatRoomResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ChatRoomResponse>> call,
                                           Response<ApiResponse<ChatRoomResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {

                            Intent chatIntent = new Intent(YaksokDetail.this, ChattingRoom.class);
                            chatIntent.putExtra("roomId", response.body().getData().getRoomId());
                            chatIntent.putExtra("myParticipantId", response.body().getData().getMyParticipantId());
                            chatIntent.putExtra("roomName", friend.getNickname());
                            chatIntent.putExtra("SHARE_YAKSOK_ID", yaksok.getId().longValue());
                            chatIntent.putExtra("SHARE_MESSAGE", buildShareMessage(yaksok));
                            startActivity(chatIntent);

                        } else {
                            showToast(parseErrorMessage(response, "채팅방 연결에 실패했습니다."));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) {
                        showToast("네트워크 오류가 발생했습니다.");
                    }
                });
    }

    private String buildShareMessage(Yaksok yaksok) {
        String nickname = SprefsManager.getUserNickName(this);
        return nickname + "님이 약속을 공유했습니다.\n" + yaksok.getTitle();
    }

    // 응답 원문은 로그에만 남기고, 사용자에게는 message만 보여준다.
    private String parseErrorMessage(Response<?> response, String defaultMessage) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : null;
            Log.e("DetailYaksok", "서버 에러 상세: " + errorBody);

            if (errorBody != null) {
                ApiResponse<?> error = new Gson().fromJson(errorBody, ApiResponse.class);
                if (error != null && error.getMessage() != null) {
                    return error.getMessage();
                }
            }
        } catch (Exception e) {
            Log.e("DetailYaksok", "에러 응답 파싱 실패", e);
        }
        return defaultMessage;
    }

    private void showMenu(View view, Yaksok yaksok){
        PopupMenu menu = new PopupMenu(this, view);

        menu.getMenuInflater().inflate(R.menu.yaksok_menu, menu.getMenu());

        menu.setOnMenuItemClickListener(item ->{
            int id = item.getItemId();

            if(id == R.id.yaksok_share){
                showFriendPickerDialog(yaksok);
                return true;
            }
            else if(id == R.id.yaksok_modify) {
                Intent intent = new Intent(YaksokDetail.this, ModifyYaksok.class);
                intent.putExtra("YAKSOK_DATA", yaksok);
                modifyLauncher.launch(intent);
                return true;
            }
            else if(id == R.id.yaksok_delete){
                showDeleteConfirmDialog(yaksok);
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}