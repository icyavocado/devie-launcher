package com.voxel.launcher3;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.ovacoda.devie.R;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class PageIndicator extends LinearLayout {
    private int mActiveMarkerIndex;
    private LayoutInflater mLayoutInflater;
    private ArrayList<PageIndicatorMarker> mMarkers;
    private int mMaxWindowSize;
    private int[] mWindowRange;

    public static class PageMarkerResources {
        int activeId;
        int inactiveId;

        public PageMarkerResources() {
            this.activeId = R.drawable.ic_pageindicator_current;
            this.inactiveId = R.drawable.ic_pageindicator_default;
        }

        public PageMarkerResources(int i, int i2) {
            this.activeId = i;
            this.inactiveId = i2;
        }
    }

    public PageIndicator(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PageIndicator(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mWindowRange = new int[2];
        this.mMarkers = new ArrayList<>();
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.PageIndicator, i, 0);
        this.mMaxWindowSize = typedArrayObtainStyledAttributes.getInteger(0, 15);
        int[] iArr = this.mWindowRange;
        iArr[0] = 0;
        iArr[1] = 0;
        this.mLayoutInflater = LayoutInflater.from(context);
        typedArrayObtainStyledAttributes.recycle();
        getLayoutTransition().setDuration(175L);
    }

    private void disableLayoutTransitions() {
        LayoutTransition layoutTransition = getLayoutTransition();
        layoutTransition.disableTransitionType(2);
        layoutTransition.disableTransitionType(3);
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
    }

    private void enableLayoutTransitions() {
        LayoutTransition layoutTransition = getLayoutTransition();
        layoutTransition.enableTransitionType(2);
        layoutTransition.enableTransitionType(3);
        layoutTransition.enableTransitionType(0);
        layoutTransition.enableTransitionType(1);
    }

    void addMarker(int i, PageMarkerResources pageMarkerResources, boolean z) {
        int iMax = Math.max(0, Math.min(i, this.mMarkers.size()));
        PageIndicatorMarker pageIndicatorMarker = (PageIndicatorMarker) this.mLayoutInflater.inflate(R.layout.page_indicator_marker, (ViewGroup) this, false);
        pageIndicatorMarker.setMarkerDrawables(pageMarkerResources.activeId, pageMarkerResources.inactiveId);
        this.mMarkers.add(iMax, pageIndicatorMarker);
        offsetWindowCenterTo(this.mActiveMarkerIndex, z);
    }

    void addMarkers(ArrayList<PageMarkerResources> arrayList, boolean z) {
        for (int i = 0; i < arrayList.size(); i++) {
            addMarker(Integer.MAX_VALUE, arrayList.get(i), z);
        }
    }

    void offsetWindowCenterTo(int i, boolean z) {
        if (i < 0) {
            new Throwable().printStackTrace();
        }
        int iMin = Math.min(this.mMarkers.size(), this.mMaxWindowSize);
        int iMin2 = Math.min(this.mMarkers.size(), Math.max(0, i - (iMin / 2)) + this.mMaxWindowSize);
        int iMin3 = iMin2 - Math.min(this.mMarkers.size(), iMin);
        int i2 = (iMin2 - iMin3) / 2;
        this.mMarkers.size();
        int[] iArr = this.mWindowRange;
        boolean z2 = (iArr[0] == iMin3 && iArr[1] == iMin2) ? false : true;
        if (!z) {
            disableLayoutTransitions();
        }
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View view = (PageIndicatorMarker) getChildAt(childCount);
            int iIndexOf = this.mMarkers.indexOf(view);
            if (iIndexOf < iMin3 || iIndexOf >= iMin2) {
                removeView(view);
            }
        }
        for (int i3 = 0; i3 < this.mMarkers.size(); i3++) {
            PageIndicatorMarker pageIndicatorMarker = this.mMarkers.get(i3);
            if (iMin3 > i3 || i3 >= iMin2) {
                pageIndicatorMarker.inactivate(true);
            } else {
                if (indexOfChild(pageIndicatorMarker) < 0) {
                    addView(pageIndicatorMarker, i3 - iMin3);
                }
                if (i3 == i) {
                    pageIndicatorMarker.activate(z2);
                } else {
                    pageIndicatorMarker.inactivate(z2);
                }
            }
        }
        if (!z) {
            enableLayoutTransitions();
        }
        int[] iArr2 = this.mWindowRange;
        iArr2[0] = iMin3;
        iArr2[1] = iMin2;
    }

    void removeAllMarkers(boolean z) {
        while (this.mMarkers.size() > 0) {
            removeMarker(Integer.MAX_VALUE, z);
        }
    }

    void removeMarker(int i, boolean z) {
        if (this.mMarkers.size() > 0) {
            this.mMarkers.remove(Math.max(0, Math.min(this.mMarkers.size() - 1, i)));
            offsetWindowCenterTo(this.mActiveMarkerIndex, z);
        }
    }

    void setActiveMarker(int i) {
        this.mActiveMarkerIndex = i;
        offsetWindowCenterTo(i, false);
    }

    void updateMarker(int i, PageMarkerResources pageMarkerResources) {
        this.mMarkers.get(i).setMarkerDrawables(pageMarkerResources.activeId, pageMarkerResources.inactiveId);
    }
}
