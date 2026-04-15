package com.example.medication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medication.R;
import com.example.medication.model.NotificationYaksok;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    private List<NotificationYaksok> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onStatusChanged(int position, boolean isDone);
    }

    public MedicationAdapter(List<NotificationYaksok> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationYaksok item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvInfo.setText(item.getTime() + "\n" + item.getInstruction());

        // 체크 상태 설정 (무한 루프 방지를 위해 리스너 잠시 제거)
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(item.isTaken());
        holder.cbDone.setText(item.isTaken() ? "완료" : "미복용");

        // 아이템 생성시 현재 복용 상태에 따라 초기화(처음 화면에 그려질 때)
        holder.itemView.setAlpha(item.isTaken() ? 0.5f : 1.0f);

        holder.cbDone.setOnClickListener(v -> {
            boolean targetState = !item.isTaken(); // 바꾸고자 하는 목표 상태

            // 다이얼로그를 띄우기 전까지는 체크박스 상태가 변하지 않도록 강제로 고정
            holder.cbDone.setChecked(item.isTaken());

            // 확인 다이얼로그 호출 (필요한 정보들을 파라미터로 전달)
            showConfirmDialog(holder.itemView.getContext(), position, targetState, holder);
        });

          /*
        직접 클릭하여 복용 상태를 변경할 때
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
        });
*/
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        CheckBox cbDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_pill_name);
            tvInfo = itemView.findViewById(R.id.tv_pill_info);
            cbDone = itemView.findViewById(R.id.cb_done);

        }
    }

    private void showConfirmDialog(Context context, int position, boolean isDone, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        NotificationYaksok item = items.get(position);

        String title = isDone ? "복용 완료 처리하시겠습니까?" : "복용 취소 처리하시겠습니까?";
        builder.setTitle(title);
        builder.setMessage(item.getName() + " 약속을 확인합니다.");

        builder.setPositiveButton("확인", (dialog, which) -> {
            // 1. 데이터 모델 변경
            item.setTaken(isDone);

            // 2. UI 즉시 업데이트
            holder.cbDone.setChecked(isDone);
            holder.itemView.setAlpha(isDone ? 0.5f : 1.0f);
            holder.cbDone.setText(isDone ? "완료" : "미복용");

            // 3. 메인 액티비티에 알려서 프로그레스 바 갱신
            if (listener != null) {
                listener.onStatusChanged(position, isDone);
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }
}