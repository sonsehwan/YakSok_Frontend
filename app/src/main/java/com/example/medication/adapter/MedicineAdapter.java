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

/**
 * 의약품 검색 결과를 리사이클러 뷰에 표시하기 위한 어댑터입니다.
 */
public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<MedicineSearchResponse> medicineList = new ArrayList<>();

    /**
     * 새로운 아이템 리스트를 기존 리스트 뒤에 추가합니다. (무한 스크롤용)
     */
    public void addItems(List<MedicineSearchResponse> items) {
        int startPosition = medicineList.size();
        medicineList.addAll(items);
        notifyItemRangeInserted(startPosition, items.size());
    }

    /**
     * 검색어가 바뀌었을 때 기존 리스트를 모두 비웁니다.
     */
    public void clearItems() {
        medicineList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_simple_medicine_info.xml 레이아웃을 인플레이트합니다.
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
                .placeholder(android.R.drawable.ic_menu_report_image) // 로딩 중 표시할 이미지
                .error(android.R.drawable.ic_menu_close_clear_cancel) // 로드 실패 시 표시할 이미지
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    /**
     * 리사이클러 뷰의 각 아이템 뷰를 담는 홀더 클래스입니다.
     */
    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;

        public MedicineViewHolder(@NonNull View itemView) {
            // [수정] 부모 생성자 호출은 super(itemView) 하나만 존재해야 하며,
            // static 클래스이므로 parent에 접근할 수 없던 문제를 해결했습니다.
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_medicine);
            tvName = itemView.findViewById(R.id.tv_medicine_name);
        }
    }
}