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
import com.sansoft.harmony.database.history.model.SearchHistoryEntry
import io.reactivex.rxjava3.core.Flowable

@Dao
interface SearchHistoryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: SearchHistoryEntry): Long

    @Query("SELECT * FROM search_history ORDER BY creation_date DESC")
    fun getAll(): Flowable<List<SearchHistoryEntry>>

    @Query("DELETE FROM search_history WHERE id = :id")
    fun delete(id: Long): Int

    @Query("DELETE FROM search_history")
    fun deleteAll(): Int
}
