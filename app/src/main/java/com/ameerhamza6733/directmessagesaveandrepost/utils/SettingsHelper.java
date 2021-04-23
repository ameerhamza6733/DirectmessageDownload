package com.ameerhamza6733.directmessagesaveandrepost.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.APP_LANGUAGE;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.APP_THEME;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.APP_UA;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.AUTOPLAY_VIDEOS;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.BROWSER_UA;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.CHECK_ACTIVITY;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.CHECK_UPDATES;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.COOKIE;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.CUSTOM_DATE_TIME_FORMAT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.CUSTOM_DATE_TIME_FORMAT_ENABLED;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.DATE_TIME_SELECTION;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.DEFAULT_TAB;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.DEVICE_UUID;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.DM_MARK_AS_SEEN;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.DOWNLOAD_USER_FOLDER;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.FOLDER_PATH;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.FOLDER_SAVE_TO;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.MARK_AS_SEEN;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.MUTED_VIDEOS;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_DARK_THEME;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_HASHTAG_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_LIGHT_THEME;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_LIKED_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_LOCATION_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_PROFILE_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_TAGGED_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.PREF_TOPIC_POSTS_LAYOUT;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.SHOW_CAPTIONS;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.SHOW_QUICK_ACCESS_DIALOG;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.SKIPPED_VERSION;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.SWAP_DATE_TIME_FORMAT_ENABLED;


public final class SettingsHelper {
    private final SharedPreferences sharedPreferences;

    public SettingsHelper(@NonNull final Context context) {
        this.sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public String getString(@StringSettings final String key) {
        final String stringDefault = getStringDefault(key);
        if (sharedPreferences != null) return sharedPreferences.getString(key, stringDefault);
        return stringDefault;
    }


    public boolean getBoolean(@BooleanSettings final String key) {
        if (sharedPreferences != null) return sharedPreferences.getBoolean(key, false);
        return false;
    }

    @NonNull
    private String getStringDefault(@StringSettings final String key) {
        if (Constants.DATE_TIME_FORMAT.equals(key))
            return "hh:mm:ss a 'on' dd-MM-yyyy";
        if (DATE_TIME_SELECTION.equals(key))
            return "0;3;0";
        return "";
    }


    public void putString(@StringSettings final String key, final String val) {
        if (sharedPreferences != null) sharedPreferences.edit().putString(key, val).apply();
    }

    @StringDef(
            {APP_LANGUAGE, APP_THEME, APP_UA, BROWSER_UA, COOKIE, FOLDER_PATH, Constants.DATE_TIME_FORMAT, DATE_TIME_SELECTION,
                    CUSTOM_DATE_TIME_FORMAT, DEVICE_UUID, SKIPPED_VERSION, DEFAULT_TAB, PREF_DARK_THEME, PREF_LIGHT_THEME,
                    PREF_POSTS_LAYOUT, PREF_PROFILE_POSTS_LAYOUT, PREF_TOPIC_POSTS_LAYOUT, PREF_HASHTAG_POSTS_LAYOUT,
                    PREF_LOCATION_POSTS_LAYOUT, PREF_LIKED_POSTS_LAYOUT, PREF_TAGGED_POSTS_LAYOUT})
    public @interface StringSettings {
    }

    @StringDef({DOWNLOAD_USER_FOLDER, FOLDER_SAVE_TO, AUTOPLAY_VIDEOS, SHOW_QUICK_ACCESS_DIALOG, MUTED_VIDEOS,
            SHOW_CAPTIONS, CUSTOM_DATE_TIME_FORMAT_ENABLED, MARK_AS_SEEN, DM_MARK_AS_SEEN, CHECK_ACTIVITY,
            CHECK_UPDATES, SWAP_DATE_TIME_FORMAT_ENABLED})
    public @interface BooleanSettings {
    }



}