package com.sansoft.harmony.repository;

import androidx.annotation.NonNull;

import com.sansoft.harmony.api.FirebaseVideoService;
import com.sansoft.harmony.database.dao.VideoDao;
import com.sansoft.harmony.database.entity.VideoEntity;
import com.sansoft.harmony.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

public class VideoRepository {
    private final VideoDao videoDao;
    private final FirebaseVideoService firebaseVideoService;

    public VideoRepository(@NonNull final VideoDao videoDao,
                           @NonNull final FirebaseVideoService firebaseVideoService) {
        this.videoDao = videoDao;
        this.firebaseVideoService = firebaseVideoService;
    }

    @NonNull
    public List<VideoItem> loadVideos() {
        final List<VideoEntity> cachedVideos = videoDao.getVideos();
        if (cachedVideos != null && !cachedVideos.isEmpty()) {
            return mapToItems(cachedVideos);
        }

        final List<VideoItem> firebaseVideos = firebaseVideoService.fetchVideos();
        if (firebaseVideos == null || firebaseVideos.isEmpty()) {
            return new ArrayList<>();
        }

        videoDao.insertVideos(mapToEntities(firebaseVideos));
        return firebaseVideos;
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
    private List<VideoEntity> mapToEntities(@NonNull final List<VideoItem> items) {
        final List<VideoEntity> entities = new ArrayList<>(items.size());
        for (final VideoItem item : items) {
            entities.add(new VideoEntity(item.videoId, item.title, item.uploader,
                    item.thumbnailUrl, item.duration));
        }
        return entities;
    }
}
