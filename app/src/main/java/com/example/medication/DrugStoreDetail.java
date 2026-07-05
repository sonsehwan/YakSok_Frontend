package com.example.medication;

import static com.example.medication.util.SprefsManager.getUser;
import static com.example.medication.util.SprefsManager.getUserEmail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.databinding.ActivityDrugStoreDetailBinding;
import com.example.medication.model.DrugStore;
import com.example.medication.model.request.ChatRoomRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrugStoreDetail extends AppCompatActivity {

    private ActivityDrugStoreDetailBinding binding;
    DrugStore drugStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityDrugStoreDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        KakaoMapSdk.init(this, "0a910c6f0a6d191f86e46da9c0449d84");

        setupMapView();

        Intent intent = getIntent();

        drugStore = (DrugStore) intent.getSerializableExtra("drugStore");

        binding.tvDetailName.setText(drugStore.getDutyName());
        binding.tvDetailAddr.setText(drugStore.getDutyAddr());
        binding.tvDetailTel.setText(drugStore.getDutyTel1());
        binding.tvDetailHours.setText(formatTime(drugStore.getStartTime(), drugStore.getEndTime()));

        binding.btnStartChatting.setOnClickListener(v -> {
            UserResponse currentUser = getUser(this);
            String userRole = currentUser.getRole();
            String userDrugStoreHpid = null;
            // 로그인 회원이 약사일 경우
            if(Objects.equals(userRole, "DRUGSTORE")){
                userDrugStoreHpid = currentUser.getMyDrugStore().getHpid();
                Log.d("userDrugStoreHpid", userDrugStoreHpid);
                Log.d("drugStoreHpid", drugStore.getHpid());
            }
            String userEmail = getUserEmail(this);
            String hpid = drugStore.getHpid();

            if (hpid == null) {
                Toast.makeText(DrugStoreDetail.this, "약국 정보(ID)를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            //Objects에서 equals로 비교하면 userEmail도 null확인을 해준다.
            if(Objects.equals(userDrugStoreHpid, hpid)){
                Toast.makeText(DrugStoreDetail.this, "자신의 약국에 채팅을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            ChatRoomRequest request = new ChatRoomRequest(userEmail, hpid);

            // Retrofit 통신 시작
            NetworkClient.getChatApi().enterChatRoom(request).enqueue(new Callback<ApiResponse<ChatRoomResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ChatRoomResponse>> call, Response<ApiResponse<ChatRoomResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        ChatRoomResponse roomResponse = (ChatRoomResponse) response.body().getData();
                        Long roomId = roomResponse.getRoomId();

                        Intent chatIntent = new Intent(DrugStoreDetail.this, ChattingRoom.class);
                        chatIntent.putExtra("roomId", roomId);
                        chatIntent.putExtra("roomName", drugStore.getDutyName());
                        startActivity(chatIntent);

                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "에러 내용 없음";
                            Log.e("ChatApiError", "서버 에러 상세: " + errorBody);
                            Toast.makeText(DrugStoreDetail.this, "실패 원인: " + errorBody, Toast.LENGTH_LONG).show();
                            Log.e("ChatApiError", "서버 에러 상세: " + errorBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) {
                    Toast.makeText(DrugStoreDetail.this, "네트워크 통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void setupMapView(){
        binding.mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                Log.d("KakaoMap", "onMapDestroy: 지도가 종료되었습니다.");
            }

            @Override
            public void onMapError(Exception e) {
                Log.e("KakaoMap", "onMapError: " + e.getMessage());
            }
        }, new KakaoMapReadyCallback(){
            @Override
            public void onMapReady(@NonNull KakaoMap kakaoMap) {
                showDrugStoreOnMap(kakaoMap);
            }
        });
    }

    private void showDrugStoreOnMap(KakaoMap kakaoMap){
        String latStr = drugStore.getLatitude();
        String lngStr = drugStore.getLongitude();

        if(latStr != null && lngStr != null){
            try{
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);

                LatLng position = LatLng.from(lat, lng);

                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(position, 16));

                LabelStyles styles = kakaoMap.getLabelManager().addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.ic_mark)));

                // 2. 위치와 스타일을 합쳐서 옵션 만들기
                LabelOptions options = LabelOptions.from(position).setStyles(styles);

                // 3. 지도에 최종적으로 마커 띄우기
                kakaoMap.getLabelManager().getLayer().addLabel(options);
            }catch (NumberFormatException e){
                Log.e("KakaoMap", "좌표 변환 오류", e);
            }
        }
    }

    private String formatTime(String startTime, String endTime) {
        String fstartTime = startTime.substring(0,2);
        String lstartTime = startTime.substring(2);
        String finalStartTime = fstartTime + ":" + lstartTime;

        String fendTime = endTime.substring(0,2);
        String lendTime = endTime.substring(2);
        String finalEndTime = fendTime + ":" + lendTime;

        return finalStartTime + " ~ " + finalEndTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.resume(); // 지도 렌더링 재개
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.pause(); // 지도 렌더링 일시정지
    }
}