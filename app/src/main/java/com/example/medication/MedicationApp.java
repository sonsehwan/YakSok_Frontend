package com.example.medication;

import android.app.Application;
import android.content.Context;

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

        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(new PretendardInterceptor())
                        .build()
        );
    }
}
