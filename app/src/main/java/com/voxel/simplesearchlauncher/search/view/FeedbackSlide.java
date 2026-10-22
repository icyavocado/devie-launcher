package com.voxel.simplesearchlauncher.search.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.evie.search.SearchPresenter;
import com.ovacoda.devie.DevieLinks;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class FeedbackSlide extends OptionSlide {
    private Activity mActivity;
    private int mNegativeIndex;
    private SearchPresenter mSearch;
    private SlideLayout mSlideLayout;

    public FeedbackSlide(Context context, SlideLayout slideLayout, SearchPresenter searchPresenter, Activity activity, int i) {
        super(context, slideLayout);
        this.mSlideLayout = slideLayout;
        this.mSearch = searchPresenter;
        this.mActivity = activity;
        this.mNegativeIndex = i;
        this.mTextView.setText(R.string.feedback_feedback_question);
        this.mPositiveButton.setText(R.string.feedback_feedback_positive);
        this.mNegativeButton.setText(R.string.feedback_feedback_negative);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$FeedbackSlide$UnN6fkGGHK2u-8u6owz5fj_91vs
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                FeedbackSlide.this.lambda$new$0( view);
            }
        });
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$FeedbackSlide$2J4KbzeOGBpC7_qwr6_Q9pNfki4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                FeedbackSlide.this.lambda$new$1( view);
            }
        });
    }

    public void lambda$new$0(View view) {
        /* REWORK(github): GitHub issue tracker instead of the email intent. */
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(268468224);
        intent.setData(Uri.parse(DevieLinks.ISSUES_URL));
        this.mActivity.startActivity(intent);
        this.mSearch.removeCustomPresenter();
        AnalyticsHandler.getInstance().logEvent("ev_search_feedback_finished", new AnalyticsHandler.EventProp().add("type", "neg_enjoy_yes_feedback"));
    }

    public void lambda$new$1(View view) {
        this.mSlideLayout.setDisplayedChild(this.mNegativeIndex);
        AnalyticsHandler.getInstance().logEvent("ev_search_feedback_finished", new AnalyticsHandler.EventProp().add("type", "neg_enjoy_no_feedback"));
    }
}
