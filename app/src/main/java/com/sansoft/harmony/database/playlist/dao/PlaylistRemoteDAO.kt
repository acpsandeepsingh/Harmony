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
import com.sansoft.harmony.database.playlist.model.PlaylistRemoteEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaylistRemoteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlistRemote: PlaylistRemoteEntity): Long

    @Query("SELECT * FROM playlist_remotes")
    fun getAll(): Flowable<List<PlaylistRemoteEntity>>

    @Query("DELETE FROM playlist_remotes WHERE playlist_id = :playlistId")
    fun delete(playlistId: Long): Int
}
