package com.voxel.simplesearchlauncher.search.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.evie.search.SearchPresenter;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SetDefaultLauncherSlide extends OptionSlide {
    private SearchPresenter mSearch;

    public SetDefaultLauncherSlide(Context context, SlideLayout slideLayout, SearchPresenter searchPresenter, final Activity activity) {
        super(context, slideLayout);
        this.mSearch = searchPresenter;
        this.mTextView.setText(R.string.reset_default_home_prompt);
        this.mPositiveButton.setText(android.R.string.ok);
        this.mNegativeButton.setText(R.string.reset_default_home_later);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$SetDefaultLauncherSlide$zIYKrz_y7-zwS4JyRQ5mXLHhMjY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SetDefaultLauncherSlide.this.lambda$new$0( activity, view);
            }
        });
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$SetDefaultLauncherSlide$-SiOlQi9IXG8-vRtvxVbvXdVyXE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SetDefaultLauncherSlide.this.mSearch.removeCustomPresenter();
            }
        });
    }

    public void lambda$new$0(Activity activity, View view) {
        ResetLauncherActivity.resetHome(activity, true, true);
        this.mSearch.removeCustomPresenter();
    }
}
