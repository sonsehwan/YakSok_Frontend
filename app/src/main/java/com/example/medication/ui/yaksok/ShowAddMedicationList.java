package com.example.medication.ui.yaksok;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.medication.databinding.BottomSheetAddYaksokBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShowAddMedicationList extends BottomSheetDialogFragment {

    private BottomSheetAddYaksokBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceStatue) {
        binding = BottomSheetAddYaksokBinding.inflate(inflater, container, false);

        binding.llCreatePrescription.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreatePrescription.class));
            dismiss();
        });

        binding.llCreateMedicineEnvelope.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreatePaperEnvelope.class));
            dismiss();
        });

        binding.llCreateDirect.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreateDirectSchedule.class));
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