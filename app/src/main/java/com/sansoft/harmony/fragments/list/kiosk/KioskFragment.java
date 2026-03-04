package com.sansoft.harmony.fragments.list.kiosk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.evernote.android.state.State;
import com.google.android.material.chip.ChipGroup;
import com.sansoft.harmony.R;
import com.sansoft.harmony.error.ErrorInfo;
import com.sansoft.harmony.error.UserAction;
import com.sansoft.harmony.extractor.ListExtractor;
import com.sansoft.harmony.extractor.NewPipe;
import com.sansoft.harmony.extractor.ServiceList;
import com.sansoft.harmony.extractor.StreamingService;
import com.sansoft.harmony.extractor.exceptions.ExtractionException;
import com.sansoft.harmony.extractor.kiosk.KioskInfo;
import com.sansoft.harmony.extractor.linkhandler.ListLinkHandlerFactory;
import com.sansoft.harmony.extractor.localization.ContentCountry;
import com.sansoft.harmony.extractor.services.media_ccc.extractors.MediaCCCLiveStreamKiosk;
import com.sansoft.harmony.extractor.stream.StreamInfoItem;
import com.sansoft.harmony.extractor.stream.StreamType;
import com.sansoft.harmony.fragments.list.BaseListInfoFragment;
import com.sansoft.harmony.util.ExtractorHelper;
import com.sansoft.harmony.util.KioskTranslator;
import com.sansoft.harmony.util.Localization;
import com.sansoft.harmony.util.NavigationHelper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;

public class KioskFragment extends BaseListInfoFragment<StreamInfoItem, KioskInfo> {
    @State
    String kioskId = "";
    String kioskTranslatedName;
    @State
    ContentCountry contentCountry;
    private ChipGroup chipGroup;
    private String currentSearchQuery = "latest songs official music"; // Default search query
    private boolean isLiveNews = false;

    public static KioskFragment getInstance(final int serviceId) throws ExtractionException {
        return getInstance(serviceId, NewPipe.getService(serviceId)
                .getKioskList().getDefaultKioskId());
    }

    public static KioskFragment getInstance(final int serviceId, final String kioskId)
            throws ExtractionException {
        final KioskFragment instance = new KioskFragment();
        final StreamingService service = NewPipe.getService(serviceId);
        final ListLinkHandlerFactory factory = service.getKioskList()
                .getListLinkHandlerFactoryByType(kioskId);
        instance.setInitialData(serviceId, factory.fromId(kioskId).getUrl(), kioskId);
        instance.kioskId = kioskId;
        return instance;
    }

    public KioskFragment() {
        super(UserAction.REQUESTED_KIOSK);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kioskTranslatedName = KioskTranslator.getTranslatedKioskName(kioskId, activity);
        name = kioskTranslatedName;
        contentCountry = Localization.getPreferredContentCountry(requireContext());
        isLiveNews = "LIVES".equals(kioskId);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_kiosk, container, false);
        chipGroup = view.findViewById(R.id.chip_group);
        if (isLiveNews) {
            view.findViewById(R.id.chip_scroll_view).setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isLiveNews) {
            setupChipGroupListener();
            chipGroup.check(R.id.chip_new_songs);
        }
    }

    private void setupChipGroupListener() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_new_songs) {
                currentSearchQuery = "latest songs official music";
            } else if (checkedId == R.id.chip_bollywood) {
                currentSearchQuery = "latest bollywood songs official music";
            } else if (checkedId == R.id.chip_punjabi) {
                currentSearchQuery = "latest punjabi songs official music";
            } else if (checkedId == R.id.chip_indi_pop) {
                currentSearchQuery = "latest indie pop songs official music";
            } else if (checkedId == R.id.chip_classical) {
                currentSearchQuery = "indian classical music";
            } else if (checkedId == R.id.chip_sufi) {
                currentSearchQuery = "sufi songs";
            } else if (checkedId == R.id.chip_ghazal) {
                currentSearchQuery = "ghazal songs";
            }
            reloadContent();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Localization.getPreferredContentCountry(requireContext()).equals(contentCountry)) {
            reloadContent();
        }
        if (useAsFrontPage && activity != null) {
            try {
                setTitle(kioskTranslatedName);
            } catch (final Exception e) {
                showSnackBarError(new ErrorInfo(e, UserAction.UI_ERROR, "Setting kiosk title"));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null && useAsFrontPage) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onSearch(final String query) {
        try {
            final String[] contentFilter = isLiveNews
                    ? new String[]{"lives"} : new String[]{"videos"};
            NavigationHelper.openSearchFragment(getFM(),
                    getServiceId(),
                    query,
                    contentFilter);
        } catch (final Exception e) {
            showSnackBarError(new ErrorInfo(e, UserAction.UI_ERROR,
                    "Unable to open search fragment"));
        }
    }

    @Override
    public Single<KioskInfo> loadResult(final boolean forceReload) {
        final String query = isLiveNews ? "live news tv india" : currentSearchQuery;
        final List<String> contentFilter = isLiveNews
            ? Collections.singletonList("lives")
            : Collections.singletonList("videos");

        return ExtractorHelper.getSearchInfo(serviceId,
                query,
                contentFilter,
                "",
                forceReload)
                .map(searchInfo -> {
                    final List<StreamInfoItem> streamItems = searchInfo.getRelatedItems().stream()
                            .filter(item -> item instanceof StreamInfoItem)
                            .map(item -> (StreamInfoItem) item)
                            .filter(stream -> {
                                if (isLiveNews) {
                                    final String title = stream.getName().toLowerCase();
                                    return stream.getStreamType() == StreamType.LIVE_STREAM
                                            && (title.contains("news")
                                            || title.contains("live")
                                            || title.contains("tv")
                                            || title.contains("breaking")
                                            || title.contains("channel"));
                                } else {
                                    return stream.getDuration() >= 60;
                                }
                            })
                            .collect(Collectors.toList());

                    return new KioskInfo(searchInfo.getServiceId(),
                            searchInfo.getLinkHandler(),
                            searchInfo.getName()) {
                        @NonNull
                        @Override
                        public List<StreamInfoItem> getRelatedItems() {
                            return streamItems;
                        }
                    };
                });
    }

    @Override
    public Single<ListExtractor.InfoItemsPage<StreamInfoItem>> loadMoreItemsLogic() {
        return Single.just(new ListExtractor.InfoItemsPage<>(Collections.emptyList(), null));
    }

    @Override
    public void handleResult(@NonNull final KioskInfo result) {
        super.handleResult(result);

        name = kioskTranslatedName;
        setTitle(kioskTranslatedName);
    }

    @Override
    public void showEmptyState() {
        super.showEmptyState();
        if (MediaCCCLiveStreamKiosk.KIOSK_ID.equals(currentInfo.getId())
                && ServiceList.MediaCCC.getServiceId() == currentInfo.getServiceId()) {
            setEmptyStateMessage(R.string.no_live_streams);
        }
    }
}
