package com.ameerhamza6733.directmessagesaveandrepost.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum MediaItemType implements Serializable {
    @SerializedName("1")
    MEDIA_TYPE_IMAGE(1),
    @SerializedName("2")
    MEDIA_TYPE_VIDEO(2),
    @SerializedName("8")
    MEDIA_TYPE_SLIDER(8),
    @SerializedName("11")
    MEDIA_TYPE_VOICE(11),
    // 5 is arbitrary
    @SerializedName("5")
    MEDIA_TYPE_LIVE(5);

    private static final Map<Integer, MediaItemType> map = new HashMap<>();

    static {
        for (MediaItemType type : MediaItemType.values()) {
            map.put(type.id, type);
        }
    }

    private final int id;

    MediaItemType(final int id) {
        this.id = id;
    }

    public static MediaItemType valueOf(final int id) {
        return map.get(id);
    }

    public int getId() {
        return id;
    }
}