package com.sansoft.harmony.database.favorite.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.favorite.model.FavoriteSong

@Dao
interface FavoriteSongDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteSong: FavoriteSong)

    @Query("DELETE FROM favorite_songs WHERE videoId = :videoId")
    suspend fun delete(videoId: String)

    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    suspend fun getAll(): List<FavoriteSong>
}