package com.sansoft.harmony.api;

import androidx.annotation.NonNull;

import com.sansoft.harmony.models.SearchResponse;
import com.sansoft.harmony.models.StreamResponse;
import com.sansoft.harmony.models.Video;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;

public class ApiClient {
    private final ApiService apiService;

    public ApiClient(@NonNull final ApiService apiService) {
        this.apiService = apiService;
    }

    @NonNull
    public List<Video> searchVideos(@NonNull final String query) throws IOException {
        final Response<SearchResponse> response = apiService.searchVideos(query).execute();
        if (!response.isSuccessful() || response.body() == null
                || response.body().videos == null) {
            return Collections.emptyList();
        }
        return response.body().videos;
    }

    public Video getVideoDetails(@NonNull final String videoId) throws IOException {
        final Response<Video> response = apiService.getVideoDetails(videoId).execute();
        if (!response.isSuccessful()) {
            return null;
        }
        return response.body();
    }

    public String getStreamUrl(@NonNull final String videoId) throws IOException {
        final Response<StreamResponse> response = apiService.getStreamUrl(videoId).execute();
        if (!response.isSuccessful() || response.body() == null) {
            return null;
        }
        return response.body().streamUrl;
    }
}
