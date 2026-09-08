package com.example.medication.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

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