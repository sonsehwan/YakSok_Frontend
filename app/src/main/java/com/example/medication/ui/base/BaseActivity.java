package com.example.medication.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.medication.ui.login.Login;
import com.example.medication.util.AuthEventBus;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class BaseActivity extends AppCompatActivity implements AuthEventBus.Listener {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockPortrait();
        applyLightSystemBarIcons();
    }

    // 테마 XML의 windowLightStatusBar만으로는 기기/OS 버전에 따라 상태바 아이콘 색이
    // 밝은 값으로 되돌아가는 경우가 있어(edge-to-edge 강제 적용 등), 모든 화면에서 코드로 재확인한다.
    private void applyLightSystemBarIcons() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    private void lockPortrait() {
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } catch (IllegalStateException e) {
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                throw e;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthEventBus.get().subscribe(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AuthEventBus.get().unsubscribe(this);
    }

    @Override
    public void onForceLogout() {
        if (this instanceof Login) {
            return;
        }

        Toast.makeText(this, "로그인이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}