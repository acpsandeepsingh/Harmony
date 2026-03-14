package com.sansoft.harmony.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Video {
    @SerializedName(value = "videoId", alternate = {"id"})
    @NonNull
    public String videoId = "";

    @SerializedName("title")
    @NonNull
    public String title = "";

    @SerializedName(value = "channel", alternate = {"channelName", "uploader"})
    @NonNull
    public String channel = "";

    @SerializedName(value = "thumbnail", alternate = {"thumbnailUrl"})
    @NonNull
    public String thumbnail = "";

    @SerializedName("duration")
    public long duration;

    @SerializedName(value = "streamUrl", alternate = {"url"})
    public String streamUrl;
}
