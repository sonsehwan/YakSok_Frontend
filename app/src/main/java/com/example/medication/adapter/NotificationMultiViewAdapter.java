package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.NotificationListItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationMultiViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<NotificationListItem> allItems;
    private List<NotificationListItem> visibleItems;
    private final Set<String> collapsedCategories = new HashSet<>(); //접혀있는 헤더의 이름을 저장
    private final OnItemCheckListener checkListener;

    public interface OnItemCheckListener {
        void onCheckChanged();
    }

    public NotificationMultiViewAdapter(List<NotificationListItem> allItems, OnItemCheckListener listener) {
        this.allItems = allItems;
        this.checkListener = listener;
        updateVisibleItems();
    }

    private void updateVisibleItems() {
        List<NotificationListItem> newList = new ArrayList<>();
        boolean isCurrentSectionCollapsed = false;

        for(NotificationListItem item : allItems) {
            if(item instanceof NotificationListItem.HeaderItem) {
                NotificationListItem.HeaderItem header = (NotificationListItem.HeaderItem) item;
                isCurrentSectionCollapsed = collapsedCategories.contains(header.getTitle());
                newList.add(header);
            }else{
                if(!isCurrentSectionCollapsed) {
                    newList.add(item);
                }
            }
        }
        this.visibleItems = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position){
        return visibleItems.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == NotificationListItem.TYPE_HEADER){
            View v = inflater.inflate(R.layout.item_time_header, parent, false);
            return new HeaderViewHolder(v);
        }else{
            View v = inflater.inflate(R.layout.item_medication, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position){
        NotificationListItem item = visibleItems.get(position);
        if(holder instanceof HeaderViewHolder){
            NotificationListItem.HeaderItem header = (NotificationListItem.HeaderItem) item;
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.tvTitle.setText(header.getTitle());

            boolean isCollapsed = collapsedCategories.contains(header.getTimeCategory());
            h.ivArrow.animate().rotation(isCollapsed ? 0 : 90).setDuration(200).start();

            h.itemView.setOnClickListener(v -> {
                if (isCollapsed) {
                    collapsedCategories.remove(header.getTimeCategory());
                } else {
                    collapsedCategories.add(header.getTimeCategory());
                }
                updateVisibleItems();
            });
        }else if (holder instanceof ItemViewHolder) {
            NotificationListItem.MedicationItem med = (NotificationListItem.MedicationItem) item;
            ItemViewHolder h = (ItemViewHolder) holder;

            h.tvName.setText(med.getData().getTitle());
            h.tvInfo.setText(med.getData().getInstruction());
        }
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivArrow;
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_time_title);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        ItemViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_pill_name);
            tvInfo = itemView.findViewById(R.id.tv_pill_info);
        }
    }
}
