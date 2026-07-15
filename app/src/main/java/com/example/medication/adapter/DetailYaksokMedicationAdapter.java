package com.example.medication.adapter;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_medication_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PillRequest item = items.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .into(holder.ivImage);

        holder.tvName.setText(item.getName());
        holder.tvFreq.setText(item.getDailyFrequency() + " 번");
        holder.etDosage.setText(item.getDosage());

        holder.etDosage.setEnabled(false);
        holder.etDosage.setFocusable(false);
        holder.etDosage.setClickable(false);

        holder.btnFreqPlus.setVisibility(View.INVISIBLE);
        holder.btnFreqMinus.setVisibility(View.INVISIBLE);
        holder.btnRemove.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvFreq;
        EditText etDosage;
        TextView btnFreqPlus, btnFreqMinus;
        ImageView btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_pill_img);
            tvName = itemView.findViewById(R.id.tv_pill_name);
            tvFreq = itemView.findViewById(R.id.tv_freq_value);
            etDosage = itemView.findViewById(R.id.et_dosage_value);
            btnFreqPlus = itemView.findViewById(R.id.btn_freq_plus);
            btnFreqMinus = itemView.findViewById(R.id.btn_freq_minus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}