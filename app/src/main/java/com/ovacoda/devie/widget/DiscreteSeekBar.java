package com.ovacoda.devie.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class DiscreteSeekBar extends SeekBar {
    private static final int DEFAULT_TRACK_COLOR = 0x80b8b8b8;
    private static final int DEFAULT_PROGRESS_COLOR = 0xff3bdab6;
    private static final int DEFAULT_THUMB_COLOR = 0xff0bd7ba;

    private int mMin;
    private int mMax = 100;
    private OnProgressChangeListener mListener;
    private boolean mSuppressNotify;

    public interface OnProgressChangeListener {
        void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser);

        void onStartTrackingTouch(DiscreteSeekBar seekBar);

        void onStopTrackingTouch(DiscreteSeekBar seekBar);
    }

    public DiscreteSeekBar(Context context) {
        super(context);
        initialize();
    }

    public DiscreteSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize();
    }

    public DiscreteSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        initialize();
    }

    public DiscreteSeekBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        initialize();
    }

    private void initialize() {
        setProgressBackgroundTintList(ColorStateList.valueOf(DEFAULT_TRACK_COLOR));
        setProgressTintList(ColorStateList.valueOf(DEFAULT_PROGRESS_COLOR));
        setThumbTintList(ColorStateList.valueOf(DEFAULT_THUMB_COLOR));
        super.setMax(mMax - mMin);
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (mSuppressNotify) {
                    return;
                }
                if (mListener != null) {
                    mListener.onProgressChanged(DiscreteSeekBar.this, mMin + i, z);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mListener != null) {
                    mListener.onStartTrackingTouch(DiscreteSeekBar.this);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mListener != null) {
                    mListener.onStopTrackingTouch(DiscreteSeekBar.this);
                }
            }
        });
    }

    public int getMin() {
        return mMin;
    }

    public int getMax() {
        return mMax;
    }

    public void setMin(int i) {
        setRange(i, mMax);
    }

    public void setMax(int i) {
        setRange(mMin, i);
    }

    private void setRange(int i, int i2) {
        int i3;
        if (i > i2) {
            i3 = i2;
            i2 = i;
        } else {
            i3 = i;
        }
        int progress = getProgress();
        mSuppressNotify = true;
        super.setMax(i2 - i3);
        mMin = i3;
        mMax = i2;
        mSuppressNotify = false;
        setProgress(progress);
    }

    @Override // android.widget.ProgressBar
    public synchronized void setProgress(int i) {
        if (i < mMin) {
            i = mMin;
        } else if (i > mMax) {
            i = mMax;
        }
        if (mMin + super.getProgress() == i) {
            return;
        }
        super.setProgress(i - mMin);
    }

    @Override // android.widget.ProgressBar
    public synchronized int getProgress() {
        return mMin + super.getProgress();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.mListener = onProgressChangeListener;
    }

    public void setScrubberColor(int i) {
        setProgressTintList(ColorStateList.valueOf(i));
    }

    public void setThumbColor(int i, int i2) {
        setThumbTintList(new ColorStateList(new int[][]{new int[]{16842919}, new int[]{}}, new int[]{i2, i}));
    }
}
