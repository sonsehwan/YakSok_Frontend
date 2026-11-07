package com.example.medication;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.util.FriendEventBus;
import com.example.medication.util.YaksokEventBus;

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
            manager.cancel(intent.getStringExtra("notificationTag"), notificationId);
        }

        // 약속 완료 버튼 클릭시
        if ("ACTION_COMPLETE".equals(action)) {
            Log.d("NotificationAction", "약속 완료 버튼 클릭됨 ID: " + notificationId);
            PendingResult pendingResult = goAsync();
            completeNotification(context, notificationId, pendingResult);

        } else if ("ACTION_SNOOZE".equals(action)) { // 미루기 버튼 클릭 시
            Log.d("NotificationAction", "미루기 버튼 클릭됨 ID: " + notificationId);
            Toast.makeText(context, "10분 뒤에 다시 알려드릴게요.", Toast.LENGTH_SHORT).show();

        } else if ("ACTION_FRIEND_ACCEPT".equals(action) || "ACTION_FRIEND_REJECT".equals(action)) { // 친구 요청 수락/거절
            long requestId = intent.getLongExtra("requestId", -1);
            if (requestId != -1) {
                answerFriendRequest(context, requestId, "ACTION_FRIEND_ACCEPT".equals(action), goAsync());
            }
        }
    }

    private void answerFriendRequest(Context context, long requestId, boolean accept, PendingResult pendingResult) {
        NetworkClient.getFriendApi().answerFriendRequest(requestId, new FriendRequestAnswerDto(accept))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        try {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, accept ? "친구 요청을 수락했습니다." : "친구 요청을 거절했습니다.",
                                        Toast.LENGTH_SHORT).show();
                                FriendEventBus.get().publish();
                            } else {
                                // 이미 앱에서 처리한 요청이면 서버가 거절한다.
                                Log.e("NotificationAction", "친구 요청 응답 실패: " + response.code());
                                Toast.makeText(context, "요청을 처리하지 못했습니다. 앱에서 확인해주세요.", Toast.LENGTH_LONG).show();
                            }
                        } finally {
                            pendingResult.finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        try {
                            Log.e("NotificationAction", "친구 요청 응답 네트워크 오류", t);
                            Toast.makeText(context, "네트워크 오류! 앱에서 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        } finally {
                            pendingResult.finish();
                        }
                    }
                });
    }

    public void completeNotification(Context context, int notificationId, PendingResult pendingResult){
        NetworkClient.getYaksokApi().updateNotificationStatus((long)notificationId, true)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        try{
                            if (!response.isSuccessful()) {
                                Log.e("통신 실패", "알림 상태 변경 실패: " + response.code());
                                Toast.makeText(context, "서버 오류 발생! 앱을 열어 다시 완료해주세요.", Toast.LENGTH_LONG).show();
                            }else{
                                Log.d("통신 성공", "알림 상태 변경 성공");
                                Toast.makeText(context, "약 복용이 완료 처리되었습니다.", Toast.LENGTH_SHORT).show();
                                YaksokEventBus.get().publish();
                            }
                        }finally {
                            pendingResult.finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        try{
                            String errorMessage = t.getMessage() != null ? t.getMessage() : "원인 불명";
                            Log.e("통신 에러", errorMessage);
                            Toast.makeText(context, "네트워크 오류! 앱을 열어 다시 완료해주세요.", Toast.LENGTH_LONG).show();
                        }finally{
                            pendingResult.finish();
                        }
                    }
                });
    }


}
