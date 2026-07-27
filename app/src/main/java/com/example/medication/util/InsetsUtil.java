package com.example.medication.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtil {

    // 배경은 화면 끝까지 꽉 채우고(edge-to-edge), 콘텐츠만 시스템 바를 피하도록 padding을 준다.
    // 하단은 BottomNavigationView가 스스로 인셋만큼 padding을 넣으므로 여기서 건드리지 않는다.
    // (건드리면 이중으로 적용돼서 아이콘/라벨이 잘린다)
    public static void applySystemBarPadding(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            // 소비하지 않고 그대로 반환해야 자식인 BottomNavigationView가 인셋을 받을 수 있다.
            return insets;
        });
    }
}