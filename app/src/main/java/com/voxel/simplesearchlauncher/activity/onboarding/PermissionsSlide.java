package com.voxel.simplesearchlauncher.activity.onboarding;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.activity.OnboardingActivity;
import com.voxel.simplesearchlauncher.view.Slide;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class PermissionsSlide extends Slide implements ActivityCompat.OnRequestPermissionsResultCallback {
    private int failures;
    private OnboardingActivity mActivity;
    private PermissionItem mContactsItem;
    private final Button mNegativeButton;
    private final View mNegativeButtonLayout;
    private HashMap<String, PermissionItem> mPermissionItemsMap;
    private final Button mPositiveButton;
    private SlideLayout mSlideLayout;
    private PermissionItem mStorageItem;
    // REWORK: jadx dropped this field declaration; the location permission item
    // is created and registered alongside the storage item.
    private PermissionItem mLocationItem;

    /* JADX INFO: Access modifiers changed from: private */
    class PermissionItem {
        CheckBox checkBox;
        boolean hasFailedGrant = false;
        ViewGroup itemView;
        TextView text;
        TextView tipText;
        View tipView;
        TextView title;

        PermissionItem(View view, int i, int i2, int i3) {
            this.itemView = (ViewGroup) view;
            this.title = (TextView) this.itemView.findViewById(R.id.onboarding_permission_title);
            this.text = (TextView) this.itemView.findViewById(R.id.onboarding_permission_text);
            this.checkBox = (CheckBox) this.itemView.findViewById(R.id.onboarding_permission_checkbox);
            this.tipView = this.itemView.findViewById(R.id.onboarding_permission_tip);
            this.tipText = (TextView) this.tipView.findViewById(R.id.onboarding_permission_tip_text);
            this.title.setText(i);
            this.text.setText(i2);
            Resources resources = view.getResources();
            this.tipText.setText(resources.getString(R.string.permission_tip, resources.getString(i3)));
            this.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.activity.onboarding.-$$Lambda$PermissionsSlide$PermissionItem$D9h-NQ5hjr5ryGqmynXGuPLgkB0
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    PermissionsSlide.PermissionItem.lambda$new$0(PermissionItem.this, PermissionsSlide.this, compoundButton, z); /* REWORK: jadx dropped the captured outer receiver */
                }
            });
        }

        public static /* synthetic */ void lambda$new$0(PermissionItem permissionItem, PermissionsSlide permissionsSlide, CompoundButton compoundButton, boolean z) {
            permissionItem.updateTipText(z);
            permissionsSlide.updateButtons();
        }

        private void updateTipText(boolean z) {
            if (z && this.hasFailedGrant) {
                this.tipView.setVisibility(0);
            } else {
                this.tipView.setVisibility(8);
            }
        }
    }

    public PermissionsSlide(OnboardingActivity onboardingActivity, SlideLayout slideLayout) {
        super(onboardingActivity.getLayoutInflater().inflate(R.layout.onboarding_permission_slide, (ViewGroup) slideLayout, false));
        this.failures = 0;
        this.mActivity = onboardingActivity;
        this.mSlideLayout = slideLayout;
        ViewGroup viewGroup = (ViewGroup) this.mView.findViewById(R.id.onboarding_permission_item_layout);
        viewGroup.setLayoutTransition(new LayoutTransition());
        viewGroup.getLayoutTransition().enableTransitionType(4);
        this.mStorageItem = new PermissionItem(this.mView.findViewById(R.id.onboarding_permission_storage_item), R.string.permission_storage_title, R.string.permission_storage_text, R.string.permission_storage_name);
        this.mLocationItem = new PermissionItem(this.mView.findViewById(R.id.onboarding_permission_location_item), R.string.permission_location_title, R.string.permission_location_text, R.string.permission_location_name);
        this.mContactsItem = new PermissionItem(this.mView.findViewById(R.id.onboarding_permission_contacts_item), R.string.permission_contacts_title, R.string.permission_contacts_text, R.string.permission_contacts_name);
        this.mPermissionItemsMap = new HashMap<>();
        this.mPermissionItemsMap.put("android.permission.READ_CONTACTS", this.mContactsItem);
        if (Utilities.isAtLeast81()) {
            this.mStorageItem.itemView.setVisibility(0);
            this.mStorageItem.checkBox.setEnabled(false);
            this.mStorageItem.checkBox.setChecked(true);
            this.mPermissionItemsMap.put("android.permission.READ_EXTERNAL_STORAGE", this.mStorageItem);
            /* REWORK: jadx dropped the registration of the location item (only the
               field's declaration survived); without it the storage/location row's
               Grant Access button never requested location. */
            this.mPermissionItemsMap.put("android.permission.ACCESS_FINE_LOCATION", this.mLocationItem);
        }
        /* REWORK: on API 33+ the legacy storage permission is always denied for
           targetSdk 34+ apps (scoped storage); the launcher's storage needs are
           media reads only, so drop the dead row there. */
        if (Utilities.isAtLeastT()) {
            PermissionItem storageItem = this.mPermissionItemsMap.remove("android.permission.READ_EXTERNAL_STORAGE");
            if (storageItem != null) {
                storageItem.itemView.setVisibility(8);
            }
        }
        this.mPositiveButton = (Button) this.mView.findViewById(R.id.onboarding_permission_positive_button);
        this.mNegativeButton = (Button) this.mView.findViewById(R.id.onboarding_permission_negative_button);
        this.mNegativeButtonLayout = this.mView.findViewById(R.id.onboarding_permission_negative_button_layout);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.activity.onboarding.-$$Lambda$PermissionsSlide$OMu1yveBGdnjtGbWsqVq1y-lQVw
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                PermissionsSlide.this.lambda$new$0( view);
            }
        });
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.activity.onboarding.-$$Lambda$PermissionsSlide$qSr9qO_DhRroLMPEx9ZzG9hWuxc
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                PermissionsSlide.this.lambda$new$1( view);
            }
        });
        this.mActivity.setOnRequestPermissionsResultCallback(this);
        for (String str : this.mPermissionItemsMap.keySet()) {
            updateItemForGrantStatus(str, ContextCompat.checkSelfPermission(this.mActivity, str));
        }
    }

    public void lambda$new$0(View view) {
        if (this.mPositiveButton.getVisibility() == 0) {
            this.requestPermissions();
        }
    }

    public void lambda$new$1(View view) {
        if (this.mNegativeButtonLayout.getVisibility() == 0) {
            this.nextSlide();
            this.mPositiveButton.setOnClickListener(null);
            this.mNegativeButton.setOnClickListener(null);
        }
    }

    public static boolean needsPermissions(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add("android.permission.READ_CONTACTS");
        /* REWORK: legacy READ_EXTERNAL_STORAGE never grants on targetSdk 34+ (API 33
           devices+); storage access goes through SAF, so don't gate on it. */
        if (Utilities.isAtLeast81() && !Utilities.isAtLeastT()) {
            arrayList.add("android.permission.READ_EXTERNAL_STORAGE");
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (ContextCompat.checkSelfPermission(context, (String) it.next()) != 0) {
                return true;
            }
        }
        return false;
    }

    private void nextSlide() {
        if (this.mSlideLayout.indexOfSlide(this) < this.mSlideLayout.getChildCount() - 1) {
            this.mSlideLayout.showNext();
        } else {
            this.mActivity.finish();
        }
    }

    private void requestPermissions() {
        ArrayList arrayList = new ArrayList();
        for (String str : this.mPermissionItemsMap.keySet()) {
            if (this.mPermissionItemsMap.get(str).checkBox.isChecked() && ContextCompat.checkSelfPermission(this.mActivity, str) != 0) {
                arrayList.add(str);
            }
        }
        if (arrayList.size() > 0) {
            ActivityCompat.requestPermissions(this.mActivity, (String[]) arrayList.toArray(new String[arrayList.size()]), 1);
        } else {
            updateButtons();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateButtons() {
        boolean z = true;
        boolean z2 = false;
        for (PermissionItem permissionItem : this.mPermissionItemsMap.values()) {
            if (permissionItem.itemView.getVisibility() == 0) {
                if (permissionItem.checkBox.isChecked()) {
                    z2 = true;
                } else {
                    z = false;
                }
            }
        }
        if (z || z2) {
            this.mPositiveButton.setVisibility(0);
            if (this.failures >= 2) {
                this.mNegativeButtonLayout.setVisibility(0);
            } else {
                this.mNegativeButtonLayout.setVisibility(8);
            }
        } else {
            this.mPositiveButton.setVisibility(8);
            this.mNegativeButtonLayout.setVisibility(0);
        }
        if (z) {
            this.mPositiveButton.setText(R.string.onboarding_permission_grant_access);
        } else if (z2) {
            this.mPositiveButton.setText(R.string.onboarding_permission_grant_limited_access);
        }
    }

    private void updateItemForGrantStatus(String str, int i) {
        PermissionItem permissionItem = this.mPermissionItemsMap.get(str);
        if (permissionItem == null) {
            return;
        }
        if (i == 0) {
            permissionItem.itemView.setVisibility(8);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, str)) {
            permissionItem.tipView.setVisibility(0);
            permissionItem.hasFailedGrant = true;
        }
    }

    @Override // com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideEnteringAnimation(SlideLayout slideLayout) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.ALPHA, 0.0f, 1.0f);
        objectAnimatorOfFloat.setDuration(750L);
        animatorSet.play(objectAnimatorOfFloat);
        return animatorSet;
    }

    @Override // com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideExitingAnimation(SlideLayout slideLayout) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.ALPHA, 1.0f, 0.0f));
        return animatorSet;
    }

    @Override // androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        boolean z = false;
        for (int i2 = 0; i2 < strArr.length && i2 < iArr.length; i2++) {
            String str = strArr[i2];
            updateItemForGrantStatus(str, iArr[i2]);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, str)) {
                z = true;
            }
        }
        if (!z) {
            nextSlide();
        } else {
            this.failures++;
            updateButtons();
        }
    }
}
