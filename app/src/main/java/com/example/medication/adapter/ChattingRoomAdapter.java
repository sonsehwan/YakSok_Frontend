package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChattingRoomAdapter extends RecyclerView.Adapter<ChattingRoomAdapter.ChatViewHolder> {

    private List<ChatMessage> messageList = new ArrayList<>();
    private String myEmail;

    // 내 이메일을 전달받아 내가 보낸 메시지인지 판별합니다.
    public ChattingRoomAdapter(String myEmail) {
        this.myEmail = myEmail;
    }

    public void addMessage(ChatMessage msg) {
        messageList.add(msg);
        notifyItemInserted(messageList.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        // 현재 시간을 포맷팅 (서버에서 받은 시간이 있다면 그것을 우선으로 쓰는 것이 좋습니다)
        String timeString = new SimpleDateFormat("a h:mm", Locale.KOREA).format(new Date());

        if (msg.getSender().equals(myEmail)) {
            // [내가 보낸 메시지일 경우]
            holder.llMyChat.setVisibility(View.VISIBLE);
            holder.llOtherChat.setVisibility(View.GONE);

            holder.tvMyName.setText(msg.getSender());
            holder.tvMyMessage.setText(msg.getMessage());
            holder.tvMyTime.setText(timeString);
        } else {
            // [상대방이 보낸 메시지일 경우]
            holder.llOtherChat.setVisibility(View.VISIBLE);
            holder.llMyChat.setVisibility(View.GONE);

            holder.tvOtherName.setText(msg.getSender()); // 보낸 사람 이메일 또는 이름
            holder.tvOtherMessage.setText(msg.getMessage());
            holder.tvOtherTime.setText(timeString);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llOtherChat, llMyChat;
        TextView tvMyName, tvOtherName, tvOtherMessage, tvOtherTime;
        TextView tvMyMessage, tvMyTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            llOtherChat = itemView.findViewById(R.id.ll_other_chat);
            llMyChat = itemView.findViewById(R.id.ll_my_chat);

            tvOtherName = itemView.findViewById(R.id.tv_other_name);
            tvOtherMessage = itemView.findViewById(R.id.tv_other_message);
            tvOtherTime = itemView.findViewById(R.id.tv_other_time);

            tvMyName = itemView.findViewById(R.id.tv_my_name);
            tvMyMessage = itemView.findViewById(R.id.tv_my_message);
            tvMyTime = itemView.findViewById(R.id.tv_my_time);
        }
    }
}