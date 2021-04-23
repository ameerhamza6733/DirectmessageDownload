package com.ameerhamza6733.directmessagesaveandrepost;

/**
 * Created by AmeerHamza on 9/15/2017.
 */

public class Post {
    private String imageURL;
    private String videoURL;
    private StringBuilder hashTags;
    private String content;
    private String medium;
    private String postID;
    private String pathToStorage;
    private String url;
    private String type;




    public Post() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postDownloadingName) {
        postID = postDownloadingName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void setPathToStorage(String pathToStorage) {
        this.pathToStorage = pathToStorage;
    }

    public String getPathToStorage() {
        return pathToStorage;
    }
}
