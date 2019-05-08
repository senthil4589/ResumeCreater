package com.apnatime.resume.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceSettings {

    public static final String MOBILE_NUMBER="mobile_no";
    public static final String FIREBASE_USER_ID="user_id";
    public static final String LOGIN_STATUS="login_status";
    public static final String MY_CONFIG = "resume_app_settings";

    public static void setMobileNo(String agent) {
        SharedPreferences configPrefs = getPreferences();
        SharedPreferences.Editor editor = configPrefs.edit();
        editor.putString(MOBILE_NUMBER, agent);
        applyPreferenceChanges(editor);
    }

    public static String getMobileNo() {
        return getPreferences().getString(MOBILE_NUMBER, "");
    }

    public static void setUserId(String agent) {
        SharedPreferences configPrefs = getPreferences();
        SharedPreferences.Editor editor = configPrefs.edit();
        editor.putString(FIREBASE_USER_ID, agent);
        applyPreferenceChanges(editor);
    }

    public static String getUserId() {
        return getPreferences().getString(FIREBASE_USER_ID, "");
    }



    public static void setLoginStatus(boolean flag){
        SharedPreferences configPrefs = getPreferences();
        SharedPreferences.Editor editor = configPrefs.edit();
        editor.putBoolean(LOGIN_STATUS, flag);
        applyPreferenceChanges(editor);
    }

    public static boolean getLoginStatus(){
        return getPreferences().getBoolean(LOGIN_STATUS, false);
    }


    public static SharedPreferences getPreferences() {
        return ResumeApplication.getAppContext().getSharedPreferences(MY_CONFIG, Context.MODE_PRIVATE);
    }
    public static void applyPreferenceChanges(SharedPreferences.Editor editor) {
        editor.commit();
    }
}
