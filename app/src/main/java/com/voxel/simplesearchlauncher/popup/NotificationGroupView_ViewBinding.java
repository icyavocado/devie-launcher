package com.voxel.simplesearchlauncher.popup;

import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationGroupView_ViewBinding implements Unbinder {
    private NotificationGroupView target;

    public NotificationGroupView_ViewBinding(NotificationGroupView notificationGroupView, View view) {
        this.target = notificationGroupView;
        notificationGroupView.mHeaderCount = (TextView) Utils.findRequiredViewAsType(view, R.id.notification_count, "field 'mHeaderCount'", TextView.class);
        notificationGroupView.mItemView = (NotificationItemView) Utils.findRequiredViewAsType(view, R.id.main_view, "field 'mItemView'", NotificationItemView.class);
        notificationGroupView.mFooter = (NotificationFooterView) Utils.findRequiredViewAsType(view, R.id.footer, "field 'mFooter'", NotificationFooterView.class);
        notificationGroupView.mIconView = Utils.findRequiredView(view, R.id.popup_item_icon, "field 'mIconView'");
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        NotificationGroupView notificationGroupView = this.target;
        if (notificationGroupView == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        notificationGroupView.mHeaderCount = null;
        notificationGroupView.mItemView = null;
        notificationGroupView.mFooter = null;
        notificationGroupView.mIconView = null;
    }
}
