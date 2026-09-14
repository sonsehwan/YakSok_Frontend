package com.example.medication.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.medication.R;
import com.example.medication.model.request.FirebaseTokenRequest;
import com.example.medication.model.response.ApiResponse;
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

    private ImageView ivLogout;
    private LinearLayout llMyInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsUtil.applySystemBarPadding(view.findViewById(R.id.main));

        initViews(view);
        setupClickListeners();
    }

    private void setupClickListeners() {
        ivLogout.setOnClickListener(v -> showLogOutDialog());

        llMyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyInfo.class);
            startActivity(intent);
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

    private void initViews(View root) {
        ivLogout = root.findViewById(R.id.iv_logout);
        llMyInfo = root.findViewById(R.id.ll_my_info);
    }
}
