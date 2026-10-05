package com.example.medication.ui.finddrugstore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListPopupWindow;
import android.widget.SpinnerAdapter;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.example.medication.R;

// 기본 Spinner는 드롭다운이 앵커 위에서부터 겹쳐 뜨고 높이 제한도 못 건다.
// 앵커 바로 아래에서 최대 MAX_VISIBLE_ITEMS개까지만 보이게 직접 ListPopupWindow를 띄운다.
public class BelowSpinner extends AppCompatSpinner {

    private static final int MAX_VISIBLE_ITEMS = 10;
    private static final int ITEM_HEIGHT_DP = 48;
    private static final int GAP_DP = 4;

    public BelowSpinner(Context context) {
        super(context);
    }

    public BelowSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BelowSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        SpinnerAdapter source = getAdapter();
        if (source == null || source.getCount() == 0) {
            return true;
        }

        float density = getResources().getDisplayMetrics().density;
        ListPopupWindow popup = new ListPopupWindow(getContext());
        popup.setAnchorView(this);
        popup.setModal(true);
        popup.setWidth(getWidth());
        popup.setVerticalOffset((int) (GAP_DP * density));
        popup.setHeight((int) (Math.min(source.getCount(), MAX_VISIBLE_ITEMS) * ITEM_HEIGHT_DP * density));
        popup.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bg_input_box));
        popup.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return source.getCount();
            }

            @Override
            public Object getItem(int position) {
                return source.getItem(position);
            }

            @Override
            public long getItemId(int position) {
                return source.getItemId(position);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return source.getDropDownView(position, convertView, parent);
            }
        });
        popup.setOnItemClickListener((parent, view, position, id) -> {
            setSelection(position);
            popup.dismiss();
        });
        popup.show();
        popup.setSelection(getSelectedItemPosition());
        return true;
    }
}
