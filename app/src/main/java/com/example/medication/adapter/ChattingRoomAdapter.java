package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.databinding.ItemMyTextMessageBinding;
import com.example.medication.databinding.ItemMyYaksokShareMessageBinding;
import com.example.medication.databinding.ItemOtherTextMessageBinding;
import com.example.medication.databinding.ItemOtherYaksokShareMessageBinding;
import com.example.medication.model.ChatMessage;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private final Long myParticipantId;
    private final OnYaksokClickListener listener;

    // 이 방에서 나를 대표하는 참여자 id를 전달받아 내가 보낸 메시지인지 판별합니다.
    public ChattingRoomAdapter(Long myParticipantId, OnYaksokClickListener listener) {
        this.myParticipantId = myParticipantId;
        this.listener = listener;
    }

    public void addMessage(ChatMessage msg) {
        messageList.add(msg);
        notifyItemInserted(messageList.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messageList.get(position);

        boolean isMine = Objects.equals(msg.getSenderParticipantId(), myParticipantId);
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
                        ItemMyTextMessageBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_OTHER_TEXT:
                return new OtherTextViewHolder(
                        ItemOtherTextMessageBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_MY_SHARE:
                return new MyShareViewHolder(
                        ItemMyYaksokShareMessageBinding.inflate(inflater, parent, false));
            default:
                return new OtherShareViewHolder(
                        ItemOtherYaksokShareMessageBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof MyTextViewHolder) {
            MyTextViewHolder h = (MyTextViewHolder) holder;
            h.binding.tvMyName.setText(msg.getSenderNickname());
            h.binding.tvMyMessage.setText(msg.getMessage());
            h.binding.tvMyTime.setText(formatTime(msg.getCreatedAt()));

        } else if (holder instanceof OtherTextViewHolder) {
            OtherTextViewHolder h = (OtherTextViewHolder) holder;
            h.binding.tvOtherName.setText(msg.getSenderNickname());
            h.binding.tvOtherMessage.setText(msg.getMessage());
            h.binding.tvOtherTime.setText(formatTime(msg.getCreatedAt()));

        } else if (holder instanceof MyShareViewHolder) {
            MyShareViewHolder h = (MyShareViewHolder) holder;
            h.binding.tvMyName.setText(msg.getSenderNickname());
            h.binding.tvYaksokName.setText(msg.getMessage());
            h.binding.tvMyTime.setText(formatTime(msg.getCreatedAt()));
            bindYaksokButton(h.binding.btnOtherYaksok, msg);

        } else if (holder instanceof OtherShareViewHolder) {
            OtherShareViewHolder h = (OtherShareViewHolder) holder;
            h.binding.tvOtherName.setText(msg.getSenderNickname());
            h.binding.tvYaksokName.setText(msg.getMessage());
            h.binding.tvOtherTime.setText(formatTime(msg.getCreatedAt()));
            bindYaksokButton(h.binding.btnOtherYaksok, msg);
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
        ItemMyTextMessageBinding binding;

        MyTextViewHolder(@NonNull ItemMyTextMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class OtherTextViewHolder extends RecyclerView.ViewHolder {
        ItemOtherTextMessageBinding binding;

        OtherTextViewHolder(@NonNull ItemOtherTextMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class MyShareViewHolder extends RecyclerView.ViewHolder {
        ItemMyYaksokShareMessageBinding binding;

        MyShareViewHolder(@NonNull ItemMyYaksokShareMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class OtherShareViewHolder extends RecyclerView.ViewHolder {
        ItemOtherYaksokShareMessageBinding binding;

        OtherShareViewHolder(@NonNull ItemOtherYaksokShareMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
