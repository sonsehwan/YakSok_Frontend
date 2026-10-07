package com.example.medication.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        // 상태바/내비바 아이콘 색은 EdgeToEdge(auto)가 uiMode로 정한다. 앱이 라이트 전용이므로
        // MedicationApp의 MODE_NIGHT_NO와 짝이 맞아야 어두운 아이콘이 된다(그 줄을 빼면 다크 폰에서 흰 아이콘).
        EdgeToEdge.enable(this);
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