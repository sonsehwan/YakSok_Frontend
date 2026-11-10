package com.example.medication.ui.setting;

import static com.example.medication.util.AuthMethod.deleteToken;
import static com.example.medication.util.KakaoLoginHelper.kakaoLogout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medication.databinding.FragmentSettingsBinding;
import com.example.medication.model.response.UserResponse;
import com.example.medication.ui.common.AppDialog;
import com.example.medication.ui.login.Login;
import com.example.medication.ui.myinfo.MyInfo;
import com.example.medication.util.InsetsUtil;
import com.example.medication.util.SprefsManager;

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
        binding.swNotification.setChecked(SprefsManager.isNotificationEnabled(requireContext()));
        binding.swNotification.setOnCheckedChangeListener(this::onNotificationToggle);
    }

    private void onNotificationToggle(CompoundButton button, boolean enabled) {
       SprefsManager.setNotificationEnabled(button.getContext(), enabled);
    }

    private void showLogOutDialog() {
        AppDialog.confirm(requireContext(), "로그아웃", "로그아웃 하시겠습니까?", "로그아웃", false, () -> {
            Activity activity = requireActivity();

            UserResponse user = SprefsManager.getUser(activity);
            boolean isKakaoUser = user != null && user.getSocialType() != null;

            deleteToken(activity, () -> {
                SprefsManager.clearUserInfo(activity);
                if (isKakaoUser) {
                    kakaoLogout();
                }
                activity.finishAffinity();
                activity.startActivity(new Intent(activity, Login.class));
            });
        });
    }
}
