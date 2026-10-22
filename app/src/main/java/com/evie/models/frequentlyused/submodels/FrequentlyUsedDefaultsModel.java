package com.evie.models.frequentlyused.submodels;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.Telephony;
import com.evie.models.frequentlyused.data.FrequentlyUsedApp;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedDefaultsModel {
    private final Context mContext;
    private final BehaviorSubject<List<FrequentlyUsedApp>> mDefaults = BehaviorSubject.create();
    private boolean mInitialized = false;

    public FrequentlyUsedDefaultsModel(Context context) {
        this.mContext = context;
    }

    private FrequentlyUsedApp getBrowser() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse("http://www.google.com"));
        intent.addCategory("android.intent.category.DEFAULT");
        return getActivityForIntent(intent);
    }

    private FrequentlyUsedApp getCalendar() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setFlags(270532608);
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, 123456L));
        return getActivityForIntent(intent);
    }

    private FrequentlyUsedApp getCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        return getActivityForIntent(intent);
    }

    private FrequentlyUsedApp getContacts() {
        Intent intentForPackageName;
        Intent intentForPackageName2;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.APP_CONTACTS");
        FrequentlyUsedApp activityForIntent = getActivityForIntent(intent);
        if (activityForIntent == null && (intentForPackageName2 = getIntentForPackageName("com.android.contacts")) != null) {
            activityForIntent = getActivityForIntent(intentForPackageName2);
        }
        return (activityForIntent != null || (intentForPackageName = getIntentForPackageName("com.google.android.contacts")) == null) ? activityForIntent : getActivityForIntent(intentForPackageName);
    }

    private ResolveInfo getDefaultAppInfoForAction(Context context, Intent intent) {
        List<ResolveInfo> listQueryIntentActivities;
        ResolveInfo resolveInfoResolveActivity = context.getPackageManager().resolveActivity(intent, 65536);
        ResolveInfo launchableAppInfoForResolveInfo = null;
        if (resolveInfoResolveActivity != null) {
            launchableAppInfoForResolveInfo = resolveInfoResolveActivity.activityInfo.name.equals("com.android.internal.app.ResolverActivity") ? null : getLaunchableAppInfoForResolveInfo(context, resolveInfoResolveActivity);
            if (launchableAppInfoForResolveInfo == null && (listQueryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 65536)) != null && !listQueryIntentActivities.isEmpty()) {
                Iterator<ResolveInfo> it = listQueryIntentActivities.iterator();
                while (it.hasNext() && (launchableAppInfoForResolveInfo = getLaunchableAppInfoForResolveInfo(context, it.next())) == null) {
                }
            }
        }
        return launchableAppInfoForResolveInfo;
    }

    private ResolveInfo getLaunchableAppInfoForResolveInfo(Context context, ResolveInfo resolveInfo) {
        if (resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(resolveInfo.activityInfo.packageName);
        List<ResolveInfo> listQueryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (listQueryIntentActivities == null || listQueryIntentActivities.size() <= 0) {
            return null;
        }
        return listQueryIntentActivities.get(0);
    }

    private FrequentlyUsedApp getMessenger() {
        try {
            return getActivityForIntent(this.mContext.getPackageManager().getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(this.mContext)));
        } catch (Throwable unused) {
            return null;
        }
    }

    private FrequentlyUsedApp getPhone() {
        Intent intent = new Intent("android.intent.action.DIAL");
        intent.addCategory("android.intent.category.DEFAULT");
        return getActivityForIntent(intent);
    }

    private FrequentlyUsedApp getSettings() {
        Intent intent = new Intent("android.settings.SETTINGS");
        intent.addCategory("android.intent.category.DEFAULT");
        return getActivityForIntent(intent);
    }

    public List lambda$getDefaultFrequentlyUsedApps$0() throws Exception {
        ArrayList arrayList = new ArrayList();
        arrayList.add(this.getBrowser());
        arrayList.add(this.getPhone());
        arrayList.add(this.getMessenger());
        arrayList.add(this.getCamera());
        arrayList.add(this.getSettings());
        arrayList.add(this.getContacts());
        arrayList.add(this.getCalendar());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (it.next() == null) {
                it.remove();
            }
        }
        return arrayList;
    }

    FrequentlyUsedApp getActivityForIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        try {
            ResolveInfo defaultAppInfoForAction = getDefaultAppInfoForAction(this.mContext, intent);
            if (defaultAppInfoForAction == null) {
                return null;
            }
            ActivityInfo activityInfo = defaultAppInfoForAction.activityInfo;
            return new FrequentlyUsedApp(activityInfo.packageName, activityInfo.name);
        } catch (Throwable unused) {
            return null;
        }
    }

    @SuppressLint({"CheckResult"})
    public Observable<List<FrequentlyUsedApp>> getDefaultFrequentlyUsedApps() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            Observable observableSubscribeOn = Observable.fromCallable(new Callable() { // from class: com.evie.models.frequentlyused.submodels.-$$Lambda$FrequentlyUsedDefaultsModel$jKjSxlZ7xH3BO6cQC4ZscYBYiz4
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return FrequentlyUsedDefaultsModel.this.lambda$getDefaultFrequentlyUsedApps$0();
                }
            }).subscribeOn(Schedulers.io());
            final BehaviorSubject<List<FrequentlyUsedApp>> behaviorSubject = this.mDefaults;
            behaviorSubject.getClass();
            observableSubscribeOn.subscribe(new Consumer() { // from class: com.evie.models.frequentlyused.submodels.-$$Lambda$0QD_bvz-n6C4nj2EF1sNcbmJ-nU
                @Override // io.reactivex.functions.Consumer
                public final void accept(Object obj) {
                    behaviorSubject.onNext((List) obj);
                }
            });
        }
        return this.mDefaults;
    }

    Intent getIntentForPackageName(String str) {
        try {
            return this.mContext.getPackageManager().getLaunchIntentForPackage(str);
        } catch (Throwable unused) {
            return null;
        }
    }
}
