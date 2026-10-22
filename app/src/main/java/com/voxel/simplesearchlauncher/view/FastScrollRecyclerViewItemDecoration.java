package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class FastScrollRecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
    private Context mContext;
    private Paint mIndexPaint;
    private Paint mOverlayPaint = new Paint();
    private int mOverlayTextHeight;
    private int mOverlayTextSize;
    private int mOverlayYOffset;

    public FastScrollRecyclerViewItemDecoration(Context context) {
        this.mContext = context;
        this.mOverlayTextSize = (int) this.mContext.getResources().getDimension(R.dimen.fast_scroll_overlay_text_size);
        this.mOverlayYOffset = (int) this.mContext.getResources().getDimension(R.dimen.fast_scroll_overlay_y_offset);
        this.mOverlayPaint.setColor(-1);
        this.mOverlayPaint.setTextSize(this.mOverlayTextSize);
        this.mOverlayPaint.setAntiAlias(true);
        this.mOverlayPaint.setFakeBoldText(true);
        this.mOverlayPaint.setStyle(Paint.Style.FILL);
        this.mOverlayTextHeight = (int) Math.abs(this.mOverlayPaint.getFontMetrics().top);
        this.mIndexPaint = new Paint();
        this.mIndexPaint.setAntiAlias(true);
        this.mIndexPaint.setStyle(Paint.Style.FILL);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.ItemDecoration
    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        super.onDrawOver(canvas, recyclerView, state);
        FastScrollRecyclerView fastScrollRecyclerView = (FastScrollRecyclerView) recyclerView;
        float f = fastScrollRecyclerView.mScaledIndexWidth;
        float f2 = fastScrollRecyclerView.mScaledIndexHeight;
        float f3 = fastScrollRecyclerView.mSectionStartX;
        float f4 = fastScrollRecyclerView.mSectionStartY;
        String[] strArr = fastScrollRecyclerView.mSections;
        String str = fastScrollRecyclerView.mCurrentSection;
        boolean z = fastScrollRecyclerView.mShowLetter;
        float f5 = fastScrollRecyclerView.mTouchY;
        float f6 = fastScrollRecyclerView.mOverlayFactor;
        if (strArr == null) {
            return;
        }
        float f7 = 1.0f;
        if (AlphaUtil.floatToIntAlpha(f6) > 0 && str != null && !str.equals("")) {
            int iRound = Math.round(((f - this.mOverlayPaint.measureText(str.toUpperCase())) / 2.0f) + f3);
            int iMax = Math.max(this.mOverlayTextHeight, Math.round(f5 + (this.mOverlayYOffset * f6)));
            this.mOverlayPaint.setAlpha(AlphaUtil.floatToIntAlpha(f6));
            this.mOverlayPaint.setTextSize(this.mOverlayTextSize * (((1.0f - f6) * 0.5f) + (f6 * 1.0f)));
            canvas.drawText(str.toUpperCase(), iRound, iMax, this.mOverlayPaint);
        }
        this.mIndexPaint.setTextSize(f / 2.0f);
        int iAbs = (int) (((int) Math.abs(this.mIndexPaint.getFontMetrics().top)) - f2);
        int i = 0;
        while (i < strArr.length) {
            String upperCase = strArr[i].toUpperCase();
            this.mIndexPaint.setColor(-3355444);
            this.mIndexPaint.setAlpha((int) (((f7 - f6) * 200.0f) + (50.0f * f6)));
            this.mIndexPaint.setFakeBoldText(false);
            int i2 = i + 1;
            canvas.drawText(strArr[i].toUpperCase(), Math.round(((f - this.mIndexPaint.measureText(upperCase)) / 2.0f) + f3), Math.round(recyclerView.getPaddingTop() + f4 + (i2 * f2) + iAbs), this.mIndexPaint);
            i = i2;
            f7 = 1.0f;
        }
    }
}
