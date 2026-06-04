package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.DrugStore;

import java.util.List;

public class DrugStoreAdapter extends RecyclerView.Adapter<DrugStoreAdapter.ViewHolder> {

    private List<DrugStore> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DrugStore drugStore, int position);
    }

    public DrugStoreAdapter(List<DrugStore> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drugstore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrugStore item = items.get(position);

        holder.tvName.setText(item.getDutyName());

        String fstartTime = item.getStartTime().substring(0,2);
        String lstartTime = item.getStartTime().substring(2);
        String finalStartTime = fstartTime + ":" + lstartTime;

        String fendTime = item.getEndTime().substring(0,2);
        String lendTime = item.getEndTime().substring(2);
        String finalEndTime = fendTime + ":" + lendTime;
        holder.tvHours.setText("영업시간: " + finalStartTime + " ~ " + finalEndTime);
        //holder.tvDistance.setText(item.getDistance() + "m");
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<DrugStore> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvHours, tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_drugstore_name);
            tvHours = itemView.findViewById(R.id.tv_drugstore_hours);
            tvDistance = itemView.findViewById(R.id.tv_drugstore_distance);
        }
    }
}