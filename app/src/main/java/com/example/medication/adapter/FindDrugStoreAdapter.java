package com.example.medication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.SearchDrugStore;

import java.util.List;

public class FindDrugStoreAdapter extends RecyclerView.Adapter<FindDrugStoreAdapter.ViewHolder> {

    private List<SearchDrugStore> drugStores;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SearchDrugStore drugStore);
    }

    public FindDrugStoreAdapter(List<SearchDrugStore> drugStores, OnItemClickListener listener) {
        this.drugStores = drugStores;
        this.listener = listener;
    }

    public void updateData(List<SearchDrugStore> newDrugStores) {
        this.drugStores = newDrugStores;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_simple_drugstore.xml 레이아웃을 뷰 객체로 팽창(Inflate)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_drugstore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchDrugStore store = drugStores.get(position);

        holder.tvName.setText(store.getDutyName());
        holder.tvAddress.setText(store.getDutyAddr());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return drugStores != null ? drugStores.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_drugstore_name);
            tvAddress = itemView.findViewById(R.id.tv_drugstore_address);
        }
    }
}