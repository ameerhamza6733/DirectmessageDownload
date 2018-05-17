package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by AmeerHamza on 10/5/2017.
 */

public class MyStandout extends StandOutWindow {
    @Override
    public void onCreate() {
        super.onCreate();

    }


    @Override
    public String getPersistentNotificationMessage(int id) {
        return "Click to close.";
    }

    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, MyStandout.class, id);
    }
    public static boolean isRunning=false;
    @Override
    public String getAppName() {
        return "inta downloader";
    }

    @Override
    public int getAppIcon() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public void createAndAttachView(final int id, FrameLayout frame) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.mystandout, frame, true);
        Button mQuickDownload = view.findViewById(R.id.quickDownload);
        isRunning=true;
        mQuickDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return  new StandOutLayoutParams(id, 200, 200,
                StandOutLayoutParams.BOTTOM, StandOutLayoutParams.RIGHT);
    }
    @Override
    public int getFlags(int id) {
        return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
                | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }
    @Override
    public boolean onClose(int id, Window window){
        super.onClose(id,window);

        isRunning=false;

        return false;
    }

}