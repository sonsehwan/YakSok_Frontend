package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShowAddMedicationList extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceStatue){
        View view = inflater.inflate(R.layout.show_add_medcation_list, container, false);

        LinearLayout btnPrescription = view.findViewById(R.id.ll_create_prescription);
        LinearLayout btnEnvelope = view.findViewById(R.id.ll_create_medicine_envelope);
        LinearLayout btnDirect = view.findViewById(R.id.ll_create_direct);

        btnPrescription.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreatePrescription.class));
            dismiss();
        });

        btnEnvelope.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreatePrescription.class));
            dismiss();
        });

        btnDirect.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreateDirectSchedule.class));
            dismiss();
        });

        return view;
    }
}