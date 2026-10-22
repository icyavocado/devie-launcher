package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.voxel.launcher3.LauncherAnimUtils;
import com.voxel.launcher3.LogAccelerateInterpolator;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.anim.PropertyListBuilder;
import com.voxel.launcher3.anim.PropertyResetListener;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.graphics.TriangleShape;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.simplesearchlauncher.notification.NotificationContent;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class PopupContainerWithArrow extends LinearLayout {
    private ActionHandler mActionHandler;
    private View mArrow;
    private float mArrowOffset;
    protected boolean mIsAboveIcon;
    private boolean mIsLeftAligned;
    private boolean mIsRtl;
    NotificationGroupView mNotificationGroupView;
    protected Animator mOpenCloseAnimator;
    private AnimatorSet mReduceHeightAnimatorSet;
    ShortcutsGroupView mShortcutsGroupView;
    private final Rect mTempRect;

    public interface ActionHandler {
        void registerNotificationActions(NotificationItemView notificationItemView);

        void registerShortcutActions(View view);
    }

    public PopupContainerWithArrow(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PopupContainerWithArrow(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTempRect = new Rect();
        this.mIsRtl = Utilities.isRtl(getResources());
    }

    private View addArrowView(int i, int i2, int i3, int i4) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i3, i4);
        if (this.mIsLeftAligned) {
            layoutParams.gravity = 3;
            layoutParams.leftMargin = i;
        } else {
            layoutParams.gravity = 5;
            layoutParams.rightMargin = i;
        }
        if (this.mIsAboveIcon) {
            layoutParams.topMargin = i2;
        } else {
            layoutParams.bottomMargin = i2;
        }
        View view = new View(getContext());
        if (Gravity.isVertical(((FrameLayout.LayoutParams) getLayoutParams()).gravity)) {
            view.setVisibility(4);
        } else {
            ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.create(i3, i4, !this.mIsAboveIcon));
            Paint paint = shapeDrawable.getPaint();
            paint.setColor(((PopupGroupBaseView) getChildAt(this.mIsAboveIcon ? getChildCount() - 1 : 0)).getArrowColor(this.mIsAboveIcon));
            paint.setPathEffect(new CornerPathEffect(getResources().getDimensionPixelSize(R.dimen.popup_arrow_corner_radius)));
            view.setBackground(shapeDrawable);
            if (Utilities.ATLEAST_LOLLIPOP) {
                view.setElevation(getElevation());
            }
        }
        addView(view, this.mIsAboveIcon ? getChildCount() : 0, layoutParams);
        return view;
    }

    private void addDummyViews(List<PopupFragment.Item> list, int i) {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.popup_items_spacing);
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(getContext());
        int size = list.size();
        int i2 = 0;
        while (i2 < size) {
            PopupFragment.Item item = list.get(i2);
            View view = null;
            PopupFragment.Item item2 = i2 < size + (-1) ? list.get(i2 + 1) : null;
            if (item == PopupFragment.Item.NOTIFICATION) {
                this.mNotificationGroupView = (NotificationGroupView) layoutInflaterFrom.inflate(R.layout.popup_notification_group, (ViewGroup) this, false);
                addView(this.mNotificationGroupView);
                NotificationGroupView notificationGroupView = this.mNotificationGroupView;
                if (i == 1) {
                    notificationGroupView.setFooterVisibility(8);
                }
                this.mActionHandler.registerNotificationActions(this.mNotificationGroupView.getMainItemView());
                view = notificationGroupView;
            } else if (item.isShortcut) {
                if (this.mShortcutsGroupView == null) {
                    this.mShortcutsGroupView = (ShortcutsGroupView) layoutInflaterFrom.inflate(R.layout.popup_shortcuts_group, (ViewGroup) this, false);
                    addView(this.mShortcutsGroupView);
                }
                ShortcutsGroupView shortcutsGroupView = this.mShortcutsGroupView;
                View viewInflate = layoutInflaterFrom.inflate(item.layoutId, (ViewGroup) this, false);
                this.mShortcutsGroupView.addShortcutView(viewInflate, item);
                this.mActionHandler.registerShortcutActions(viewInflate);
                view = shortcutsGroupView;
            }
            boolean z = item2 != null && (item.isShortcut ^ item2.isShortcut);
            if (view != null && z) {
                ((LinearLayout.LayoutParams) view.getLayoutParams()).bottomMargin = dimensionPixelSize;
            }
            i2++;
        }
    }

    private void animateOpen() {
        setVisibility(0);
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        int itemCount = getItemCount();
        long integer = getResources().getInteger(R.integer.config_deepShortcutOpenDuration);
        long integer2 = getResources().getInteger(R.integer.config_deepShortcutArrowOpenDuration);
        long j = integer - integer2;
        long integer3 = getResources().getInteger(R.integer.config_deepShortcutOpenStagger);
        LogAccelerateInterpolator logAccelerateInterpolator = new LogAccelerateInterpolator(100, 0);
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
        int i = 0;
        while (i < itemCount) {
            final PopupGroupBaseView itemViewAt = getItemViewAt(i);
            long j2 = integer2;
            itemViewAt.setVisibility(4);
            itemViewAt.setAlpha(0.0f);
            Animator animatorCreateOpenAnimation = itemViewAt.createOpenAnimation(this.mIsAboveIcon, this.mIsLeftAligned);
            animatorCreateOpenAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    itemViewAt.setVisibility(0);
                }
            });
            animatorCreateOpenAnimation.setDuration(integer);
            animatorCreateOpenAnimation.setStartDelay(((long) (this.mIsAboveIcon ? (itemCount - i) - 1 : i)) * integer3);
            animatorCreateOpenAnimation.setInterpolator(decelerateInterpolator);
            animatorSetCreateAnimatorSet.play(animatorCreateOpenAnimation);
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(itemViewAt, (Property) View.ALPHA /* REWORK: raw-ified; Property is invariant in its target type */, 1.0f);
            objectAnimatorOfFloat.setInterpolator(logAccelerateInterpolator);
            j = j;
            objectAnimatorOfFloat.setDuration(j);
            animatorSetCreateAnimatorSet.play(objectAnimatorOfFloat);
            i++;
            integer2 = j2;
        }
        animatorSetCreateAnimatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PopupContainerWithArrow.this.mOpenCloseAnimator = null;
            }
        });
        this.mArrow.setScaleX(0.0f);
        this.mArrow.setScaleY(0.0f);
        ObjectAnimator duration = createArrowScaleAnim(1.0f).setDuration(integer2);
        duration.setStartDelay(j);
        animatorSetCreateAnimatorSet.play(duration);
        this.mOpenCloseAnimator = animatorSetCreateAnimatorSet;
        animatorSetCreateAnimatorSet.start();
    }

    private ObjectAnimator createArrowScaleAnim(float f) {
        return LauncherAnimUtils.ofPropertyValuesHolder(this.mArrow, new PropertyListBuilder().scale(f).build());
    }

    private boolean isAlignedWithStart() {
        return (this.mIsLeftAligned && !this.mIsRtl) || (!this.mIsLeftAligned && this.mIsRtl);
    }

    public void lambda$trimNotifications$2(List list, int i) {
        if (this.mNotificationGroupView == null) {
            return;
        }
        if (list.size() != 0) {
            this.mNotificationGroupView.trimNotifications(this, list, i);
            return;
        }
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        int integer = this.getResources().getInteger(R.integer.config_removeNotificationViewDuration);
        final int dimensionPixelSize = this.getResources().getDimensionPixelSize(R.dimen.popup_items_spacing);
        animatorSetCreateAnimatorSet.play(this.reduceNotificationViewHeight(this.mNotificationGroupView.getHeightMinusFooter() + dimensionPixelSize, integer));
        final PopupGroupBaseView itemViewAt = this.mIsAboveIcon ? this.getItemViewAt(this.getItemCount() - 2) : this.mNotificationGroupView;
        if (itemViewAt != null) {
            ValueAnimator duration = ValueAnimator.ofFloat(1.0f, 0.0f).setDuration(integer);
            duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupContainerWithArrow$u5WyyFnnRRHDSJY5bPVx_qKeJVU
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ((ViewGroup.MarginLayoutParams) itemViewAt.getLayoutParams()).bottomMargin = (int) (dimensionPixelSize * ((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            animatorSetCreateAnimatorSet.play(duration);
        }
        ObjectAnimator duration2 = ObjectAnimator.ofFloat(this.mNotificationGroupView, (Property) ALPHA /* REWORK: raw-ified; Property is invariant in its target type */, 0.0f).setDuration(integer);
        duration2.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PopupContainerWithArrow popupContainerWithArrow2 = PopupContainerWithArrow.this;
                popupContainerWithArrow2.removeView(popupContainerWithArrow2.mNotificationGroupView);
                PopupContainerWithArrow popupContainerWithArrow3 = PopupContainerWithArrow.this;
                popupContainerWithArrow3.mNotificationGroupView = null;
                if (popupContainerWithArrow3.getItemCount() == 0) {
                    PopupContainerWithArrow.this.handleClose(false);
                }
            }
        });
        animatorSetCreateAnimatorSet.play(duration2);
        long integer2 = this.getResources().getInteger(R.integer.config_deepShortcutArrowOpenDuration);
        ObjectAnimator duration3 = this.createArrowScaleAnim(0.0f).setDuration(integer2);
        duration3.setStartDelay(0L);
        ObjectAnimator duration4 = this.createArrowScaleAnim(1.0f).setDuration(integer2);
        double d = integer;
        double d2 = integer2;
        Double.isNaN(d2);
        Double.isNaN(d);
        duration4.setStartDelay((long) (d - (d2 * 1.5d)));
        animatorSetCreateAnimatorSet.playSequentially(duration3, duration4);
        animatorSetCreateAnimatorSet.start();
    }

    public void lambda$updateNotifications$0(List list, int i) {
        NotificationGroupView notificationGroupView = this.mNotificationGroupView;
        if (notificationGroupView != null) {
            notificationGroupView.applyNotificationContents(list, i);
        }
    }

    private void orientAboutIcon(View view, DragLayer dragLayer, int i) {
        Drawable drawable;
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight() + i;
        dragLayer.getDescendantRectRelativeToSelf(view, this.mTempRect);
        Rect insets = dragLayer.getInsets();
        int width = (int) (view.getWidth() * view.getScaleX());
        Resources resources = getResources();
        int i2 = width / 2;
        int dimensionPixelSize = (i2 - (resources.getDimensionPixelSize(R.dimen.deep_shortcut_icon_size) / 2)) - resources.getDimensionPixelSize(R.dimen.popup_padding_start);
        int dimensionPixelSize2 = (i2 - (resources.getDimensionPixelSize(R.dimen.deep_shortcut_drag_handle_size) / 2)) - resources.getDimensionPixelSize(R.dimen.popup_padding_end);
        int i3 = this.mTempRect.left + dimensionPixelSize;
        int i4 = (this.mTempRect.right - measuredWidth) - dimensionPixelSize2;
        float f = i3;
        boolean z = i3 + measuredWidth < dragLayer.getRight() - insets.right;
        boolean z2 = i4 > dragLayer.getLeft() + insets.left;
        float width2 = ((!z2 || z) && (!this.mIsRtl || (!z2 && z))) ? f : i4;
        this.mIsLeftAligned = width2 == f;
        if (this.mIsRtl) {
            width2 -= dragLayer.getWidth() - measuredWidth;
        }
        float paddingTop = (this.mTempRect.top + view.getPaddingTop()) - measuredHeight;
        this.mIsAboveIcon = paddingTop > ((float) (dragLayer.getTop() + insets.top));
        if (!this.mIsAboveIcon) {
            int height = view.getHeight();
            if ((view instanceof TextView) && (drawable = ((TextView) view).getCompoundDrawables()[1]) != null) {
                height = drawable.getBounds().height();
            }
            paddingTop = this.mTempRect.top + view.getPaddingTop() + height;
        }
        float right = this.mIsRtl ? width2 + insets.right : width2 - insets.left;
        float f2 = paddingTop - insets.top;
        ((FrameLayout.LayoutParams) getLayoutParams()).gravity = 0;
        if (f2 < dragLayer.getTop() || measuredHeight + f2 > dragLayer.getBottom()) {
            ((FrameLayout.LayoutParams) getLayoutParams()).gravity = 16;
            int i5 = (i3 + width) - insets.left;
            int i6 = (i4 - width) - insets.left;
            if (this.mIsRtl) {
                if (i6 > dragLayer.getLeft()) {
                    this.mIsLeftAligned = false;
                    right = i6;
                } else {
                    this.mIsLeftAligned = true;
                    right = i5;
                }
            } else if (i5 + measuredWidth < dragLayer.getRight()) {
                this.mIsLeftAligned = true;
                right = i5;
            } else {
                this.mIsLeftAligned = false;
                right = i6;
            }
            this.mIsAboveIcon = true;
        }
        if (right < dragLayer.getLeft()) {
            this.mArrowOffset = right - dragLayer.getLeft();
            right = dragLayer.getLeft();
        } else {
            float f3 = measuredWidth + right;
            if (f3 > dragLayer.getRight()) {
                this.mArrowOffset = f3 - dragLayer.getRight();
                right = dragLayer.getRight() - measuredWidth;
            }
        }
        int i7 = ((FrameLayout.LayoutParams) getLayoutParams()).gravity;
        if (!Gravity.isHorizontal(i7)) {
            setX(right);
        }
        if (Gravity.isVertical(i7)) {
            return;
        }
        setY(f2);
    }

    protected void animateClose(Animator.AnimatorListener animatorListener) {
        Animator animator = this.mOpenCloseAnimator;
        if (animator != null) {
            animator.cancel();
        }
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        int itemCount = getItemCount();
        int i = 0;
        for (int i2 = 0; i2 < itemCount; i2++) {
            if (getItemViewAt(i2).isOpenOrOpening()) {
                i++;
            }
        }
        long integer = getResources().getInteger(R.integer.config_deepShortcutCloseDuration);
        long integer2 = getResources().getInteger(R.integer.config_deepShortcutArrowOpenDuration);
        long integer3 = getResources().getInteger(R.integer.config_deepShortcutCloseStagger);
        LogAccelerateInterpolator logAccelerateInterpolator = new LogAccelerateInterpolator(100, 0);
        int i3 = this.mIsAboveIcon ? itemCount - i : 0;
        int i4 = i3;
        while (i4 < i3 + i) {
            final PopupGroupBaseView itemViewAt = getItemViewAt(i4);
            Animator animatorCreateCloseAnimation = itemViewAt.createCloseAnimation(this.mIsAboveIcon, this.mIsLeftAligned, integer);
            AnimatorSet animatorSet = animatorSetCreateAnimatorSet;
            long j = ((long) (this.mIsAboveIcon ? i4 - i3 : (i - i4) - 1)) * integer3;
            animatorCreateCloseAnimation.setStartDelay(j);
            int i5 = i3;
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(itemViewAt, (Property) View.ALPHA /* REWORK: raw-ified; Property is invariant in its target type */, 0.0f);
            objectAnimatorOfFloat.setStartDelay(j + integer2);
            objectAnimatorOfFloat.setDuration(integer - integer2);
            objectAnimatorOfFloat.setInterpolator(logAccelerateInterpolator);
            animatorSet.play(objectAnimatorOfFloat);
            animatorCreateCloseAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator2) {
                    itemViewAt.setVisibility(4);
                }
            });
            animatorSet.play(animatorCreateCloseAnimation);
            i4++;
            animatorSetCreateAnimatorSet = animatorSet;
            i3 = i5;
        }
        AnimatorSet animatorSet2 = animatorSetCreateAnimatorSet;
        ObjectAnimator duration = createArrowScaleAnim(0.0f).setDuration(integer2);
        duration.setStartDelay(0L);
        animatorSet2.play(duration);
        if (animatorListener != null) {
            animatorSet2.addListener(animatorListener);
        }
        animatorSet2.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator2) {
                PopupContainerWithArrow popupContainerWithArrow = PopupContainerWithArrow.this;
                popupContainerWithArrow.mOpenCloseAnimator = null;
                popupContainerWithArrow.closeComplete();
            }
        });
        this.mOpenCloseAnimator = animatorSet2;
        animatorSet2.start();
    }

    protected void closeComplete() {
        Animator animator = this.mOpenCloseAnimator;
        if (animator != null) {
            animator.cancel();
            this.mOpenCloseAnimator = null;
        }
        this.mActionHandler = null;
    }

    protected int getItemCount() {
        return getChildCount() - 1;
    }

    protected PopupGroupBaseView getItemViewAt(int i) {
        if (!this.mIsAboveIcon) {
            i++;
        }
        return (PopupGroupBaseView) getChildAt(i);
    }

    protected void handleClose(boolean z) {
        if (z) {
            animateClose(null);
        } else {
            closeComplete();
        }
    }

    public void populateAndShow(ActionHandler actionHandler, View view, DragLayer dragLayer, List<PopupFragment.Item> list, int i) {
        this.mActionHandler = actionHandler;
        Resources resources = getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.popup_arrow_width);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.popup_arrow_height);
        int dimensionPixelSize3 = resources.getDimensionPixelSize(R.dimen.popup_arrow_vertical_offset);
        addDummyViews(list, i);
        measure(0, 0);
        int i2 = dimensionPixelSize2 + dimensionPixelSize3;
        orientAboutIcon(view, dragLayer, i2);
        if (this.mIsAboveIcon) {
            removeAllViews();
            this.mShortcutsGroupView = null;
            this.mNotificationGroupView = null;
            ArrayList arrayList = new ArrayList(list);
            Collections.reverse(arrayList);
            addDummyViews(arrayList, i);
            measure(0, 0);
            orientAboutIcon(view, dragLayer, i2);
        }
        this.mArrow = addArrowView(Math.round(resources.getDimensionPixelSize(isAlignedWithStart() ? R.dimen.popup_arrow_horizontal_offset_start : R.dimen.popup_arrow_horizontal_offset_end) + this.mArrowOffset), dimensionPixelSize3, dimensionPixelSize, dimensionPixelSize2);
        this.mArrow.setPivotX(dimensionPixelSize / 2);
        this.mArrow.setPivotY(this.mIsAboveIcon ? 0.0f : dimensionPixelSize2);
        animateOpen();
    }

    public Animator reduceNotificationViewHeight(int i, int i2) {
        AnimatorSet animatorSet = this.mReduceHeightAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        final int i3 = this.mIsAboveIcon ? i : -i;
        this.mReduceHeightAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        this.mReduceHeightAnimatorSet.play(this.mNotificationGroupView.animateHeightRemoval(i));
        PropertyResetListener propertyResetListener = new PropertyResetListener(TRANSLATION_Y, Float.valueOf(0.0f));
        for (int i4 = 0; i4 < getItemCount(); i4++) {
            PopupGroupBaseView itemViewAt = getItemViewAt(i4);
            if (this.mIsAboveIcon || itemViewAt != this.mNotificationGroupView) {
                ObjectAnimator duration = ObjectAnimator.ofFloat(itemViewAt, (Property) TRANSLATION_Y /* REWORK: raw-ified; Property is invariant */, itemViewAt.getTranslationY() + i3).setDuration(i2);
                duration.addListener(propertyResetListener);
                this.mReduceHeightAnimatorSet.play(duration);
            }
        }
        this.mReduceHeightAnimatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupContainerWithArrow.6
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (PopupContainerWithArrow.this.mIsAboveIcon) {
                    PopupContainerWithArrow popupContainerWithArrow = PopupContainerWithArrow.this;
                    popupContainerWithArrow.setTranslationY(popupContainerWithArrow.getTranslationY() + i3);
                }
                PopupContainerWithArrow.this.mReduceHeightAnimatorSet = null;
            }
        });
        return this.mReduceHeightAnimatorSet;
    }

    public void trimNotifications(Handler handler, final List<String> list, final int i) {
        handler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupContainerWithArrow$jWh2EVQ0AeLQdr54LzLSgBd-bJI
            @Override // java.lang.Runnable
            public final void run() {
                PopupContainerWithArrow.this.lambda$trimNotifications$2( list, i);
            }
        });
    }

    public void updateNotifications(Handler handler, final List<NotificationContent> list, final int i) {
        handler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupContainerWithArrow$m1fYGFhCzYm8Y5Gf9Iy-dxXCMJc
            @Override // java.lang.Runnable
            public final void run() {
                PopupContainerWithArrow.this.lambda$updateNotifications$0( list, i);
            }
        });
    }

    public void updateShortcuts(Handler handler, DeepShortcutManager deepShortcutManager, List<DeepShortcutInfoCompat> list, List<SystemShortcut> list2) {
        if (this.mShortcutsGroupView == null) {
            return;
        }
        if (this.mIsAboveIcon) {
            Collections.reverse(list);
        }
        /* REWORK: jadx made the loop vars final while the posted runnables capture them;
         * snapshot per pass. */
        int i = 0;
        while (i < list.size()) {
            final int icap = i;
            final PopupFragment.UnbadgedShortcutInfo unbadgedShortcutInfo = new PopupFragment.UnbadgedShortcutInfo(list.get(icap), deepShortcutManager, getContext());
            unbadgedShortcutInfo.rank = icap;
            handler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupContainerWithArrow$5iGFVxGZJecrocbG8TgOCqasCcU
                @Override // java.lang.Runnable
                public final void run() {
                    PopupContainerWithArrow.this.mShortcutsGroupView.applyShortcutInfo(icap, unbadgedShortcutInfo);
                }
            });
            i++;
        }
        int i2 = 0;
        while (i2 < list2.size()) {
            final int i2cap = i2;
            final SystemShortcut systemShortcut = list2.get(i2cap);
            handler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupContainerWithArrow$9y1C-7i1i7GPXtIWJKI3-ei3goo
                @Override // java.lang.Runnable
                public final void run() {
                    PopupContainerWithArrow.this.mShortcutsGroupView.applySystemShortcut(i2cap, systemShortcut);
                }
            });
            i2++;
        }
    }
}
