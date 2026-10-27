package com.voxel.simplesearchlauncher.popup;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.ovacoda.devie.R;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;

/* JADX INFO: loaded from: classes2.dex */
public class ShortcutItemView extends ConstraintLayout {
    private static final Point sTempPoint = new Point();

    View mDividerView;

    View mHandleView;
    Point mIconLastTouchPos;

    ImageView mIconView;
    PopupFragment.UnbadgedShortcutInfo mInfo;
    SystemShortcut mSystemShortcut;

    TextView mTextView;

    public ShortcutItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ShortcutItemView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIconLastTouchPos = new Point();
    }

    public void applyShortcutInfo(PopupFragment.UnbadgedShortcutInfo unbadgedShortcutInfo) {
        setTag(unbadgedShortcutInfo);
        this.mInfo = unbadgedShortcutInfo;
        this.mIconView.setBackground(new BitmapDrawable(getResources(), unbadgedShortcutInfo.getIcon(LauncherAppState.getInstance().getIconCache())));
        this.mIconView.setImageDrawable(null);
        this.mIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        CharSequence longLabel = unbadgedShortcutInfo.mDetail.getLongLabel();
        boolean z = !TextUtils.isEmpty(longLabel) && this.mTextView.getPaint().measureText(longLabel.toString()) <= ((float) ((this.mTextView.getWidth() - this.mTextView.getTotalPaddingLeft()) - this.mTextView.getTotalPaddingRight()));
        TextView textView = this.mTextView;
        if (!z) {
            longLabel = unbadgedShortcutInfo.mDetail.getShortLabel();
        }
        textView.setText(longLabel);
        this.mHandleView.setVisibility(0);
    }

    public void applySystemShortcut(SystemShortcut systemShortcut) {
        this.mSystemShortcut = systemShortcut;
        this.mIconView.setBackground(null);
        this.mIconView.setImageDrawable(this.mSystemShortcut.getMainIcon(getContext()));
        this.mIconView.setScaleType(ImageView.ScaleType.CENTER);
        this.mTextView.setText(systemShortcut.getLabel(getContext()));
        this.mHandleView.setVisibility(8);
    }

    public ShortcutInfo getFinalInfo() {
        ShortcutInfo shortcutInfo = new ShortcutInfo(this.mInfo);
        LauncherAppState.getInstance().getModel().updateDeepShortcutInfo(this.mInfo.mDetail, shortcutInfo);
        if (!Utilities.isNycMR1OrAbove()) {
            shortcutInfo.itemType = 1;
        }
        return shortcutInfo;
    }

    public Point getIconCenter() {
        Point point = sTempPoint;
        int measuredHeight = getMeasuredHeight() / 2;
        point.x = measuredHeight;
        point.y = measuredHeight;
        if (Utilities.isRtl(getResources())) {
            sTempPoint.x = getMeasuredWidth() - sTempPoint.x;
        }
        return sTempPoint;
    }

    public View getIconView() {
        return this.mIconView;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mIconView = (ImageView) this.findViewById(R.id.icon);
        this.mTextView = (TextView) this.findViewById(R.id.text);
        this.mHandleView = this.findViewById(R.id.handle);
        this.mDividerView = this.findViewById(R.id.divider);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0 || action == 2) {
            this.mIconLastTouchPos.set((int) motionEvent.getX(), (int) motionEvent.getY());
        }
        return super.onTouchEvent(motionEvent);
    }

    public void setIconVisible(boolean z) {
        this.mIconView.setVisibility(z ? 0 : 4);
    }

    public void showDivider() {
        this.mDividerView.setVisibility(0);
    }
}
