package com.example.medication;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.medication.util.PretendardInterceptor;

import io.github.inflationx.viewpump.ViewPump;
import lombok.Getter;

public class MedicationApp extends Application {

    @Getter
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();

        context = getApplicationContext();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(new PretendardInterceptor())
                        .build()
        );
    }
}
