package com.evie.models.frequentlyused;

import android.os.Handler;
import android.os.Looper;
import com.evie.models.frequentlyused.data.FrequentlyUsedApp;
import com.evie.models.frequentlyused.room.AppLaunchEntry;
import com.voxel.simplesearchlauncher.analytics.AnalyticsFilter;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedAnalyticsFilter extends AnalyticsFilter {
    private final FrequentlyUsedModel mFrequentlyUsed;

    public FrequentlyUsedAnalyticsFilter(FrequentlyUsedModel frequentlyUsedModel) {
        this.mFrequentlyUsed = frequentlyUsedModel;
    }

    @Override // com.voxel.simplesearchlauncher.analytics.AnalyticsFilter
    public JSONObject filter(JSONObject jSONObject) {
        try {
            switch (jSONObject.getString("event")) {
                case "ev_app_launch":
                case "ev_host_launcher_app_launch":
                    JSONObject jSONObject2 = jSONObject.getJSONObject("properties");
                    String string = jSONObject2.getString("launch_package_name");
                    String string2 = jSONObject2.getString("launch_class_name");
                    List<FrequentlyUsedApp> listBlockingFirst = this.mFrequentlyUsed.getFrequentlyUsedApps().blockingFirst();
                    /* REWORK: the write used to land synchronously inside the
                     * click chain, so the visible frequent-apps row re-sorted
                     * (clicked app promoted, neighbours shifted) right as the
                     * activity-open animation started — a one-frame icon swap
                     * in the row. Defer the write so the rebind happens after
                     * the search overlay is gone. predicted_rank still uses
                     * the pre-launch frequencies. */
                    int rank = 0;
                    for (int i = 0; i < listBlockingFirst.size(); i++) {
                        if (listBlockingFirst.get(i).getPackageName().equals(string)) {
                            jSONObject2.put("predicted_rank", i + 1);
                            rank = i + 1;
                            break;
                        }
                    }
                    String pkg = string;
                    String className = string2;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> this.mFrequentlyUsed.addAppLaunchEntry(new AppLaunchEntry(0, pkg, className)), 600);
                    return jSONObject;
                default:
                    return jSONObject;
            }
        } catch (JSONException unused) {
            Timber.i("Skipping attaching frequently used index to app launch event due to missing properties.", new Object[0]);
            return jSONObject;
        }
    }
}
