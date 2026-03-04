package com.sansoft.harmony;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.sansoft.harmony.databinding.ActivityMainBinding;
import com.sansoft.harmony.databinding.DrawerHeaderBinding;
import com.sansoft.harmony.databinding.DrawerLayoutBinding;
import com.sansoft.harmony.databinding.InstanceSpinnerLayoutBinding;
import com.sansoft.harmony.databinding.ToolbarLayoutBinding;
import com.sansoft.harmony.error.ErrorUtil;
import com.sansoft.harmony.extractor.NewPipe;
import com.sansoft.harmony.extractor.ServiceList;
import com.sansoft.harmony.extractor.StreamingService;
import com.sansoft.harmony.extractor.comments.CommentsInfoItem;
import com.sansoft.harmony.extractor.exceptions.ExtractionException;
import com.sansoft.harmony.fragments.BackPressable;
import com.sansoft.harmony.fragments.MainFragment;
import com.sansoft.harmony.fragments.detail.VideoDetailFragment;
import com.sansoft.harmony.fragments.list.comments.CommentRepliesFragment;
import com.sansoft.harmony.fragments.list.search.SearchFragment;
import com.sansoft.harmony.local.feed.notifications.NotificationWorker;
import com.sansoft.harmony.player.Player;
import com.sansoft.harmony.player.event.OnKeyDownListener;
import com.sansoft.harmony.player.helper.PlayerHolder;
import com.sansoft.harmony.player.playqueue.PlayQueue;
import com.sansoft.harmony.settings.UpdateSettingsFragment;
import com.sansoft.harmony.settings.migration.MigrationManager;
import com.sansoft.harmony.util.Constants;
import com.sansoft.harmony.util.DeviceUtils;
import com.sansoft.harmony.util.KioskTranslator;
import com.sansoft.harmony.util.Localization;
import com.sansoft.harmony.util.NavigationHelper;
import com.sansoft.harmony.util.PermissionHelper;
import com.sansoft.harmony.util.ReleaseVersionUtil;
import com.sansoft.harmony.util.SerializedCache;
import com.sansoft.harmony.util.ServiceHelper;
import com.sansoft.harmony.util.StateSaver;
import com.sansoft.harmony.util.ThemeHelper;
import com.sansoft.harmony.util.external_communication.ShareUtils;
import com.sansoft.harmony.views.FocusOverlayView;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @SuppressWarnings("ConstantConditions")
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    private ActivityMainBinding mainBinding;
    private DrawerHeaderBinding drawerHeaderBinding;
    private DrawerLayoutBinding drawerLayoutBinding;
    private ToolbarLayoutBinding toolbarLayoutBinding;

    private ActionBarDrawerToggle toggle;

    private BroadcastReceiver broadcastReceiver;

    private static final int ITEM_ID_SUBSCRIPTIONS = -1;
    private static final int ITEM_ID_FEED = -2;
    private static final int ITEM_ID_BOOKMARKS = -3;
    private static final int ITEM_ID_DOWNLOADS = -4;
    private static final int ITEM_ID_HISTORY = -5;

    private static final int ORDER = 0;
    public static final String KEY_IS_IN_BACKGROUND = "is_in_background";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG, "onCreate() called with: "
                    + "savedInstanceState = [" + savedInstanceState + "]");
        }

        Localization.migrateAppLanguageSettingIfNecessary(getApplicationContext());
        ThemeHelper.setDayNightMode(this);
        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));

        if (DeviceUtils.supportsWebView()) {
            try {
                new WebView(this);
            } catch (final Throwable e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to create WebView", e);
                }
            }
        }

        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefEditor = sharedPreferences.edit();

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        drawerLayoutBinding = mainBinding.drawerLayout;
        drawerHeaderBinding = DrawerHeaderBinding.bind(drawerLayoutBinding.navigation
                .getHeaderView(0));
        toolbarLayoutBinding = mainBinding.toolbarLayout;
        setContentView(mainBinding.getRoot());

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            initFragments();
        }

        setSupportActionBar(toolbarLayoutBinding.toolbar);
        try {
            setupDrawer();
        } catch (final Exception e) {
            ErrorUtil.showUiErrorSnackbar(this, "Setting up drawer", e);
        }
        if (DeviceUtils.isTv(this)) {
            FocusOverlayView.setupFocusObserver(this);
        }
        openMiniPlayerUponPlayerStarted();

        if (PermissionHelper.checkPostNotificationsPermission(this,
                PermissionHelper.POST_NOTIFICATIONS_REQUEST_CODE)) {

            NotificationWorker.initialize(this);
        }
        if (!UpdateSettingsFragment.wasUserAskedForConsent(this)
                && !App.getInstance().isFirstRun()
                && ReleaseVersionUtil.INSTANCE.isReleaseApk()) {
            UpdateSettingsFragment.askForConsentToUpdateChecks(this);
        }

        if (!DEBUG) {
            showKeepAndroidDialog();
        }

        MigrationManager.showUserInfoIfPresent(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final App app = App.getInstance();

        if (sharedPreferences.getBoolean(app.getString(R.string.update_app_key), false)
                && sharedPreferences
                .getBoolean(app.getString(R.string.update_check_consent_key), false)) {
            NewVersionWorker.enqueueNewVersionCheckingWork(app, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPrefEditor.putBoolean(KEY_IS_IN_BACKGROUND, false).apply();
        Log.d(TAG, "App moved to foreground");
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPrefEditor.putBoolean(KEY_IS_IN_BACKGROUND, true).apply();
        Log.d(TAG, "App moved to background");
    }
    private void setupDrawer() throws ExtractionException {
        addDrawerMenuForCurrentService();

        toggle = new ActionBarDrawerToggle(this, mainBinding.getRoot(),
                toolbarLayoutBinding.toolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        mainBinding.getRoot().addDrawerListener(toggle);

        drawerLayoutBinding.navigation.setNavigationItemSelectedListener(this::drawerItemSelected);
        setupDrawerHeader();
    }

    private void addDrawerMenuForCurrentService() throws ExtractionException {
        //Tabs
        drawerLayoutBinding.navigation.getMenu()
                .add(R.id.menu_tabs_group, ITEM_ID_SUBSCRIPTIONS, ORDER,
                        R.string.tab_subscriptions)
                .setIcon(R.drawable.ic_tv);
        drawerLayoutBinding.navigation.getMenu()
                .add(R.id.menu_tabs_group, ITEM_ID_FEED, ORDER, R.string.fragment_feed_title)
                .setIcon(R.drawable.ic_subscriptions);
        drawerLayoutBinding.navigation.getMenu()
                .add(R.id.menu_tabs_group, ITEM_ID_BOOKMARKS, ORDER, R.string.tab_bookmarks)
                .setIcon(R.drawable.ic_bookmark);
        drawerLayoutBinding.navigation.getMenu()
                .add(R.id.menu_tabs_group, ITEM_ID_DOWNLOADS, ORDER, R.string.downloads)
                .setIcon(R.drawable.ic_file_download);
        drawerLayoutBinding.navigation.getMenu()
                .add(R.id.menu_tabs_group, ITEM_ID_HISTORY, ORDER, R.string.action_history)
                .setIcon(R.drawable.ic_history);

        //Kiosks
        final StreamingService service = ServiceList.YouTube;

        int kioskMenuItemId = 0;

        for (final String ks : service.getKioskList().getAvailableKiosks()) {
            drawerLayoutBinding.navigation.getMenu()
                    .add(R.id.menu_kiosks_group, kioskMenuItemId, 0, KioskTranslator
                            .getTranslatedKioskName(ks, this))
                    .setIcon(KioskTranslator.getKioskIcon(ks));
            kioskMenuItemId++;
        }
    }

    private boolean drawerItemSelected(final MenuItem item) {
        final int groupId = item.getGroupId();
        if (groupId == R.id.menu_tabs_group) {
            tabSelected(item);
        } else if (groupId == R.id.menu_kiosks_group) {
            try {
                kioskSelected(item);
            } catch (final Exception e) {
                ErrorUtil.showUiErrorSnackbar(this, "Selecting drawer kiosk", e);
            }
        } else {
            return false;
        }

        mainBinding.getRoot().closeDrawers();
        return true;
    }

    private void tabSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case ITEM_ID_SUBSCRIPTIONS:
                NavigationHelper.openSubscriptionFragment(getSupportFragmentManager());
                break;
            case ITEM_ID_FEED:
                NavigationHelper.openFeedFragment(getSupportFragmentManager());
                break;
            case ITEM_ID_BOOKMARKS:
                NavigationHelper.openBookmarksFragment(getSupportFragmentManager());
                break;
            case ITEM_ID_DOWNLOADS:
                NavigationHelper.openDownloads(this);
                break;
            case ITEM_ID_HISTORY:
                NavigationHelper.openStatisticFragment(getSupportFragmentManager());
                break;
        }
    }

    private void kioskSelected(final MenuItem item) throws ExtractionException {
        final StreamingService currentService = ServiceList.YouTube;
        int kioskMenuItemId = 0;
        for (final String kioskId : currentService.getKioskList().getAvailableKiosks()) {
            if (kioskMenuItemId == item.getItemId()) {
                NavigationHelper.openKioskFragment(getSupportFragmentManager(),
                        currentService.getServiceId(), kioskId);
                break;
            }
            kioskMenuItemId++;
        }
    }

    private void setupDrawerHeader() {
        if (getString(R.string.app_name).length() > "NewPipe".length()) {
            final ViewGroup.LayoutParams layoutParams =
                    drawerHeaderBinding.drawerHeaderNewpipeTitle.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            drawerHeaderBinding.drawerHeaderNewpipeTitle.setLayoutParams(layoutParams);
            drawerHeaderBinding.drawerHeaderNewpipeTitle.setMaxLines(2);
            drawerHeaderBinding.drawerHeaderNewpipeTitle.setMinWidth(getResources()
                    .getDimensionPixelSize(R.dimen.drawer_header_newpipe_title_default_width));
            drawerHeaderBinding.drawerHeaderNewpipeTitle.setMaxWidth(getResources()
                    .getDimensionPixelSize(R.dimen.drawer_header_newpipe_title_max_width));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            StateSaver.clearStateFiles();
        }
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onResume() {
        Localization.initPrettyTime(Localization.resolvePrettyTime());
        super.onResume();

        mainBinding.getRoot().closeDrawer(GravityCompat.START, false);
        try {
            final int selectedServiceId = ServiceList.YouTube.getServiceId();
            final String selectedServiceName = NewPipe.getService(selectedServiceId)
                    .getServiceInfo().getName();
            drawerHeaderBinding.drawerHeaderServiceView.setText(selectedServiceName);
            drawerHeaderBinding.drawerHeaderServiceIcon.setImageResource(ServiceHelper
                    .getIcon(selectedServiceId));

            drawerHeaderBinding.drawerHeaderServiceView.post(() -> drawerHeaderBinding
                    .drawerHeaderServiceView.setSelected(true));
            drawerHeaderBinding.drawerHeaderActionButton.setContentDescription(
                    getString(R.string.drawer_header_description) + selectedServiceName);
        } catch (final Exception e) {
            ErrorUtil.showUiErrorSnackbar(this, "Setting up service toggle", e);
        }

        if (sharedPreferences.getBoolean(Constants.KEY_THEME_CHANGE, false)) {
            if (DEBUG) {
                Log.d(TAG, "Theme has changed, recreating activity...");
            }
            sharedPrefEditor.putBoolean(Constants.KEY_THEME_CHANGE, false).apply();
            ActivityCompat.recreate(this);
        }

        if (sharedPreferences.getBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false)) {
            if (DEBUG) {
                Log.d(TAG, "main page has changed, recreating main fragment...");
            }
            sharedPrefEditor.putBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false).apply();
            NavigationHelper.openMainActivity(this);
        }

        final boolean isHistoryEnabled = sharedPreferences.getBoolean(
                getString(R.string.enable_watch_history_key), true);
        drawerLayoutBinding.navigation.getMenu().findItem(ITEM_ID_HISTORY)
                .setVisible(isHistoryEnabled);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        }
        if (intent != null) {
            final String action = intent.getAction();
            if ((action != null && action.equals(Intent.ACTION_MAIN))
                    && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                return;
            }
        }

        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        final Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player_holder);
        if (fragment instanceof OnKeyDownListener
                && !bottomSheetHiddenOrCollapsed()) {
            return ((OnKeyDownListener) fragment).onKeyDown(keyCode)
                    || super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) {
            Log.d(TAG, "onBackPressed() called");
        }

        if (DeviceUtils.isTv(this)) {
            if (mainBinding.getRoot().isDrawerOpen(drawerLayoutBinding.navigation)) {
                mainBinding.getRoot().closeDrawers();
                return;
            }
        }

        if (bottomSheetHiddenOrCollapsed()) {
            final FragmentManager fm = getSupportFragmentManager();
            final Fragment fragment = fm.findFragmentById(R.id.fragment_holder);
            if (fragment instanceof BackPressable) {
                if (((BackPressable) fragment).onBackPressed()) {
                    return;
                }
            } else if (fragment instanceof CommentRepliesFragment) {
                openDetailFragmentFromCommentReplies(fm, false);
            }

        } else {
            final Fragment fragmentPlayer = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_player_holder);
            if (fragmentPlayer instanceof BackPressable) {
                if (!((BackPressable) fragmentPlayer).onBackPressed()) {
                    BottomSheetBehavior.from(mainBinding.fragmentPlayerHolder)
                            .setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (final int i : grantResults) {
            if (i == PackageManager.PERMISSION_DENIED) {
                return;
            }
        }
        switch (requestCode) {
            case PermissionHelper.DOWNLOADS_REQUEST_CODE:
                NavigationHelper.openDownloads(this);
                break;
            case PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE:
                final Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_player_holder);
                if (fragment instanceof VideoDetailFragment) {
                    ((VideoDetailFragment) fragment).openDownloadDialog();
                }
                break;
            case PermissionHelper.POST_NOTIFICATIONS_REQUEST_CODE:
                NotificationWorker.initialize(this);
                break;
        }
    }

    private void onHomeButtonPressed() {
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.fragment_holder);

        if (fragment instanceof CommentRepliesFragment) {
            openDetailFragmentFromCommentReplies(fm, true);
        } else if (!NavigationHelper.tryGotoSearchFragment(fm)) {
            NavigationHelper.gotoMainFragment(fm);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (DEBUG) {
            Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "]");
        }
        super.onCreateOptionsMenu(menu);

        final Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (!(fragment instanceof SearchFragment)) {
            toolbarLayoutBinding.toolbarSearchContainer.getRoot().setVisibility(View.GONE);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        updateDrawerNavigation();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (DEBUG) {
            Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        }

        if (item.getItemId() == android.R.id.home) {
            onHomeButtonPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFragments() {
        if (DEBUG) {
            Log.d(TAG, "initFragments() called");
        }
        StateSaver.clearStateFiles();
        if (getIntent() != null && getIntent().hasExtra(Constants.KEY_LINK_TYPE)) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                NavigationHelper.openMainFragment(getSupportFragmentManager());
            }

            handleIntent(getIntent());
        } else {
            NavigationHelper.gotoMainFragment(getSupportFragmentManager());
        }
    }

    private void updateDrawerNavigation() {
        if (getSupportActionBar() == null) {
            return;
        }

        final Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_holder);
        if (fragment instanceof MainFragment) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (toggle != null) {
                toggle.syncState();
                toolbarLayoutBinding.toolbar.setNavigationOnClickListener(v -> mainBinding.getRoot()
                        .open());
                mainBinding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
            }
        } else {
            mainBinding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbarLayoutBinding.toolbar.setNavigationOnClickListener(v -> onHomeButtonPressed());
        }
    }

    private void handleIntent(final Intent intent) {
        try {
            if (DEBUG) {
                Log.d(TAG, "handleIntent() called with: intent = [" + intent + "]");
            }

            if (intent.hasExtra(Constants.KEY_LINK_TYPE)) {
                final String url = intent.getStringExtra(Constants.KEY_URL);
                final int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
                String title = intent.getStringExtra(Constants.KEY_TITLE);
                if (title == null) {
                    title = "";
                }

                final StreamingService.LinkType linkType = ((StreamingService.LinkType) intent
                        .getSerializableExtra(Constants.KEY_LINK_TYPE));
                assert linkType != null;
                switch (linkType) {
                    case STREAM:
                        final String intentCacheKey = intent.getStringExtra(
                                Player.PLAY_QUEUE_KEY);
                        final PlayQueue playQueue = intentCacheKey != null
                                ? SerializedCache.getInstance()
                                .take(intentCacheKey, PlayQueue.class)
                                : null;

                        final boolean switchingPlayers = intent.getBooleanExtra(
                                VideoDetailFragment.KEY_SWITCHING_PLAYERS, false);
                        NavigationHelper.openVideoDetailFragment(
                                getApplicationContext(), getSupportFragmentManager(),
                                serviceId, url, title, playQueue, switchingPlayers);
                        break;
                    case CHANNEL:
                        NavigationHelper.openChannelFragment(getSupportFragmentManager(),
                                serviceId, url, title);
                        break;
                    case PLAYLIST:
                        NavigationHelper.openPlaylistFragment(getSupportFragmentManager(),
                                serviceId, url, title);
                        break;
                }
            } else if (intent.hasExtra(Constants.KEY_OPEN_SEARCH)) {
                String searchString = intent.getStringExtra(Constants.KEY_SEARCH_STRING);
                if (searchString == null) {
                    searchString = "";
                }
                final int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
                NavigationHelper.openSearchFragment(
                        getSupportFragmentManager(),
                        serviceId,
                        searchString);

            } else {
                NavigationHelper.gotoMainFragment(getSupportFragmentManager());
            }
        } catch (final Exception e) {
            ErrorUtil.showUiErrorSnackbar(this, "Handling intent", e);
        }
    }

    private void openMiniPlayerIfMissing() {
        final Fragment fragmentPlayer = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player_holder);
        if (fragmentPlayer == null) {
            NavigationHelper.showMiniPlayer(getSupportFragmentManager());
        }
    }

    private void openMiniPlayerUponPlayerStarted() {
        if (getIntent().getSerializableExtra(Constants.KEY_LINK_TYPE)
                == StreamingService.LinkType.STREAM) {
            return;
        }

        if (PlayerHolder.getInstance().isPlayerOpen()) {
            openMiniPlayerIfMissing();
        } else {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    if (Objects.equals(intent.getAction(),
                            VideoDetailFragment.ACTION_PLAYER_STARTED)
                            && PlayerHolder.getInstance().isPlayerOpen()) {
                        openMiniPlayerIfMissing();
                        unregisterReceiver(broadcastReceiver);
                        broadcastReceiver = null;
                    }
                }
            };
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(VideoDetailFragment.ACTION_PLAYER_STARTED);
            ContextCompat.registerReceiver(this, broadcastReceiver, intentFilter,
                    ContextCompat.RECEIVER_EXPORTED);

            PlayerHolder.getInstance().tryBindIfNeeded(this);
        }
    }

    private void openDetailFragmentFromCommentReplies(
            @NonNull final FragmentManager fm,
            final boolean popBackStack
    ) {
        @Nullable final String fragmentUnderEntryName;
        if (fm.getBackStackEntryCount() < 2) {
            fragmentUnderEntryName = null;
        } else {
            fragmentUnderEntryName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2)
                    .getName();
        }

        @Nullable final CommentRepliesFragment repliesFragment =
                (CommentRepliesFragment) fm.findFragmentByTag(CommentRepliesFragment.TAG);
        @Nullable final CommentsInfoItem rootComment =
                repliesFragment == null ? null : repliesFragment.getCommentsInfoItem();

        if (popBackStack) {
            fm.popBackStackImmediate();
        }

        if (CommentRepliesFragment.TAG.equals(fragmentUnderEntryName)) {
            return;
        }

        final BottomSheetBehavior<FragmentContainerView> behavior = BottomSheetBehavior
                .from(mainBinding.fragmentPlayerHolder);
        if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            return;
        }

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet,
                                       final int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    final Fragment detailFragment = fm.findFragmentById(
                            R.id.fragment_player_holder);
                    if (detailFragment instanceof VideoDetailFragment && rootComment != null) {
                        ((VideoDetailFragment) detailFragment).scrollToComment(rootComment);
                    }
                    behavior.removeBottomSheetCallback(this);
                }
            }

            @Override
            public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {
            }
        });

        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private boolean bottomSheetHiddenOrCollapsed() {
        final BottomSheetBehavior<FrameLayout> bottomSheetBehavior =
                BottomSheetBehavior.from(mainBinding.fragmentPlayerHolder);

        final int sheetState = bottomSheetBehavior.getState();
        return sheetState == BottomSheetBehavior.STATE_HIDDEN
                || sheetState == BottomSheetBehavior.STATE_COLLAPSED;
    }

    private void showKeepAndroidDialog() {
        final var prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final var now = Instant.now();
        final var kaoLastCheck = Instant.ofEpochMilli(prefs.getLong(
                getString(R.string.kao_last_checked_key),
                0
        ));

        final var supportedLannguages = List.of("fr", "de", "ca", "es", "id", "it", "pl",
                "pt", "cs", "sk", "fa", "ar", "tr", "el", "th", "ru", "uk", "ko", "zh", "ja");
        final var locale = Localization.getAppLocale();
        final String kaoBaseUrl = "https://keepandroidopen.org/";
        final String kaoURI;
        if (supportedLannguages.contains(locale.getLanguage())) {
            if ("zh".equals(locale.getLanguage())) {
                kaoURI = kaoBaseUrl + ("TW".equals(locale.getCountry()) ? "zh-TW" : "zh-CN");
            } else {
                kaoURI = kaoBaseUrl + locale.getLanguage();
            }
        } else {
            kaoURI = kaoBaseUrl;
        }
        final var solutionURI =
                "https://github.com/woheller69/FreeDroidWarn?tab=readme-ov-file#solutions";

        if (kaoLastCheck.plus(30, ChronoUnit.DAYS).isBefore(now)) {
            final var dialog = new AlertDialog.Builder(this)
                    .setTitle("Keep Android Open")
                    .setCancelable(false)
                    .setMessage(this.getString(R.string.kao_dialog_warning))
                    .setPositiveButton(this.getString(android.R.string.ok), (d, w) -> {
                        prefs.edit()
                                .putLong(
                                        getString(R.string.kao_last_checked_key),
                                        now.toEpochMilli()
                                )
                                .apply();
                    })
                    .setNeutralButton(this.getString(R.string.kao_solution), null)
                    .setNegativeButton(this.getString(R.string.kao_dialog_more_info), null)
                    .show();

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v ->
                    ShareUtils.openUrlInBrowser(this, kaoURI)
            );
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                    ShareUtils.openUrlInBrowser(this, solutionURI)
            );
        }
    }
}
