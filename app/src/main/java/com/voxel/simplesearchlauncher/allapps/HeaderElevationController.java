package com.voxel.simplesearchlauncher.allapps;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.ovacoda.devie.R;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class HeaderElevationController extends RecyclerView.OnScrollListener {
    private int mCurrentY = 0;

    public static class ControllerV16 extends HeaderElevationController {
        private final float mScrollToElevation;
        private final List<View> mShadowViews;

        public ControllerV16(Context context, List<View> list) {
            this.mScrollToElevation = context.getResources().getDimension(R.dimen.all_apps_header_scroll_to_elevation);
            this.mShadowViews = list;
        }

        @Override // com.voxel.simplesearchlauncher.allapps.HeaderElevationController
        public void onScroll(int i) {
            float fMin = Math.min(i, this.mScrollToElevation) / this.mScrollToElevation;
            Iterator<View> it = this.mShadowViews.iterator();
            while (it.hasNext()) {
                it.next().getBackground().setAlpha(AlphaUtil.floatToIntAlpha(AlphaUtil.limitAlpha(fMin)));
            }
        }
    }

    abstract void onScroll(int i);

    @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
    public final void onScrolled(RecyclerView recyclerView, int i, int i2) {
        this.mCurrentY = recyclerView.computeVerticalScrollOffset();
        onScroll(this.mCurrentY);
    }

    public void reset() {
        this.mCurrentY = 0;
        onScroll(this.mCurrentY);
    }
}
