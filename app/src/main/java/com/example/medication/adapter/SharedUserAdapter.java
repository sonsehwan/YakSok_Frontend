package com.example.medication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.databinding.ItemSharedUserNameBinding;
import com.example.medication.model.response.SharedUser;

import java.util.List;

public class SharedUserAdapter extends RecyclerView.Adapter<SharedUserAdapter.ViewHolder> {

    private List<SharedUser> items;
    private SharedUserAdapter.OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(SharedUser user, int position);
    }

    public SharedUserAdapter(List<SharedUser> items, OnItemClickListener listener){
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSharedUserNameBinding binding = ItemSharedUserNameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SharedUserAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedUserAdapter.ViewHolder holder, int position) {
        SharedUser item = items.get(position);

        String nickname = item.getNickName();
        holder.binding.tvNickname.setText(nickname);
        holder.binding.tvAvatar.setText((nickname == null || nickname.isEmpty()) ? "?" : nickname.substring(0, 1));

        boolean selected = position == selectedPosition;
        holder.binding.tvAvatar.setBackgroundResource(
                selected ? R.drawable.bg_avatar_ring_selected : R.drawable.bg_avatar_circle_neutral);
        Context context = holder.itemView.getContext();
        holder.binding.tvAvatar.setTextColor(ContextCompat.getColor(context, selected ? R.color.p700 : R.color.g500));
        holder.binding.tvNickname.setTextColor(ContextCompat.getColor(context, selected ? R.color.g900 : R.color.g500));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            setSelectedPosition(pos);
            if (listener != null) {
                listener.onItemClick(item, pos);
            }
        });
    }

    // 탭 진입 시 첫 번째 공유자를 자동으로 선택 표시할 때 쓴다.
    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<SharedUser> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSharedUserNameBinding binding;

        public ViewHolder(@NonNull ItemSharedUserNameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
