package com.sansoft.harmony.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sansoft.harmony.database.entity.VideoEntity;

import java.util.List;

@Dao
public interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY title COLLATE NOCASE ASC")
    List<VideoEntity> getVideos();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideos(List<VideoEntity> videos);
}
