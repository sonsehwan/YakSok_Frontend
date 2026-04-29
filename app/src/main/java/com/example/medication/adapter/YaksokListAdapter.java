package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.Yaksok;

import java.util.List;

public class YaksokListAdapter extends RecyclerView.Adapter<YaksokListAdapter.ViewHolder> {

    private List<Yaksok> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Yaksok yaksok, int position);
    }

    public YaksokListAdapter(List<Yaksok> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_yaksok, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Yaksok item = items.get(position);

        String status = "상태 없음";
        if(item.getStatus() != null){
            if(item.getStatus().equals("복용 완료")){
                status = "복용 완료";
            }else{
                status = "복용 중";
            }
        }

        holder.tvTitle.setText(item.getTitle());
        holder.tvPeriod.setText(item.getStartDate() + " 부터 " + item.getPrescriptionDays() + "일간");
        holder.tvStatus.setText(status);

        // 상태에 따라 색상을 다르게 주고 싶다면 여기서 처리할 수 있습니다.
        if ("복용 완료".equals(item.getStatus())) {
            holder.tvStatus.setTextColor(0xFF999999); // 회색
        } else {
            holder.tvStatus.setTextColor(0xFFFBC02D); // 노란색
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<Yaksok> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPeriod, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_yaksok_title);
            tvPeriod = itemView.findViewById(R.id.tv_yaksok_period);
            tvStatus = itemView.findViewById(R.id.tv_yaksok_status);
        }
    }
}