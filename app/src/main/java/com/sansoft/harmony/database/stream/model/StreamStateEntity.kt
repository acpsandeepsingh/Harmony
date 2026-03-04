no/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.stream.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_states")
data class StreamStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "stream_id")
    val streamId: Long,

    @ColumnInfo(name = "progress_time")
    val progressTime: Long
)
