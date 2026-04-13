package com.example.medication.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medication.R;
import com.example.medication.model.MedicationSetting;

import java.util.List;

public class AddMedicationSettingAdapter extends RecyclerView.Adapter<AddMedicationSettingAdapter.ViewHolder> {

    private final List<MedicationSetting> items;

    public AddMedicationSettingAdapter(List<MedicationSetting> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_medication_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationSetting item = items.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .into(holder.ivImage);

        if (holder.dosageWatcher != null) {
            holder.etDosage.removeTextChangedListener(holder.dosageWatcher);
        }

        holder.tvName.setText(item.getName());
        holder.tvDays.setText(item.getPrescriptionDays() + " 일");
        holder.tvFreq.setText(item.getDailyFrequency() + " 번");
        holder.etDosage.setText(item.getDosage());

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
        holder.etDosage.addTextChangedListener(holder.dosageWatcher);

        holder.btnDaysPlus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                item.setPrescriptionDays(item.getPrescriptionDays() + 1);
                notifyItemChanged(currentPos);
            }
        });

        holder.btnDaysMinus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && item.getPrescriptionDays() > 1) {
                item.setPrescriptionDays(item.getPrescriptionDays() - 1);
                notifyItemChanged(currentPos);
            }
        });

        holder.btnFreqPlus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                item.setDailyFrequency(item.getDailyFrequency() + 1);
                notifyItemChanged(currentPos);
            }
        });

        holder.btnFreqMinus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && item.getDailyFrequency() > 1) {
                item.setDailyFrequency(item.getDailyFrequency() - 1);
                notifyItemChanged(currentPos);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
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
        ImageView ivImage;
        TextView tvName, tvDays, tvFreq;
        EditText etDosage;
        TextView btnDaysPlus, btnDaysMinus, btnFreqPlus, btnFreqMinus;
        ImageView btnRemove;
        TextWatcher dosageWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_pill_img);
            tvName = itemView.findViewById(R.id.tv_pill_name);
            tvDays = itemView.findViewById(R.id.tv_days_value);
            tvFreq = itemView.findViewById(R.id.tv_freq_value);
            etDosage = itemView.findViewById(R.id.et_dosage_value);
            btnDaysPlus = itemView.findViewById(R.id.btn_days_plus);
            btnDaysMinus = itemView.findViewById(R.id.btn_days_minus);
            btnFreqPlus = itemView.findViewById(R.id.btn_freq_plus);
            btnFreqMinus = itemView.findViewById(R.id.btn_freq_minus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}