package com.voxel.simplesearchlauncher.popup;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationItemView_ViewBinding implements Unbinder {
    private NotificationItemView target;

    public NotificationItemView_ViewBinding(NotificationItemView notificationItemView, View view) {
        this.target = notificationItemView;
        notificationItemView.mTextAndBackground = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.text_and_background, "field 'mTextAndBackground'", ViewGroup.class);
        notificationItemView.mTitleView = (TextView) Utils.findRequiredViewAsType(view, R.id.title, "field 'mTitleView'", TextView.class);
        notificationItemView.mTextView = (TextView) Utils.findRequiredViewAsType(view, R.id.text, "field 'mTextView'", TextView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        NotificationItemView notificationItemView = this.target;
        if (notificationItemView == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        notificationItemView.mTextAndBackground = null;
        notificationItemView.mTitleView = null;
        notificationItemView.mTextView = null;
    }
}
