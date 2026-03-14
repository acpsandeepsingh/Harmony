package com.sansoft.harmony.player;

import androidx.annotation.NonNull;

public class PlayerManager {

    public interface PlaybackStarter {
        void play(@NonNull String streamUrl);
    }

    private final PlaybackStarter playbackStarter;

    public PlayerManager(@NonNull final PlaybackStarter playbackStarter) {
        this.playbackStarter = playbackStarter;
    }

    public void playStream(@NonNull final String streamUrl) {
        playbackStarter.play(streamUrl);
    }
}
