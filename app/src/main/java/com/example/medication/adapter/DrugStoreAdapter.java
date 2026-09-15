package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.databinding.ItemDrugstoreBinding;
import com.example.medication.model.DrugStore;
import com.example.medication.util.LocationUtil;

import java.util.ArrayList;
import java.util.List;

public class DrugStoreAdapter extends RecyclerView.Adapter<DrugStoreAdapter.ViewHolder> {

    private final List<DrugStore> items = new ArrayList<>();
    private OnItemClickListener listener;
    private double myLat;
    private double myLng;

    public DrugStoreAdapter(double myLat, double myLng){
        this.myLat = myLat;
        this.myLng = myLng;
    }

    public void updateLocation(double currentLat, double currentLng) {
        this.myLat = currentLat;
        this.myLng = currentLng;
    }

    public interface OnItemClickListener {
        void onItemClick(DrugStore drugStore);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void addItems(List<DrugStore> newItems) {
        int startPosition = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDrugstoreBinding binding = ItemDrugstoreBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrugStore item = items.get(position);

        holder.binding.tvDrugstoreName.setText(item.getDutyName());


        holder.binding.tvDrugstoreHours.setText(formatTime(item.getStartTime(), item.getEndTime()));

        String distance = calculateDistance(myLat, myLng, item);
        holder.binding.tvDrugstoreDistance.setText(distance);

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
        ItemDrugstoreBinding binding;
        public ViewHolder(@NonNull ItemDrugstoreBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String calculateDistance(double startLat, double startLng,DrugStore item) {
        try{
            double endLat = Double.parseDouble(item.getLatitude());
            double endLng = Double.parseDouble(item.getLongitude());

            float distance = LocationUtil.calculateDistance(startLat, startLng, endLat, endLng);

            return LocationUtil.formatDistance(distance);
        }catch (Exception e){
            e.printStackTrace();
            return "거리를 알 수 없음";
        }
    }

    private String formatTime(String startTime, String endTime) {
        String fstartTime = startTime.substring(0,2);
        String lstartTime = startTime.substring(2);
        String finalStartTime = fstartTime + ":" + lstartTime;

        String fendTime = endTime.substring(0,2);
        String lendTime = endTime.substring(2);
        String finalEndTime = fendTime + ":" + lendTime;

        return "영업시간: " + finalStartTime + " ~ " + finalEndTime;
    }
}
