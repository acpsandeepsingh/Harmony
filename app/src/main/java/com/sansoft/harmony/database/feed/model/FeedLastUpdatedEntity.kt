/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.feed.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_last_updated")
data class FeedLastUpdatedEntity(
    @PrimaryKey
    @ColumnInfo(name = "feed_url")
    val feedUrl: String,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)
