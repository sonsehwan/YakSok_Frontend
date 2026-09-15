package com.example.medication.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medication.databinding.ItemAddMedicationSettingBinding;
import com.example.medication.model.request.PillRequest;

import java.util.List;

public class AddMedicationSettingAdapter extends RecyclerView.Adapter<AddMedicationSettingAdapter.ViewHolder> {

    private final List<PillRequest> items;

    public AddMedicationSettingAdapter(List<PillRequest> items) {
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

        if (holder.dosageWatcher != null) {
            holder.binding.etDosageValue.removeTextChangedListener(holder.dosageWatcher);
        }

        holder.binding.tvPillName.setText(item.getName());
        holder.binding.tvFreqValue.setText(item.getDailyFrequency() + " 번");
        holder.binding.etDosageValue.setText(item.getDosage());

        holder.dosageWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setDosage(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        holder.binding.etDosageValue.addTextChangedListener(holder.dosageWatcher);

        holder.binding.btnFreqPlus.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                item.setDailyFrequency(item.getDailyFrequency() + 1);
                notifyItemChanged(currentPos);
            }
        });

        holder.binding.btnFreqMinus.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && item.getDailyFrequency() > 1) {
                item.setDailyFrequency(item.getDailyFrequency() - 1);
                notifyItemChanged(currentPos);
            }
        });

        holder.binding.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, items.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAddMedicationSettingBinding binding;
        TextWatcher dosageWatcher;

        public ViewHolder(@NonNull ItemAddMedicationSettingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
