package com.sansoft.harmony.models;

import com.google.gson.annotations.SerializedName;

public class StreamResponse {
    @SerializedName(value = "streamUrl", alternate = {"url"})
    public String streamUrl;
}
