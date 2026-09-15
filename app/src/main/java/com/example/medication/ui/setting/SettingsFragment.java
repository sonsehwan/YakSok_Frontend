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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.medication.databinding.FragmentSettingsBinding;
import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.request.NotificationSettingRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserResponse;
import com.example.medication.network.NetworkClient;
import com.example.medication.network.UserApi;
import com.example.medication.ui.login.Login;
import com.example.medication.ui.myinfo.MyInfo;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;
import com.google.firebase.messaging.FirebaseMessaging;

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
        binding.swNotification.setChecked(user != null && user.isNotificationEnabled());
        binding.swNotification.setOnCheckedChangeListener(this::onNotificationToggle);
    }

    private void onNotificationToggle(CompoundButton button, boolean enabled) {
        UserApi api = NetworkClient.getApi();
        api.updateNotificationSetting(new NotificationSettingRequest(enabled)).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    SprefsManager.setUserInfo(requireContext(), response.body().getData());
                } else {
                    Log.e("NotificationSetting", "알림 설정 변경 실패");
                    button.setOnCheckedChangeListener(null);
                    button.setChecked(!enabled);
                    button.setOnCheckedChangeListener(SettingsFragment.this::onNotificationToggle);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Log.e("NotificationSetting", "네트워크 통신 실패: " + t.getMessage());
                button.setOnCheckedChangeListener(null);
                button.setChecked(!enabled);
                button.setOnCheckedChangeListener(SettingsFragment.this::onNotificationToggle);
            }
        });
    }

    private void showLogOutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    deleteToken();
                    SprefsManager.clearUserInfo(requireContext());
                    requireActivity().finishAffinity();
                    Intent intent = new Intent(requireContext(), Login.class);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
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
