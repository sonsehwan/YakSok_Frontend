package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.DrugStore;

import java.util.ArrayList;
import java.util.List;

public class DrugStoreAdapter extends RecyclerView.Adapter<DrugStoreAdapter.ViewHolder> {

    private final List<DrugStore> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DrugStore drugStore);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void addItems(List<DrugStore> newItems) {
        int startPosition = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(startPosition, items.size());
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
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