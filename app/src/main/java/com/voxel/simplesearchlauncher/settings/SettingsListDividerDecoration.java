package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.ovacoda.devie.R;

public class SettingsListDividerDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;

    public SettingsListDividerDecoration(Context context) {
        this.mDivider = context.getDrawable(R.drawable.settings_row_divider);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int adapterCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == -1 || position >= adapterCount - 1) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            /* The inset divider has no intrinsic width (-1); it inherits its
             * width from the bounds, exactly like LinearLayout.showDividers. */
            this.mDivider.setBounds(parent.getLeft(), top, parent.getRight(), top + this.mDivider.getIntrinsicHeight());
            this.mDivider.draw(canvas);
        }
    }
}
