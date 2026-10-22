package com.voxel.simplesearchlauncher.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.voxel.launcher3.DynamicGrid;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class GridLineController {
    private ValueAnimator mGridLineAnimator;
    private int mGridLineColor;
    private FrameLayout mGridLineContainer;
    private List<View> mGridLines = new ArrayList();
    private int mGridLineAlpha = 0;
    private ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.view.-$$Lambda$GridLineController$Tl4iATMQPJesYZDJq-FN_uow6F0
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
            GridLineController.this.lambda$new$0( valueAnimator);
        }
    };

    public GridLineController(FrameLayout frameLayout) {
        this.mGridLineContainer = frameLayout;
        this.mGridLineColor = frameLayout.getContext().getResources().getColor(R.color.evie_theme_green);
    }

    public void lambda$new$0(ValueAnimator valueAnimator) {
        this.mGridLineAlpha = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        Iterator<View> it = this.mGridLines.iterator();
        while (it.hasNext()) {
            it.next().getBackground().setAlpha(this.mGridLineAlpha);
        }
    }

    public void hideGridLines() {
        ValueAnimator valueAnimator = this.mGridLineAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mGridLineAnimator = ValueAnimator.ofInt(this.mGridLineAlpha, 0);
        this.mGridLineAnimator.setDuration(300L);
        this.mGridLineAnimator.addUpdateListener(this.updateListener);
        this.mGridLineAnimator.start();
    }

    public void showGridLines() {
        ValueAnimator valueAnimator = this.mGridLineAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mGridLineAnimator = ValueAnimator.ofInt(this.mGridLineAlpha, 255);
        this.mGridLineAnimator.setDuration(150L);
        this.mGridLineAnimator.addUpdateListener(this.updateListener);
        this.mGridLineAnimator.start();
    }

    public void updateGridLines(int i, int i2) {
        this.mGridLines.clear();
        this.mGridLineContainer.removeAllViews();
        Context context = this.mGridLineContainer.getContext();
        float width = this.mGridLineContainer.getWidth() / i2;
        float height = this.mGridLineContainer.getHeight() / i;
        int iPxFromDp = DynamicGrid.pxFromDp(2.0f, this.mGridLineContainer.getResources().getDisplayMetrics());
        for (int i3 = 1; i3 < i; i3++) {
            int iRound = Math.round(i3 * height);
            View view = new View(context);
            ColorDrawable colorDrawable = new ColorDrawable(this.mGridLineColor);
            colorDrawable.setAlpha(this.mGridLineAlpha);
            view.setLayoutParams(new ViewGroup.LayoutParams(-1, iPxFromDp));
            view.setBackground(colorDrawable);
            view.setTranslationY(iRound);
            this.mGridLines.add(view);
            this.mGridLineContainer.addView(view);
        }
        ColorDrawable colorDrawable2 = new ColorDrawable(this.mGridLineColor);
        colorDrawable2.setAlpha(this.mGridLineAlpha);
        View view2 = new View(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, iPxFromDp);
        layoutParams.gravity = 48;
        view2.setLayoutParams(layoutParams);
        view2.setBackground(colorDrawable2);
        this.mGridLines.add(view2);
        this.mGridLineContainer.addView(view2);
        ColorDrawable colorDrawable3 = new ColorDrawable(this.mGridLineColor);
        colorDrawable3.setAlpha(this.mGridLineAlpha);
        View view3 = new View(context);
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, iPxFromDp);
        layoutParams2.gravity = 80;
        view3.setLayoutParams(layoutParams2);
        view3.setBackground(colorDrawable3);
        this.mGridLines.add(view3);
        this.mGridLineContainer.addView(view3);
        for (int i4 = 1; i4 < i2; i4++) {
            int iRound2 = Math.round(i4 * width);
            View view4 = new View(context);
            ColorDrawable colorDrawable4 = new ColorDrawable(this.mGridLineColor);
            colorDrawable4.setAlpha(this.mGridLineAlpha);
            view4.setLayoutParams(new ViewGroup.LayoutParams(iPxFromDp, -1));
            view4.setBackground(colorDrawable4);
            view4.setTranslationX(iRound2);
            this.mGridLines.add(view4);
            this.mGridLineContainer.addView(view4);
        }
        ColorDrawable colorDrawable5 = new ColorDrawable(this.mGridLineColor);
        colorDrawable5.setAlpha(this.mGridLineAlpha);
        View view5 = new View(context);
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(iPxFromDp, -1);
        layoutParams3.gravity = 3;
        view5.setLayoutParams(layoutParams3);
        view5.setBackground(colorDrawable5);
        this.mGridLines.add(view5);
        this.mGridLineContainer.addView(view5);
        ColorDrawable colorDrawable6 = new ColorDrawable(this.mGridLineColor);
        colorDrawable6.setAlpha(this.mGridLineAlpha);
        View view6 = new View(context);
        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(iPxFromDp, -1);
        layoutParams4.gravity = 5;
        view6.setLayoutParams(layoutParams4);
        view6.setBackground(colorDrawable6);
        this.mGridLines.add(view6);
        this.mGridLineContainer.addView(view6);
    }
}
