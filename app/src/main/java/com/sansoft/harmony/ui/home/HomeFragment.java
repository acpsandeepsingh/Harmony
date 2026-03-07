package com.sansoft.harmony.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sansoft.harmony.extractor.NewPipeStreamExtractor;
import com.sansoft.harmony.model.VideoItem;
import com.sansoft.harmony.player.PlayerManager;
import com.sansoft.harmony.repository.VideoRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    private VideoRepository videoRepository;
    @Nullable
    private NewPipeStreamExtractor streamExtractor;
    @Nullable
    private PlayerManager playerManager;

    public HomeFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull final android.view.LayoutInflater inflater,
                             @Nullable final android.view.ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final VideoAdapter adapter = new VideoAdapter(this::playVideo);
        recyclerView.setAdapter(adapter);
        loadVideosAsync(adapter);

        return recyclerView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
    }

    public void bindDependencies(@NonNull final VideoRepository repository,
                                 @NonNull final NewPipeStreamExtractor extractor,
                                 @NonNull final PlayerManager manager) {
        videoRepository = repository;
        streamExtractor = extractor;
        playerManager = manager;
    }

    @NonNull
    private List<VideoItem> loadVideos() {
        if (videoRepository == null) {
            return Collections.emptyList();
        }
        return videoRepository.loadVideos();
    }

    private void loadVideosAsync(@NonNull final VideoAdapter adapter) {
        backgroundExecutor.execute(() -> {
            final List<VideoItem> items = loadVideos();
            mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }
                adapter.submitItems(items);
            });
        });
    }

    private void playVideo(@NonNull final VideoItem item) {
        if (streamExtractor == null || playerManager == null) {
            return;
        }

        try {
            playerManager.playStream(streamExtractor.extractByVideoId(item.videoId));
        } catch (final IOException | RuntimeException ignored) {
            // Handled by existing app error flows in real integration.
        } catch (final Exception ignored) {
            // Covers checked extractor exceptions.
        }
    }
}
