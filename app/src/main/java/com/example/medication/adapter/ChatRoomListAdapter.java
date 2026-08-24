package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.response.ChatRoomListDto;

import java.util.List;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(ChatRoomListDto room);
    }

    public interface OnRoomLongClickListener {
        void onRoomLongClick(ChatRoomListDto room);
    }

    private final List<ChatRoomListDto> rooms;
    private final OnRoomClickListener listener;
    private final OnRoomLongClickListener longClickListener;

    public ChatRoomListAdapter(List<ChatRoomListDto> rooms, OnRoomClickListener listener, OnRoomLongClickListener longClickListener) {
        this.rooms = rooms;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRoomName;
        private final TextView tvLastMessage;
        private final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(ChatRoomListDto room) {
            tvRoomName.setText(room.getRoomName());

            String last = room.getLastMessage();
            tvLastMessage.setText(last != null ? last : "아직 대화가 없어요.");

            tvTime.setText(formatTime(room.getLastMessageAt()));

            itemView.setOnClickListener(v -> listener.onRoomClick(room));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onRoomLongClick(room);
                return true;
            });
        }

        // 서버가 "2026-07-28T11:05:00" 형태로 내려준다. 시:분만 잘라 쓴다.
        private String formatTime(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.length() < 16) {
                return "";
            }
            return isoDateTime.substring(11, 16);
        }
    }
}
