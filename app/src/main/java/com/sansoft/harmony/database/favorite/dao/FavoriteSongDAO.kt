/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.favorite.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.favorite.model.FavoriteSong
import io.reactivex.rxjava3.core.Flowable

@Dao
interface FavoriteSongDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(favoriteSong: FavoriteSong): Long

    @Query("DELETE FROM favorite_songs WHERE service_id = :serviceId AND url = :url")
    fun delete(serviceId: Int, url: String): Int

    @Query("SELECT * FROM favorite_songs")
    fun getAll(): Flowable<List<FavoriteSong>>

    @Query("SELECT * FROM favorite_songs WHERE service_id = :serviceId AND url = :url")
    fun getByUrl(serviceId: Int, url: String): Flowable<List<FavoriteSong>>
}
