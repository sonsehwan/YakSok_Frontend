package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.databinding.ItemSharedUserNameBinding;
import com.example.medication.model.response.SharedUser;

import java.util.List;

public class SharedUserAdapter extends RecyclerView.Adapter<SharedUserAdapter.ViewHolder> {

    private List<SharedUser> items;
    private SharedUserAdapter.OnItemClickListener listener;

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

        holder.binding.tvNickname.setText(item.getNickName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<SharedUser> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
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
