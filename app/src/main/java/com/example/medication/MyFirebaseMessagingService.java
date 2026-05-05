package com.example.medication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
        FirebaseTokenRequest request = new FirebaseTokenRequest(email, token);
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

        // 알림 메시지(Notification)가 포함된 경우
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body);
        }

        // 데이터 메시지(Data)가 포함된 경우(커스텀 알림용)
        if (!remoteMessage.getData().isEmpty()) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            if (title != null && body != null) {
                showNotification(title, body);
            }
        }
    }

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "yaksok_channel";

        // Android 8.0 이상 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "YakSok Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("YakSok의 약 복용 시간 알림을 수신합니다.");
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