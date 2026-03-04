/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.feed.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.feed.model.FeedEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface FeedDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feed: FeedEntity): Long

    @Query("SELECT * FROM feed")
    fun getAll(): Flowable<List<FeedEntity>>

    @Query("DELETE FROM feed")
    fun deleteAll(): Int
}
