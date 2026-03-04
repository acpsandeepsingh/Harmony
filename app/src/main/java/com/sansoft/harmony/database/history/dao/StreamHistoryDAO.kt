/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.history.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.history.model.StreamHistoryEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface StreamHistoryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(streamHistory: StreamHistoryEntity): Long

    @Query("SELECT * FROM stream_history ORDER BY access_date DESC")
    fun getAll(): Flowable<List<StreamHistoryEntity>>

    @Query("DELETE FROM stream_history WHERE stream_id = :streamId")
    fun delete(streamId: Long): Int

    @Query("DELETE FROM stream_history")
    fun deleteAll(): Int
}
