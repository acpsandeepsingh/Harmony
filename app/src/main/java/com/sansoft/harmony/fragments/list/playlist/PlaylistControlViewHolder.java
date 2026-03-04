package com.sansoft.harmony.fragments.list.playlist;

import com.sansoft.harmony.player.playqueue.PlayQueue;

/**
 * Interface for {@code R.layout.playlist_control} view holders
 * to give access to the play queue.
 */
public interface PlaylistControlViewHolder {
    PlayQueue getPlayQueue();
}
