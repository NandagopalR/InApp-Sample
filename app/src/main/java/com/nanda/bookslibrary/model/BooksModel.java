package com.nanda.bookslibrary.model;

import com.google.gson.annotations.SerializedName;

public class BooksModel {

    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    @SerializedName("id")
    private String id;

    @SerializedName("selfLink")
    private String selfLink;

    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }
}