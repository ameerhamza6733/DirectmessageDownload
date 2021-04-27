package com.ameerhamza6733.directmessagesaveandrepost.model;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;


public class Media implements Serializable {
    private final String id;
    private final String code;
    private final MediaItemType mediaType;
    private final ImageVersions2 imageVersions2;
    private final List<VideoVersion> videoVersions;
    private final Caption caption;
    private final Audio audio;
    private final List<Media> carouselMedia;
    private boolean isSidecarChild;


    public Media(final String id,
                 final String code,
                 final ImageVersions2 imageVersions2,
                 final MediaItemType mediaType,
                 final List<VideoVersion> videoVersions,
                 final Caption caption,
                 final Audio audio,
                 final List<Media> carouselMedia) {
        this.id = id;
        this.code = code;
        this.imageVersions2 = imageVersions2;
        this.mediaType = mediaType;
        this.videoVersions = videoVersions;
        this.caption = caption;
        this.audio = audio;
        this.carouselMedia = carouselMedia;
    }


    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }


    public ImageVersions2 getImageVersions2() {
        return imageVersions2;
    }

    public MediaItemType getMediaType() {
        return mediaType;
    }


    public List<VideoVersion> getVideoVersions() {
        return videoVersions;
    }


    public Caption getCaption() {
        return caption;
    }

    public Audio getAudio() {
        return audio;
    }

    public List<Media> getCarouselMedia() {
        return carouselMedia;
    }

    public void setIsSidecarChild(boolean isSidecarChild) {
        this.isSidecarChild = isSidecarChild;
    }

    public boolean isSidecarChild() {
        return isSidecarChild;
    }

    public void setPostCaption(final String caption) {
        final Caption caption1 = getCaption();
        caption1.setText(caption);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Media media = (Media) o;
        return
                Objects.equals(id, media.id) &&
                        isSidecarChild == media.isSidecarChild &&
                        Objects.equals(code, media.code) &&
                        mediaType == media.mediaType &&
                        Objects.equals(imageVersions2, media.imageVersions2) &&
                        Objects.equals(videoVersions, media.videoVersions) &&
                        Objects.equals(caption, media.caption) &&
                        Objects.equals(audio, media.audio) &&
                        Objects.equals(carouselMedia, media.carouselMedia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, mediaType,
                imageVersions2, videoVersions, caption, audio, carouselMedia, isSidecarChild);
    }
}
