package com.example.medication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.medication.model.response.UserResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SprefsManager {

    //저장폴더 이름
    private static final String PREF_NAME = "YakSokPrefs";
    //토큰 전용 저장폴더 이름 (로그아웃 시 통째로 clear() 하기 위해 분리)
    private static final String AUTH_PREF_NAME = "YakSokAuthPrefs";

    //데이터를 저장할 데이터 키값
    private static final String KEY_USER_DATA = "user_data"; // 유저 객체 전체 JSON
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String LEGACY_KEY_YAKSOK_LIST = "yaksok_list";
    private static final String LEGACY_KEY_NOTIFICATION_LIST = "notification_list";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled_";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

    //저장소를 불러온다.(저장소가 없으면 만들어서 전달한다.)
    private static SharedPreferences getPreference(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    //토큰 전용 저장소
    private static SharedPreferences getAuthPreference(Context context){
        return context.getSharedPreferences(AUTH_PREF_NAME, Context.MODE_PRIVATE);
    }

    // 로그인/재발급 성공 시 토큰 저장
    public static void saveTokens(Context context, String accessToken, String refreshToken){
        SharedPreferences.Editor editor = getAuthPreference(context).edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public static String getAccessToken(Context context){
        return getAuthPreference(context).getString(KEY_ACCESS_TOKEN, null);
    }

    public static String getRefreshToken(Context context){
        return getAuthPreference(context).getString(KEY_REFRESH_TOKEN, null);
    }

    public static void clearAuthInfo(Context context){
        getAuthPreference(context).edit().clear().apply();
    }

    public static void setUserInfo(Context context, UserResponse user){
        SharedPreferences.Editor editor = getPreference(context).edit();

        Gson gson = getGson();
        String json = gson.toJson(user);

        editor.putString(KEY_USER_DATA, json);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // 저장된 유저 객체를 가져오기
    public static UserResponse getUser(Context context){
        String json = getPreference(context).getString(KEY_USER_DATA, null);
        if(json == null) return null;

        return new Gson().fromJson(json, UserResponse.class);
    }

    // 닉네임만 따로 뽑아서 가져오기
    public static String getUserNickName(Context context) {
        UserResponse user = getUser(context);
        return (user != null) ? user.getNickname() : "사용자";
    }

    // 이메일만 따로 뽑아서 가져오기
    public static String getUserEmail(Context context){
        UserResponse user = getUser(context);
        return (user != null) ? user.getEmail() : "이메일";
    }

    // 유저 id만 따로 뽑아서 가져오기
    public static Long getUserId(Context context){
        UserResponse user = getUser(context);
        return (user != null) ? user.getId() : null;
    }

    public static boolean isNotificationEnabled(Context context){
        Long userId = getUserId(context);
        if(userId == null) return false;

        return(getPreference(context).getBoolean(
                KEY_NOTIFICATION_ENABLED + userId,
                true
        ));
    }

    public static void setNotificationEnabled(Context context, boolean enabled){
        Long userId = getUserId(context);
        if(userId == null) return;

        getPreference(context).edit()
                .putBoolean(KEY_NOTIFICATION_ENABLED + userId, enabled)
                .apply();
    }

    // 로그인 상태 확인
    public static boolean isLoggedIn(Context context) {
        return getPreference(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    //로그아웃 시 모든 정보 삭제
    public static void clearUserInfo(Context context){
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.remove(KEY_USER_DATA);
        editor.remove(KEY_IS_LOGGED_IN);
        // 예전 버전이 남긴 약속/알림 캐시. 더는 쓰지 않으니 기기에서 지운다
        editor.remove(LEGACY_KEY_YAKSOK_LIST);
        editor.remove(LEGACY_KEY_NOTIFICATION_LIST);
        editor.apply();

        clearAuthInfo(context);
    }
}
