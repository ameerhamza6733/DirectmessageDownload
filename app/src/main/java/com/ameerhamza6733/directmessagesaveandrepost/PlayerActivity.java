package com.ameerhamza6733.directmessagesaveandrepost;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

/**
 * Created by hamza rafiq on 10/14/18.
 */

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;
    private String mediaPath;

    public static final String EXTRA_VIDEO_PATH="EXTRA_VIDEO_PATH";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_view);

        if (getIntent()!=null) {
           mediaPath= getIntent().getStringExtra(EXTRA_VIDEO_PATH);
        }

        playerView=findViewById(R.id.player_view_);
    }

    @Override
    protected void onStart() {
        super.onStart();

        simpleExoPlayer= ExoPlayerFactory.newSimpleInstance(this,new DefaultTrackSelector());
        playerView.setPlayer(simpleExoPlayer);

        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this,"exo-demo"));

        ExtractorMediaSource extractorMediaSource = new ExtractorMediaSource.Factory(defaultDataSourceFactory)
                .createMediaSource(FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(mediaPath)));

        simpleExoPlayer.prepare(extractorMediaSource);
        simpleExoPlayer.setPlayWhenReady(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        playerView.setPlayer(null);
        simpleExoPlayer.release();
        simpleExoPlayer=null;
    }
}
