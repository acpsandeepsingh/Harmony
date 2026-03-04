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
import com.sansoft.harmony.database.feed.model.FeedGroupEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface FeedGroupDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feedGroup: FeedGroupEntity): Long

    @Query("SELECT * FROM feed_groups")
    fun getAll(): Flowable<List<FeedGroupEntity>>

    @Query("DELETE FROM feed_groups WHERE id = :groupId")
    fun delete(groupId: Long): Int
}
