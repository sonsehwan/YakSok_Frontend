package com.example.medication.util;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.medication.R;

import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

public class PretendardInterceptor implements Interceptor {
    @Nullable
    private Typeface cachedPretendard;

    @NonNull
    @Override
    public InflateResult intercept(Chain chain){
        InflateResult result = chain.proceed(chain.request());
        View view = result.view();

        if(view instanceof TextView){
            applyPretendard((TextView)view);
        }
        return result;
    }

    private void applyPretendard(@NonNull TextView textView){
        if (cachedPretendard == null) {
            cachedPretendard = ResourcesCompat.getFont(
                    textView.getContext().getApplicationContext(),
                    R.font.pretendard          // res/font/pretendard.xml (400/600/700 묶음)
            );
        }
        if (cachedPretendard == null) return;

        Typeface current = textView.getTypeface();
        int style = (current != null) ? current.getStyle() : Typeface.NORMAL;
        textView.setTypeface(cachedPretendard, style);
    }
}
