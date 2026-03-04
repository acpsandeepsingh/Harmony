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
import com.sansoft.harmony.database.stream.model.StreamEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface StreamDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stream: StreamEntity): Long

    @Query("SELECT * FROM streams WHERE id = :streamId")
    fun getById(streamId: Long): Flowable<StreamEntity>

    @Query("DELETE FROM streams WHERE id = :streamId")
    fun delete(streamId: Long): Int
}
