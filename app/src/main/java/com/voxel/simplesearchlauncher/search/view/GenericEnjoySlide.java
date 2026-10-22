package com.voxel.simplesearchlauncher.search.view;

import android.content.Context;
import android.view.View;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class GenericEnjoySlide extends OptionSlide {
    private int mNegativeIndex;
    private int mPositiveIndex;
    private SlideLayout mSlideLayout;

    public GenericEnjoySlide(Context context, SlideLayout slideLayout, int i, int i2) {
        super(context, slideLayout);
        this.mSlideLayout = slideLayout;
        this.mPositiveIndex = i;
        this.mNegativeIndex = i2;
        this.mTextView.setText(context.getString(R.string.feedback_generic_enjoying_app_question, context.getString(R.string.app_name)));
        this.mPositiveButton.setText(R.string.feedback_enjoying_app_positive);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$GenericEnjoySlide$bYvXZpQ4b9gUNWQSTT4xoPMxSfY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                GenericEnjoySlide genericEnjoySlide = GenericEnjoySlide.this;
                genericEnjoySlide.mSlideLayout.setDisplayedChild(genericEnjoySlide.mPositiveIndex);
            }
        });
        this.mNegativeButton.setText(R.string.feedback_enjoying_app_negative);
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$GenericEnjoySlide$1RD-Je6R0Awk2MA4tT-Ekh_hx_s
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                GenericEnjoySlide genericEnjoySlide = GenericEnjoySlide.this;
                genericEnjoySlide.mSlideLayout.setDisplayedChild(genericEnjoySlide.mNegativeIndex);
            }
        });
    }
}
