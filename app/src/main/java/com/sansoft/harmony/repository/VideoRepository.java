package com.sansoft.harmony.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sansoft.harmony.api.ApiClient;
import com.sansoft.harmony.database.dao.VideoDao;
import com.sansoft.harmony.database.entity.VideoEntity;
import com.sansoft.harmony.model.VideoItem;
import com.sansoft.harmony.models.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoRepository {
    private static final String DEFAULT_QUERY = "trending music";

    private final VideoDao videoDao;
    private final ApiClient apiClient;

    public VideoRepository(@NonNull final VideoDao videoDao,
                           @NonNull final ApiClient apiClient) {
        this.videoDao = videoDao;
        this.apiClient = apiClient;
    }

    @NonNull
    public List<VideoItem> loadVideos() {
        final List<VideoEntity> cachedVideos = videoDao.getVideos();
        if (cachedVideos != null && !cachedVideos.isEmpty()) {
            return mapToItems(cachedVideos);
        }

        final List<VideoItem> remoteVideos = searchVideos(DEFAULT_QUERY);
        if (remoteVideos.isEmpty()) {
            return Collections.emptyList();
        }

        videoDao.insertVideos(mapToEntities(remoteVideos));
        return remoteVideos;
    }

    @NonNull
    public List<VideoItem> searchVideos(@NonNull final String query) {
        try {
            return mapToItems(apiClient.searchVideos(query));
        } catch (final IOException ignored) {
            return Collections.emptyList();
        }
    }

    @Nullable
    public VideoItem fetchVideoDetails(@NonNull final String videoId) {
        try {
            final Video video = apiClient.getVideoDetails(videoId);
            return video == null ? null : toVideoItem(video);
        } catch (final IOException ignored) {
            return null;
        }
    }

    @Nullable
    public String fetchStreamUrl(@NonNull final String videoId) {
        try {
            return apiClient.getStreamUrl(videoId);
        } catch (final IOException ignored) {
            return null;
        }
    }

    @NonNull
    private List<VideoItem> mapToItems(@NonNull final List<VideoEntity> entities) {
        final List<VideoItem> items = new ArrayList<>(entities.size());
        for (final VideoEntity entity : entities) {
            items.add(new VideoItem(entity.videoId, entity.title, entity.uploader,
                    entity.thumbnailUrl, entity.duration));
        }
        return items;
    }

    @NonNull
    private List<VideoItem> mapToItems(@NonNull final List<Video> videos) {
        final List<VideoItem> items = new ArrayList<>(videos.size());
        for (final Video video : videos) {
            items.add(toVideoItem(video));
        }
        return items;
    }

    @NonNull
    private VideoItem toVideoItem(@NonNull final Video video) {
        return new VideoItem(video.videoId, video.title, video.channel,
                video.thumbnail, video.duration);
    }

    @NonNull
    private List<VideoEntity> mapToEntities(@NonNull final List<VideoItem> items) {
        final List<VideoEntity> entities = new ArrayList<>(items.size());
        for (final VideoItem item : items) {
            entities.add(new VideoEntity(item.videoId, item.title, item.uploader,
                    item.thumbnailUrl, item.duration));
        }
        return entities;
    }
}
