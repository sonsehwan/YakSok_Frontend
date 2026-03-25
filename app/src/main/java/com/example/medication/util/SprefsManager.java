package com.example.medication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.medication.model.response.UserResponse;
import com.google.gson.Gson;

public class SprefsManager {

    //저장폴더 이름
    private static final String PREF_NAME = "YakSokPrefs";

    //데이터를 저장할 데이터 키값
    private static final String KEY_USER_DATA = "user_data"; // 유저 객체 전체 JSON
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    //저장소를 불러온다.(저장소가 없으면 만들어서 전달한다.)
    private static SharedPreferences getPreference(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    //로그인 성공시 유저 정보 저장
    public static void setUserInfo(Context context, UserResponse user){
        SharedPreferences.Editor editor = getPreference(context).edit();

        Gson gson = new Gson();
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

    // 로그인 상태 확인
    public static boolean isLoggedIn(Context context) {
        return getPreference(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    //로그아웃 시 모든 정보 삭제
    public static void clearUserInfo(Context context){
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.clear();
        editor.apply();
    }
}
