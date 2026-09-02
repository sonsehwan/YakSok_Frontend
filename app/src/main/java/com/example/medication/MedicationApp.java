package com.example.medication;

import android.app.Application;

import com.example.medication.util.PretendardInterceptor;

import io.github.inflationx.viewpump.ViewPump;

public class MedicationApp extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(new PretendardInterceptor())
                        .build()
        );
    }
}
