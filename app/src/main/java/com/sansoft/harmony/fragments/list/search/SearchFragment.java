package com.sansoft.harmony.fragments.list.search;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;
import static com.sansoft.harmony.extractor.utils.Utils.isBlank;
import static com.sansoft.harmony.ktx.ViewUtils.animate;
import static com.sansoft.harmony.util.ExtractorHelper.showMetaInfoInTextView;
import static java.util.Arrays.asList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;
import androidx.collection.SparseArrayCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.evernote.android.state.State;

import com.sansoft.harmony.R;
import com.sansoft.harmony.databinding.FragmentSearchBinding;
import com.sansoft.harmony.error.ErrorInfo;
import com.sansoft.harmony.error.ErrorUtil;
import com.sansoft.harmony.error.ReCaptchaActivity;
import com.sansoft.harmony.error.UserAction;
import com.sansoft.harmony.extractor.InfoItem;
import com.sansoft.harmony.extractor.ListExtractor;
import com.sansoft.harmony.extractor.MetaInfo;
import com.sansoft.harmony.extractor.NewPipe;
import com.sansoft.harmony.extractor.Page;
import com.sansoft.harmony.extractor.StreamingService;
import com.sansoft.harmony.extractor.exceptions.ParsingException;
import com.sansoft.harmony.extractor.search.SearchExtractor;
import com.sansoft.harmony.extractor.search.SearchInfo;
import com.sansoft.harmony.fragments.BackPressable;
import com.sansoft.harmony.fragments.list.BaseListFragment;
import com.sansoft.harmony.ktx.AnimationType;
import com.sansoft.harmony.ktx.ExceptionUtils;
import com.sansoft.harmony.local.history.HistoryRecordManager;
import com.sansoft.harmony.settings.NewPipeSettings;
import com.sansoft.harmony.util.Constants;
import com.sansoft.harmony.util.DeviceUtils;
import com.sansoft.harmony.util.ExtractorHelper;
import com.sansoft.harmony.util.KeyboardUtil;
import com.sansoft.harmony.util.NavigationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends BaseListFragment<SearchInfo, ListExtractor.InfoItemsPage<?>>
        implements BackPressable {

    private static final int THRESHOLD_NETWORK_SUGGESTION = 1;
    private static final int SUGGESTIONS_DEBOUNCE = 120; //ms
    private final PublishSubject<String> suggestionPublisher = PublishSubject.create();

    @State
    int filterItemCheckedId = -1;

    @State
    protected int serviceId = Constants.NO_SERVICE_ID;

    @State
    String searchString;

    @State
    String[] contentFilter = new String[0];

    @State
    String sortFilter;

    @State
    String lastSearchedString;

    @State
    String searchSuggestion;

    @State
    boolean isCorrectedSearch;

    @State
    MetaInfo[] metaInfo;

    @State
    boolean wasSearchFocused = false;

    private final SparseArrayCompat<String> menuItemToFilterName = new SparseArrayCompat<>();
    private StreamingService service;
    @Nullable
    private Page nextPage;
    private boolean showLocalSuggestions = true;
    private boolean showRemoteSuggestions = true;

    private Disposable searchDisposable;
    private Disposable suggestionDisposable;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private SuggestionListAdapter suggestionListAdapter;
    private HistoryRecordManager historyRecordManager;

    private FragmentSearchBinding searchBinding;

    private View searchToolbarContainer;
    private EditText searchEditText;
    private View searchClear;

    private boolean suggestionsPanelVisible = false;

    private TextWatcher textWatcher;

    public static SearchFragment getInstance(final int serviceId, final String searchString) {
        final SearchFragment searchFragment = new SearchFragment();
        searchFragment.setQuery(serviceId, searchString, new String[]{"all"}, "");

        if (!TextUtils.isEmpty(searchString)) {
            searchFragment.setSearchOnResume();
        }

        return searchFragment;
    }

    private void setSearchOnResume() {
        wasLoading.set(true);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        showLocalSuggestions = NewPipeSettings.showLocalSearchSuggestions(activity, prefs);
        showRemoteSuggestions = NewPipeSettings.showRemoteSearchSuggestions(activity, prefs);

        suggestionListAdapter = new SuggestionListAdapter();
        historyRecordManager = new HistoryRecordManager(context);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View rootView, final Bundle savedInstanceState) {
        searchBinding = FragmentSearchBinding.bind(rootView);
        super.onViewCreated(rootView, savedInstanceState);

        updateService();

        if (service != null) {
            searchEditText.setHint(
                    getString(R.string.search_with_service_name,
                            service.getServiceInfo().getName()));
        }
        showSearchOnStart();
        initSearchListeners();
    }

    private void updateService() {
        try {
            service = NewPipe.getService(serviceId);
        } catch (final Exception e) {
            ErrorUtil.showUiErrorSnackbar(this, "Getting service for id " + serviceId, e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateService();
    }

    @Override
    public void onPause() {
        super.onPause();

        wasSearchFocused = searchEditText.hasFocus();

        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }
        disposables.clear();
        hideKeyboardSearch();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (suggestionDisposable == null || suggestionDisposable.isDisposed()) {
            initSuggestionObserver();
        }

        if (!TextUtils.isEmpty(searchString)) {
            if (wasLoading.getAndSet(false)) {
                search(searchString, contentFilter, sortFilter);
                return;
            } else if (infoListAdapter.getItemsList().isEmpty()) {
                if (savedState == null) {
                    search(searchString, contentFilter, sortFilter);
                    return;
                } else if (!isLoading.get() && !wasSearchFocused && lastPanelError == null) {
                    infoListAdapter.clearStreamItemList();
                    showEmptyState();
                }
            }
        }

        handleSearchSuggestion();

        showMetaInfoInTextView(metaInfo == null ? null : Arrays.asList(metaInfo),
                searchBinding.searchMetaInfoTextView, searchBinding.searchMetaInfoSeparator,
                disposables);

        if (TextUtils.isEmpty(searchString) || wasSearchFocused) {
            showKeyboardSearch();
            showSuggestionsPanel();
        } else {
            hideKeyboardSearch();
            hideSuggestionsPanel();
        }
        wasSearchFocused = false;
    }

    @Override
    public void onDestroyView() {
        unsetSearchListeners();
        searchBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }
        disposables.clear();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ReCaptchaActivity.RECAPTCHA_REQUEST) {
            if (resultCode == Activity.RESULT_OK
                    && !TextUtils.isEmpty(searchString)) {
                search(searchString, contentFilter, sortFilter);
            } else {
                Log.e(TAG, "ReCaptcha failed");
            }
        } else {
            Log.e(TAG, "Request code from activity not supported [" + requestCode + "]");
        }
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        searchBinding.suggestionsList.setAdapter(suggestionListAdapter);
        searchBinding.suggestionsList.setItemAnimator(null);
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull final RecyclerView recyclerView,
                                        @NonNull final RecyclerView.ViewHolder viewHolder) {
                return getSuggestionMovementFlags(viewHolder);
            }

            @Override
            public boolean onMove(@NonNull final RecyclerView recyclerView,
                                  @NonNull final RecyclerView.ViewHolder viewHolder,
                                  @NonNull final RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
                onSuggestionItemSwiped(viewHolder);
            }
        }).attachToRecyclerView(searchBinding.suggestionsList);

        searchToolbarContainer = activity.findViewById(R.id.toolbar_search_container);
        searchEditText = searchToolbarContainer.findViewById(R.id.toolbar_search_edit_text);
        searchClear = searchToolbarContainer.findViewById(R.id.toolbar_search_clear);
    }

    @Override
    public void writeTo(final Queue<Object> objectsToSave) {
        super.writeTo(objectsToSave);
        objectsToSave.add(nextPage);
    }

    @Override
    public void readFrom(@NonNull final Queue<Object> savedObjects) throws Exception {
        super.readFrom(savedObjects);
        nextPage = (Page) savedObjects.poll();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle bundle) {
        searchString = searchEditText != null
                ? getSearchEditString().trim()
                : searchString;
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void reloadContent() {
        if (!TextUtils.isEmpty(searchString) || (searchEditText != null
                && !isSearchEditBlank())) {
            search(!TextUtils.isEmpty(searchString)
                    ? searchString
                    : getSearchEditString(), this.contentFilter, "");
        } else {
            if (searchEditText != null) {
                searchEditText.setText("");
                showKeyboardSearch();
            }
            hideErrorPanel();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (service == null) {
            updateService();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final var filter = Collections.singletonList(menuItemToFilterName.get(item.getItemId()));
        changeContentFilter(item, filter);
        return true;
    }

    private void restoreFilterChecked(final Menu menu, final int itemId) {
        if (itemId != -1) {
            final MenuItem item = menu.findItem(itemId);
            if (item == null) {
                return;
            }

            item.setChecked(true);
        }
    }

    private void showSearchOnStart() {
        searchEditText.setText(searchString);

        if (TextUtils.isEmpty(searchString)
                || isSearchEditBlank()) {
            searchToolbarContainer.setTranslationX(100);
            searchToolbarContainer.setAlpha(0.0f);
            searchToolbarContainer.setVisibility(View.VISIBLE);
            searchToolbarContainer.animate()
                    .translationX(0)
                    .alpha(1.0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator()).start();
        } else {
            searchToolbarContainer.setTranslationX(0);
            searchToolbarContainer.setAlpha(1.0f);
            searchToolbarContainer.setVisibility(View.VISIBLE);
        }
    }

    private void initSearchListeners() {
        searchClear.setOnClickListener(v -> {
            if (isSearchEditBlank()) {
                NavigationHelper.gotoMainFragment(getFM());
                return;
            }

            searchBinding.correctSuggestion.setVisibility(View.GONE);

            searchEditText.setText("");
            suggestionListAdapter.submitList(null);
            showKeyboardSearch();
        });

        TooltipCompat.setTooltipText(searchClear, getString(R.string.clear));

        searchEditText.setOnClickListener(v -> {
            if ((showLocalSuggestions || showRemoteSuggestions) && !isErrorPanelVisible()) {
                showSuggestionsPanel();
            }
            if (DeviceUtils.isTv(getContext())) {
                showKeyboardSearch();
            }
        });

        searchEditText.setOnFocusChangeListener((final View v, final boolean hasFocus) -> {
            if ((showLocalSuggestions || showRemoteSuggestions)
                    && hasFocus && !isErrorPanelVisible()) {
                showSuggestionsPanel();
            }
        });

        suggestionListAdapter.setListener(new SuggestionListAdapter.OnSuggestionItemSelected() {
            @Override
            public void onSuggestionItemSelected(final SuggestionItem item) {
                search(item.query, new String[]{"all"}, "");
                searchEditText.setText(item.query);
            }

            @Override
            public void onSuggestionItemInserted(final SuggestionItem item) {
                searchEditText.setText(item.query);
                searchEditText.setSelection(searchEditText.getText().length());
            }

            @Override
            public void onSuggestionItemLongClick(final SuggestionItem item) {
                if (item.fromHistory) {
                    showDeleteSuggestionDialog(item);
                }
            }
        });

        if (textWatcher != null) {
            searchEditText.removeTextChangedListener(textWatcher);
        }
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                                          final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                                      final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                for (final CharacterStyle span : s.getSpans(0, s.length(), CharacterStyle.class)) {
                    s.removeSpan(span);
                }

                final String newText = getSearchEditString().trim();
                suggestionPublisher.onNext(newText);
            }
        };
        searchEditText.addTextChangedListener(textWatcher);
        searchEditText.setOnEditorActionListener(
                (final TextView v, final int actionId, final KeyEvent event) -> {
                    if (actionId == EditorInfo.IME_ACTION_PREVIOUS) {
                        hideKeyboardSearch();
                    } else if (event != null
                            && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            || event.getAction() == EditorInfo.IME_ACTION_SEARCH)) {
                        searchEditText.setText(getSearchEditString().trim());
                        search(getSearchEditString(), new String[]{"all"}, "");
                        return true;
                    }
                    return false;
                });

        if (suggestionDisposable == null || suggestionDisposable.isDisposed()) {
            initSuggestionObserver();
        }
    }

    private void unsetSearchListeners() {
        searchClear.setOnClickListener(null);
        searchClear.setOnLongClickListener(null);
        searchEditText.setOnClickListener(null);
        searchEditText.setOnFocusChangeListener(null);
        searchEditText.setOnEditorActionListener(null);

        if (textWatcher != null) {
            searchEditText.removeTextChangedListener(textWatcher);
        }
        textWatcher = null;
    }

    private void showSuggestionsPanel() {
        suggestionsPanelVisible = true;
        animate(searchBinding.suggestionsPanel, true, 200,
                AnimationType.LIGHT_SLIDE_AND_ALPHA);
    }

    private void hideSuggestionsPanel() {
        suggestionsPanelVisible = false;
        animate(searchBinding.suggestionsPanel, false, 200,
                AnimationType.LIGHT_SLIDE_AND_ALPHA);
    }

    private void showKeyboardSearch() {
        KeyboardUtil.showKeyboard(activity, searchEditText);
    }

    private void hideKeyboardSearch() {
        KeyboardUtil.hideKeyboard(activity, searchEditText);
    }

    private void showDeleteSuggestionDialog(final SuggestionItem item) {
        if (activity == null || historyRecordManager == null || searchEditText == null) {
            return;
        }
        final String query = item.query;
        new AlertDialog.Builder(activity)
                .setTitle(query)
                .setMessage(R.string.delete_item_search_history)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    final Disposable onDelete = historyRecordManager.deleteSearchHistory(query)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    howManyDeleted -> suggestionPublisher
                                            .onNext(getSearchEditString()),
                                    throwable -> showSnackBarError(new ErrorInfo(throwable,
                                            UserAction.DELETE_FROM_HISTORY,
                                            "Deleting item failed")));
                    disposables.add(onDelete);
                })
                .show();
    }

    @Override
    public boolean onBackPressed() {
        if (suggestionsPanelVisible
                && !infoListAdapter.getItemsList().isEmpty()
                && !isLoading.get()) {
            hideSuggestionsPanel();
            hideKeyboardSearch();
            searchEditText.setText(lastSearchedString);
            return true;
        }
        return false;
    }


    private Observable<List<SuggestionItem>> getLocalSuggestionsObservable(
            final String query, final int similarQueryLimit) {
        return historyRecordManager
                .getRelatedSearches(query, similarQueryLimit, 25)
                .toObservable()
                .map(searchHistoryEntries ->
                        searchHistoryEntries.stream()
                                .map(entry -> new SuggestionItem(true, entry))
                                .collect(Collectors.toList()));
    }

    private Observable<List<SuggestionItem>> getRemoteSuggestionsObservable(final String query) {
        return ExtractorHelper
                .suggestionsFor(serviceId, query)
                .toObservable()
                .map(strings -> {
                    final List<SuggestionItem> result = new ArrayList<>();
                    for (final String entry : strings) {
                        result.add(new SuggestionItem(false, entry));
                    }
                    return result;
                });
    }

    private void initSuggestionObserver() {
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }

        suggestionDisposable = suggestionPublisher
                .debounce(SUGGESTIONS_DEBOUNCE, TimeUnit.MILLISECONDS)
                .startWithItem(searchString == null ? "" : searchString)
                .switchMap(query -> {
                    final boolean shallShowRemoteSuggestionsNow = showRemoteSuggestions
                            && query.length() >= THRESHOLD_NETWORK_SUGGESTION;

                    if (showLocalSuggestions && shallShowRemoteSuggestionsNow) {
                        return Observable.zip(
                                getLocalSuggestionsObservable(query, 3),
                                getRemoteSuggestionsObservable(query),
                                (local, remote) -> {
                                    remote.removeIf(remoteItem -> local.stream().anyMatch(
                                            localItem -> localItem.equals(remoteItem)));
                                    local.addAll(remote);
                                    return local;
                                })
                                .materialize();
                    } else if (showLocalSuggestions) {
                        return getLocalSuggestionsObservable(query, 25)
                                .materialize();
                    } else if (shallShowRemoteSuggestionsNow) {
                        return getRemoteSuggestionsObservable(query)
                                .materialize();
                    } else {
                        return Single.fromCallable(Collections::<SuggestionItem>emptyList)
                                .toObservable()
                                .materialize();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listNotification -> {
                            if (listNotification.isOnNext()) {
                                if (listNotification.getValue() != null) {
                                    handleSuggestions(listNotification.getValue());
                                }
                            } else if (listNotification.isOnError()
                                    && listNotification.getError() != null
                                    && !ExceptionUtils.isInterruptedCaused(
                                    listNotification.getError())) {
                                showSnackBarError(new ErrorInfo(listNotification.getError(),
                                        UserAction.GET_SUGGESTIONS, searchString, serviceId));
                            }
                        }, throwable -> showSnackBarError(new ErrorInfo(
                                throwable, UserAction.GET_SUGGESTIONS, searchString, serviceId)));
    }

    @Override
    protected void doInitialLoadLogic() {
    }

    private void search(@NonNull final String theSearchString,
                        final String[] theContentFilter,
                        final String theSortFilter) {
        if (theSearchString.isEmpty()) {
            return;
        }

        try {
            final StreamingService streamingService = NewPipe.getServiceByUrl(theSearchString);
            showLoading();
            disposables.add(Observable
                    .fromCallable(() -> NavigationHelper.getIntentByLink(activity,
                            streamingService, theSearchString))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(intent -> {
                        getFM().popBackStackImmediate();
                        activity.startActivity(intent);
                    }, throwable -> showTextError(getString(R.string.unsupported_url))));
            return;
        } catch (final Exception ignored) {
        }

        lastSearchedString = this.searchString;
        this.searchString = theSearchString;
        infoListAdapter.clearStreamItemList();
        hideSuggestionsPanel();
        showMetaInfoInTextView(null, searchBinding.searchMetaInfoTextView,
                searchBinding.searchMetaInfoSeparator, disposables);
        hideKeyboardSearch();

        disposables.add(historyRecordManager.onSearched(serviceId, theSearchString)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignored -> {
                        },
                        throwable -> showSnackBarError(new ErrorInfo(throwable, UserAction.SEARCHED,
                                theSearchString, serviceId))
                ));

        suggestionPublisher.onNext(theSearchString);
        startLoading(false);
    }

    @Override
    public void startLoading(final boolean forceLoad) {
        super.startLoading(forceLoad);
        disposables.clear();
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        searchDisposable = ExtractorHelper.searchFor(serviceId,
                searchString,
                Arrays.asList(contentFilter),
                sortFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((searchResult, throwable) -> isLoading.set(false))
                .subscribe(this::handleResult, this::onItemError);

    }

    @Override
    protected void loadMoreItems() {
        if (!Page.isValid(nextPage)) {
            return;
        }
        isLoading.set(true);
        showListFooter(true);
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        searchDisposable = ExtractorHelper.getMoreSearchItems(
                serviceId,
                searchString,
                asList(contentFilter),
                sortFilter,
                nextPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((nextItemsResult, throwable) -> isLoading.set(false))
                .subscribe(this::handleNextItems, this::onItemError);
    }

    @Override
    protected boolean hasMoreItems() {
        return Page.isValid(nextPage);
    }

    @Override
    protected void onItemSelected(final InfoItem selectedItem) {
        super.onItemSelected(selectedItem);
        hideKeyboardSearch();
    }

    private void onItemError(final Throwable exception) {
        if (exception instanceof SearchExtractor.NothingFoundException) {
            infoListAdapter.clearStreamItemList();
            showEmptyState();
        } else {
            showError(new ErrorInfo(exception, UserAction.SEARCHED, searchString, serviceId,
                    getOpenInBrowserUrlForErrors()));
        }
    }

    @Nullable
    private String getOpenInBrowserUrlForErrors() {
        if (TextUtils.isEmpty(searchString)) {
            return null;
        }
        try {
            return service.getSearchQHFactory().getUrl(searchString,
                    Arrays.asList(contentFilter), sortFilter);
        } catch (final NullPointerException | ParsingException ignored) {
            return null;
        }
    }

    private void changeContentFilter(final MenuItem item, final List<String> theContentFilter) {
        filterItemCheckedId = item.getItemId();
        item.setChecked(true);

        if (service != null) {
            final boolean isNotFiltered = theContentFilter.isEmpty()
                    || "all".equals(theContentFilter.get(0));
            if (isNotFiltered) {
                searchEditText.setHint(
                        getString(R.string.search_with_service_name,
                                service.getServiceInfo().getName()));
            } else {
                searchEditText.setHint(getString(R.string.search_with_service_name_and_filter,
                        service.getServiceInfo().getName(),
                        item.getTitle()));
            }
        }

        contentFilter = theContentFilter.toArray(new String[0]);

        if (!TextUtils.isEmpty(searchString)) {
            search(searchString, contentFilter, sortFilter);
        }
    }

    private void setQuery(final int theServiceId,
                          final String theSearchString,
                          final String[] theContentFilter,
                          final String theSortFilter) {
        serviceId = theServiceId;
        searchString = theSearchString;
        contentFilter = theContentFilter;
        sortFilter = theSortFilter;
    }

    private String getSearchEditString() {
        return searchEditText.getText().toString();
    }

    private boolean isSearchEditBlank() {
        return isBlank(getSearchEditString());
    }

    public void handleSuggestions(@NonNull final List<SuggestionItem> suggestions) {
        suggestionListAdapter.submitList(suggestions,
                () -> {
                    if (searchBinding != null) {
                        searchBinding.suggestionsList.scrollToPosition(0);
                    }
                });

        if (suggestionsPanelVisible && isErrorPanelVisible()) {
            hideLoading();
        }
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        showListFooter(false);
    }

    @Override
    public void handleResult(@NonNull final SearchInfo result) {
        final List<Throwable> exceptions = result.getErrors();
        if (!exceptions.isEmpty()
                && !(exceptions.size() == 1
                && exceptions.get(0) instanceof SearchExtractor.NothingFoundException)) {
            showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                    searchString, serviceId, getOpenInBrowserUrlForErrors()));
        }

        searchSuggestion = result.getSearchSuggestion();
        if (searchSuggestion != null) {
            searchSuggestion = searchSuggestion.trim();
        }
        isCorrectedSearch = result.isCorrectedSearch();

        metaInfo = result.getMetaInfo().toArray(new MetaInfo[0]);
        showMetaInfoInTextView(result.getMetaInfo(), searchBinding.searchMetaInfoTextView,
                searchBinding.searchMetaInfoSeparator, disposables);

        handleSearchSuggestion();

        lastSearchedString = searchString;
        nextPage = result.getNextPage();

        if (infoListAdapter.getItemsList().isEmpty()) {
            if (!result.getRelatedItems().isEmpty()) {
                infoListAdapter.addInfoItemList(result.getRelatedItems());
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
                return;
            }
        }

        super.handleResult(result);
    }

    private void handleSearchSuggestion() {
        if (TextUtils.isEmpty(searchSuggestion)) {
            searchBinding.correctSuggestion.setVisibility(View.GONE);
        } else {
            final String helperText = getString(isCorrectedSearch
                    ? R.string.search_showing_result_for
                    : R.string.did_you_mean);

            final String highlightedSearchSuggestion =
                    "<b><i>" + Html.escapeHtml(searchSuggestion) + "</i></b>";
            final String text = String.format(helperText, highlightedSearchSuggestion);
            searchBinding.correctSuggestion.setText(HtmlCompat.fromHtml(text,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            searchBinding.correctSuggestion.setOnClickListener(v -> {
                searchBinding.correctSuggestion.setVisibility(View.GONE);
                search(searchSuggestion, contentFilter, sortFilter);
                searchEditText.setText(searchSuggestion);
            });

            searchBinding.correctSuggestion.setOnLongClickListener(v -> {
                searchEditText.setText(searchSuggestion);
                searchEditText.setSelection(searchSuggestion.length());
                showKeyboardSearch();
                return true;
            });

            searchBinding.correctSuggestion.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void handleNextItems(final ListExtractor.InfoItemsPage<?> result) {
        showListFooter(false);
        infoListAdapter.addInfoItemList(result.getItems());

        if (!result.getErrors().isEmpty()) {
            if (nextPage == null) {
                showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                        "\"" + searchString + "\" → nextPage == null", serviceId,
                        getOpenInBrowserUrlForErrors()));
            } else {
                showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                        "\"" + searchString + "\" → pageUrl: " + nextPage.getUrl() + ", "
                                + "pageIds: " + nextPage.getIds() + ", "
                                + "pageCookies: " + nextPage.getCookies(),
                        serviceId, getOpenInBrowserUrlForErrors()));
            }
        }

        nextPage = result.getNextPage();
        super.handleNextItems(result);
    }

    @Override
    public void handleError() {
        super.handleError();
        hideSuggestionsPanel();
        hideKeyboardSearch();
    }

    public int getSuggestionMovementFlags(@NonNull final RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return 0;
        }

        final SuggestionItem item = suggestionListAdapter.getCurrentList().get(position);
        return item.fromHistory ? makeMovementFlags(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
    }

    public void onSuggestionItemSwiped(@NonNull final RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getBindingAdapterPosition();
        final String query = suggestionListAdapter.getCurrentList().get(position).query;
        final Disposable onDelete = historyRecordManager.deleteSearchHistory(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        howManyDeleted -> suggestionPublisher
                                .onNext(getSearchEditString()),
                        throwable -> showSnackBarError(new ErrorInfo(throwable,
                                UserAction.DELETE_FROM_HISTORY, "Deleting item failed")));
        disposables.add(onDelete);
    }
}
