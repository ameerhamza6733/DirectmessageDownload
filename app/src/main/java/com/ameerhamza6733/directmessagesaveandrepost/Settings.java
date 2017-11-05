package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    public static final String ADO_DOWNLOADING_START_KEY = "ato_start_downloading";

    private EditText mDefaultCaptionEditText;
    private CheckBox mSaveHistoryCheckBox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Button mSaveCaptionButton;
    private CheckBox mAtoStartDownloadingCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        mAtoStartDownloadingCheckBox= (CheckBox) findViewById(R.id.ato_start_downloading_checkBox);
    }

    private void setChackBoxStates() {
        if(sharedPreferences.getBoolean(SHARED_PREF_CHECK_BOX_KEY,true))
            mSaveHistoryCheckBox.setChecked(sharedPreferences.getBoolean(SHARED_PREF_CHECK_BOX_KEY,true));
        if (sharedPreferences.getBoolean(ADO_DOWNLOADING_START_KEY,true))
            mAtoStartDownloadingCheckBox.setChecked(sharedPreferences.getBoolean(ADO_DOWNLOADING_START_KEY,true));
        mDefaultCaptionEditText.setText(sharedPreferences.getString(DEFAULT_CAPTION_KEY,""));
    }

    private void listerners() {
        mSaveHistoryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(SHARED_PREF_CHECK_BOX_KEY,b).apply();
            }
        });
        mAtoStartDownloadingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(ADO_DOWNLOADING_START_KEY,b).apply();
            }
        });

        mSaveCaptionButton.setOnClickListener(new View.OnClickListener() {@Override
            public void onClick(View view) {

                if (mDefaultCaptionEditText.getText().toString().isEmpty())
                {
                    Toast.makeText(Settings.this,"Please Caption tex first",Toast.LENGTH_SHORT).show();
                    return;
                }
                editor.putString(DEFAULT_CAPTION_KEY,mDefaultCaptionEditText.getText().toString()).apply();
                Toast.makeText(Settings.this,"Default Caption Saved",Toast.LENGTH_LONG).show();
            }
        });
    }
}
