package com.example.medication.ui.chattingroom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medication.R;
import com.example.medication.adapter.ChatRoomListAdapter;
import com.example.medication.databinding.FragmentChatRoomListBinding;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomListDto;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomListFragment extends Fragment {

    private FragmentChatRoomListBinding binding;

    private boolean isOwner = false;
    private boolean showConsult = false;

    private final List<ChatRoomListDto> rooms = new ArrayList<>();
    private ChatRoomListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentChatRoomListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsUtil.applySystemBarPadding(binding.mainRoot);

        adapter = new ChatRoomListAdapter(rooms, this::openChatRoom, this::showRoomActionsDialog);
        binding.rvChatRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChatRooms.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_chat_room));
        binding.rvChatRooms.addItemDecoration(divider);

        setupChatTypeToggle();
    }

    private void setupChatTypeToggle() {
        UserResponse user = SprefsManager.getUser(requireContext());
        isOwner = user != null && "DRUGSTORE".equals(user.getRole());

        if (!isOwner) {
            binding.rgChatType.setVisibility(View.GONE);
            return;
        }

        binding.rgChatType.setVisibility(View.VISIBLE);
        binding.rgChatType.check(R.id.rb_chat_friend);
        showConsult = false;

        binding.rgChatType.setOnCheckedChangeListener((group, checkedId) -> {
            showConsult = checkedId == R.id.rb_chat_consult;
            loadChatRooms();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatRooms();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openChatRoom(ChatRoomListDto room) {
        Intent intent = new Intent(requireContext(), ChattingRoom.class);
        intent.putExtra("roomId", room.getRoomId());
        intent.putExtra("myParticipantId", room.getMyParticipantId());
        intent.putExtra("roomName", room.getRoomName());
        startActivity(intent);
    }

    private void showRoomActionsDialog(ChatRoomListDto room) {
        String[] actions = {"삭제"};
        new AlertDialog.Builder(requireContext())
                .setTitle(room.getRoomName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showDeleteConfirmDialog(room);
                    }
                })
                .show();
    }

    private void showDeleteConfirmDialog(ChatRoomListDto room) {
        new AlertDialog.Builder(requireContext())
                .setTitle("채팅방 나가기")
                .setMessage("'" + room.getRoomName() + "' 채팅방을 목록에서 지울까요?")
                .setPositiveButton("나가기", (dialog, which) -> deleteChatRoom(room))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteChatRoom(ChatRoomListDto room) {
        NetworkClient.getChatApi().deleteChatRoom(room.getRoomId())
                .enqueue(new Callback<ApiResponse<List<ChatRoomListDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ChatRoomListDto>>> call,
                                           Response<ApiResponse<List<ChatRoomListDto>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChatRoomListDto> data = response.body().getData();

                            rooms.clear();
                            if (data != null) {
                                rooms.addAll(data);
                            }
                            adapter.notifyDataSetChanged();

                            binding.tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Toast.makeText(requireContext(), "채팅방을 지우지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ChatRoomListDto>>> call, Throwable t) {
                        Toast.makeText(requireContext(), "서버와 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadChatRooms() {
        Call<ApiResponse<List<ChatRoomListDto>>> call = (isOwner && showConsult)
                ? NetworkClient.getChatApi().getConsultRooms()
                : NetworkClient.getChatApi().getMyChatRooms();

        call.enqueue(new Callback<ApiResponse<List<ChatRoomListDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatRoomListDto>>> call,
                                   Response<ApiResponse<List<ChatRoomListDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatRoomListDto> data = response.body().getData();

                    rooms.clear();
                    if (data != null) {
                        rooms.addAll(data);
                    }
                    adapter.notifyDataSetChanged();

                    binding.tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e("채팅목록", "조회 실패: " + response.code());
                    Toast.makeText(requireContext(), "채팅방 목록을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatRoomListDto>>> call, Throwable t) {
                Log.e("채팅목록", "통신 실패: " + t.getMessage());
                Toast.makeText(requireContext(), "서버와 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
