package com.example.medication.ui.friend;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medication.adapter.FriendListAdapter;
import com.example.medication.adapter.ReceivedRequestAdapter;
import com.example.medication.databinding.DialogAddFriendBinding;
import com.example.medication.databinding.DialogMyQrBinding;
import com.example.medication.databinding.DialogProfileBinding;
import com.example.medication.databinding.DialogReceivedRequestBinding;
import com.example.medication.databinding.FragmentFriendListBinding;
import com.example.medication.databinding.PopupFriendQrMenuBinding;
import com.example.medication.model.request.FriendChatRoomRequest;
import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomResponse;
import com.example.medication.model.response.FriendListDto;
import com.example.medication.model.response.FriendQrCodeDto;
import com.example.medication.model.response.FriendResponseDto;
import com.example.medication.model.response.ReceivedFriendRequestDto;
import com.example.medication.model.response.UserProfileDto;
import com.example.medication.model.response.UserResponse;
import com.example.medication.model.response.UserSearchResultDto;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.chattingroom.ChattingRoom;
import com.example.medication.ui.common.AppDialog;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendListFragment extends Fragment {

    private FragmentFriendListBinding binding;
    private FriendListAdapter adapter;

    // Fragment의 ActivityResultLauncher는 필드 초기화 시점(생성자)에 등록해야 안전하다.
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchQrScanner();
                } else {
                    Toast.makeText(requireContext(), "카메라 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<ScanOptions> qrScanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if(result.getContents() != null){
                    showProfileDialog(result.getContents());
                }
            });

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
        binding.ivQr.setOnClickListener(this::showQrMenu);
        binding.ivAddFriend.setOnClickListener(v -> showAddFriendDialog());
        binding.layoutFriendRequest.setOnClickListener(v -> showReceivedRequestDialog());
    }

    // QR 아이콘 클릭 시 "내 프로필"/"코드 스캔" 팝업 메뉴를 띄운다.
    private void showQrMenu(View anchor) {
        PopupFriendQrMenuBinding menu = PopupFriendQrMenuBinding.inflate(getLayoutInflater());
        PopupWindow popup = AppDialog.popup(menu.getRoot(), 168);

        menu.llMyProfile.setOnClickListener(v -> {
            popup.dismiss();
            showMyQrDialog();
        });
        menu.llScan.setOnClickListener(v -> {
            popup.dismiss();
            startQrScan();
        });
        AppDialog.showBelow(popup, anchor);
    }

    private void startQrScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchQrScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions()
                .setOrientationLocked(true)
                .setBeepEnabled(false)
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setCaptureActivity(PortraitCaptureActivity.class)
                .setPrompt("QR코드를 화면 안에 맞춰주세요.");
        qrScanLauncher.launch(options);
    }

    private void showMyQrDialog() {
        NetworkClient.getFriendApi().issueQrCode()
                .enqueue(new Callback<ApiResponse<FriendQrCodeDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendQrCodeDto>> call,
                                           Response<ApiResponse<FriendQrCodeDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            renderMyQrDialog(response.body().getData().getCode());
                        } else {
                            showError(response, "QR 코드를 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FriendQrCodeDto>> call, Throwable t) {
                        Log.e("FriendList", "QR 코드 발급 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderMyQrDialog(String code) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogMyQrBinding dialogBinding = DialogMyQrBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(dialogBinding.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        String qrImageUrl = NetworkClient.getQrCodeApiUrl() + "?size=220x220&data=" + Uri.encode(code);
        Glide.with(this).load(qrImageUrl).into(dialogBinding.ivQrCode);

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // QR 스캔으로 알아낸 code로 상대 프로필 + 나와의 관계를 조회해 다이얼로그로 띄운다.
    private void showProfileDialog(String code) {
        NetworkClient.getFriendApi().getUserProfile(code)
                .enqueue(new Callback<ApiResponse<UserProfileDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserProfileDto>> call,
                                           Response<ApiResponse<UserProfileDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            renderProfileDialog(code, response.body().getData());
                        } else {
                            showError(response, "프로필을 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserProfileDto>> call, Throwable t) {
                        Log.e("FriendList", "프로필 조회 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderProfileDialog(String code, UserProfileDto profile) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogProfileBinding dialogBinding = DialogProfileBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(dialogBinding.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
        bindProfileDialog(dialogBinding, code, profile);

        dialog.show();
    }

    // 관계 상태(relation)에 따라 아바타/닉네임/이메일/액션 버튼을 채운다. 액션 성공 시 이 메서드를
    // 다시 불러 다이얼로그를 새로고침한다(다이얼로그를 새로 띄우지 않고 그대로 둔 채 버튼만 갱신).
    private void bindProfileDialog(DialogProfileBinding dialogBinding, String code, UserProfileDto profile) {
        String nickname = profile.getNickname();
        dialogBinding.tvAvatar.setText((nickname == null || nickname.isEmpty()) ? "?" : nickname.substring(0, 1));
        dialogBinding.tvNickname.setText(nickname);
        dialogBinding.tvEmail.setText(profile.getEmail());

        switch (profile.getRelation()) {
            case SELF:
                // .pen의 Spacer처럼 버튼 자리는 남겨 다이얼로그 높이가 다른 상태와 같게 둔다.
                dialogBinding.btnAction.setVisibility(View.INVISIBLE);
                break;
            case FRIEND:
                setProfileActionButton(dialogBinding, "채팅하기", true, v -> openChatWithFriend(profile));
                break;
            case SENT:
                setProfileActionButton(dialogBinding, "요청 보냄", false, null);
                break;
            case RECEIVED:
                setProfileActionButton(dialogBinding, "친구 요청 수락하기", true,
                        v -> acceptFriendRequestFromProfile(dialogBinding, code, profile));
                break;
            case NONE:
            default:
                setProfileActionButton(dialogBinding, "친구 요청 보내기", true,
                        v -> sendFriendRequestFromProfile(dialogBinding, code, profile));
                break;
        }
    }

    private void setProfileActionButton(DialogProfileBinding dialogBinding, String text, boolean enabled,
                                        @Nullable View.OnClickListener listener) {
        dialogBinding.btnAction.setVisibility(View.VISIBLE);
        dialogBinding.btnAction.setText(text);
        dialogBinding.btnAction.setEnabled(enabled);
        dialogBinding.btnAction.setAlpha(enabled ? 1f : 0.5f);
        dialogBinding.btnAction.setOnClickListener(listener);
    }

    private void sendFriendRequestFromProfile(DialogProfileBinding dialogBinding, String code, UserProfileDto profile) {
        FriendRequestCreateDto request = new FriendRequestCreateDto(profile.getUserId());

        NetworkClient.getFriendApi().createFriendRequest(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                            refreshProfileDialog(dialogBinding, code);
                        } else {
                            showError(response, "친구 요청에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptFriendRequestFromProfile(DialogProfileBinding dialogBinding, String code, UserProfileDto profile) {
        FriendRequestAnswerDto answer = new FriendRequestAnswerDto(true);

        NetworkClient.getFriendApi().answerFriendRequest(profile.getRequestId(), answer)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
                            fetchFriendList();      // 친구 목록 갱신
                            updateRequestCount();   // 요청 개수 갱신
                            refreshProfileDialog(dialogBinding, code);
                        } else {
                            showError(response, "요청 처리에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshProfileDialog(DialogProfileBinding dialogBinding, String code) {
        NetworkClient.getFriendApi().getUserProfile(code)
                .enqueue(new Callback<ApiResponse<UserProfileDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserProfileDto>> call,
                                           Response<ApiResponse<UserProfileDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            bindProfileDialog(dialogBinding, code, response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserProfileDto>> call, Throwable t) {
                        Log.e("FriendList", "프로필 새로고침 실패: " + t.getMessage());
                    }
                });
    }

    // 이미 친구인 상대를 스캔했을 때 바로 채팅방을 연다 (YaksokDetail.openChatRoomAndShare와 같은 흐름).
    private void openChatWithFriend(UserProfileDto profile) {
        FriendChatRoomRequest request = new FriendChatRoomRequest(profile.getUserId());

        NetworkClient.getChatApi().enterFriendChatRoom(request)
                .enqueue(new Callback<ApiResponse<ChatRoomResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ChatRoomResponse>> call,
                                           Response<ApiResponse<ChatRoomResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            Intent chatIntent = new Intent(requireContext(), ChattingRoom.class);
                            chatIntent.putExtra("roomId", response.body().getData().getRoomId());
                            chatIntent.putExtra("myParticipantId", response.body().getData().getMyParticipantId());
                            chatIntent.putExtra("roomName", profile.getNickname());
                            startActivity(chatIntent);
                        } else {
                            showError(response, "채팅방 연결에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setRecyclerView() {
        binding.rvYaksokList.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FriendListAdapter(new ArrayList<>(),
                (friend, position) -> renderProfileDialog(null, UserProfileDto.fromFriend(friend)),
                this::showFriendActionsDialog);

        binding.rvYaksokList.setAdapter(adapter);
    }

    private void fetchFriendList() {
        NetworkClient.getFriendApi().getFriendList()
                .enqueue(new Callback<ApiResponse<FriendListDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendListDto>> call,
                                           Response<ApiResponse<FriendListDto>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            bindFriendList(response.body().getData().getFriends());
                        } else {
                            showError(response, "친구 목록을 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FriendListDto>> call, Throwable t) {
                        if (binding == null) return;
                        Log.e("FriendList", "친구 목록 통신 실패: " + t.getMessage());
                        Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 친구 목록과 개수, 빈 목록 안내 문구를 한 번에 갱신한다.
    private void bindFriendList(List<FriendResponseDto> friends) {
        adapter.updateData(friends);

        int count = (friends == null) ? 0 : friends.size();
        binding.tvFriendCount.setText("친구 " + count);
        binding.tvEmptyFriend.setVisibility(count == 0 ? TextView.VISIBLE : TextView.GONE);
    }

    // 친구 항목을 길게 누르면 액션 메뉴를 띄운다.
    private void showFriendActionsDialog(FriendResponseDto friend) {
        AppDialog.deleteAction(requireContext(), friend.getNickname(), () -> showDeleteConfirmDialog(friend));
    }

    private void showDeleteConfirmDialog(FriendResponseDto friend) {
        AppDialog.confirm(requireContext(), "친구 삭제",
                "'" + friend.getNickname() + "' 님을 내 친구 목록에서 삭제할까요?\n"
                        + "상대방 목록에는 내가 그대로 남습니다.",
                "삭제", true, () -> deleteFriend(friend));
    }

    private void deleteFriend(FriendResponseDto friend) {
        NetworkClient.getFriendApi().deleteFriend(friend.getFriendId())
                .enqueue(new Callback<ApiResponse<FriendListDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FriendListDto>> call,
                                           Response<ApiResponse<FriendListDto>> response) {
                        if (binding == null) return;
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
                        if (binding == null) return;
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

                        if (binding == null) return;

                        int count = 0;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            count = response.body().getData().size();
                        }
                        binding.tvRequestCount.setText(String.valueOf(count));
                        binding.tvRequestCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReceivedFriendRequestDto>>> call, Throwable t) {
                        if (binding == null) return;
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

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
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

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
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
                String errorJson = response.errorBody().string();
                ApiResponse<?> error = new Gson().fromJson(errorJson, ApiResponse.class);
                if (error != null && error.getMessage() != null) {
                    message = error.getMessage();
                }
            } else if (response.body() instanceof ApiResponse
                    && ((ApiResponse<?>) response.body()).getMessage() != null) {
                message = ((ApiResponse<?>) response.body()).getMessage();
            }
        } catch (Exception e) {
            Log.e("FriendList", "에러 응답 파싱 실패: " + e.getMessage());
        }
        Log.e("FriendList", "HTTP " + response.code() + ": " + message);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
