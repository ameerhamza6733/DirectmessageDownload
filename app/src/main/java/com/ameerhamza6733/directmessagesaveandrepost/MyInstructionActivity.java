package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.StepFragment;
import com.hololo.tutorial.library.TutorialActivity;



public class MyInstructionActivity extends TutorialActivity implements StepFragment.OnButtonClickedListener {


    //    private int age;
//
    public static final String KEY_SHARED_PREF_IS_FIRST_TIME = "KEY_SHARED_PREF_IS_FIRST_TIME";
    public static final String IS_FIRST_TIME = "isFirstTime";

    public static final String COPY_LINK = "Copy Post Link";
    public static final String COPY_LINK_MESSAGE = "1)Open instagram find your any good Post\n2)click on \u2807 \n3)Click on Copy Link";
    public static final String COPY_LINK_COLOR = "#FF0957";
    public static final String COPY_LINK_BUTTON_TITLE = "try now";

    public static final String PAST_LIKK = "Past Post Link";
    public static final String PAST_LINK_MESSAGE = "1)Open this app again \n2)past link into text field\n3)click on save Post button";
    public static final String PAST_LINK_COLOR = "#00D4BA";
    public static final String PAST_LINK_BUTTON_TITLE = "ok";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            writeToSharedPraf();
            show_Instructons_fragment(COPY_LINK, COPY_LINK_MESSAGE, COPY_LINK_COLOR, COPY_LINK_BUTTON_TITLE, R.drawable.howto);
            show_Instructons_fragment(PAST_LIKK, PAST_LINK_MESSAGE, PAST_LINK_COLOR, PAST_LINK_BUTTON_TITLE,R.drawable.howto2);


    }

    private void show_Instructons_fragment(String title, String Content, String BackgroundColor, String ButtonTitle, int mDrawable) {
        addFragment(new Step.Builder().setTitle(title)
                .setContent(Content)
                .setBackgroundColor(Color.parseColor(BackgroundColor)) // int background color
                .setDrawable(mDrawable) // int top drawable
                .setButtonTitle(ButtonTitle)
                .build());
    }

    private void writeToSharedPraf() {

            My_Share_Pref.Companion.saveIsFirstTime(this, false);

    }

    @Override
    public void onButtonClicked(View view) {
        switch (((Button) view).getText().toString().toLowerCase()) {
            case COPY_LINK_BUTTON_TITLE:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.instagram.android")));
                } catch (android.content.ActivityNotFoundException anfe) {

                }
                break;
            case PAST_LINK_BUTTON_TITLE:
            Intent intent=    new Intent(this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
              finish();
                break;

        }
    }

    @Override
    public void finishTutorial() {

        super.finishTutorial();
    }
}

