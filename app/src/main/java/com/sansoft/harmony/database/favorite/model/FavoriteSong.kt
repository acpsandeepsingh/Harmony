package com.sansoft.harmony.database.favorite.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String,
    val duration: Long,
    val addedAt: Date
)