package com.sansoft.harmony.model;

import androidx.annotation.NonNull;

public class VideoItem {
    @NonNull
    public final String videoId;
    @NonNull
    public final String title;
    @NonNull
    public final String uploader;
    @NonNull
    public final String thumbnailUrl;
    public final long duration;

    public VideoItem(@NonNull final String videoId,
                     @NonNull final String title,
                     @NonNull final String uploader,
                     @NonNull final String thumbnailUrl,
                     final long duration) {
        this.videoId = videoId;
        this.title = title;
        this.uploader = uploader;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
    }
}
