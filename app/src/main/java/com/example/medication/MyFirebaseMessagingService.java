package com.example.medication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // 기기 토큰이 새로 발급되거나 갱신될 때 호출됨
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("Create_New_Token", "새로 생성된 등록 토큰: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Retrofit 등을 사용하여 백엔드 API(/api/users/fcm-token) 호출
        // 이 토큰이 있어야 서버에서 사용자님 기기를 찾아 알림을 쏠 수 있습니다.
    }

    // 포그라운드 상태이거나 데이터 메시지를 수신했을 때 호출됨
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "yaksok_channel";

        // Android 8.0 이상 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "YakSok Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        notificationManager.notify(0, builder.build());
    }
}