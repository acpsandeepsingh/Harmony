package com.sansoft.harmony.player;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class ExoPlayerManager {
    private final ExoPlayer exoPlayer;

    public ExoPlayerManager(@NonNull final Context context,
                            @NonNull final StyledPlayerView playerView) {
        exoPlayer = new ExoPlayer.Builder(context).build();
        playerView.setPlayer(exoPlayer);
        playerView.setControllerAutoShow(true);
    }

    public void play(@NonNull final String streamUrl) {
        exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl));
        exoPlayer.prepare();
        exoPlayer.play();
    }

    public void pause() {
        exoPlayer.pause();
    }

    public void seekTo(final long positionMs) {
        exoPlayer.seekTo(positionMs);
    }

    public void setFullscreen(final boolean isFullscreen) {
        exoPlayer.setVideoScalingMode(isFullscreen
                ? com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                : com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    }

    public void release() {
        exoPlayer.release();
    }
}
