package com.example.medication.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 지도 SDK(MapView)는 내부 SurfaceView가 터치를 직접 소비해서, MapView에 OnTouchListener를 달아도
 * 호출되지 않는다. 그래서 MapView를 감싸는 이 컨테이너의 dispatchTouchEvent에서 자식에게 넘기기 전에
 * 조상 ScrollView의 가로채기를 미리 막아, 지도 위 드래그/핀치 줌이 스크롤과 겹치지 않게 한다.
 */
public class MapTouchContainer extends FrameLayout {

    public MapTouchContainer(@NonNull Context context) {
        super(context);
    }

    public MapTouchContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ViewParent parent = getParent();
        if (parent != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    parent.requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    parent.requestDisallowInterceptTouchEvent(false);
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
