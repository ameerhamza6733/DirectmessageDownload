package com.ameerhamza6733.directmessagesaveandrepost;

import android.app.Application;
import android.text.TextUtils;

import com.ameerhamza6733.directmessagesaveandrepost.utils.Constants;
import com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils;
import com.ameerhamza6733.directmessagesaveandrepost.utils.SettingsHelper;

import java.net.CookieHandler;
import java.util.UUID;


import static com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils.NET_COOKIE_MANAGER;
import static com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils.settingsHelper;

/**
 * Created by apple on 5/27/18.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CookieHandler.setDefault(NET_COOKIE_MANAGER);

        if (settingsHelper == null) {
            settingsHelper = new SettingsHelper(this);
        }
        String cookie = settingsHelper.getString(Constants.COOKIE);
        CookieUtils.setupCookies(cookie);
        if (TextUtils.isEmpty(settingsHelper.getString(Constants.DEVICE_UUID))) {
            settingsHelper.putString(Constants.DEVICE_UUID, UUID.randomUUID().toString());
        }
    }
}
