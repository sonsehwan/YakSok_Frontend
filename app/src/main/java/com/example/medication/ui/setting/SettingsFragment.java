package com.example.medication.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medication.databinding.FragmentSettingsBinding;
import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.common.AppDialog;
import com.example.medication.ui.login.Login;
import com.example.medication.ui.myinfo.MyInfo;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsUtil.applySystemBarPadding(binding.main);

        setupClickListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupClickListeners() {
        binding.ivLogout.setOnClickListener(v -> showLogOutDialog());

        binding.llMyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyInfo.class);
            startActivity(intent);
        });

        UserResponse user = SprefsManager.getUser(requireContext());
        binding.swNotification.setChecked(user != null && SprefsManager.isNotificationEnabled(requireContext()));
        binding.swNotification.setOnCheckedChangeListener(this::onNotificationToggle);
    }

    private void onNotificationToggle(CompoundButton button, boolean enabled) {
       SprefsManager.setNotificationEnabled(button.getContext(), enabled);
    }

    private void showLogOutDialog() {
        AppDialog.confirm(requireContext(), "로그아웃", "로그아웃 하시겠습니까?", "로그아웃", false, () -> {
            // clearUserInfo 가 사용자 정보를 지우므로 그 전에 카카오 회원인지 확인해 둔다
            UserResponse user = SprefsManager.getUser(requireContext());
            boolean isKakaoUser = user != null && user.getSocialType() != null;

            deleteToken();
            SprefsManager.clearUserInfo(requireContext());
            if (isKakaoUser) {
                kakaoLogout();
            }
            requireActivity().finishAffinity();
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
        });
    }

    // 카카오 SDK가 앱 안에 들고 있는 카카오 토큰을 지운다. 실패해도 우리 앱 로그아웃에는 영향이 없다
    private void kakaoLogout() {
        UserApiClient.getInstance().logout(error -> {
            if (error != null) {
                Log.w("KakaoLogin", "카카오 로그아웃 실패", error);
            }
            return Unit.INSTANCE;
        });
    }

    private void deleteToken() {
        Long userId = SprefsManager.getUserId(requireContext());

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FcmToken", "로그아웃 중 토큰 조회 실패", task.getException());
                        return;
                    }
                    sendTokenDeleteToServer(userId, task.getResult());
                });
    }

    private void sendTokenDeleteToServer(Long userId, String token) {
        FirebaseTokenRequest request = new FirebaseTokenRequest(token);
        UserApi api = NetworkClient.getApi();

        api.deleteFcmToken(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d("FcmToken", "토큰을 저장적으로 삭제하였습니다.");
                } else {
                    Log.e("FcmToken", "토큰 삭제에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("FcmToken", "네트워크 통신 실패: " + t.getMessage());
            }
        });
    }

}
