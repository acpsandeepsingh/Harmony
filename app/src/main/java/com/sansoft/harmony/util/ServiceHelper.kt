/*
 * SPDX-FileCopyrightText: 2018-2026 NewPipe contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sansoft.harmony.R
import com.sansoft.harmony.extractor.ServiceList
import com.sansoft.harmony.extractor.StreamingService
import java.util.concurrent.TimeUnit

object ServiceHelper {
    @JvmStatic
    @DrawableRes
    fun getIcon(serviceId: Int): Int {
        // Only YouTube is supported
        return R.drawable.ic_smart_display
    }

    @JvmStatic
    fun getTranslatedFilterString(filter: String, context: Context): String {
        return when (filter) {
            "all" -> context.getString(R.string.all)
            "videos", "sepia_videos", "music_videos" -> context.getString(R.string.videos_string)
            "channels" -> context.getString(R.string.channels)
            "playlists", "music_playlists" -> context.getString(R.string.playlists)
            "tracks" -> context.getString(R.string.tracks)
            "users" -> context.getString(R.string.users)
            "conferences" -> context.getString(R.string.conferences)
            "events" -> context.getString(R.string.events)
            "music_songs" -> context.getString(R.string.songs)
            "music_albums" -> context.getString(R.string.albums)
            "music_artists" -> context.getString(R.string.artists)
            else -> filter
        }
    }

    /**
     * Get a resource string with instructions for importing subscriptions for each service.
     *
     * @param serviceId service to get the instructions for
     * @return the string resource containing the instructions or -1 if the service don't support it
     */
    @JvmStatic
    @StringRes
    fun getImportInstructions(serviceId: Int): Int {
        return R.string.import_youtube_instructions
    }

    /**
     * For services that support importing from a channel url, return a hint that will
     * be used in the EditText that the user will type in his channel url.
     *
     * @param serviceId service to get the hint for
     * @return the hint's string resource or -1 if the service don't support it
     */
    @JvmStatic
    @StringRes
    fun getImportInstructionsHint(serviceId: Int): Int {
        return -1
    }

    @JvmStatic
    fun getSelectedServiceId(context: Context): Int {
        return ServiceList.YouTube.serviceId
    }

    @JvmStatic
    fun getSelectedService(context: Context): StreamingService {
        return ServiceList.YouTube
    }

    @JvmStatic
    fun getNameOfServiceById(serviceId: Int): String {
        return ServiceList.YouTube.serviceInfo.name
    }

    /**
     * @param serviceId the id of the service
     * @return the service corresponding to the provided id
     */
    @JvmStatic
    fun getServiceById(serviceId: Int): StreamingService {
        return ServiceList.YouTube
    }

    @JvmStatic
    fun setSelectedServiceId(context: Context, serviceId: Int) {
        // no-op, only YouTube is supported
    }

    @JvmStatic
    fun getCacheExpirationMillis(serviceId: Int): Long {
        return TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
    }

    @JvmStatic
    fun initServices(context: Context) {
        // no-op, only YouTube is supported
    }
}
