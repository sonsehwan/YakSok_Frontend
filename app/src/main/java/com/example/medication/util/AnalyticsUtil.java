package com.example.medication.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsUtil {
    /**
     * 앱 어디서든 이벤트를 기록할 수 있는 공통 메소드
     * @param context 호출하는 액티비티의 context (this)
     * @param eventName 기록할 이벤트 이름 (예: "medicine_checked")
     * @param params 함께 보낼 상세 정보 (없으면 null)
     */
    public static void logEvent(Context context, String eventName, Bundle params) {
        FirebaseAnalytics.getInstance(context).logEvent(eventName, params);
    }
}
