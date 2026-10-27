package com.voxel.simplesearchlauncher.popup;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.util.Themes;
import com.voxel.simplesearchlauncher.notification.NotificationContent;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
@TargetApi(21)
public class NotificationItemView extends FrameLayout {
    private int mBackgroundColor;
    private NotificationContent mNotificationContent;
    private SwipeHelper.Callback mSwipeCallback;

    ViewGroup mTextAndBackground;

    TextView mTextView;

    TextView mTitleView;

    public NotificationItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationItemView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSwipeCallback = new SwipeHelper.Callback() { // from class: com.voxel.simplesearchlauncher.popup.NotificationItemView.1
            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public boolean canChildBeDismissed(View view) {
                return NotificationItemView.this.mNotificationContent != null && NotificationItemView.this.mNotificationContent.dismissable;
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public View getChildAtPosition(MotionEvent motionEvent) {
                return NotificationItemView.this;
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public float getFalsingThresholdFactor() {
                return 1.0f;
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public boolean isAntiFalsingNeeded() {
                return false;
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public void onBeginDrag(View view) {
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public void onChildDismissed(View view) {
                NotificationController.getInstance(NotificationItemView.this.getContext()).dismissNotification(NotificationItemView.this.mNotificationContent.notificationKey);
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public void onChildSnappedBack(View view, float f) {
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public void onDragCancelled(View view) {
            }

            @Override // com.voxel.simplesearchlauncher.popup.SwipeHelper.Callback
            public boolean updateSwipeProgress(View view, boolean z, float f) {
                return true;
            }
        };
    }

    public void applyNotificationInfo(NotificationContent notificationContent, View view) {
        applyNotificationInfo(notificationContent, view, false);
    }

    public void applyNotificationInfo(NotificationContent notificationContent, View view, boolean z) {
        this.mNotificationContent = notificationContent;
        CharSequence charSequence = this.mNotificationContent.title;
        CharSequence charSequence2 = this.mNotificationContent.text;
        if (TextUtils.isEmpty(charSequence) || TextUtils.isEmpty(charSequence2)) {
            this.mTitleView.setMaxLines(2);
            TextView textView = this.mTitleView;
            if (TextUtils.isEmpty(charSequence)) {
                charSequence = charSequence2;
            }
            textView.setText(charSequence);
            this.mTextView.setVisibility(8);
        } else {
            this.mTitleView.setText(charSequence);
            this.mTextView.setText(charSequence2);
        }
        view.setBackground(this.mNotificationContent.getIconForBackground(getContext(), this.mBackgroundColor));
        PendingIntent pendingIntent = this.mNotificationContent.intent;
        setTranslationX(0.0f);
        setTag(new ItemInfo());
        if (z) {
            ObjectAnimator.ofFloat(this.mTextAndBackground, (Property) ALPHA /* REWORK: raw-ified; Property is invariant in its target type */, 0.0f, 1.0f).setDuration(150L).start();
        }
    }

    public NotificationContent getNotificationContent() {
        return this.mNotificationContent;
    }

    public SwipeHelper.Callback getSwipeHelperCallback() {
        return this.mSwipeCallback;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTextAndBackground = (ViewGroup) this.findViewById(R.id.text_and_background);
        this.mTitleView = (TextView) this.findViewById(R.id.title);
        this.mTextView = (TextView) this.findViewById(R.id.text);
        ColorDrawable colorDrawable = (ColorDrawable) this.mTextAndBackground.getBackground();
        this.mBackgroundColor = colorDrawable.getColor();
        this.mTextAndBackground.setBackground(new RippleDrawable(ColorStateList.valueOf(Themes.getAttrColor(getContext(), android.R.attr.colorControlHighlight)), colorDrawable, null));
    }
}
