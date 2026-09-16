package com.example.medication.ui.friend;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medication.databinding.ActivityProfileBinding;
import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.UserProfileDto;
import com.example.medication.network.NetworkClient;
import com.example.medication.ui.base.BaseActivity;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends BaseActivity {

    public static final String EXTRA_QR_CODE = "EXTRA_QR_CODE";

    private ActivityProfileBinding binding;
    private String qrCode;
    private UserProfileDto profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.ivBack.setOnClickListener(v -> finish());

        qrCode = getIntent().getStringExtra(EXTRA_QR_CODE);
        if (qrCode == null || qrCode.isEmpty()) {
            Toast.makeText(this, "잘못된 QR 코드입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchProfile();
    }

    private void fetchProfile() {
        NetworkClient.getFriendApi().getUserProfile(qrCode)
                .enqueue(new Callback<ApiResponse<UserProfileDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserProfileDto>> call,
                                           Response<ApiResponse<UserProfileDto>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            bindProfile(response.body().getData());
                        } else {
                            showError(response, "프로필을 불러오지 못했습니다.");
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserProfileDto>> call, Throwable t) {
                        Toast.makeText(Profile.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void bindProfile(UserProfileDto profile) {
        this.profile = profile;

        String nickname = profile.getNickname();
        binding.tvAvatar.setText((nickname == null || nickname.isEmpty()) ? "?" : nickname.substring(0, 1));
        binding.tvNickname.setText(nickname);
        binding.tvEmail.setText(profile.getEmail());

        switch (profile.getRelation()) {
            case SELF:
                binding.btnAction.setVisibility(android.view.View.GONE);
                break;
            case FRIEND:
                setActionButton("이미 친구입니다", false, null);
                break;
            case SENT:
                setActionButton("요청 보냄", false, null);
                break;
            case RECEIVED:
                setActionButton("친구 요청 수락하기", true, v -> acceptFriendRequest());
                break;
            case NONE:
            default:
                setActionButton("친구 요청 보내기", true, v -> sendFriendRequest());
                break;
        }
    }

    private void setActionButton(String text, boolean enabled, android.view.View.OnClickListener listener) {
        binding.btnAction.setVisibility(android.view.View.VISIBLE);
        binding.btnAction.setText(text);
        binding.btnAction.setEnabled(enabled);
        binding.btnAction.setAlpha(enabled ? 1f : 0.5f);
        binding.btnAction.setOnClickListener(listener);
    }

    private void sendFriendRequest() {
        FriendRequestCreateDto request = new FriendRequestCreateDto(profile.getUserId());

        NetworkClient.getFriendApi().createFriendRequest(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(Profile.this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                            fetchProfile();
                        } else {
                            showError(response, "친구 요청에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(Profile.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptFriendRequest() {
        FriendRequestAnswerDto answer = new FriendRequestAnswerDto(true);

        NetworkClient.getFriendApi().answerFriendRequest(profile.getRequestId(), answer)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(Profile.this, "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
                            fetchProfile();
                        } else {
                            showError(response, "요청 처리에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(Profile.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showError(Response<?> response, String fallback) {
        String message = fallback;
        try {
            if (response.errorBody() != null) {
                ApiResponse<?> error = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (error != null && error.getMessage() != null) {
                    message = error.getMessage();
                }
            }
        } catch (Exception ignored) {
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
