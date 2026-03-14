package com.sansoft.harmony.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResponse {
    @SerializedName(value = "videos", alternate = {"results", "items"})
    public List<Video> videos;
}
