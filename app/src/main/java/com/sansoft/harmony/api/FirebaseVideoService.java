package com.sansoft.harmony.api;

import com.sansoft.harmony.model.VideoItem;

import java.util.List;

public interface FirebaseVideoService {
    List<VideoItem> fetchVideos();
}
