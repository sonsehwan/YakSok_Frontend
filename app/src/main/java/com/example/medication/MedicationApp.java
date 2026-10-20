package com.example.medication;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.medication.util.PretendardInterceptor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.kakao.sdk.common.KakaoSdk;

import io.github.inflationx.viewpump.ViewPump;
import lombok.Getter;

public class MedicationApp extends Application {

    @Getter
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();

        FirebaseApp.initializeApp(this);
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY);
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                BuildConfig.DEBUG
                        ? DebugAppCheckProviderFactory.getInstance()
                        : PlayIntegrityAppCheckProviderFactory.getInstance());

        context = getApplicationContext();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(new PretendardInterceptor())
                        .build()
        );
    }
}
