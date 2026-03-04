package com.sansoft.harmony.player.event;

import com.google.android.exoplayer2.PlaybackParameters;

import com.sansoft.harmony.extractor.stream.StreamInfo;
import com.sansoft.harmony.player.playqueue.PlayQueue;

public interface PlayerEventListener {
    void onQueueUpdate(PlayQueue queue);
    void onPlaybackUpdate(int state, int repeatMode, boolean shuffled,
                          PlaybackParameters parameters);
    void onProgressUpdate(int currentProgress, int duration, int bufferPercent);
    void onMetadataUpdate(StreamInfo info, PlayQueue queue);
    default void onAudioTrackUpdate() { }
    void onServiceStopped();
}
