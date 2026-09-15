package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.databinding.ItemFriendBinding;
import com.example.medication.model.response.FriendResponseDto;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<FriendResponseDto> items;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(FriendResponseDto friend, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(FriendResponseDto friend);
    }

    // 롱클릭이 필요 없는 화면(친구 선택 다이얼로그 등)에서 쓰는 생성자
    public FriendListAdapter(List<FriendResponseDto> items, OnItemClickListener listener) {
        this(items, listener, null);
    }

    public FriendListAdapter(List<FriendResponseDto> items,
                             OnItemClickListener listener,
                             OnItemLongClickListener longClickListener) {
        this.items = items;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendBinding binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendResponseDto item = items.get(position);

        holder.binding.tvTimeFriendName.setText(item.getNickname());
        holder.binding.tvFriendEmail.setText(item.getEmail());
        holder.binding.tvAvatar.setText(initialOf(item.getNickname()));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onItemClick(item, pos);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            // 롱클릭을 쓰지 않는 화면에서는 이벤트를 소비하지 않고 그대로 흘려보낸다
            if (longClickListener == null) {
                return false;
            }
            longClickListener.onItemLongClick(item);
            return true;
        });
    }

    // 닉네임 첫 글자를 아바타에 표시
    private String initialOf(String nickname) {
        if (nickname == null || nickname.isEmpty()) return "?";
        return nickname.substring(0, 1);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<FriendResponseDto> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemFriendBinding binding;

        public ViewHolder(@NonNull ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
