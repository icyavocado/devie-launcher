package com.evie.common.services.links;

import android.content.Intent;
import java.util.Collections;
import java.util.List;

/* REWORK: analytics data slot removed; LinkRequest is now just links + app + callback. */
public class LinkRequest {
    private static final LinkCallback NO_CALLBACK = new LinkCallback() { // from class: com.evie.common.services.links.LinkRequest.1
        @Override // com.evie.common.services.links.LinkCallback
        public boolean onResolved(Intent intent) {
            return true;
        }
    };
    private String app;
    private LinkCallback callback;
    private final List<String> links;

    public LinkRequest(String str) {
        this((List<String>) Collections.singletonList(str));
    }

    public LinkRequest(List<String> list) {
        this.callback = NO_CALLBACK;
        this.links = list;
    }

    public LinkRequest app(String str) {
        this.app = str;
        return this;
    }

    public String app() {
        return this.app;
    }

    public LinkCallback callback() {
        return this.callback;
    }

    public LinkRequest callback(LinkCallback linkCallback) {
        this.callback = linkCallback;
        return this;
    }

    public List<String> links() {
        return this.links;
    }
}