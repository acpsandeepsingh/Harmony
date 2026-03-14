package com.sansoft.harmony.api;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitInstance {
    public static final String DEFAULT_BASE_URL = "https://harmonystream-api.onrender.com/api/";

    private RetrofitInstance() {
    }

    @NonNull
    public static ApiService createApiService() {
        return createApiService(DEFAULT_BASE_URL);
    }

    @NonNull
    public static ApiService createApiService(@NonNull final String baseUrl) {
        final OkHttpClient client = new OkHttpClient.Builder().build();
        final Gson gson = new GsonBuilder().create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        return retrofit.create(ApiService.class);
    }
}
