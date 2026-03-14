package com.sansoft.harmony.api;

import com.sansoft.harmony.models.SearchResponse;
import com.sansoft.harmony.models.StreamResponse;
import com.sansoft.harmony.models.Video;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("search")
    Call<SearchResponse> searchVideos(@Query("q") String query);

    @GET("videos/{videoId}")
    Call<Video> getVideoDetails(@Path("videoId") String videoId);

    @GET("videos/{videoId}/stream")
    Call<StreamResponse> getStreamUrl(@Path("videoId") String videoId);
}
