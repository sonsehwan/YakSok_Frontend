package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChattingRoomAdapter extends RecyclerView.Adapter<ChattingRoomAdapter.MessageViewHolder> {
    private List<ChatMessage> messageList = new ArrayList<>();

    public void addMessage(ChatMessage msg) {
        messageList.add(msg);
        notifyItemInserted(messageList.size() - 1);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 안드로이드 기본 제공 레이아웃(simple_list_item_1)을 임시로 사용합니다.
        // 나중에는 내가 보낸 메시지(오른쪽 말풍선), 남이 보낸 메시지(왼쪽 말풍선) 전용 XML을 만들어 연결하시면 됩니다!
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        // "보낸사람: 메세지내용" 형식으로 출력
        holder.tvText.setText(msg.getSender() + " : " + msg.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(android.R.id.text1);
        }
    }
}
