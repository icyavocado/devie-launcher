package com.voxel.simplesearchlauncher.search.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.evie.search.SearchPresenter;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class AppFlashFeedbackSlide extends OptionSlide {
    private Activity mActivity;
    private int mNegativeIndex;
    private SearchPresenter mSearch;
    private SlideLayout mSlideLayout;

    public AppFlashFeedbackSlide(Context context, SlideLayout slideLayout, SearchPresenter searchPresenter, Activity activity, int i) {
        super(context, slideLayout);
        this.mSlideLayout = slideLayout;
        this.mSearch = searchPresenter;
        this.mActivity = activity;
        this.mNegativeIndex = i;
        this.mTextView.setText(R.string.feedback_feedback_question);
        this.mPositiveButton.setText(R.string.feedback_feedback_positive);
        this.mNegativeButton.setText(R.string.feedback_feedback_negative);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$AppFlashFeedbackSlide$tqgzhUU3qszGlImCiZ0_TOnwBcA
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppFlashFeedbackSlide.this.lambda$new$0( view);
            }
        });
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$AppFlashFeedbackSlide$tWy3XxE0SRPgBfMWwXWY9VACJuY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppFlashFeedbackSlide.this.lambda$new$1( view);
            }
        });
    }

    public void lambda$new$0(View view) {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.parse("mailto:AppFlashFeedback@verizonwireless.com?subject=Feedback"));
        intent.putExtra("android.intent.extra.EMAIL", "AppFlashFeedback@verizonwireless.com");
        intent.putExtra("android.intent.extra.SUBJECT", "Feedback");
        this.mActivity.startActivity(intent);
        this.mSearch.removeCustomPresenter();
        AnalyticsHandler.getInstance().logEvent("ev_search_feedback_finished", new AnalyticsHandler.EventProp().add("type", "neg_enjoy_yes_feedback"));
    }

    public void lambda$new$1(View view) {
        this.mSlideLayout.setDisplayedChild(this.mNegativeIndex);
        AnalyticsHandler.getInstance().logEvent("ev_search_feedback_finished", new AnalyticsHandler.EventProp().add("type", "neg_enjoy_no_feedback"));
    }
}
