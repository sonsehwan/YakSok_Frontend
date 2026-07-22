package com.example.medication;

import static com.example.medication.util.SprefsManager.getUserEmail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.adapter.ChattingRoomAdapter;
import com.example.medication.model.ChatMessage;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.google.gson.Gson;

import java.util.List;

import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ChattingRoom extends AppCompatActivity {

    private static final String TAG = "ChattingRoom";

    // 백엔드 서버의 실제 IP 주소와 포트를 입력
    private static final String WEBSOCKET_URL = "ws://54.116.63.204:8081/ws-stomp";

    private Long roomId;
    private String roomName;
    private String myEmail;

    private StompClient mStompClient;
    private Disposable mTopicDisposable;
    private Gson gson = new Gson();

    private RecyclerView rvMessages;
    private ChattingRoomAdapter chattingRoomAdapter;
    private EditText etMessage;
    private TextView tvRoomName;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);

        // 상태바 / 하단 네비게이션 바 색상 설정
        getWindow().setStatusBarColor(Color.parseColor("#FFEB3B"));
        getWindow().setNavigationBarColor(Color.WHITE);

        // Android 10 이상에서 네비게이션 바 대비 효과 제거
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // 상태바 아이콘을 어둡게 표시
        windowInsetsController.setAppearanceLightStatusBars(true);

        // Android 8.0 이상에서 하단 네비게이션 아이콘을 어둡게 표시
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowInsetsController.setAppearanceLightNavigationBars(true);
        }

        initViews();

        // Intent에서 방 정보 꺼내기
        Intent intent = getIntent();
        roomId = intent.getLongExtra("roomId", -1);
        roomName = intent.getStringExtra("roomName");

        myEmail = getUserEmail(this);

        if (roomId == -1) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvRoomName.setText(roomName != null ? roomName : "상담방");

        chattingRoomAdapter = new ChattingRoomAdapter(myEmail, yaksokId -> {
            Toast.makeText(this, "약속 " + yaksokId + " 열기 (미구현)", Toast.LENGTH_SHORT).show();
        });

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(chattingRoomAdapter);

        loadPreviousMessages();

        // 소켓 연결 시작
        connectStomp();

        // 전송 버튼 클릭 이벤트
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();

            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    private void loadPreviousMessages() {
        NetworkClient.getChatApi().getPreviousMessages(roomId)
                .enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ChatMessage>>> call,
                            Response<ApiResponse<List<ChatMessage>>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChatMessage> pastMessages = response.body().getData();

                            if (pastMessages != null && !pastMessages.isEmpty()) {
                                for (ChatMessage msg : pastMessages) {
                                    chattingRoomAdapter.addMessage(msg);
                                }

                                rvMessages.scrollToPosition(chattingRoomAdapter.getItemCount() - 1);

                                Log.d(TAG, "과거 메시지 " + pastMessages.size() + "개 로드 완료");
                            }
                        } else {
                            Log.e(TAG, "과거 메시지 조회 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) {
                        Log.e(TAG, "네트워크 통신 오류 (과거 메시지): ", t);
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void connectStomp() {
        // STOMP 클라이언트 생성
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_URL);

        // 연결 상태 모니터링
        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d(TAG, "STOMP 연결 성공!");
                    subscribeToRoom();
                    break;

                case ERROR:
                    Log.e(TAG, "STOMP 연결 오류", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    Log.d(TAG, "STOMP 연결 종료");
                    break;
            }
        });

        // 서버에 연결 시도
        mStompClient.connect();
    }

    private void subscribeToRoom() {
        String topicPath = "/sub/chat/room/" + roomId;

        mTopicDisposable = mStompClient.topic(topicPath)
                .subscribe(topicMessage -> {
                    ChatMessage receivedMessage =
                            gson.fromJson(topicMessage.getPayload(), ChatMessage.class);

                    runOnUiThread(() -> {
                        chattingRoomAdapter.addMessage(receivedMessage);
                        rvMessages.scrollToPosition(chattingRoomAdapter.getItemCount() - 1);
                    });
                }, throwable -> {
                    Log.e(TAG, "구독 중 에러 발생", throwable);
                });
    }

    @SuppressLint("CheckResult")
    private void sendMessage(String text) {
        ChatMessage chatMessage = new ChatMessage(String.valueOf(roomId), myEmail, ChatMessage.MessageType.TEXT, text);
        String jsonPayload = gson.toJson(chatMessage);

        mStompClient.send("/pub/chat/message", jsonPayload)
                .subscribe(() -> {
                    Log.d(TAG, "메시지 전송 성공!");
                }, throwable -> {
                    Log.e(TAG, "메시지 전송 실패", throwable);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTopicDisposable != null && !mTopicDisposable.isDisposed()) {
            mTopicDisposable.dispose();
        }

        if (mStompClient != null) {
            mStompClient.disconnect();
        }
    }

    private void initViews() {
        tvRoomName = findViewById(R.id.tv_room_name);
        rvMessages = findViewById(R.id.rv_chat_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
    }
}