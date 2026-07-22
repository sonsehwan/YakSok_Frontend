package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_received_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceivedFriendRequestDto item = items.get(position);

        holder.tvNickname.setText(item.getNickname());
        holder.tvEmail.setText(item.getEmail());
        holder.tvAvatar.setText(initialOf(item.getNickname()));

        holder.btnAccept.setOnClickListener(v -> listener.onAnswer(item, true));
        holder.btnReject.setOnClickListener(v -> listener.onAnswer(item, false));
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
        TextView tvNickname, tvEmail, tvAvatar;
        Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}