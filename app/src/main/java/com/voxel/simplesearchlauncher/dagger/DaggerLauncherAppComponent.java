package com.voxel.simplesearchlauncher.dagger;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import androidx.fragment.app.Fragment;
import com.evie.common.utils.MemoryWatcher;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.evie.models.frequentlyused.room.AppLaunchDatabase;
import com.evie.models.frequentlyused.submodels.FrequentlyUsedLocalModel;
import com.voxel.launcher3.BaseApplication;
import com.voxel.launcher3.BaseApplication_MembersInjector;
import com.voxel.launcher3.FlavorInitializer;
import com.voxel.launcher3.FlavorInitializer_MembersInjector;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherAppState_MembersInjector;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.LauncherModel_MembersInjector;
import com.voxel.launcher3.Launcher_MembersInjector;
import com.voxel.launcher3.fresco.HybridNetworkFetcher;
import com.voxel.launcher3.fresco.HybridNetworkFetcher_Factory;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.simplesearchlauncher.activity.OnboardingActivity;
import com.voxel.simplesearchlauncher.allapps.AllAppsFragment;
import com.voxel.simplesearchlauncher.allapps.AllAppsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment;
import com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.appoptions.ChangeIconFragment;
import com.voxel.simplesearchlauncher.appoptions.ChangeIconFragment_MembersInjector;
import com.voxel.simplesearchlauncher.dagger.module.AppDatabaseModule_ProvidesAppDatabaseFactory;
import com.voxel.simplesearchlauncher.dagger.module.CommonAndroidInjectionModule_AllAppsFragment;
import com.voxel.simplesearchlauncher.dagger.module.CommonAndroidInjectionModule_ContributesConfirmShortcutActivityInjector;
import com.voxel.simplesearchlauncher.dagger.module.CommonAndroidInjectionModule_WidgetsFragment;
import com.voxel.simplesearchlauncher.dagger.module.DeepShortcutManagerModule;
import com.voxel.simplesearchlauncher.dagger.module.DeepShortcutManagerModule_ProvideDeepShortcutManagerFactory;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_AppDrawerSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_AppOptionsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_ChangeIconFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_CustomGestureSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_DesktopSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_DockSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_FolderSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_HideAppSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_IconPackSettingsFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_Launcher;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_OnboardingActivity;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_PopupFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_ResetLauncherFailureFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_ResetLauncherPromptFragment;
import com.voxel.simplesearchlauncher.dagger.module.FlavorAndroidInjectionModule_SelectAppFragment;
import com.voxel.simplesearchlauncher.dagger.module.FrequentlyUsedModule;
import com.voxel.simplesearchlauncher.dagger.module.FrequentlyUsedModule_ProvidesFrequentlyUsedLocalModelFactory;
import com.voxel.simplesearchlauncher.dagger.module.FrequentlyUsedModule_ProvidesFrequentlyUsedModelFactory;
import com.voxel.simplesearchlauncher.dagger.module.HideAppsModule;
import com.voxel.simplesearchlauncher.dagger.module.HideAppsModule_ProvidesHideAppsModelFactory;
import com.voxel.simplesearchlauncher.dagger.module.MemoryRegistryModule;
import com.voxel.simplesearchlauncher.dagger.module.MemoryRegistryModule_ProvideMemoryRegistryFactory;
import com.voxel.simplesearchlauncher.dagger.module.SingletonsModule;
import com.voxel.simplesearchlauncher.dagger.module.SingletonsModule_ProvidesAnalyticsHandlerFactory;
import com.voxel.simplesearchlauncher.dagger.module.SingletonsModule_ProvidesSettingsStorageFactory;
import com.voxel.simplesearchlauncher.dagger.module.SingletonsModule_ProvidesUserProfileManagerFactory;
import com.voxel.simplesearchlauncher.dagger.module.http.OkHttpModule;
import com.voxel.simplesearchlauncher.dagger.module.http.OkHttpModule_ProvidesOkHttpClientFactory;
import com.voxel.simplesearchlauncher.fragment.ResetLauncherFailureFragment;
import com.voxel.simplesearchlauncher.fragment.ResetLauncherFailureFragment_MembersInjector;
import com.voxel.simplesearchlauncher.fragment.ResetLauncherPromptFragment;
import com.voxel.simplesearchlauncher.fragment.ResetLauncherPromptFragment_MembersInjector;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager_Factory;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter_Factory;
import com.voxel.simplesearchlauncher.model.AppsInfoLabelOverrideFilter;
import com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager;
import com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager_MembersInjector;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager_Factory;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.model.managers.UserProfileManager;
import com.voxel.simplesearchlauncher.popup.PopupFragment;
import com.voxel.simplesearchlauncher.popup.PopupFragment_MembersInjector;
import com.voxel.simplesearchlauncher.search.SearchUiHelper;
import com.voxel.simplesearchlauncher.settings.AppDrawerSettingsFragment;
import com.voxel.simplesearchlauncher.settings.AppDrawerSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.CustomGestureSettingsFragment;
import com.voxel.simplesearchlauncher.settings.CustomGestureSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.DesktopSettingsFragment;
import com.voxel.simplesearchlauncher.settings.DesktopSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.DockSettingsFragment;
import com.voxel.simplesearchlauncher.settings.DockSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.FolderSettingsFragment;
import com.voxel.simplesearchlauncher.settings.FolderSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.HideAppsSettingsFragment;
import com.voxel.simplesearchlauncher.settings.HideAppsSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.IconPackSettingsFragment;
import com.voxel.simplesearchlauncher.settings.IconPackSettingsFragment_MembersInjector;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager_Factory;
import com.voxel.simplesearchlauncher.settings.gestures.SelectAppFragment;
import com.voxel.simplesearchlauncher.settings.gestures.SelectAppFragment_MembersInjector;
import com.voxel.simplesearchlauncher.shortcut.ConfirmShortcutActivity;
import com.voxel.simplesearchlauncher.shortcut.ConfirmShortcutActivity_MembersInjector;
import com.voxel.simplesearchlauncher.utils.ConnectionPoolKiller;
import com.voxel.simplesearchlauncher.utils.ConnectionPoolKiller_Factory;
import com.voxel.simplesearchlauncher.utils.MemoryRegistry;
import com.voxel.simplesearchlauncher.widget.WidgetsFragment;
import com.voxel.simplesearchlauncher.widget.WidgetsFragment_MembersInjector;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.DispatchingAndroidInjector_Factory;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.MapBuilder;
import dagger.internal.SetFactory;
import dagger.internal.SingleCheck;
import dagger.internal.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
/* JADX INFO: REWORK: regenerated component; sidescreen/feed/Jigsaw-adjacent graph removed */
public final class DaggerLauncherAppComponent extends LauncherAppComponent {
    private Provider<CommonAndroidInjectionModule_AllAppsFragment.AllAppsFragmentSubcomponent.Builder> allAppsFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_AppDrawerSettingsFragment.AppDrawerSettingsFragmentSubcomponent.Builder> appDrawerSettingsFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_AppOptionsFragment.AppOptionsFragmentSubcomponent.Builder> appOptionsFragmentSubcomponentBuilderProvider;
    private Provider<Application> applicationProvider;
    private Provider<AppsInfoIconFilter> appsInfoIconFilterProvider;
    private Provider<FlavorAndroidInjectionModule_ChangeIconFragment.ChangeIconFragmentSubcomponent.Builder> changeIconFragmentSubcomponentBuilderProvider;
    private Provider<CommonAndroidInjectionModule_ContributesConfirmShortcutActivityInjector.ConfirmShortcutActivitySubcomponent.Builder> confirmShortcutActivitySubcomponentBuilderProvider;
    private Provider<ConnectionPoolKiller> connectionPoolKillerProvider;
    private final Context context;
    private Provider<Context> contextProvider;
    private Provider<FlavorAndroidInjectionModule_CustomGestureSettingsFragment.CustomGestureSettingsFragmentSubcomponent.Builder> customGestureSettingsFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_DesktopSettingsFragment.DesktopSettingsFragmentSubcomponent.Builder> desktopSettingsFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_DockSettingsFragment.DockSettingsFragmentSubcomponent.Builder> dockSettingsFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_FolderSettingsFragment.FolderSettingsFragmentSubcomponent.Builder> folderSettingsFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_HideAppSettingsFragment.HideAppsSettingsFragmentSubcomponent.Builder> hideAppsSettingsFragmentSubcomponentBuilderProvider;
    private Provider<HybridNetworkFetcher> hybridNetworkFetcherProvider;
    private Provider<IconPackManager> iconPackManagerProvider;
    private Provider<FlavorAndroidInjectionModule_IconPackSettingsFragment.IconPackSettingsFragmentSubcomponent.Builder> iconPackSettingsFragmentSubcomponentBuilderProvider;
    private Provider<LauncherAppComponent> launcherAppComponentProvider;
    private Provider<FlavorAndroidInjectionModule_Launcher.LauncherSubcomponent.Builder> launcherSubcomponentBuilderProvider;
    private Provider<LayoutSettingsManager> layoutSettingsManagerProvider;
    private Provider<LocalAppsManager> localAppsManagerProvider;
    private final MemoryWatcher memoryWatcher;
    private Provider<FlavorAndroidInjectionModule_OnboardingActivity.OnboardingActivitySubcomponent.Builder> onboardingActivitySubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_PopupFragment.PopupFragmentSubcomponent.Builder> popupFragmentSubcomponentBuilderProvider;
    private Provider<DeepShortcutManager> provideDeepShortcutManagerProvider;
    private Provider<MemoryRegistry> provideMemoryRegistryProvider;
    private Provider<AnalyticsHandler> providesAnalyticsHandlerProvider;
    private Provider<AppLaunchDatabase> providesAppDatabaseProvider;
    private Provider<FrequentlyUsedLocalModel> providesFrequentlyUsedLocalModelProvider;
    private Provider<FrequentlyUsedModel> providesFrequentlyUsedModelProvider;
    private Provider<HideAppsModel> providesHideAppsModelProvider;
    private Provider<OkHttpClient> providesOkHttpClientProvider;
    private Provider<SettingsStorage> providesSettingsStorageProvider;
    private Provider<UserProfileManager> providesUserProfileManagerProvider;
    private Provider<FlavorAndroidInjectionModule_ResetLauncherFailureFragment.ResetLauncherFailureFragmentSubcomponent.Builder> resetLauncherFailureFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_ResetLauncherPromptFragment.ResetLauncherPromptFragmentSubcomponent.Builder> resetLauncherPromptFragmentSubcomponentBuilderProvider;
    private Provider<FlavorAndroidInjectionModule_SelectAppFragment.SelectAppFragmentSubcomponent.Builder> selectAppFragmentSubcomponentBuilderProvider;
    private Provider<CommonAndroidInjectionModule_WidgetsFragment.WidgetsFragmentSubcomponent.Builder> widgetsFragmentSubcomponentBuilderProvider;

    /* JADX INFO: loaded from: classes2.dex */
    private final class AllAppsFragmentSubcomponentBuilder extends CommonAndroidInjectionModule_AllAppsFragment.AllAppsFragmentSubcomponent.Builder {
        private AllAppsFragment seedInstance;

        private AllAppsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<AllAppsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, AllAppsFragment.class);
            return new AllAppsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(AllAppsFragment allAppsFragment) {
            this.seedInstance = (AllAppsFragment) Preconditions.checkNotNull(allAppsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class AllAppsFragmentSubcomponentImpl implements CommonAndroidInjectionModule_AllAppsFragment.AllAppsFragmentSubcomponent {
        private AllAppsFragmentSubcomponentImpl(AllAppsFragment allAppsFragment) {
        }

        private AllAppsFragment injectAllAppsFragment(AllAppsFragment allAppsFragment) {
            AllAppsFragment_MembersInjector.injectLocalAppsManager(allAppsFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            AllAppsFragment_MembersInjector.injectMIconPackManager(allAppsFragment, (IconPackManager) DaggerLauncherAppComponent.this.iconPackManagerProvider.get());
            AllAppsFragment_MembersInjector.injectMLayoutSettingsManager(allAppsFragment, (LayoutSettingsManager) DaggerLauncherAppComponent.this.layoutSettingsManagerProvider.get());
            AllAppsFragment_MembersInjector.injectMAnalyticsHandler(allAppsFragment, (AnalyticsHandler) DaggerLauncherAppComponent.this.providesAnalyticsHandlerProvider.get());
            return allAppsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(AllAppsFragment allAppsFragment) {
            injectAllAppsFragment(allAppsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class AppDrawerSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_AppDrawerSettingsFragment.AppDrawerSettingsFragmentSubcomponent.Builder {
        private AppDrawerSettingsFragment seedInstance;

        private AppDrawerSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<AppDrawerSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, AppDrawerSettingsFragment.class);
            return new AppDrawerSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(AppDrawerSettingsFragment appDrawerSettingsFragment) {
            this.seedInstance = (AppDrawerSettingsFragment) Preconditions.checkNotNull(appDrawerSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class AppDrawerSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_AppDrawerSettingsFragment.AppDrawerSettingsFragmentSubcomponent {
        private AppDrawerSettingsFragmentSubcomponentImpl(AppDrawerSettingsFragment appDrawerSettingsFragment) {
        }

        private AppDrawerSettingsFragment injectAppDrawerSettingsFragment(AppDrawerSettingsFragment appDrawerSettingsFragment) {
            AppDrawerSettingsFragment_MembersInjector.injectMLayoutSettingsManager(appDrawerSettingsFragment, (LayoutSettingsManager) DaggerLauncherAppComponent.this.layoutSettingsManagerProvider.get());
            return appDrawerSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(AppDrawerSettingsFragment appDrawerSettingsFragment) {
            injectAppDrawerSettingsFragment(appDrawerSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class AppOptionsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_AppOptionsFragment.AppOptionsFragmentSubcomponent.Builder {
        private AppOptionsFragment seedInstance;

        private AppOptionsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<AppOptionsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, AppOptionsFragment.class);
            return new AppOptionsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(AppOptionsFragment appOptionsFragment) {
            this.seedInstance = (AppOptionsFragment) Preconditions.checkNotNull(appOptionsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class AppOptionsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_AppOptionsFragment.AppOptionsFragmentSubcomponent {
        private AppOptionsFragmentSubcomponentImpl(AppOptionsFragment appOptionsFragment) {
        }

        private AppOptionsFragment injectAppOptionsFragment(AppOptionsFragment appOptionsFragment) {
            AppOptionsFragment_MembersInjector.injectMLocalAppsManager(appOptionsFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            return appOptionsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(AppOptionsFragment appOptionsFragment) {
            injectAppOptionsFragment(appOptionsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ChangeIconFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_ChangeIconFragment.ChangeIconFragmentSubcomponent.Builder {
        private ChangeIconFragment seedInstance;

        private ChangeIconFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<ChangeIconFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, ChangeIconFragment.class);
            return new ChangeIconFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(ChangeIconFragment changeIconFragment) {
            this.seedInstance = (ChangeIconFragment) Preconditions.checkNotNull(changeIconFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ChangeIconFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_ChangeIconFragment.ChangeIconFragmentSubcomponent {
        private ChangeIconFragmentSubcomponentImpl(ChangeIconFragment changeIconFragment) {
        }

        private ChangeIconFragment injectChangeIconFragment(ChangeIconFragment changeIconFragment) {
            ChangeIconFragment_MembersInjector.injectMLocalAppsManager(changeIconFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            ChangeIconFragment_MembersInjector.injectMIconPackManager(changeIconFragment, (IconPackManager) DaggerLauncherAppComponent.this.iconPackManagerProvider.get());
            return changeIconFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(ChangeIconFragment changeIconFragment) {
            injectChangeIconFragment(changeIconFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ConfirmShortcutActivitySubcomponentBuilder extends CommonAndroidInjectionModule_ContributesConfirmShortcutActivityInjector.ConfirmShortcutActivitySubcomponent.Builder {
        private ConfirmShortcutActivity seedInstance;

        private ConfirmShortcutActivitySubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<ConfirmShortcutActivity> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, ConfirmShortcutActivity.class);
            return new ConfirmShortcutActivitySubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(ConfirmShortcutActivity confirmShortcutActivity) {
            this.seedInstance = (ConfirmShortcutActivity) Preconditions.checkNotNull(confirmShortcutActivity);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ConfirmShortcutActivitySubcomponentImpl implements CommonAndroidInjectionModule_ContributesConfirmShortcutActivityInjector.ConfirmShortcutActivitySubcomponent {
        private ConfirmShortcutActivitySubcomponentImpl(ConfirmShortcutActivity confirmShortcutActivity) {
        }

        private ConfirmShortcutActivity injectConfirmShortcutActivity(ConfirmShortcutActivity confirmShortcutActivity) {
            ConfirmShortcutActivity_MembersInjector.injectMDeepShortcutManager(confirmShortcutActivity, (DeepShortcutManager) DaggerLauncherAppComponent.this.provideDeepShortcutManagerProvider.get());
            ConfirmShortcutActivity_MembersInjector.injectMSettingsStorage(confirmShortcutActivity, (SettingsStorage) DaggerLauncherAppComponent.this.providesSettingsStorageProvider.get());
            return confirmShortcutActivity;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(ConfirmShortcutActivity confirmShortcutActivity) {
            injectConfirmShortcutActivity(confirmShortcutActivity);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class CustomGestureSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_CustomGestureSettingsFragment.CustomGestureSettingsFragmentSubcomponent.Builder {
        private CustomGestureSettingsFragment seedInstance;

        private CustomGestureSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<CustomGestureSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, CustomGestureSettingsFragment.class);
            return new CustomGestureSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(CustomGestureSettingsFragment customGestureSettingsFragment) {
            this.seedInstance = (CustomGestureSettingsFragment) Preconditions.checkNotNull(customGestureSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class CustomGestureSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_CustomGestureSettingsFragment.CustomGestureSettingsFragmentSubcomponent {
        private CustomGestureSettingsFragmentSubcomponentImpl(CustomGestureSettingsFragment customGestureSettingsFragment) {
        }

        private CustomGestureSettingsFragment injectCustomGestureSettingsFragment(CustomGestureSettingsFragment customGestureSettingsFragment) {
            CustomGestureSettingsFragment_MembersInjector.injectLocalAppsManager(customGestureSettingsFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            return customGestureSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(CustomGestureSettingsFragment customGestureSettingsFragment) {
            injectCustomGestureSettingsFragment(customGestureSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class DesktopSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_DesktopSettingsFragment.DesktopSettingsFragmentSubcomponent.Builder {
        private DesktopSettingsFragment seedInstance;

        private DesktopSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<DesktopSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, DesktopSettingsFragment.class);
            return new DesktopSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(DesktopSettingsFragment desktopSettingsFragment) {
            this.seedInstance = (DesktopSettingsFragment) Preconditions.checkNotNull(desktopSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class DesktopSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_DesktopSettingsFragment.DesktopSettingsFragmentSubcomponent {
        private DesktopSettingsFragmentSubcomponentImpl(DesktopSettingsFragment desktopSettingsFragment) {
        }

        private DesktopSettingsFragment injectDesktopSettingsFragment(DesktopSettingsFragment desktopSettingsFragment) {
            DesktopSettingsFragment_MembersInjector.injectMLayoutSettingsManager(desktopSettingsFragment, (LayoutSettingsManager) DaggerLauncherAppComponent.this.layoutSettingsManagerProvider.get());
            return desktopSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(DesktopSettingsFragment desktopSettingsFragment) {
            injectDesktopSettingsFragment(desktopSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class DockSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_DockSettingsFragment.DockSettingsFragmentSubcomponent.Builder {
        private DockSettingsFragment seedInstance;

        private DockSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<DockSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, DockSettingsFragment.class);
            return new DockSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(DockSettingsFragment dockSettingsFragment) {
            this.seedInstance = (DockSettingsFragment) Preconditions.checkNotNull(dockSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class DockSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_DockSettingsFragment.DockSettingsFragmentSubcomponent {
        private DockSettingsFragmentSubcomponentImpl(DockSettingsFragment dockSettingsFragment) {
        }

        private DockSettingsFragment injectDockSettingsFragment(DockSettingsFragment dockSettingsFragment) {
            DockSettingsFragment_MembersInjector.injectMLayoutSettingsManager(dockSettingsFragment, (LayoutSettingsManager) DaggerLauncherAppComponent.this.layoutSettingsManagerProvider.get());
            return dockSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(DockSettingsFragment dockSettingsFragment) {
            injectDockSettingsFragment(dockSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class FolderSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_FolderSettingsFragment.FolderSettingsFragmentSubcomponent.Builder {
        private FolderSettingsFragment seedInstance;

        private FolderSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<FolderSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, FolderSettingsFragment.class);
            return new FolderSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(FolderSettingsFragment folderSettingsFragment) {
            this.seedInstance = (FolderSettingsFragment) Preconditions.checkNotNull(folderSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class FolderSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_FolderSettingsFragment.FolderSettingsFragmentSubcomponent {
        private FolderSettingsFragmentSubcomponentImpl(FolderSettingsFragment folderSettingsFragment) {
        }

        private FolderSettingsFragment injectFolderSettingsFragment(FolderSettingsFragment folderSettingsFragment) {
            FolderSettingsFragment_MembersInjector.injectMLocalAppsManager(folderSettingsFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            FolderSettingsFragment_MembersInjector.injectMLayoutSettingsManager(folderSettingsFragment, (LayoutSettingsManager) DaggerLauncherAppComponent.this.layoutSettingsManagerProvider.get());
            return folderSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(FolderSettingsFragment folderSettingsFragment) {
            injectFolderSettingsFragment(folderSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class HideAppsSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_HideAppSettingsFragment.HideAppsSettingsFragmentSubcomponent.Builder {
        private HideAppsSettingsFragment seedInstance;

        private HideAppsSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<HideAppsSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, HideAppsSettingsFragment.class);
            return new HideAppsSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(HideAppsSettingsFragment hideAppsSettingsFragment) {
            this.seedInstance = (HideAppsSettingsFragment) Preconditions.checkNotNull(hideAppsSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class HideAppsSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_HideAppSettingsFragment.HideAppsSettingsFragmentSubcomponent {
        private HideAppsSettingsFragmentSubcomponentImpl(HideAppsSettingsFragment hideAppsSettingsFragment) {
        }

        private HideAppsSettingsFragment injectHideAppsSettingsFragment(HideAppsSettingsFragment hideAppsSettingsFragment) {
            HideAppsSettingsFragment_MembersInjector.injectMHideAppsModel(hideAppsSettingsFragment, (HideAppsModel) DaggerLauncherAppComponent.this.providesHideAppsModelProvider.get());
            return hideAppsSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(HideAppsSettingsFragment hideAppsSettingsFragment) {
            injectHideAppsSettingsFragment(hideAppsSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class IconPackSettingsFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_IconPackSettingsFragment.IconPackSettingsFragmentSubcomponent.Builder {
        private IconPackSettingsFragment seedInstance;

        private IconPackSettingsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<IconPackSettingsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, IconPackSettingsFragment.class);
            return new IconPackSettingsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(IconPackSettingsFragment iconPackSettingsFragment) {
            this.seedInstance = (IconPackSettingsFragment) Preconditions.checkNotNull(iconPackSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class IconPackSettingsFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_IconPackSettingsFragment.IconPackSettingsFragmentSubcomponent {
        private IconPackSettingsFragmentSubcomponentImpl(IconPackSettingsFragment iconPackSettingsFragment) {
        }

        private IconPackSettingsFragment injectIconPackSettingsFragment(IconPackSettingsFragment iconPackSettingsFragment) {
            IconPackSettingsFragment_MembersInjector.injectMLocalAppsManager(iconPackSettingsFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            IconPackSettingsFragment_MembersInjector.injectMIconPackManager(iconPackSettingsFragment, (IconPackManager) DaggerLauncherAppComponent.this.iconPackManagerProvider.get());
            return iconPackSettingsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(IconPackSettingsFragment iconPackSettingsFragment) {
            injectIconPackSettingsFragment(iconPackSettingsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class LauncherSubcomponentBuilder extends FlavorAndroidInjectionModule_Launcher.LauncherSubcomponent.Builder {
        private Launcher seedInstance;

        private LauncherSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<Launcher> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, Launcher.class);
            return new LauncherSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(Launcher launcher) {
            this.seedInstance = (Launcher) Preconditions.checkNotNull(launcher);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class LauncherSubcomponentImpl implements FlavorAndroidInjectionModule_Launcher.LauncherSubcomponent {
        private LauncherSubcomponentImpl(Launcher launcher) {
        }


        private Launcher injectLauncher(Launcher launcher) {
            Launcher_MembersInjector.injectLocalAppsManager(launcher, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            Launcher_MembersInjector.injectMDeepShortcutManager(launcher, (DeepShortcutManager) DaggerLauncherAppComponent.this.provideDeepShortcutManagerProvider.get());
            Launcher_MembersInjector.injectMFrequentlyUsedModel(launcher, (FrequentlyUsedModel) DaggerLauncherAppComponent.this.providesFrequentlyUsedModelProvider.get());
            Launcher_MembersInjector.injectMHideAppsModel(launcher, (HideAppsModel) DaggerLauncherAppComponent.this.providesHideAppsModelProvider.get());
            Launcher_MembersInjector.injectMAnalyticsHandler(launcher, (AnalyticsHandler) DaggerLauncherAppComponent.this.providesAnalyticsHandlerProvider.get());
            return launcher;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(Launcher launcher) {
            injectLauncher(launcher);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class OnboardingActivitySubcomponentBuilder extends FlavorAndroidInjectionModule_OnboardingActivity.OnboardingActivitySubcomponent.Builder {
        private OnboardingActivity seedInstance;

        private OnboardingActivitySubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<OnboardingActivity> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, OnboardingActivity.class);
            return new OnboardingActivitySubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(OnboardingActivity onboardingActivity) {
            this.seedInstance = (OnboardingActivity) Preconditions.checkNotNull(onboardingActivity);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class OnboardingActivitySubcomponentImpl implements FlavorAndroidInjectionModule_OnboardingActivity.OnboardingActivitySubcomponent {
        private OnboardingActivitySubcomponentImpl(OnboardingActivity onboardingActivity) {
        }

        @Override // dagger.android.AndroidInjector
        /* REWORK: no members to inject since the intro slide was removed. */
        public void inject(OnboardingActivity onboardingActivity) {
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class PopupFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_PopupFragment.PopupFragmentSubcomponent.Builder {
        private PopupFragment seedInstance;

        private PopupFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<PopupFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, PopupFragment.class);
            return new PopupFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(PopupFragment popupFragment) {
            this.seedInstance = (PopupFragment) Preconditions.checkNotNull(popupFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class PopupFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_PopupFragment.PopupFragmentSubcomponent {
        private PopupFragmentSubcomponentImpl(PopupFragment popupFragment) {
        }

        private PopupFragment injectPopupFragment(PopupFragment popupFragment) {
            PopupFragment_MembersInjector.injectMDeepShortcutManager(popupFragment, (DeepShortcutManager) DaggerLauncherAppComponent.this.provideDeepShortcutManagerProvider.get());
            return popupFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(PopupFragment popupFragment) {
            injectPopupFragment(popupFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ResetLauncherFailureFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_ResetLauncherFailureFragment.ResetLauncherFailureFragmentSubcomponent.Builder {
        private ResetLauncherFailureFragment seedInstance;

        private ResetLauncherFailureFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<ResetLauncherFailureFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, ResetLauncherFailureFragment.class);
            return new ResetLauncherFailureFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(ResetLauncherFailureFragment resetLauncherFailureFragment) {
            this.seedInstance = (ResetLauncherFailureFragment) Preconditions.checkNotNull(resetLauncherFailureFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ResetLauncherFailureFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_ResetLauncherFailureFragment.ResetLauncherFailureFragmentSubcomponent {
        private ResetLauncherFailureFragmentSubcomponentImpl(ResetLauncherFailureFragment resetLauncherFailureFragment) {
        }

        private ResetLauncherFailureFragment injectResetLauncherFailureFragment(ResetLauncherFailureFragment resetLauncherFailureFragment) {
            ResetLauncherFailureFragment_MembersInjector.injectMAnalyticsHandler(resetLauncherFailureFragment, (AnalyticsHandler) DaggerLauncherAppComponent.this.providesAnalyticsHandlerProvider.get());
            return resetLauncherFailureFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(ResetLauncherFailureFragment resetLauncherFailureFragment) {
            injectResetLauncherFailureFragment(resetLauncherFailureFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ResetLauncherPromptFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_ResetLauncherPromptFragment.ResetLauncherPromptFragmentSubcomponent.Builder {
        private ResetLauncherPromptFragment seedInstance;

        private ResetLauncherPromptFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<ResetLauncherPromptFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, ResetLauncherPromptFragment.class);
            return new ResetLauncherPromptFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(ResetLauncherPromptFragment resetLauncherPromptFragment) {
            this.seedInstance = (ResetLauncherPromptFragment) Preconditions.checkNotNull(resetLauncherPromptFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class ResetLauncherPromptFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_ResetLauncherPromptFragment.ResetLauncherPromptFragmentSubcomponent {
        private ResetLauncherPromptFragmentSubcomponentImpl(ResetLauncherPromptFragment resetLauncherPromptFragment) {
        }

        private ResetLauncherPromptFragment injectResetLauncherPromptFragment(ResetLauncherPromptFragment resetLauncherPromptFragment) {
            ResetLauncherPromptFragment_MembersInjector.injectMAnalyticsHandler(resetLauncherPromptFragment, (AnalyticsHandler) DaggerLauncherAppComponent.this.providesAnalyticsHandlerProvider.get());
            return resetLauncherPromptFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(ResetLauncherPromptFragment resetLauncherPromptFragment) {
            injectResetLauncherPromptFragment(resetLauncherPromptFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class SelectAppFragmentSubcomponentBuilder extends FlavorAndroidInjectionModule_SelectAppFragment.SelectAppFragmentSubcomponent.Builder {
        private SelectAppFragment seedInstance;

        private SelectAppFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<SelectAppFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, SelectAppFragment.class);
            return new SelectAppFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(SelectAppFragment selectAppFragment) {
            this.seedInstance = (SelectAppFragment) Preconditions.checkNotNull(selectAppFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class SelectAppFragmentSubcomponentImpl implements FlavorAndroidInjectionModule_SelectAppFragment.SelectAppFragmentSubcomponent {
        private SelectAppFragmentSubcomponentImpl(SelectAppFragment selectAppFragment) {
        }

        private SelectAppFragment injectSelectAppFragment(SelectAppFragment selectAppFragment) {
            SelectAppFragment_MembersInjector.injectMLocalAppsManager(selectAppFragment, (LocalAppsManager) DaggerLauncherAppComponent.this.localAppsManagerProvider.get());
            return selectAppFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(SelectAppFragment selectAppFragment) {
            injectSelectAppFragment(selectAppFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class WidgetsFragmentSubcomponentBuilder extends CommonAndroidInjectionModule_WidgetsFragment.WidgetsFragmentSubcomponent.Builder {
        private WidgetsFragment seedInstance;

        private WidgetsFragmentSubcomponentBuilder() {
        }

        @Override // dagger.android.AndroidInjector.Builder
        /* JADX INFO: renamed from: build */
        public AndroidInjector<WidgetsFragment> build() {
            Preconditions.checkBuilderRequirement(this.seedInstance, WidgetsFragment.class);
            return new WidgetsFragmentSubcomponentImpl(this.seedInstance);
        }

        @Override // dagger.android.AndroidInjector.Builder
        public void seedInstance(WidgetsFragment widgetsFragment) {
            this.seedInstance = (WidgetsFragment) Preconditions.checkNotNull(widgetsFragment);
        }
    }



    /* JADX INFO: loaded from: classes2.dex */
    private final class WidgetsFragmentSubcomponentImpl implements CommonAndroidInjectionModule_WidgetsFragment.WidgetsFragmentSubcomponent {
        private WidgetsFragmentSubcomponentImpl(WidgetsFragment widgetsFragment) {
        }

        private WidgetsFragment injectWidgetsFragment(WidgetsFragment widgetsFragment) {
            WidgetsFragment_MembersInjector.injectMIconPackManager(widgetsFragment, (IconPackManager) DaggerLauncherAppComponent.this.iconPackManagerProvider.get());
            return widgetsFragment;
        }

        @Override // dagger.android.AndroidInjector
        public void inject(WidgetsFragment widgetsFragment) {
            injectWidgetsFragment(widgetsFragment);
        }
    }


    private DaggerLauncherAppComponent(SingletonsModule singletonsModule, OkHttpModule okHttpModule, FrequentlyUsedModule frequentlyUsedModule, MemoryRegistryModule memoryRegistryModule, DeepShortcutManagerModule deepShortcutManagerModule, HideAppsModule hideAppsModule, Application application, Context context, MemoryWatcher memoryWatcher) {
        this.memoryWatcher = memoryWatcher;
        this.context = context;
        initialize(singletonsModule, okHttpModule, frequentlyUsedModule, memoryRegistryModule, deepShortcutManagerModule, hideAppsModule, application, context, memoryWatcher);
    }

    private static final class Builder implements LauncherAppComponent.Builder {
        private Application application;
        private Context context;
        private DeepShortcutManagerModule deepShortcutManagerModule;
        private FrequentlyUsedModule frequentlyUsedModule;
        private HideAppsModule hideAppsModule;
        private MemoryRegistryModule memoryRegistryModule;
        private MemoryWatcher memoryWatcher;
        private OkHttpModule okHttpModule;
        private SingletonsModule singletonsModule;

        @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent.Builder
        public Builder application(Application application) {
            this.application = (Application) Preconditions.checkNotNull(application);
            return this;
        }

        @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent.Builder
        public Builder context(Context context) {
            this.context = (Context) Preconditions.checkNotNull(context);
            return this;
        }

        @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent.Builder
        public Builder memoryWatcher(MemoryWatcher memoryWatcher) {
            this.memoryWatcher = (MemoryWatcher) Preconditions.checkNotNull(memoryWatcher);
            return this;
        }

        @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent.Builder
        public LauncherAppComponent build() {
            if (this.singletonsModule == null) {
                this.singletonsModule = new SingletonsModule();
            }
            if (this.okHttpModule == null) {
                this.okHttpModule = new OkHttpModule();
            }
            if (this.frequentlyUsedModule == null) {
                this.frequentlyUsedModule = new FrequentlyUsedModule();
            }
            if (this.memoryRegistryModule == null) {
                this.memoryRegistryModule = new MemoryRegistryModule();
            }
            if (this.deepShortcutManagerModule == null) {
                this.deepShortcutManagerModule = new DeepShortcutManagerModule();
            }
            if (this.hideAppsModule == null) {
                this.hideAppsModule = new HideAppsModule();
            }
            Preconditions.checkBuilderRequirement(this.application, Application.class);
            Preconditions.checkBuilderRequirement(this.context, Context.class);
            Preconditions.checkBuilderRequirement(this.memoryWatcher, MemoryWatcher.class);
            return new DaggerLauncherAppComponent(this.singletonsModule, this.okHttpModule, this.frequentlyUsedModule, this.memoryRegistryModule, this.deepShortcutManagerModule, this.hideAppsModule, this.application, this.context, this.memoryWatcher);
        }
    }

    public static LauncherAppComponent.Builder builder() {
        return new Builder();
    }

    private AppsInfoLabelOverrideFilter getAppsInfoLabelOverrideFilter() {
        return new AppsInfoLabelOverrideFilter(this.context, this.localAppsManagerProvider.get());
    }

    private DispatchingAndroidInjector<Activity> getDispatchingAndroidInjectorOfActivity() {
        // REWORK: Dagger 2.22.1's factory returns the injector provider; call get() directly.
        return DispatchingAndroidInjector_Factory.<Activity>create(InstanceFactory.create(getMapOfClassOfAndProviderOfFactoryOf()), InstanceFactory.create(Collections.<String, Provider<AndroidInjector.Factory<?>>>emptyMap())).get();
    }

    private DispatchingAndroidInjector<Fragment> getDispatchingAndroidInjectorOfFragment() {
        // REWORK: Dagger 2.22.1's factory returns the injector provider; call get() directly.
        return DispatchingAndroidInjector_Factory.<Fragment>create(InstanceFactory.create(getMapOfClassOfAndProviderOfFactoryOf()), InstanceFactory.create(Collections.<String, Provider<AndroidInjector.Factory<?>>>emptyMap())).get();
    }

    private DispatchingAndroidInjector<Service> getDispatchingAndroidInjectorOfService() {
        // REWORK: Dagger 2.22.1's factory returns the injector provider; call get() directly.
        return DispatchingAndroidInjector_Factory.<Service>create(InstanceFactory.create(getMapOfClassOfAndProviderOfFactoryOf()), InstanceFactory.create(Collections.<String, Provider<AndroidInjector.Factory<?>>>emptyMap())).get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // dagger's Provider is invariant; decompiled providers are typed narrower
    private static <T> Provider<T> injectCast(Provider<?> provider) {
        return (Provider<T>) provider;
    }

    private Map<Class<?>, Provider<AndroidInjector.Factory<?>>> getMapOfClassOfAndProviderOfFactoryOf() {
        Map<Class<?>, Provider<AndroidInjector.Factory<?>>> map = new HashMap<>();
        map.put(AllAppsFragment.class, injectCast(this.allAppsFragmentSubcomponentBuilderProvider));
        map.put(WidgetsFragment.class, injectCast(this.widgetsFragmentSubcomponentBuilderProvider));
        map.put(OnboardingActivity.class, injectCast(this.onboardingActivitySubcomponentBuilderProvider));
        map.put(Launcher.class, injectCast(this.launcherSubcomponentBuilderProvider));
        map.put(SelectAppFragment.class, injectCast(this.selectAppFragmentSubcomponentBuilderProvider));
        map.put(CustomGestureSettingsFragment.class, injectCast(this.customGestureSettingsFragmentSubcomponentBuilderProvider));
        map.put(ResetLauncherFailureFragment.class, injectCast(this.resetLauncherFailureFragmentSubcomponentBuilderProvider));
        map.put(ResetLauncherPromptFragment.class, injectCast(this.resetLauncherPromptFragmentSubcomponentBuilderProvider));
        map.put(PopupFragment.class, injectCast(this.popupFragmentSubcomponentBuilderProvider));
        map.put(HideAppsSettingsFragment.class, injectCast(this.hideAppsSettingsFragmentSubcomponentBuilderProvider));
        map.put(AppOptionsFragment.class, injectCast(this.appOptionsFragmentSubcomponentBuilderProvider));
        map.put(DesktopSettingsFragment.class, injectCast(this.desktopSettingsFragmentSubcomponentBuilderProvider));
        map.put(DockSettingsFragment.class, injectCast(this.dockSettingsFragmentSubcomponentBuilderProvider));
        map.put(IconPackSettingsFragment.class, injectCast(this.iconPackSettingsFragmentSubcomponentBuilderProvider));
        map.put(ChangeIconFragment.class, injectCast(this.changeIconFragmentSubcomponentBuilderProvider));
        map.put(AppDrawerSettingsFragment.class, injectCast(this.appDrawerSettingsFragmentSubcomponentBuilderProvider));
        map.put(FolderSettingsFragment.class, injectCast(this.folderSettingsFragmentSubcomponentBuilderProvider));
        map.put(ConfirmShortcutActivity.class, injectCast(this.confirmShortcutActivitySubcomponentBuilderProvider));
        return map;
    }

    private void initialize(SingletonsModule singletonsModule, OkHttpModule okHttpModule, FrequentlyUsedModule frequentlyUsedModule, MemoryRegistryModule memoryRegistryModule, DeepShortcutManagerModule deepShortcutManagerModule, HideAppsModule hideAppsModule, Application application, Context context, MemoryWatcher memoryWatcher) {
        this.contextProvider = InstanceFactory.create(context);
        this.applicationProvider = InstanceFactory.create(application);
        this.localAppsManagerProvider = DoubleCheck.provider(LocalAppsManager_Factory.create(this.contextProvider));
        // REWORK: SetFactory.builder(0, 0).build() is raw (SetFactory<Object>) in Dagger 2.22.1,
        // so shape the empty set explicitly for the gson-deserializer slot.
        this.providesOkHttpClientProvider = DoubleCheck.provider(OkHttpModule_ProvidesOkHttpClientFactory.create(okHttpModule));
        this.providesSettingsStorageProvider = DoubleCheck.provider(SingletonsModule_ProvidesSettingsStorageFactory.create(singletonsModule, this.contextProvider));
        this.providesAnalyticsHandlerProvider = DoubleCheck.provider(SingletonsModule_ProvidesAnalyticsHandlerFactory.create(singletonsModule));
        this.providesAppDatabaseProvider = DoubleCheck.provider(AppDatabaseModule_ProvidesAppDatabaseFactory.create(this.contextProvider));
        this.providesFrequentlyUsedLocalModelProvider = DoubleCheck.provider(FrequentlyUsedModule_ProvidesFrequentlyUsedLocalModelFactory.create(frequentlyUsedModule, this.providesAppDatabaseProvider));
        this.providesFrequentlyUsedModelProvider = DoubleCheck.provider(FrequentlyUsedModule_ProvidesFrequentlyUsedModelFactory.create(frequentlyUsedModule, this.contextProvider, this.providesFrequentlyUsedLocalModelProvider));
        this.providesUserProfileManagerProvider = DoubleCheck.provider(SingletonsModule_ProvidesUserProfileManagerFactory.create(singletonsModule));
        this.provideMemoryRegistryProvider = DoubleCheck.provider(MemoryRegistryModule_ProvideMemoryRegistryFactory.create(memoryRegistryModule));
        this.hybridNetworkFetcherProvider = HybridNetworkFetcher_Factory.create(this.contextProvider, this.providesOkHttpClientProvider);
        this.iconPackManagerProvider = DoubleCheck.provider(IconPackManager_Factory.create(this.contextProvider, this.localAppsManagerProvider));
        this.appsInfoIconFilterProvider = AppsInfoIconFilter_Factory.create(this.contextProvider, this.localAppsManagerProvider);
        this.provideDeepShortcutManagerProvider = DoubleCheck.provider(DeepShortcutManagerModule_ProvideDeepShortcutManagerFactory.create(deepShortcutManagerModule, this.contextProvider));
        this.layoutSettingsManagerProvider = DoubleCheck.provider(LayoutSettingsManager_Factory.create(this.contextProvider));
        this.connectionPoolKillerProvider = DoubleCheck.provider(ConnectionPoolKiller_Factory.create(this.contextProvider, this.providesOkHttpClientProvider));
        this.providesHideAppsModelProvider = DoubleCheck.provider(HideAppsModule_ProvidesHideAppsModelFactory.create(hideAppsModule, this.contextProvider));
        this.launcherAppComponentProvider = InstanceFactory.create(this);
        this.allAppsFragmentSubcomponentBuilderProvider = () -> new AllAppsFragmentSubcomponentBuilder();
        this.widgetsFragmentSubcomponentBuilderProvider = () -> new WidgetsFragmentSubcomponentBuilder();
        this.onboardingActivitySubcomponentBuilderProvider = () -> new OnboardingActivitySubcomponentBuilder();
        this.launcherSubcomponentBuilderProvider = () -> new LauncherSubcomponentBuilder();
        this.selectAppFragmentSubcomponentBuilderProvider = () -> new SelectAppFragmentSubcomponentBuilder();
        this.customGestureSettingsFragmentSubcomponentBuilderProvider = () -> new CustomGestureSettingsFragmentSubcomponentBuilder();
        this.resetLauncherFailureFragmentSubcomponentBuilderProvider = () -> new ResetLauncherFailureFragmentSubcomponentBuilder();
        this.resetLauncherPromptFragmentSubcomponentBuilderProvider = () -> new ResetLauncherPromptFragmentSubcomponentBuilder();
        this.popupFragmentSubcomponentBuilderProvider = () -> new PopupFragmentSubcomponentBuilder();
        this.hideAppsSettingsFragmentSubcomponentBuilderProvider = () -> new HideAppsSettingsFragmentSubcomponentBuilder();
        this.appOptionsFragmentSubcomponentBuilderProvider = () -> new AppOptionsFragmentSubcomponentBuilder();
        this.desktopSettingsFragmentSubcomponentBuilderProvider = () -> new DesktopSettingsFragmentSubcomponentBuilder();
        this.dockSettingsFragmentSubcomponentBuilderProvider = () -> new DockSettingsFragmentSubcomponentBuilder();
        this.iconPackSettingsFragmentSubcomponentBuilderProvider = () -> new IconPackSettingsFragmentSubcomponentBuilder();
        this.changeIconFragmentSubcomponentBuilderProvider = () -> new ChangeIconFragmentSubcomponentBuilder();
        this.appDrawerSettingsFragmentSubcomponentBuilderProvider = () -> new AppDrawerSettingsFragmentSubcomponentBuilder();
        this.folderSettingsFragmentSubcomponentBuilderProvider = () -> new FolderSettingsFragmentSubcomponentBuilder();
        this.confirmShortcutActivitySubcomponentBuilderProvider = () -> new ConfirmShortcutActivitySubcomponentBuilder();
    }

    private BackupRestoreManager injectBackupRestoreManager(BackupRestoreManager backupRestoreManager) {
        BackupRestoreManager_MembersInjector.injectMLayoutSettingsManager(backupRestoreManager, this.layoutSettingsManagerProvider.get());
        BackupRestoreManager_MembersInjector.injectMIconPackManager(backupRestoreManager, this.iconPackManagerProvider.get());
        BackupRestoreManager_MembersInjector.injectMSettingsStorage(backupRestoreManager, this.providesSettingsStorageProvider.get());
        BackupRestoreManager_MembersInjector.injectMLocalAppsManager(backupRestoreManager, this.localAppsManagerProvider.get());
        return backupRestoreManager;
    }

    private BaseApplication injectBaseApplication(BaseApplication baseApplication) {
        BaseApplication_MembersInjector.injectMAnalyticsHandler(baseApplication, this.providesAnalyticsHandlerProvider.get());
        BaseApplication_MembersInjector.injectMMemoryRegistry(baseApplication, this.provideMemoryRegistryProvider.get());
        BaseApplication_MembersInjector.injectMSettingsStorage(baseApplication, this.providesSettingsStorageProvider.get());
        BaseApplication_MembersInjector.injectMHybridNetworkFetcherProvider(baseApplication, this.hybridNetworkFetcherProvider);
        BaseApplication_MembersInjector.injectMIconPackManager(baseApplication, this.iconPackManagerProvider.get());
        BaseApplication_MembersInjector.injectMIconFilterProvider(baseApplication, this.appsInfoIconFilterProvider);
        BaseApplication_MembersInjector.injectMActivityInjector(baseApplication, getDispatchingAndroidInjectorOfActivity());
        BaseApplication_MembersInjector.injectMSupportFragmentInjector(baseApplication, getDispatchingAndroidInjectorOfFragment());
        BaseApplication_MembersInjector.injectMServiceInjector(baseApplication, getDispatchingAndroidInjectorOfService());
        return baseApplication;
    }

    private FlavorInitializer injectFlavorInitializer(FlavorInitializer flavorInitializer) {
        FlavorInitializer_MembersInjector.injectMLauncherAppComponent(flavorInitializer, this);
        FlavorInitializer_MembersInjector.injectMSettingsStorage(flavorInitializer, this.providesSettingsStorageProvider.get());
        FlavorInitializer_MembersInjector.injectMOkHttpClient(flavorInitializer, this.providesOkHttpClientProvider.get());
        FlavorInitializer_MembersInjector.injectMIconPackManager(flavorInitializer, this.iconPackManagerProvider.get());
        FlavorInitializer_MembersInjector.injectMIconFilterProvider(flavorInitializer, this.appsInfoIconFilterProvider);
        FlavorInitializer_MembersInjector.injectMFrequentlyUsedModel(flavorInitializer, this.providesFrequentlyUsedModelProvider.get());
        FlavorInitializer_MembersInjector.injectMKiller(flavorInitializer, this.connectionPoolKillerProvider.get());
        return flavorInitializer;
    }

    private LauncherAppState injectLauncherAppState(LauncherAppState launcherAppState) {
        LauncherAppState_MembersInjector.injectMLayoutSettingsManager(launcherAppState, this.layoutSettingsManagerProvider.get());
        return launcherAppState;
    }

    private LauncherModel injectLauncherModel(LauncherModel launcherModel) {
        LauncherModel_MembersInjector.injectMDeepShortcutManager(launcherModel, this.provideDeepShortcutManagerProvider.get());
        LauncherModel_MembersInjector.injectMAppsInfoLabelOverrideFilter(launcherModel, getAppsInfoLabelOverrideFilter());
        return launcherModel;
    }


    @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent
    public void inject(BaseApplication baseApplication) {
        injectBaseApplication(baseApplication);
    }

    @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent
    public void inject(FlavorInitializer flavorInitializer) {
        injectFlavorInitializer(flavorInitializer);
    }

    @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent
    public void inject(LauncherAppState launcherAppState) {
        injectLauncherAppState(launcherAppState);
    }

    @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent
    public void inject(LauncherModel launcherModel) {
        injectLauncherModel(launcherModel);
    }

    @Override // com.voxel.simplesearchlauncher.dagger.LauncherAppComponent
    public void inject(BackupRestoreManager backupRestoreManager) {
        injectBackupRestoreManager(backupRestoreManager);
    }

}