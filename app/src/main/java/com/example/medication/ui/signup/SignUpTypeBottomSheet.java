package com.example.medication.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.medication.databinding.BottomSheetSignUpTypeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SignUpTypeBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetSignUpTypeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceStatue) {
        binding = BottomSheetSignUpTypeBinding.inflate(inflater, container, false);

        binding.userType.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SignUp_Normal.class);
            intent.putExtra("SignUp_Type", "NORMAL");
            startActivity(intent);
            dismiss();
        });

        binding.drugStoreType.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SignUp_DrugStore.class);
            intent.putExtra("SignUp_Type", "DRUGSTORE");
            startActivity(intent);
            dismiss();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}