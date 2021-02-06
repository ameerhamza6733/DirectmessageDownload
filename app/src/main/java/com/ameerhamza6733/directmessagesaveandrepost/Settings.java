package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;



public class Settings extends AppCompatActivity {
    public static final String SHARED_PREF_CHECK_BOX_KEY = "com.ameerhamza6733.directmessagesaveandrepost.mSaveHistoryCheckBox";
    public static final String SHARED_PREFF_SETTINGS_NAME = "com.ameerhamza6733.directmessagesaveandrepost.mSaveHistoryCheckBox.Settings";
    public static final String DEFAULT_CAPTION_KEY = "default_caption";
    public static final String ATO_START_DOWNLOADING_ = "ato_start_downloading_";

    private EditText mDefaultCaptionEditText;
    private CheckBox mSaveHistoryCheckBox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Button mSaveCaptionButton;
    private CheckBox mAtoStartDownloadingCheckBox;
    private Boolean AtoStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       AtoStart= My_Share_Pref.Companion.SaveAtoStart(this,true);
        inlizedViews();
        setChackBoxStates();
        listerners();

    }

    private void inlizedViews() {
        mDefaultCaptionEditText = (EditText) findViewById(R.id.defult_caption_text_view);
        mSaveHistoryCheckBox = (CheckBox) findViewById(R.id.save_history_checkBox);
        sharedPreferences = this.getSharedPreferences(SHARED_PREFF_SETTINGS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mSaveCaptionButton = (Button) findViewById(R.id.button_save_defacult_caption);
        mAtoStartDownloadingCheckBox = (CheckBox) findViewById(R.id.ato_start_downloading_checkBox);
    }

    private void setChackBoxStates() {
        if (sharedPreferences.getString(SHARED_PREF_CHECK_BOX_KEY, "1").equals("1"))
            mSaveHistoryCheckBox.setChecked(sharedPreferences.getString(SHARED_PREF_CHECK_BOX_KEY, "1") == "1");
        if (AtoStart.equals("1"))
            mAtoStartDownloadingCheckBox.setChecked(AtoStart .equals("1"));
        mDefaultCaptionEditText.setText(sharedPreferences.getString(DEFAULT_CAPTION_KEY, ""));
    }

    private void listerners() {
        mSaveHistoryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putString(SHARED_PREF_CHECK_BOX_KEY, b?"1":"0").apply();
            }
        });
        mAtoStartDownloadingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

               My_Share_Pref.Companion.SaveAtoStart(Settings.this, b);
            }
        });

        mSaveCaptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mDefaultCaptionEditText.getText().toString().isEmpty()) {
                    Toast.makeText(Settings.this, "Please Caption tex first", Toast.LENGTH_SHORT).show();
                    return;
                }
                editor.putString(DEFAULT_CAPTION_KEY, mDefaultCaptionEditText.getText().toString()).apply();
                Toast.makeText(Settings.this, "Default Caption Saved", Toast.LENGTH_LONG).show();
            }
        });
    }
}
