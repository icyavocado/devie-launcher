package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import butterknife.ButterKnife;
import com.voxel.launcher3.LauncherAnimUtils;
import com.voxel.launcher3.anim.PropertyListBuilder;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class ShortcutsGroupView extends PopupGroupBaseView {
    private final List<ShortcutItemView> mShortcutViews;

    LinearLayout mShortcutsLayout;
    private LinearLayout mSystemShortcutIcons;
    private final List<View> mSystemShortcutViews;

    public ShortcutsGroupView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ShortcutsGroupView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShortcutViews = new ArrayList();
        this.mSystemShortcutViews = new ArrayList();
    }

    private void addShortcutView(View view, PopupFragment.Item item, int i) {
        if (item == PopupFragment.Item.SHORTCUT) {
            this.mShortcutViews.add((ShortcutItemView) view);
        } else {
            this.mSystemShortcutViews.add(view);
        }
        if (item == PopupFragment.Item.SYSTEM_SHORTCUT_ICON) {
            if (this.mSystemShortcutIcons == null) {
                this.mSystemShortcutIcons = (LinearLayout) ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.popup_system_shortcut_icons, (ViewGroup) this.mShortcutsLayout, false);
                this.mShortcutsLayout.addView(this.mSystemShortcutIcons, 0);
            }
            this.mSystemShortcutIcons.addView(view, i);
            return;
        }
        if (this.mShortcutsLayout.getChildCount() > 0) {
            LinearLayout linearLayout = this.mShortcutsLayout;
            View childAt = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
            if (childAt instanceof ShortcutItemView) {
                ((ShortcutItemView) childAt).showDivider();
            }
        }
        this.mShortcutsLayout.addView(view, i);
    }

    public void addShortcutView(View view, PopupFragment.Item item) {
        addShortcutView(view, item, -1);
    }

    public void applyShortcutInfo(int i, PopupFragment.UnbadgedShortcutInfo unbadgedShortcutInfo) {
        if (i >= this.mShortcutViews.size()) {
            return;
        }
        this.mShortcutViews.get(i).applyShortcutInfo(unbadgedShortcutInfo);
    }

    public void applySystemShortcut(int i, SystemShortcut systemShortcut) {
        if (i >= this.mSystemShortcutViews.size()) {
            return;
        }
        View view = this.mSystemShortcutViews.get(i);
        if (view instanceof ShortcutItemView) {
            ((ShortcutItemView) view).applySystemShortcut(systemShortcut);
            return;
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(systemShortcut.getHeaderIcon(getContext()));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            view.setContentDescription(systemShortcut.getLabel(getContext()));
            view.setTag(systemShortcut);
        }
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupGroupBaseView
    public Animator createCloseAnimation(boolean z, boolean z2, long j) {
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        animatorSetCreateAnimatorSet.play(super.createZoomCloseAnimation(null, z, z2, j));
        for (int i = 0; i < this.mShortcutsLayout.getChildCount(); i++) {
            if (this.mShortcutsLayout.getChildAt(i) instanceof ShortcutItemView) {
                View iconView = ((ShortcutItemView) this.mShortcutsLayout.getChildAt(i)).getIconView();
                iconView.setScaleX(1.0f);
                iconView.setScaleY(1.0f);
                animatorSetCreateAnimatorSet.play(LauncherAnimUtils.ofPropertyValuesHolder(iconView, new PropertyListBuilder().scale(0.0f).build()));
            }
        }
        return animatorSetCreateAnimatorSet;
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupGroupBaseView
    public Animator createOpenAnimation(boolean z, boolean z2) {
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        animatorSetCreateAnimatorSet.play(super.createZoomOpenAnimation(null, z, z2));
        for (int i = 0; i < this.mShortcutsLayout.getChildCount(); i++) {
            if (this.mShortcutsLayout.getChildAt(i) instanceof ShortcutItemView) {
                View iconView = ((ShortcutItemView) this.mShortcutsLayout.getChildAt(i)).getIconView();
                iconView.setScaleX(0.0f);
                iconView.setScaleY(0.0f);
                animatorSetCreateAnimatorSet.play(LauncherAnimUtils.ofPropertyValuesHolder(iconView, new PropertyListBuilder().scale(1.0f).build()));
            }
        }
        return animatorSetCreateAnimatorSet;
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupGroupBaseView
    public int getArrowColor(boolean z) {
        return ContextCompat.getColor(getContext(), (z || this.mSystemShortcutIcons == null) ? R.color.popup_background_color : R.color.popup_header_background_color);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
}
