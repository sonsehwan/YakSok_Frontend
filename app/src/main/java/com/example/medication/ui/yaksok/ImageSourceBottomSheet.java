package com.example.medication.ui.yaksok;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.medication.databinding.BottomSheetImageSourceBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class ImageSourceBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "image_source";
    public static final String KEY_URI = "uri";
    private static final String STATE_CAMERA_URI = "camera_uri";

    private BottomSheetImageSourceBinding binding;
    // 카메라 앱이 떠 있는 동안 프로세스가 죽을 수 있어 onSaveInstanceState로 보존한다
    private Uri pendingCameraUri;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::deliver);

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> deliver(Boolean.TRUE.equals(success) ? pendingCameraUri : null));

    // 매니페스트에 CAMERA가 선언돼 있어 권한 없이 촬영 인텐트를 보내면 SecurityException이 난다
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pendingCameraUri = savedInstanceState.getParcelable(STATE_CAMERA_URI);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetImageSourceBinding.inflate(inflater, container, false);

        binding.camera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        binding.album.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        return binding.getRoot();
    }

    private void launchCamera() {
        File dir = new File(requireContext().getCacheDir(), "images");
        dir.mkdirs();
        // 분석 후엔 필요 없는 사진이라 한 파일을 계속 덮어쓴다
        pendingCameraUri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                new File(dir, "capture.jpg"));
        cameraLauncher.launch(pendingCameraUri);
    }

    private void deliver(@Nullable Uri uri) {
        sendResult(uri);
        dismissAllowingStateLoss();
    }

    private void sendResult(@Nullable Uri uri) {
        Bundle result = new Bundle();
        result.putParcelable(KEY_URI, uri);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
    }

    // 바깥 터치·뒤로가기로 닫으면 취소로 알린다 (이미 닫히는 중이라 dismiss는 부르지 않음)
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        sendResult(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_CAMERA_URI, pendingCameraUri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
