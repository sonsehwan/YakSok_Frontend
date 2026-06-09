package com.example.medication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.databinding.ActivityDrugStoreDetailBinding;
import com.example.medication.model.DrugStore;

public class DrugStoreDetail extends AppCompatActivity {

    private ActivityDrugStoreDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityDrugStoreDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        DrugStore drugStore = (DrugStore) intent.getSerializableExtra("drugStore");

        binding.tvDetailName.setText(drugStore.getDutyName());
        binding.tvDetailAddr.setText(drugStore.getDutyAddr());
        binding.tvDetailTel.setText(drugStore.getDutyTel1());
        binding.tvDetailHours.setText(drugStore.getStartTime() + " ~ " + drugStore.getEndTime());
    }
}