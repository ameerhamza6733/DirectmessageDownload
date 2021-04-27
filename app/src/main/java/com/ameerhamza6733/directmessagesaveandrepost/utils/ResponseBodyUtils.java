package com.ameerhamza6733.directmessagesaveandrepost.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;


import com.ameerhamza6733.directmessagesaveandrepost.BuildConfig;
import com.ameerhamza6733.directmessagesaveandrepost.model.Caption;
import com.ameerhamza6733.directmessagesaveandrepost.model.ImageVersions2;
import com.ameerhamza6733.directmessagesaveandrepost.model.Media;
import com.ameerhamza6733.directmessagesaveandrepost.model.MediaCandidate;
import com.ameerhamza6733.directmessagesaveandrepost.model.MediaItemType;
import com.ameerhamza6733.directmessagesaveandrepost.model.VideoVersion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public final class ResponseBodyUtils {
    private static final String TAG = "ResponseBodyUtils";

    // isI: true if the content was requested from i.instagram.com instead of graphql
    @Nullable
    public static String getHighQualityPost(final JSONArray resources, final boolean isVideo, final boolean isI, final boolean low) {
        try {
            final int resourcesLen = resources.length();

            final String[] sources = new String[resourcesLen];
            int lastResMain = low ? 1000000 : 0, lastIndexMain = -1;
            int lastResBase = low ? 1000000 : 0, lastIndexBase = -1;
            for (int i = 0; i < resourcesLen; ++i) {
                final JSONObject item = resources.getJSONObject(i);
                if (item != null && (!isVideo || item.has(Constants.EXTRAS_PROFILE) || isI)) {
                    sources[i] = item.getString(isI ? "url" : "src");
                    final int currRes = item.getInt(isI ? "width" : "config_width") * item.getInt(isI ? "height" : "config_height");

                    final String profile = isVideo ? item.optString(Constants.EXTRAS_PROFILE) : null;

                    if (!isVideo || "MAIN".equals(profile)) {
                        if (currRes > lastResMain && !low) {
                            lastResMain = currRes;
                            lastIndexMain = i;
                        } else if (currRes < lastResMain && low) {
                            lastResMain = currRes;
                            lastIndexMain = i;
                        }
                    } else {
                        if (currRes > lastResBase && !low) {
                            lastResBase = currRes;
                            lastIndexBase = i;
                        } else if (currRes < lastResBase && low) {
                            lastResBase = currRes;
                            lastIndexBase = i;
                        }
                    }
                }
            }

            if (lastIndexMain >= 0) return sources[lastIndexMain];
            else if (lastIndexBase >= 0) return sources[lastIndexBase];
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("Testing", "", e);
        }
        return null;
    }

    public static String getHighQualityImage(final JSONObject resources) {
        String src = null;
        try {
            if (resources.has("display_resources"))
                src = getHighQualityPost(resources.getJSONArray("display_resources"), false, false, false);
            else if (resources.has("image_versions2"))
                src = getHighQualityPost(resources.getJSONObject("image_versions2").getJSONArray("candidates"), false, true, false);
            if (src == null) return resources.getString("display_url");
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("Testing", "", e);
        }
        return src;
    }


    public static Media parseGraphQLItem(final JSONObject itemJson) throws JSONException {
        if (itemJson == null) {
            return null;
        }
        final JSONObject feedItem = itemJson.has("node") ? itemJson.getJSONObject("node") : itemJson;
        final String mediaType = feedItem.optString("__typename");
        if ("GraphSuggestedUserFeedUnit".equals(mediaType)) return null;

        final boolean isVideo = feedItem.optBoolean("is_video");
        final long videoViews = feedItem.optLong("video_view_count", 0);

        final String displayUrl = feedItem.optString("display_url");
        if (TextUtils.isEmpty(displayUrl)) return null;
        final String resourceUrl;
        if (isVideo && feedItem.has("video_url")) {
            resourceUrl = feedItem.getString("video_url");
        } else {
            resourceUrl = feedItem.has("display_resources") ? getHighQualityImage(feedItem) : displayUrl;
        }
        JSONObject tempJsonObject = feedItem.optJSONObject("edge_media_preview_comment");
        final long commentsCount = tempJsonObject != null ? tempJsonObject.optLong("count") : 0;
        tempJsonObject = feedItem.optJSONObject("edge_media_preview_like");
        final long likesCount = tempJsonObject != null ? tempJsonObject.optLong("count") : 0;
        tempJsonObject = feedItem.optJSONObject("edge_media_to_caption");
        final JSONArray captions = tempJsonObject != null ? tempJsonObject.getJSONArray("edges") : null;
        String captionText = null;
        if (captions != null && captions.length() > 0) {
            if ((tempJsonObject = captions.optJSONObject(0)) != null &&
                    (tempJsonObject = tempJsonObject.optJSONObject("node")) != null) {
                captionText = tempJsonObject.getString("text");
            }
        }
        final JSONObject locationJson = feedItem.optJSONObject("location");
        // Log.d(TAG, "location: " + (location == null ? null : location.toString()));
        long locationId = 0;
        String locationName = null;
        if (locationJson != null) {
            locationName = locationJson.optString("name");
            if (locationJson.has("id")) {
                locationId = locationJson.optLong("id");
            } else if (locationJson.has("pk")) {
                locationId = locationJson.optLong("pk");
            }
            // Log.d(TAG, "locationId: " + locationId);
        }
        int height = 0;
        int width = 0;
        final JSONObject dimensions = feedItem.optJSONObject("dimensions");
        if (dimensions != null) {
            height = dimensions.optInt("height");
            width = dimensions.optInt("width");
        }
        String thumbnailUrl = null;
        final List<MediaCandidate> candidates = new ArrayList<MediaCandidate>();
        if (feedItem.has("display_resources") || feedItem.has("thumbnail_resources")) {
            final JSONArray displayResources = feedItem.has("display_resources")
                    ? feedItem.getJSONArray("display_resources")
                    : feedItem.getJSONArray("thumbnail_resources");
            for (int i = 0; i < displayResources.length(); i++) {
                final JSONObject displayResource = displayResources.getJSONObject(i);
                candidates.add(new MediaCandidate(
                        displayResource.getInt("config_width"),
                        displayResource.getInt("config_height"),
                        displayResource.getString("src")
                ));
            }
        }
        final ImageVersions2 imageVersions2 = new ImageVersions2(candidates);

        long userId = -1;
        final String id = feedItem.getString(Constants.EXTRAS_ID);
        VideoVersion videoVersion = null;
        if (isVideo) {
            videoVersion = new VideoVersion(
                    null,
                    null,
                    width,
                    height,
                    resourceUrl
            );
        }
        final Caption caption = new Caption(
                userId,
                captionText
        );

        final boolean isSlider = "GraphSidecar".equals(mediaType) && feedItem.has("edge_sidecar_to_children");
        List<Media> childItems = null;
        if (isSlider) {
            childItems = new ArrayList<>();
            // feedModelBuilder.setItemType(MediaItemType.MEDIA_TYPE_SLIDER);
            final JSONObject sidecar = feedItem.optJSONObject("edge_sidecar_to_children");
            if (sidecar != null) {
                final JSONArray children = sidecar.optJSONArray("edges");
                if (children != null) {
                    // final List<PostChild> sliderItems = getSliderItems(children);
                    // feedModelBuilder.setSliderItems(sliderItems)
                    //                 .setImageHeight(sliderItems.get(0).getHeight())
                    //                 .setImageWidth(sliderItems.get(0).getWidth());
                    for (int i = 0; i < children.length(); i++) {
                        final JSONObject child = children.optJSONObject(i);
                        if (child == null) continue;
                        final Media media = parseGraphQLItem(child);
                        media.setIsSidecarChild(true);
                        childItems.add(media);
                    }
                }
            }
        }
        MediaItemType mediaItemType = MediaItemType.MEDIA_TYPE_IMAGE;
        if (isSlider) {
            mediaItemType = MediaItemType.MEDIA_TYPE_SLIDER;
        } else if (isVideo) {
            mediaItemType = MediaItemType.MEDIA_TYPE_VIDEO;
        }

        return new Media(
                id,
                feedItem.optString(Constants.EXTRAS_SHORTCODE),
                imageVersions2,
                mediaItemType,
                isVideo ? Collections.singletonList(videoVersion) : null,
                caption,
                null,
                childItems
        );
    }


    public static String getThumbUrl(final Media media) {
        return getImageCandidate(media, CandidateType.THUMBNAIL);
    }

    public static String getImageUrl(final Media media) {
        return getImageCandidate(media, CandidateType.DOWNLOAD);
    }

    private static String getImageCandidate(final Media media, final CandidateType type) {
        if (media == null) return null;
        final ImageVersions2 imageVersions2 = media.getImageVersions2();
        if (imageVersions2 == null) return null;
        final List<MediaCandidate> candidates = imageVersions2.getCandidates();
        if (candidates == null || candidates.isEmpty()) return null;

        final List<MediaCandidate> sortedCandidates = candidates.stream()
                .sorted((c1, c2) -> Integer.compare(c2.getWidth(), c1.getWidth()))
                .filter(c -> c.getWidth() < type.getValue())
                .collect(Collectors.toList());
        final MediaCandidate candidate = sortedCandidates.get(0);
        if (candidate == null) return null;
        return candidate.getUrl();
    }


    private enum CandidateType {
        THUMBNAIL(1000),
        DOWNLOAD(10000);

        private final int value;

        CandidateType(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
