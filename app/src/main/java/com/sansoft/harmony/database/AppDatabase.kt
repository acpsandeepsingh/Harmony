/*
 * SPDX-FileCopyrightText: 2017-2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sansoft.harmony.database.favorite.dao.FavoriteSongDAO
import com.sansoft.harmony.database.favorite.model.FavoriteSong
import com.sansoft.harmony.database.feed.dao.FeedDAO
import com.sansoft.harmony.database.feed.dao.FeedGroupDAO
import com.sansoft.harmony.database.feed.model.FeedEntity
import com.sansoft.harmony.database.feed.model.FeedGroupEntity
import com.sansoft.harmony.database.feed.model.FeedGroupSubscriptionEntity
import com.sansoft.harmony.database.feed.model.FeedLastUpdatedEntity
import com.sansoft.harmony.database.history.dao.SearchHistoryDAO
import com.sansoft.harmony.database.history.dao.StreamHistoryDAO
import com.sansoft.harmony.database.history.model.SearchHistoryEntry
import com.sansoft.harmony.database.history.model.StreamHistoryEntity
import com.sansoft.harmony.database.playlist.dao.PlaylistDAO
import com.sansoft.harmony.database.playlist.dao.PlaylistRemoteDAO
import com.sansoft.harmony.database.playlist.dao.PlaylistStreamDAO
import com.sansoft.harmony.database.playlist.model.PlaylistEntity
import com.sansoft.harmony.database.playlist.model.PlaylistRemoteEntity
import com.sansoft.harmony.database.playlist.model.PlaylistStreamEntity
import com.sansoft.harmony.database.stream.dao.StreamDAO
import com.sansoft.harmony.database.stream.dao.StreamStateDAO
import com.sansoft.harmony.database.stream.model.StreamEntity
import com.sansoft.harmony.database.stream.model.StreamStateEntity
import com.sansoft.harmony.database.subscription.SubscriptionDAO
import com.sansoft.harmony.database.subscription.model.SubscriptionEntity

@TypeConverters(Converters::class)
@Database(
    version = Migrations.DB_VER_10,
    entities = [
        SubscriptionEntity::class,
        SearchHistoryEntry::class,
        StreamEntity::class,
        StreamHistoryEntity::class,
        StreamStateEntity::class,
        PlaylistEntity::class,
        PlaylistStreamEntity::class,
        PlaylistRemoteEntity::class,
        FeedEntity::class,
        FeedGroupEntity::class,
        FeedGroupSubscriptionEntity::class,
        FeedLastUpdatedEntity::class,
        FavoriteSong::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDAO(): FeedDAO
    abstract fun feedGroupDAO(): FeedGroupDAO
    abstract fun playlistDAO(): PlaylistDAO
    abstract fun playlistRemoteDAO(): PlaylistRemoteDAO
    abstract fun playlistStreamDAO(): PlaylistStreamDAO
    abstract fun searchHistoryDAO(): SearchHistoryDAO
    abstract fun streamDAO(): StreamDAO
    abstract fun streamHistoryDAO(): StreamHistoryDAO
    abstract fun streamStateDAO(): StreamStateDAO
    abstract fun subscriptionDAO(): SubscriptionDAO
    abstract fun favoriteSongDAO(): FavoriteSongDAO

    companion object {
        const val DATABASE_NAME: String = "newpipe.db"
    }
}
