package com.example.medication.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yaksok, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Yaksok item = items.get(position);

        String owner = item.getOwnerNickname();
        holder.tvTitle.setText(owner != null ? owner + "님의 " + item.getTitle() : item.getTitle());

        holder.tvPeriod.setText(item.getStartDate() + " 부터 " + item.getPrescriptionDays() + "일간");

        // 원본을 참조하므로 상대방이 약을 먹을 때마다 이 값이 최신으로 바뀐다.
        updateProgress(holder, item.getCurrentClearNotifications(), item.getTotalNotifications());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(item);
            return true;
        });
    }

    // 복약 진행률을 원형 게이지에 그린다. MainActivity 의 진행률 표시와 같은 규칙을 쓴다.
    private void updateProgress(ViewHolder holder, int done, int total) {
        int percent = total > 0 ? (int) (((float) done / total) * 100) : 0;

        // 0%면 게이지가 아무것도 안 그려져 비어 보이므로, 링을 꽉 채운 뒤 빨간색으로 표시한다.
        int visualPercent = (percent == 0) ? 100 : percent;

        holder.progressBar.setProgress(visualPercent);
        holder.tvPercent.setText(percent + "%");

        Context context = holder.itemView.getContext();
        int tintColor;

        if (percent == 0) {
            tintColor = ContextCompat.getColor(context, R.color.status_missed);
        } else if (percent == 100) {
            tintColor = ContextCompat.getColor(context, R.color.status_done);
        } else {
            tintColor = ContextCompat.getColor(context, R.color.status_pending);
        }

        holder.progressBar.setProgressTintList(ColorStateList.valueOf(tintColor));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPeriod, tvPercent;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_yaksok_title);
            tvPeriod = itemView.findViewById(R.id.tv_yaksok_period);
            tvPercent = itemView.findViewById(R.id.tv_progress_percent);
            progressBar = itemView.findViewById(R.id.status_yaksok);
        }
    }
}