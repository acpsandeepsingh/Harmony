/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.history.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "stream_history")
data class StreamHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "stream_id")
    val streamId: Long,

    @ColumnInfo(name = "access_date")
    val accessDate: Date
)
