/*
 * SPDX-FileCopyrightText: 2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2024-2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.database.subscription

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sansoft.harmony.database.subscription.model.SubscriptionEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface SubscriptionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subscription: SubscriptionEntity): Long

    @Query("SELECT * FROM subscriptions")
    fun getAll(): Flowable<List<SubscriptionEntity>>

    @Query("DELETE FROM subscriptions WHERE uid = :subscriptionId")
    fun delete(subscriptionId: Long): Int
}
