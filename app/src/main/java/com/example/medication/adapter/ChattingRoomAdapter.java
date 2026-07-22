package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.ChatMessage;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChattingRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 보낸 사람(나/상대) x 메시지 종류(텍스트/약속 공유) = 4가지
    private static final int VIEW_TYPE_MY_TEXT = 1;
    private static final int VIEW_TYPE_OTHER_TEXT = 2;
    private static final int VIEW_TYPE_MY_SHARE = 3;
    private static final int VIEW_TYPE_OTHER_SHARE = 4;

    public interface OnYaksokClickListener {
        void onYaksokClick(Long yaksokId);
    }

    private final List<ChatMessage> messageList = new ArrayList<>();
    private final String myEmail;
    private final OnYaksokClickListener listener;

    // 내 이메일을 전달받아 내가 보낸 메시지인지 판별합니다.
    public ChattingRoomAdapter(String myEmail, OnYaksokClickListener listener) {
        this.myEmail = myEmail;
        this.listener = listener;
    }

    public void addMessage(ChatMessage msg) {
        messageList.add(msg);
        notifyItemInserted(messageList.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messageList.get(position);

        boolean isMine = msg.getSender() != null && msg.getSender().equals(myEmail);
        boolean isShare = msg.getType() == ChatMessage.MessageType.SHARE_YAKSOK;

        if (isMine) {
            return isShare ? VIEW_TYPE_MY_SHARE : VIEW_TYPE_MY_TEXT;
        }
        return isShare ? VIEW_TYPE_OTHER_SHARE : VIEW_TYPE_OTHER_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_MY_TEXT:
                return new MyTextViewHolder(
                        inflater.inflate(R.layout.item_my_text_message, parent, false));
            case VIEW_TYPE_OTHER_TEXT:
                return new OtherTextViewHolder(
                        inflater.inflate(R.layout.item_other_text_message, parent, false));
            case VIEW_TYPE_MY_SHARE:
                return new MyShareViewHolder(
                        inflater.inflate(R.layout.item_my_yaksok_share_message, parent, false));
            default:
                return new OtherShareViewHolder(
                        inflater.inflate(R.layout.item_other_yaksok_share_message, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof MyTextViewHolder) {
            MyTextViewHolder h = (MyTextViewHolder) holder;
            h.tvName.setText(msg.getSenderNickname());
            h.tvMessage.setText(msg.getMessage());
            h.tvTime.setText(formatTime(msg.getCreatedAt()));

        } else if (holder instanceof OtherTextViewHolder) {
            OtherTextViewHolder h = (OtherTextViewHolder) holder;
            h.tvName.setText(msg.getSenderNickname());
            h.tvMessage.setText(msg.getMessage());
            h.tvTime.setText(formatTime(msg.getCreatedAt()));

        } else if (holder instanceof MyShareViewHolder) {
            MyShareViewHolder h = (MyShareViewHolder) holder;
            h.tvName.setText(msg.getSenderNickname());
            h.tvYaksokName.setText(msg.getMessage());
            h.tvTime.setText(formatTime(msg.getCreatedAt()));
            bindYaksokButton(h.btnYaksok, msg);

        } else if (holder instanceof OtherShareViewHolder) {
            OtherShareViewHolder h = (OtherShareViewHolder) holder;
            h.tvName.setText(msg.getSenderNickname());
            h.tvYaksokName.setText(msg.getMessage());
            h.tvTime.setText(formatTime(msg.getCreatedAt()));
            bindYaksokButton(h.btnYaksok, msg);
        }
    }

    // 서버가 준 ISO-8601 문자열을 "오후 7:10" 형태로 바꾼다
    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) return "";
        try {
            LocalDateTime time = LocalDateTime.parse(createdAt);
            return time.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN));
        } catch (Exception e) {
            return "";
        }
    }

    private void bindYaksokButton(MaterialButton button, ChatMessage msg) {
        button.setOnClickListener(v -> {
            if (listener != null && msg.getYaksokId() != null) {
                listener.onYaksokClick(msg.getYaksokId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MyTextViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage, tvTime;

        MyTextViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_my_name);
            tvMessage = itemView.findViewById(R.id.tv_my_message);
            tvTime = itemView.findViewById(R.id.tv_my_time);
        }
    }

    static class OtherTextViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage, tvTime;

        OtherTextViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_other_name);
            tvMessage = itemView.findViewById(R.id.tv_other_message);
            tvTime = itemView.findViewById(R.id.tv_other_time);
        }
    }

    static class MyShareViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvYaksokName, tvTime;
        MaterialButton btnYaksok;

        MyShareViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_my_name);
            tvYaksokName = itemView.findViewById(R.id.tv_yaksok_name);
            tvTime = itemView.findViewById(R.id.tv_my_time);
            btnYaksok = itemView.findViewById(R.id.btn_other_yaksok);
        }
    }

    static class OtherShareViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvYaksokName, tvTime;
        MaterialButton btnYaksok;

        OtherShareViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_other_name);
            tvYaksokName = itemView.findViewById(R.id.tv_yaksok_name);
            tvTime = itemView.findViewById(R.id.tv_other_time);
            btnYaksok = itemView.findViewById(R.id.btn_other_yaksok);
        }
    }
}
