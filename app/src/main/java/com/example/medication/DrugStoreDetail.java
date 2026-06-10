package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.databinding.ActivityDrugStoreDetailBinding;
import com.example.medication.model.DrugStore;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

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