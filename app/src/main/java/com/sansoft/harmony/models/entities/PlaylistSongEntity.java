package com.sansoft.harmony.models.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "playlist_songs",
        primaryKeys = {"playlistId", "videoId"},
        foreignKeys = @ForeignKey(entity = PlaylistEntity.class,
                                  parentColumns = "id",
                                  childColumns = "playlistId",
                                  onDelete = ForeignKey.CASCADE))
public class PlaylistSongEntity {
    public long playlistId;
    public String videoId;
    public String title;
    public String uploader;
    public String thumbnailUrl;
    public long duration;
}
