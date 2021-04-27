package com.ameerhamza6733.directmessagesaveandrepost;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ameerhamza6733.directmessagesaveandrepost.model.Audio;
import com.ameerhamza6733.directmessagesaveandrepost.model.Media;
import com.ameerhamza6733.directmessagesaveandrepost.model.VideoVersion;
import com.ameerhamza6733.directmessagesaveandrepost.utils.NetworkUtils;
import com.ameerhamza6733.directmessagesaveandrepost.utils.ResponseBodyUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public final class PostFetcher extends AsyncTask<Void, Void, Media> {
    private static final String TAG = "PostFetcher";

    private final String shortCode;


    public PostFetcher(final String shortCode) {
        this.shortCode = shortCode;

    }

    @Override
    protected Media doInBackground(final Void... voids) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL("https://www.instagram.com/p/" + "COGbafXHjXVRm63MeFgJmzUfLpBWaYVfKy1-540" + "/?__a=1").openConnection();
            conn.setUseCaches(false);
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                final JSONObject media = new JSONObject(NetworkUtils.readFromConnection(conn)).getJSONObject("graphql")
                        .getJSONObject("shortcode_media");

                return ResponseBodyUtils.parseGraphQLItem(media);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching post", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(final Media feedModel) {
        Log.d(TAG,  getUrlOfType(feedModel));

    }

    private  String getUrlOfType(@NonNull final Media media) {
        switch (media.getMediaType()) {
            case MEDIA_TYPE_IMAGE: {
                return ResponseBodyUtils.getImageUrl(media);
            }
            case MEDIA_TYPE_VIDEO: {
                final List<VideoVersion> videoVersions = media.getVideoVersions();
                String url = null;
                if (videoVersions != null && !videoVersions.isEmpty()) {
                    final VideoVersion videoVersion = videoVersions.get(0);
                    if (videoVersion != null) {
                        url = videoVersion.getUrl();
                    }
                }
                return url;
            }
            case MEDIA_TYPE_VOICE: {
                final Audio audio = media.getAudio();
                String url = null;
                if (audio != null) {
                    url = audio.getAudioSrc();
                }
                return url;
            }
        }
        return null;
    }
}
