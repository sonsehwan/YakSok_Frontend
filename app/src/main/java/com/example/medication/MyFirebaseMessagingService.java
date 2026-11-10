package com.example.medication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.chattingroom.ChattingRoom;
import com.example.medication.ui.main.MainActivity;
import com.example.medication.util.ChatEventBus;
import com.example.medication.util.FriendEventBus;
import com.example.medication.util.SprefsManager;
import com.example.medication.util.YaksokEventBus;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // 기기 토큰이 새로 발급되거나 갱신될 때 호출됨
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("Create_New_Token", "새로 생성된 등록 토큰: " + token);
        if (SprefsManager.isLoggedIn(this)) {
            UserResponse user = SprefsManager.getUser(this);
            if (user != null && user.getId() != null) {
                sendRegistrationToServer(user.getId(), token);
            }
        }
    }

    private void sendRegistrationToServer(Long userId, String token) {
        FirebaseTokenRequest request = new FirebaseTokenRequest(token);
        UserApi api = NetworkClient.getApi();

        api.updateFcmToken(request).enqueue(new Callback<ApiResponse<Void>>(){
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response){
                if(response.isSuccessful()){
                    Log.d("FcmToken", "새 토큰이 서버에 자동으로 업데이트되었습니다.");
                }else{
                    Log.e("FcmToken", "토큰 자동업데이트에 실패하였습니다." + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("FcmToken", "네트워크 통신 실패: " + t.getMessage());
            }
        });
    }

    // 포그라운드 상태이거나 데이터 메시지를 수신했을 때 호출됨
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData().isEmpty()) return;

        String type = remoteMessage.getData().get("type");

        if("REFRESH".equals(type)){
            Log.d("FCM", "REFRESH 수신 - 화면 갱신 신호 발행");
            YaksokEventBus.get().publish();
            return;
        }

        if("CHAT".equals(type)){
            ChatEventBus.get().publish();
            String roomId = remoteMessage.getData().get("roomId");
            if(roomId != null && roomId.equals(ChattingRoom.currentRoomId)) return;
        }

        if("FRIENDSHIP".equals(type)){
            FriendEventBus.get().publish();   // 알림은 아래 공통 흐름에서 띄운다
        }

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String notiIdStr = remoteMessage.getData().get("notificationId");

            int notificationId = 0;
            if(notiIdStr != null) {
                notificationId = Integer.parseInt(notiIdStr);
            }

            if (title != null && body != null && SprefsManager.isNotificationEnabled(this)) {
                showNotification(title, body, notificationId, type, remoteMessage.getData());
            }
    }

    private String resolveChannelId(String type, NotificationManager notificationManager) {
        String channelId;
        String channelName;
        String channelDesc;

        if ("TAKEN_ALERT".equals(type)) {
            channelId = "yaksok_share_channel";
            channelName = "공유 약속 알림";
            channelDesc = "공유받은 약속의 복약 완료 알림을 수신합니다.";
        } else if ("CHAT".equals(type)) {
            channelId = "yaksok_chat_channel";
            channelName = "채팅 알림";
            channelDesc = "새 채팅 메시지 알림을 수신합니다.";
        } else if ("FRIENDSHIP".equals(type)) {
            channelId = "yaksok_friend_channel";
            channelName = "친구 알림";
            channelDesc = "친구 요청과 수락 알림을 수신합니다.";
        } else {
            channelId = "yaksok_channel";
            channelName = "복약 알림";
            channelDesc = "YakSok의 약 복용 시간 알림을 수신합니다.";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channelDesc);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            notificationManager.createNotificationChannel(channel);
        }
        return channelId;
    }

    private void showNotification(String title, String body, int notificationId, String type,
                                  Map<String, String> data) {
        Log.d("알림", "알림 생성을 시작합니다.");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = resolveChannelId(type, notificationManager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        String safeType = (type != null) ? type : "REMINDER";
        builder.setContentIntent(openScreenIntent(safeType));
        switch (safeType) {
            case "TAKEN_ALERT":
                break;
            case "CHAT":
                break;
            case "FRIENDSHIP":
                // 받은 요청(requestId 있음)에만 버튼을 붙인다. "친구가 되었어요" 알림에는 없다.
                String requestId = data.get("requestId");
                if (requestId != null) {
                    addFriendRequestActions(builder, notificationId, Long.parseLong(requestId));
                }
                break;
            case "REMINDER":
            default:
                Intent completeIntent = new Intent(this, NotificationActionReceiver.class);
                completeIntent.setAction("ACTION_COMPLETE");
                completeIntent.putExtra("notificationId", notificationId);
                completeIntent.putExtra("notificationTag", safeType);   // notify와 같은 tag로 지워야 사라진다
                PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                        this, notificationId, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(0, "약속 완료", completePendingIntent);
                break;
        }
        notificationManager.notify(safeType, notificationId, builder.build());
    }

    // 받은 친구 요청 알림의 수락/거절 버튼. 누르면 NotificationActionReceiver가 응답 API를 호출한다.
    private void addFriendRequestActions(NotificationCompat.Builder builder, int notificationId, long requestId) {
        builder.addAction(0, "수락", friendAnswerIntent("ACTION_FRIEND_ACCEPT", notificationId, requestId))
                .addAction(0, "거절", friendAnswerIntent("ACTION_FRIEND_REJECT", notificationId, requestId));
    }

    private PendingIntent friendAnswerIntent(String action, int notificationId, long requestId) {
        Intent intent = new Intent(this, NotificationActionReceiver.class);
        intent.setAction(action);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("notificationTag", "FRIENDSHIP");
        intent.putExtra("requestId", requestId);
        // 수락/거절은 action이 달라 구분되고, requestCode를 요청 id로 줘서 요청마다 extras가 덮이지 않는다.
        return PendingIntent.getBroadcast(this, (int) requestId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent openScreenIntent(String type){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        switch (type) {
            case "TAKEN_ALERT":
                intent.putExtra(MainActivity.EXTRA_TARGET_TAB, R.id.nav_history);
                intent.putExtra(MainActivity.EXTRA_SHOW_SHARED_YAKSOK, true);
                break;
            case "FRIENDSHIP":
                intent.putExtra(MainActivity.EXTRA_TARGET_TAB, R.id.nav_friend);
                break;
            case "CHAT":
                intent.putExtra(MainActivity.EXTRA_TARGET_TAB, R.id.nav_chat);
                break;
            default: // REMINDER(복약)
                intent.putExtra(MainActivity.EXTRA_TARGET_TAB, R.id.nav_home);
                break;
        }

        return PendingIntent.getActivity(
                this,
                type.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}