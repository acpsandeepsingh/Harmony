package com.sansoft.harmony.player;

import androidx.annotation.NonNull;

import com.sansoft.harmony.extractor.stream.StreamInfo;

public class PlayerManager {

    public interface PlaybackStarter {
        void play(@NonNull StreamInfo streamInfo);
    }

    private final PlaybackStarter playbackStarter;

    public PlayerManager(@NonNull final PlaybackStarter playbackStarter) {
        this.playbackStarter = playbackStarter;
    }

    public void playStream(@NonNull final StreamInfo streamInfo) {
        playbackStarter.play(streamInfo);
    }
}
