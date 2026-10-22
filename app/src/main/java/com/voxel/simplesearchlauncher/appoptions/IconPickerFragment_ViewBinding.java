package com.voxel.simplesearchlauncher.appoptions;

import android.view.View;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.voxel.recyclerview.widget.SpringRecyclerView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class IconPickerFragment_ViewBinding implements Unbinder {
    private IconPickerFragment target;
    private View view7f0a023c;

    public IconPickerFragment_ViewBinding(final IconPickerFragment iconPickerFragment, View view) {
        this.target = iconPickerFragment;
        iconPickerFragment.mRecyclerView = (SpringRecyclerView) Utils.findRequiredViewAsType(view, R.id.recycler_view, "field 'mRecyclerView'", SpringRecyclerView.class);
        iconPickerFragment.mTitleBar = Utils.findRequiredView(view, R.id.title_bar, "field 'mTitleBar'");
        iconPickerFragment.mSearchBar = Utils.findRequiredView(view, R.id.search_bar, "field 'mSearchBar'");
        iconPickerFragment.mSearchEditText = (EditText) Utils.findRequiredViewAsType(view, R.id.search_src_text, "field 'mSearchEditText'", EditText.class);
        iconPickerFragment.mLoadingIndicator = Utils.findRequiredView(view, R.id.loading, "field 'mLoadingIndicator'");
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.search_button, "method 'onSearchButtonClick'");
        this.view7f0a023c = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new DebouncingOnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.IconPickerFragment_ViewBinding.1
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                iconPickerFragment.onSearchButtonClick();
            }
        });
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        IconPickerFragment iconPickerFragment = this.target;
        if (iconPickerFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        iconPickerFragment.mRecyclerView = null;
        iconPickerFragment.mTitleBar = null;
        iconPickerFragment.mSearchBar = null;
        iconPickerFragment.mSearchEditText = null;
        iconPickerFragment.mLoadingIndicator = null;
        this.view7f0a023c.setOnClickListener(null);
        this.view7f0a023c = null;
    }
}
