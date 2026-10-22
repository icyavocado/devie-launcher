package com.voxel.simplesearchlauncher.search.view;

import android.content.Context;
import android.view.View;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class EnjoySlide extends OptionSlide {
    private int mNegativeIndex;
    private int mPositiveIndex;
    private SlideLayout mSlideLayout;

    public EnjoySlide(Context context, SlideLayout slideLayout, int i, int i2) {
        super(context, slideLayout);
        this.mSlideLayout = slideLayout;
        this.mPositiveIndex = i;
        this.mNegativeIndex = i2;
        this.mTextView.setText(R.string.feedback_enjoying_app_question);
        this.mPositiveButton.setText(R.string.feedback_enjoying_app_positive);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$EnjoySlide$QfIFXvR1172ibBP-iVvnVspBa7Y
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                EnjoySlide enjoySlide = EnjoySlide.this;
                enjoySlide.mSlideLayout.setDisplayedChild(enjoySlide.mPositiveIndex);
            }
        });
        this.mNegativeButton.setText(R.string.feedback_enjoying_app_negative);
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$EnjoySlide$LtSJMMtJQpgjZ2AvrFKD99XgWA4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                EnjoySlide enjoySlide = EnjoySlide.this;
                enjoySlide.mSlideLayout.setDisplayedChild(enjoySlide.mNegativeIndex);
            }
        });
    }
}
