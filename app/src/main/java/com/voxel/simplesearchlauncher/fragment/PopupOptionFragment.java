package com.voxel.simplesearchlauncher.fragment;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public abstract class PopupOptionFragment extends SpringCardFragment {
    private View mContentView;

    public void lambda$onCreateChildContentView$0(View view) {
        View.OnClickListener negativeClickListener = this.getNegativeClickListener();
        if (negativeClickListener != null) {
            negativeClickListener.onClick(view);
        }
        this.runExitAnimation();
    }

    static /* synthetic */ void lambda$onCreateChildContentView$1(GradientDrawable gradientDrawable, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        gradientDrawable.setCornerRadius(view.getHeight() / 2.0f);
    }

    public void lambda$onCreateChildContentView$2(View view) {
        View.OnClickListener positiveClickListener = this.getPositiveClickListener();
        if (positiveClickListener != null) {
            positiveClickListener.onClick(view);
        }
        this.runExitAnimation();
    }

    static /* synthetic */ void lambda$onCreateChildContentView$3(GradientDrawable gradientDrawable, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        gradientDrawable.setCornerRadius(view.getHeight() / 2.0f);
    }

    protected abstract Drawable getHeaderImageDrawable();

    protected abstract CharSequence getMainText();

    protected abstract View.OnClickListener getNegativeClickListener();

    protected abstract CharSequence getNegativeText();

    protected abstract View.OnClickListener getPositiveClickListener();

    protected abstract CharSequence getPositiveText();

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    public View onCreateChildContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mContentView = layoutInflater.inflate(R.layout.fragment_popup_option, viewGroup, false);
        View.OnClickListener onClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$PopupOptionFragment$QNxkih7Zsf82M8Vxm4wSQokwYcE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                PopupOptionFragment.this.lambda$onCreateChildContentView$0( view);
            }
        };
        ((ImageView) this.mContentView.findViewById(R.id.image)).setImageDrawable(getHeaderImageDrawable());
        ((TextView) this.mContentView.findViewById(R.id.content_text)).setText(getMainText());
        Button button = (Button) this.mContentView.findViewById(R.id.ok_button);
        final GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.popup_option_positive_rounded_bg);
        gradientDrawable.setCornerRadius(button.getHeight() / 2);
        button.setBackground(gradientDrawable);
        button.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$PopupOptionFragment$3Hm2UrXkTSYjq4R3Frl4mhGn36o
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                PopupOptionFragment.lambda$onCreateChildContentView$1(gradientDrawable, view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        button.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$PopupOptionFragment$LAdzdCjKN5XLIugg1JZ52MWPuwo
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                PopupOptionFragment.this.lambda$onCreateChildContentView$2( view);
            }
        });
        button.setText(getPositiveText());
        Button button2 = (Button) this.mContentView.findViewById(R.id.cancel_button);
        final GradientDrawable gradientDrawable2 = (GradientDrawable) getResources().getDrawable(R.drawable.popup_option_negative_rounded_bg);
        gradientDrawable2.setCornerRadius(button2.getHeight() / 2);
        button2.setBackground(gradientDrawable2);
        button2.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$PopupOptionFragment$lLWeMtPPEW_hBheeweBu1uBuvWA
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                PopupOptionFragment.lambda$onCreateChildContentView$3(gradientDrawable2, view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        button2.setOnClickListener(onClickListener);
        button2.setText(getNegativeText());
        ((ImageButton) this.mContentView.findViewById(R.id.close_button)).setOnClickListener(onClickListener);
        return this.mContentView;
    }
}
