package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medication.R;
import com.example.medication.model.response.MedicineSearchResponse;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<MedicineSearchResponse> medicineList = new ArrayList<>();
    private OnItemClickListener listener;

    // 클릭 이벤트 전달을 위한 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(MedicineSearchResponse medicine);
    }

    // 외부(Activity)에서 리스너를 설정하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 새로운 아이템 리스트를 기존 리스트 뒤에 추가 (무한 스크롤용)
    public void addItems(List<MedicineSearchResponse> items) {
        int startPosition = medicineList.size();
        medicineList.addAll(items);
        notifyItemRangeInserted(startPosition, items.size());
    }

    public void clearItems() {
        medicineList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_medicine_info, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineSearchResponse medicine = medicineList.get(position);

        // 약 이름 설정
        holder.tvName.setText(medicine.getName());

        // Glide를 사용하여 공공데이터 이미지 URL 로드
        Glide.with(holder.itemView.getContext())
                .load(medicine.getImage())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .into(holder.ivImage);

        // 아이템 클릭 이벤트 바인딩
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(medicine);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_medicine);
            tvName = itemView.findViewById(R.id.tv_medicine_name);
        }
    }
}