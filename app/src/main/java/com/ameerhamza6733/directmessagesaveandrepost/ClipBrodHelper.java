package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.fuseable.ScalarCallable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by AmeerHamza on 9/17/2017.
 */

public class ClipBrodHelper {
    private String clipBrodText;

    public ClipBrodHelper() {// help to make empty obj in kotlin
    }

    public ClipBrodHelper(Context context) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
            android.content.ClipData data = clipboard.getPrimaryClip();
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                Log.d(this.getClass().getSimpleName(), "clipBordData" + data.getItemAt(0).getText());
                if (isIntigramURL(data.getItemAt(0).getText().toString()))
                    clipBrodText = String.valueOf(data.getItemAt(0).getText());
            }

        }

    }

    public void WriteToClipBord(Context context , String data){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFF_SETTINGS_NAME, Context.MODE_PRIVATE);
        data = sharedPreferences.getString(Settings.DEFAULT_CAPTION_KEY," ")+data;
        Toast.makeText(context,"Data copped to clip bord",Toast.LENGTH_SHORT).show();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text label", data);
        clipboard.setPrimaryClip(clip);
    }


    public String getClipBrodText() {
        return clipBrodText;
    }

    private boolean isIntigramURL(String data) {
        return data.contains("instagram.com/p/");
    }
}
