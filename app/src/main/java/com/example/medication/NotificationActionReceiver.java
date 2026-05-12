package com.example.medication;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        int notificationId = intent.getIntExtra("notificationId", -1);

        // 버튼을 누르면 알림창을 지운다.
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationId != -1){
            manager.cancel(notificationId);
        }

        // 약속 완료 버튼 클릭시
        if ("ACTION_COMPLETE".equals(action)) {
            Log.d("NotificationAction", "약속 완료 버튼 클릭됨 ID: " + notificationId);
            completeNotification(context, notificationId);

        } else if ("ACTION_SNOOZE".equals(action)) { // 미루기 버튼 클릭 시
            Log.d("NotificationAction", "미루기 버튼 클릭됨 ID: " + notificationId);
            Toast.makeText(context, "10분 뒤에 다시 알려드릴게요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void completeNotification(Context context, int notificationId){
        NetworkClient.getYaksokApi().updateNotificationStatus((long)notificationId, true)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (!response.isSuccessful()) {
                            Log.e("통신 실패", "알림 상태 변경 실패: " + response.code());
                            // [롤백(대체) 액션] 실패했음을 알리고 앱에서 확인하도록 유도
                            Toast.makeText(context, "서버 오류 발생! 앱을 열어 다시 완료해주세요.", Toast.LENGTH_LONG).show();
                        }else{
                            Log.d("통신 성공", "알림 상태 변경 성공");
                            // 성공했을 때만 완료 토스트를 띄워줍니다.
                            Toast.makeText(context, "약 복용이 완료 처리되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "원인 불명";
                        Log.e("통신 에러", errorMessage);
                        // [롤백(대체) 액션] 네트워크 에러 처리
                        Toast.makeText(context, "네트워크 오류! 앱을 열어 다시 완료해주세요.", Toast.LENGTH_LONG).show();
                    }
                });
    }


}
