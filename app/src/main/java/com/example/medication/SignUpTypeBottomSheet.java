package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SignUpTypeBottomSheet extends BottomSheetDialogFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceStatue) {
        View view = inflater.inflate(R.layout.activity_sign_up_type_bottom_sheet, container, false);

        TextView user_type = view.findViewById(R.id.user_type);
        TextView drugStore_type = view.findViewById(R.id.drugStore_type);

        user_type.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SignUp_Normal.class);
            intent.putExtra("SignUp_Type", "NORMAL");
            startActivity(intent);
            dismiss();
        });

        drugStore_type.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SignUp_Normal.class);
            intent.putExtra("SignUp_Type", "DRUGSTORE");
            startActivity(intent);
            dismiss();
        });

        return view;
    }
}