package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shared_user_name, parent, false);
        return new SharedUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedUserAdapter.ViewHolder holder, int position) {
        SharedUser item = items.get(position);

        holder.tvNickName.setText(item.getNickName());

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
        TextView tvNickName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickName = itemView.findViewById(R.id.tv_nickname);
        }
    }
}
