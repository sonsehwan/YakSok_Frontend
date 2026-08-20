package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.ChatRoomListAdapter;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomListDto;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomList extends AppCompatActivity {

    private RecyclerView rvChatRooms;
    private TextView tvEmpty;
    private BottomNavigationView bottomNav;

    private final List<ChatRoomListDto> rooms = new ArrayList<>();
    private ChatRoomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_room_list);
        InsetsUtil.applySystemBarPadding(findViewById(R.id.main_root));

        initViews();

        adapter = new ChatRoomListAdapter(rooms, this::openChatRoom);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(this));
        rvChatRooms.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_chat_room));
        rvChatRooms.addItemDecoration(divider);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_chat) {
                return true;
            } else if (itemId == R.id.nav_home) {
                moveTo(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_history) {
                moveTo(YaksokList.class);
                return true;
            } else if (itemId == R.id.nav_friend) {
                moveTo(FriendList.class);
                return true;
            } else if (itemId == R.id.nav_settings) {
                moveTo(Settings.class);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_chat);
        loadChatRooms();
    }

    private void initViews() {
        rvChatRooms = findViewById(R.id.rv_chat_rooms);
        tvEmpty = findViewById(R.id.tv_empty);
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    private void moveTo(Class<?> target) {
        Intent intent = new Intent(ChatRoomList.this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openChatRoom(ChatRoomListDto room) {
        Intent intent = new Intent(ChatRoomList.this, ChattingRoom.class);
        intent.putExtra("roomId", room.getRoomId());
        startActivity(intent);
    }

    private void loadChatRooms() {
        Long userId = SprefsManager.getUserId(this);

        NetworkClient.getChatApi().getMyChatRooms(userId)
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

                            tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Log.e("채팅목록", "조회 실패: " + response.code());
                            Toast.makeText(ChatRoomList.this, "채팅방 목록을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ChatRoomListDto>>> call, Throwable t) {
                        Log.e("채팅목록", "통신 실패: " + t.getMessage());
                        Toast.makeText(ChatRoomList.this, "서버와 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
