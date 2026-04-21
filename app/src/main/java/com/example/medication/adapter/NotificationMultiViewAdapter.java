package com.example.medication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.NotificationListItem;
import com.example.medication.model.NotificationYaksok;

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
            View v = inflater.inflate(R.layout.item_notification, parent, false);
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
            NotificationListItem.NotificationItem noti = (NotificationListItem.NotificationItem) item;
            ItemViewHolder h = (ItemViewHolder) holder;
            NotificationYaksok data = noti.getData();

            h.tvName.setText(noti.getData().getTitle());
            h.tvTime.setText(noti.getData().getTime());
            h.tvInfo.setText(noti.getData().getInstruction());

            h.cbDone.setOnCheckedChangeListener(null); // 리스너 간섭 방지
            h.cbDone.setChecked(data.isTaken());
            h.cbDone.setText(data.isTaken() ? "완료" : "미복용");

            holder.itemView.setAlpha(data.isTaken() ? 0.5f : 1.0f);

            h.cbDone.setOnClickListener(v -> {
                h.cbDone.setChecked(data.isTaken());

                showConfirmDialog(h.itemView.getContext(), data, h);
            });
        }
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    private void showConfirmDialog(Context context, NotificationYaksok item, ItemViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String title = !item.isTaken() ? "복용 완료 처리하시겠습니까?" : "복용 취소 처리하시겠습니까?";
        builder.setTitle(title);
        builder.setMessage(item.getTitle() + " 약속을 확인합니다.");

        builder.setPositiveButton("확인", (dialog, which) -> {
            item.setTaken(!item.isTaken()); // 상태 반전

            // UI 갱신
            holder.cbDone.setChecked(item.isTaken());
            holder.itemView.setAlpha(item.isTaken() ? 0.5f : 1.0f);
            holder.cbDone.setText(item.isTaken() ? "완료" : "미복용");

            if (checkListener != null) {
                checkListener.onCheckChanged();
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
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
        TextView tvName, tvTime, tvInfo;
        CheckBox cbDone;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_notification_name);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            tvInfo = itemView.findViewById(R.id.tv_notification_info);
            cbDone = itemView.findViewById(R.id.cb_done);
        }
    }
}
