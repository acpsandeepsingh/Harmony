package com.sansoft.harmony.util

import java.util.concurrent.TimeUnit

object TimeAgo {
    fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(duration)
        val seconds = duration - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%d:%02d", minutes, seconds)
    }
}
