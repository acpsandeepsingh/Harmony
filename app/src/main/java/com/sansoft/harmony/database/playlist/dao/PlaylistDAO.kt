/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.playlist.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.playlist.model.PlaylistEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaylistDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists")
    fun getAll(): Flowable<List<PlaylistEntity>>

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    fun delete(playlistId: Long): Int
}
