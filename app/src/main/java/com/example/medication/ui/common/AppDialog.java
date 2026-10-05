package com.example.medication.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.example.medication.R;
import com.example.medication.databinding.DialogActionDeleteBinding;
import com.example.medication.databinding.DialogConfirmBinding;

public final class AppDialog {

    private AppDialog() {}

    public static void confirm(Context context, String title, String message,
                               String positiveText, boolean danger, Runnable onPositive) {
        DialogConfirmBinding b = DialogConfirmBinding.inflate(LayoutInflater.from(context));
        Dialog dialog = create(context, b.getRoot());

        b.tvTitle.setText(title);
        b.tvMessage.setText(message);
        b.btnPositive.setText(positiveText);
        if (danger) {
            b.btnPositive.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red500)));
        }

        b.btnNegative.setOnClickListener(v -> dialog.dismiss());
        b.btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            onPositive.run();
        });
        dialog.show();
    }

    public static void deleteAction(Context context, String title, Runnable onDelete) {
        DialogActionDeleteBinding b = DialogActionDeleteBinding.inflate(LayoutInflater.from(context));
        Dialog dialog = create(context, b.getRoot());

        b.tvTitle.setText(title);
        b.llDelete.setOnClickListener(v -> {
            dialog.dismiss();
            onDelete.run();
        });
        dialog.show();
    }

    public static PopupWindow popup(View content, int widthDp) {
        Context context = content.getContext();
        float density = context.getResources().getDisplayMetrics().density;

        PopupWindow popup = new PopupWindow(content, (int) (widthDp * density),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup_menu));
        popup.setElevation(8 * density);
        return popup;
    }

    // .pen 위치: 앵커 바로 아래, 화면 오른쪽 가장자리에서 16dp 안쪽.
    public static void showBelow(PopupWindow popup, View anchor) {
        float density = anchor.getResources().getDisplayMetrics().density;
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        popup.showAtLocation(anchor, Gravity.TOP | Gravity.END,
                (int) (16 * density), location[1] + anchor.getHeight());
    }

    private static Dialog create(Context context, View content) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(content);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }
}
