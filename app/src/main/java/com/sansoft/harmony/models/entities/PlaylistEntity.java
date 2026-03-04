package com.sansoft.harmony.models.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class PlaylistEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String firebaseId;
}
