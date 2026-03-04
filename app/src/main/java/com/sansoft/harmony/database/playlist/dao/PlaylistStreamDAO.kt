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
import com.sansoft.harmony.database.playlist.model.PlaylistStreamEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaylistStreamDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlistStream: PlaylistStreamEntity): Long

    @Query("SELECT * FROM playlist_streams WHERE playlist_id = :playlistId ORDER BY join_index ASC")
    fun getByPlaylistId(playlistId: Long): Flowable<List<PlaylistStreamEntity>>

    @Query("DELETE FROM playlist_streams WHERE playlist_id = :playlistId AND stream_id = :streamId")
    fun delete(playlistId: Long, streamId: Long): Int
}
