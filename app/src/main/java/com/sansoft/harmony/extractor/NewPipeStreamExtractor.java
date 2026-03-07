package com.sansoft.harmony.extractor;

import androidx.annotation.NonNull;

import com.sansoft.harmony.extractor.stream.StreamInfo;

import com.sansoft.harmony.extractor.exceptions.ExtractionException;

import java.io.IOException;

public class NewPipeStreamExtractor {
    private final StreamingService service;

    public NewPipeStreamExtractor(@NonNull final StreamingService service) {
        this.service = service;
    }

    @NonNull
    public StreamInfo extractByVideoId(@NonNull final String videoId)
            throws IOException, ExtractionException {
        final String url = "https://www.youtube.com/watch?v=" + videoId;
        return StreamInfo.getInfo(service, url);
    }
}
