package com.sansoft.harmony.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "videos")
public class VideoEntity {
    @PrimaryKey
    @NonNull
    public final String videoId;

    @NonNull
    public final String title;

    @NonNull
    public final String uploader;

    @NonNull
    public final String thumbnailUrl;

    public final long duration;

    public VideoEntity(@NonNull final String videoId,
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
