package com.evie.search;

/* JADX INFO: loaded from: classes.dex */
public interface AppAvailabilityProvider {

    public static class DefaultProvider implements AppAvailabilityProvider {
        @Override // com.evie.search.AppAvailabilityProvider
        public boolean isHiddenFromBrowse(String str, String str2) {
            return false;
        }

        @Override // com.evie.search.AppAvailabilityProvider
        public boolean isHiddenFromSearch(String str, String str2) {
            return false;
        }
    }

    boolean isHiddenFromBrowse(String str, String str2);

    boolean isHiddenFromSearch(String str, String str2);
}
