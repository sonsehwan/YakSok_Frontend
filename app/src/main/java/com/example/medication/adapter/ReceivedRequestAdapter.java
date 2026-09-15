package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.databinding.ItemReceivedRequestBinding;
import com.example.medication.model.response.ReceivedFriendRequestDto;

import java.util.List;

public class ReceivedRequestAdapter extends RecyclerView.Adapter<ReceivedRequestAdapter.ViewHolder> {

    private final List<ReceivedFriendRequestDto> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAnswer(ReceivedFriendRequestDto request, boolean accept);
    }

    public ReceivedRequestAdapter(List<ReceivedFriendRequestDto> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReceivedRequestBinding binding = ItemReceivedRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceivedFriendRequestDto item = items.get(position);

        holder.binding.tvNickname.setText(item.getNickname());
        holder.binding.tvEmail.setText(item.getEmail());
        holder.binding.tvAvatar.setText(initialOf(item.getNickname()));

        holder.binding.btnAccept.setOnClickListener(v -> listener.onAnswer(item, true));
        holder.binding.btnReject.setOnClickListener(v -> listener.onAnswer(item, false));
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

    public void updateData(List<ReceivedFriendRequestDto> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemReceivedRequestBinding binding;

        public ViewHolder(@NonNull ItemReceivedRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
