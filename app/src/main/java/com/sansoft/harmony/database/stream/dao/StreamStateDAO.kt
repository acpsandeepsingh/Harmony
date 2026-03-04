/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.stream.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.stream.model.StreamStateEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface StreamStateDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(streamState: StreamStateEntity): Long

    @Query("SELECT * FROM stream_states WHERE stream_id = :streamId")
    fun getByStreamId(streamId: Long): Flowable<List<StreamStateEntity>>
}
