package com.evie.search.model;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ActionData {
    private String actionType;
    private String iconUrl;
    private String label;
    private List<AppLink> links = new ArrayList();
    private String subtext;

    public ActionData(String str, String str2, String str3, String str4) {
        this.label = str;
        this.subtext = str2;
        this.iconUrl = str3;
        this.actionType = str4;
    }

    public void addLink(AppLink appLink) {
        this.links.add(appLink);
    }

    public String getActionType() {
        return this.actionType;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public String getLabel() {
        return this.label;
    }

    public List<AppLink> getLinks() {
        return this.links;
    }

    public String getSubtext() {
        return this.subtext;
    }

    public void setLabel(String str) {
        this.label = str;
    }

    public void setSubtext(String str) {
        this.subtext = str;
    }
}
