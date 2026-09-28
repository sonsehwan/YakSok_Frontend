package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.databinding.ItemYaksokBinding;
import com.example.medication.model.Yaksok;

import java.util.List;

public class ShareYaksokListAdapter extends RecyclerView.Adapter<ShareYaksokListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Yaksok yaksok);
        void onItemLongClick(Yaksok yaksok);
    }

    private final List<Yaksok> items;
    private final OnItemClickListener listener;

    public ShareYaksokListAdapter(List<Yaksok> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemYaksokBinding binding = ItemYaksokBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Yaksok item = items.get(position);

        String owner = item.getOwnerNickname();
        holder.binding.tvYaksokTitle.setText(owner != null ? owner + "님의 " + item.getTitle() : item.getTitle());

        holder.binding.tvYaksokPeriod.setText(item.getStartDate() + " 시작 · " + item.getPrescriptionDays() + "일");

        bindBadges(holder, item);
        bindPercent(holder, item);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(item);
            return true;
        });
    }

    // 전체 복약 알림 중 완료한 비율(총 알림 0건이면 0%)
    private void bindPercent(ViewHolder holder, Yaksok item) {
        int total = item.getTotalNotifications();
        int percent = total > 0 ? (int) (((float) item.getCurrentClearNotifications() / total) * 100) : 0;
        holder.binding.tvYaksokPercent.setText(percent + "%");
    }

    // 복용 시간대 뱃지(아침/점심/저녁, 식후 안내)를 해당하는 것만 보여준다.
    private void bindBadges(ViewHolder holder, Yaksok item) {
        holder.binding.tvBadgeMorning.setVisibility(item.isTakeMorning() ? View.VISIBLE : View.GONE);
        holder.binding.tvBadgeLunch.setVisibility(item.isTakeLunch() ? View.VISIBLE : View.GONE);
        holder.binding.tvBadgeDinner.setVisibility(item.isTakeDinner() ? View.VISIBLE : View.GONE);

        String dosageTime = item.getDosageTime();
        boolean hasDosageTime = dosageTime != null && !dosageTime.isEmpty();
        holder.binding.tvBadgeDosage.setVisibility(hasDosageTime ? View.VISIBLE : View.GONE);
        if (hasDosageTime) {
            holder.binding.tvBadgeDosage.setText(dosageTime);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<Yaksok> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemYaksokBinding binding;

        ViewHolder(@NonNull ItemYaksokBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
