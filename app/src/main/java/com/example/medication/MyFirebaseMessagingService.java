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
import com.example.medication.util.SprefsManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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
            if (user != null && user.getEmail() != null) {
                sendRegistrationToServer(user.getEmail(), token);
            }
        }
    }

    private void sendRegistrationToServer(String email, String token) {
        FirebaseTokenRequest request = new FirebaseTokenRequest(token);
        UserApi api = NetworkClient.getApi();

        api.updateFcmToken(email, request).enqueue(new Callback<ApiResponse<Void>>(){
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

        if (!remoteMessage.getData().isEmpty()) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String notiIdStr = remoteMessage.getData().get("notificationId");

            int notificationId = 0;
            if(notiIdStr != null) {
                notificationId = Integer.parseInt(notiIdStr);
            }

            if (title != null && body != null) {
                showNotification(title, body, notificationId);
            }
        }
    }

    private void showNotification(String title, String body, int notificationId) {
        Log.d("알림", "알림 생성을 시작합니다.");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "yaksok_channel";

        // Android 8.0 이상 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "복약 알림",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("YakSok의 약 복용 시간 알림을 수신합니다.");
            notificationManager.createNotificationChannel(channel);
        }

        Intent completeIntent = new Intent(this, NotificationActionReceiver.class);
        completeIntent.setAction("ACTION_COMPLETE");
        completeIntent.putExtra("notificationId", notificationId);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                this, notificationId, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(this, NotificationActionReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("notificationId", notificationId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                this, notificationId + 1000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(0, "약속 완료", completePendingIntent)
                .addAction(0, "약속 미루기", snoozePendingIntent);
        notificationManager.notify(notificationId, builder.build());
    }
}