/*
 * SPDX-FileCopyrightText: 2016-2026 NewPipe contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.sansoft.harmony.info_list

import android.content.Context
import com.sansoft.harmony.extractor.channel.ChannelInfoItem
import com.sansoft.harmony.extractor.comments.CommentsInfoItem
import com.sansoft.harmony.extractor.playlist.PlaylistInfoItem
import com.sansoft.harmony.extractor.stream.StreamInfoItem
import com.sansoft.harmony.util.OnClickGesture

class InfoItemBuilder(val context: Context) {
    var onStreamSelectedListener: OnClickGesture<StreamInfoItem>? = null
    var onChannelSelectedListener: OnClickGesture<ChannelInfoItem>? = null
    var onPlaylistSelectedListener: OnClickGesture<PlaylistInfoItem>? = null
    var onCommentsSelectedListener: OnClickGesture<CommentsInfoItem>? = null
}
