package com.ameerhamza6733.directmessagesaveandrepost;

import java.util.List;

/**
 * Created by AmeerHamza on 9/15/2017.
 */

public class post {
    private String imageURL;
    private String videoURL;
    private StringBuilder hashTags;
    private String content;
    private String medium;
    private String PostDownloadingName;



    public post() {

    }

    public String getPostDownloadingName() {
        return PostDownloadingName;
    }

    public void setPostDownloadingName(String postDownloadingName) {
        PostDownloadingName = postDownloadingName;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public void setHashTags(StringBuilder hashTags) {
        this.hashTags = hashTags;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getVideoURL() {
        return videoURL;
    }
    StringBuilder getHashTags() {
        return hashTags;
    }

    public String getContent() {
        return content;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }
}
