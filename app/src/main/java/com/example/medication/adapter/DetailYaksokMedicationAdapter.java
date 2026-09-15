package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medication.databinding.ItemAddMedicationSettingBinding;
import com.example.medication.model.request.PillRequest;

import java.util.List;

public class DetailYaksokMedicationAdapter extends RecyclerView.Adapter<DetailYaksokMedicationAdapter.ViewHolder> {

    private final List<PillRequest> items;

    public DetailYaksokMedicationAdapter(List<PillRequest> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddMedicationSettingBinding binding = ItemAddMedicationSettingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PillRequest item = items.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .into(holder.binding.ivPillImg);

        holder.binding.tvPillName.setText(item.getName());
        holder.binding.tvFreqValue.setText(item.getDailyFrequency() + " 번");
        holder.binding.etDosageValue.setText(item.getDosage());

        holder.binding.etDosageValue.setEnabled(false);
        holder.binding.etDosageValue.setFocusable(false);
        holder.binding.etDosageValue.setClickable(false);

        holder.binding.btnFreqPlus.setVisibility(View.INVISIBLE);
        holder.binding.btnFreqMinus.setVisibility(View.INVISIBLE);
        holder.binding.btnRemove.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAddMedicationSettingBinding binding;

        public ViewHolder(@NonNull ItemAddMedicationSettingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
