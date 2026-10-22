package com.voxel.simplesearchlauncher.notification;

import com.voxel.simplesearchlauncher.notification.extractors.Extractor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
public class BadgeAppsInfo {
    private Set<String> mExtractorApps = Collections.synchronizedSet(new HashSet());
    private Set<String> mHandlerApps = Collections.synchronizedSet(new HashSet(Arrays.asList("com.facebook.katana", "com.facebook.orca", "com.twitter.android", "com.whatsapp", "org.telegram.messenger", "org.telegram.plus", "com.snapchat.android", "com.google.android.apps.messaging", "com.truecaller", "com.viber.voip", "net.one97.paytm", "com.dropbox.android", "com.sec.android.app.shealth", "jp.naver.line.android", "com.kakao.talk", "com.asus.message", "com.tencent.mm", "com.path", "com.verizon.messaging.vzmsgs", "com.facebook.pages.app", "com.android.dialer", "com.cloudmagic.mail", "com.linecorp.linelite")));
    private Set<String> mTrustedHandlers = new HashSet(Arrays.asList("com.facebook.orca", "me.bluemail.mail"));
    private Set<String> mUnreliableHandlers = new HashSet(Arrays.asList("com.whatsapp", "com.easilydo.mail", "com.google.android.youtube"));

    protected BadgeAppsInfo() {
    }

    public void addExtractors(Collection<Extractor> collection) {
        Iterator<Extractor> it = collection.iterator();
        while (it.hasNext()) {
            this.mExtractorApps.add(it.next().getPackageId());
        }
    }

    public void addHandlerPackage(String str) {
        this.mHandlerApps.add(str);
    }

    public Set<String> getHandlerPackages() {
        return this.mHandlerApps;
    }

    public boolean isHandlerPackage(String str) {
        return this.mHandlerApps.contains(str);
    }

    public boolean isHighAccuracyApp(String str) {
        if (str == null) {
            return false;
        }
        return this.mHandlerApps.contains(str) || this.mExtractorApps.contains(str);
    }

    public boolean isTrustedHandler(String str) {
        return this.mTrustedHandlers.contains(str);
    }

    public boolean isUnreliableHandler(String str) {
        return this.mUnreliableHandlers.contains(str);
    }

    public void resetExtractors() {
        this.mExtractorApps.clear();
    }
}
