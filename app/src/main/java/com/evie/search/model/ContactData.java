package com.evie.search.model;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ContactData {
    private List<ActionData> actions = new ArrayList();
    private String headerUri;
    private String link;
    private String name;
    private String overlayUri;
    private String shortcutUri;

    public ContactData(String str) {
        this.link = str;
    }

    public List<ActionData> getActions() {
        return this.actions;
    }

    public String getHeaderUri() {
        return this.headerUri;
    }

    public String getLink() {
        return this.link;
    }

    public String getName() {
        return this.name;
    }

    public String getOverlayUri() {
        return this.overlayUri;
    }

    public String getShortcutUri() {
        return this.shortcutUri;
    }

    public void setActions(List<ActionData> list) {
        this.actions = list;
    }

    public void setHeaderUri(String str) {
        this.headerUri = str;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setOverlayUri(String str) {
        this.overlayUri = str;
    }

    public void setShortcutUri(String str) {
        this.shortcutUri = str;
    }
}
