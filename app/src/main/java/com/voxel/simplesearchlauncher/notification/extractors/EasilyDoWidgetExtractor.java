package com.voxel.simplesearchlauncher.notification.extractors;

import android.content.ComponentName;
import android.content.Context;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes2.dex */
public class EasilyDoWidgetExtractor extends WidgetExtractor {
    private Pattern mCountPattern;

    public EasilyDoWidgetExtractor(Context context) {
        super(context);
        this.mCountPattern = Pattern.compile(".*\\((\\d+)\\).*");
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfigKey() {
        return String.format("extractor.%s", getPackageId());
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    protected String getFieldName() {
        return "email_widget_title";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getPackageId() {
        return "com.easilydo.mail";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public ComponentName getWidgetComponent() {
        return new ComponentName(getPackageId(), "com.easilydo.mail.widget.EmailAppWidgetProvider");
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    protected int parseCount(CharSequence charSequence) {
        Matcher matcher = this.mCountPattern.matcher(charSequence);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
