package com.example.medication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.NotificationListItem;
import com.example.medication.model.NotificationYaksok;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.network.NetworkClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            h.tvName.setText(data.getTitle());
            h.tvTime.setText(data.getTime());
            h.tvInfo.setText(data.getInstruction());

            h.cbDone.setOnCheckedChangeListener(null); // 리스너 간섭 방지
            h.cbDone.setChecked(data.isTaken());
            h.cbDone.setText(data.isTaken() ? "완료" : "미복용");
            holder.itemView.setAlpha(data.isTaken() ? 0.5f : 1.0f);

            h.cbDone.setOnClickListener(v -> {
                h.cbDone.setChecked(data.isTaken());

                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    showConfirmDialog(h.itemView.getContext(), currentPos, noti, h);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    private void showConfirmDialog(Context context, int position, NotificationListItem.NotificationItem notiItem, ItemViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        NotificationYaksok data = notiItem.getData();
        boolean originalState = data.isTaken();
        boolean newState = !originalState;

        String title = !originalState ? "복용 완료 처리하시겠습니까?" : "복용 취소 처리하시겠습니까?";
        builder.setTitle(title);
        builder.setMessage(data.getTitle() + " 약속을 확인합니다.");

        builder.setPositiveButton("확인", (dialog, which) -> {
            data.setTaken(newState); // 상태 반전
            notifyItemChanged(position);

            if (checkListener != null) {
                checkListener.onCheckChanged();
            }
            NetworkClient.getYaksokApi().updateNotificationStatus(data.getId(), newState)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (!response.isSuccessful()) {
                                // 3-1. 서버 에러 시 원래 상태로 롤백
                                rollbackStatus(context, position, data, originalState, "서버에러: " + response.code());
                                if (checkListener != null) checkListener.onCheckChanged();
                            }else{
                                Log.d("통신 성공", "알림 상태 변경 성공");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            // 3-2. 통신 에러 발생 시 원상 복구하며 에러 원인 출력
                            // 개발 중에만 t.getMessage()를 띄우고, 서비스 중에는 "네트워크 오류" 등으로 묶습니다.
                            String errorMessage = t.getMessage() != null ? t.getMessage() : "원인 불명";
                            Log.e("통신 에러", errorMessage);
                            rollbackStatus(context, position, data, originalState, "오류: " + errorMessage);
                            if (checkListener != null) checkListener.onCheckChanged();
                        }
                    });

        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void rollbackStatus(Context context, int position, NotificationYaksok data, boolean originalState, String errorMessage) {
        data.setTaken(originalState);
        notifyItemChanged(position);
        Toast.makeText(context, "통신 실패 (" + errorMessage + ")", Toast.LENGTH_SHORT).show();
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
