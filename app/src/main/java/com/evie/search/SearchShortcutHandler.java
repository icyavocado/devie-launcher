package com.evie.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.widget.LinearLayout;
import com.evie.common.dragndrop.DragSink;
import com.evie.common.dragndrop.ItemInfoCommon;
import com.evie.search.local.LocalSearchSubitem;
import com.evie.search.model.FrequentlyUsedItem;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;
import com.evie.search.model.WebSearchItem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.voxel.simplesearchlauncher.utils.BitmapUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import timber.log.Timber;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SearchShortcutHandler {
    private final SearchAdapter mAdapter;
    private DragSink mDragSink;
    private final PackageManager mPackageManager;
    private final String mPackageName;
    private final Resources mResources;

    public SearchShortcutHandler(SearchAdapter searchAdapter, Context context) {
        this.mAdapter = searchAdapter;
        this.mPackageManager = context.getPackageManager();
        this.mResources = context.getResources();
        this.mPackageName = context.getPackageName();
    }

    void clearDragSink() {
        this.mDragSink = null;
    }

    public boolean createShortcut(View view, ItemInfoCommon itemInfoCommon) {
        DragSink dragSink = this.mDragSink;
        if (dragSink == null) {
            return false;
        }
        dragSink.startDrag(view, itemInfoCommon, this.mAdapter);
        return true;
    }

    public boolean createShortcut(View view, LocalSearchSubitem localSearchSubitem) {
        Intent intent;
        SimpleDraweeView simpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.search_subitem_image);
        if (simpleDraweeView == null) {
            simpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.search_item_image);
        }
        if (localSearchSubitem.getLink().startsWith("tel:")) {
            intent = new Intent("android.intent.action.CALL", Uri.parse(localSearchSubitem.getLink()));
        } else {
            intent = new Intent("android.intent.action.VIEW", Uri.parse(localSearchSubitem.getLink()));
            if (this.mPackageManager.queryIntentActivities(intent, 0).size() == 0) {
                return false;
            }
        }
        Bitmap bitmap = BitmapUtil.getBitmap(simpleDraweeView.getDrawable(), simpleDraweeView.getWidth());
        if (bitmap == null) {
            return false;
        }
        return createShortcut(simpleDraweeView, new ItemInfoCommon(localSearchSubitem.getRecentsLabel(), intent, bitmap, "shortcut"));
    }

    public boolean createShortcut(View view, FrequentlyUsedItem frequentlyUsedItem) {
        try {
            SimpleDraweeView simpleDraweeView = (SimpleDraweeView) ((LinearLayout) view).getChildAt(0);
            Intent intent = frequentlyUsedItem.getIntent();
            ResolveInfo resolveInfoResolveActivity = this.mPackageManager.resolveActivity(intent, 0);
            if (resolveInfoResolveActivity != null) {
                return createShortcut(simpleDraweeView, new ItemInfoCommon(resolveInfoResolveActivity.loadLabel(this.mPackageManager).toString(), intent, BitmapUtil.getBitmap(simpleDraweeView.getDrawable(), simpleDraweeView.getWidth()), "shortcut"));
            }
            Timber.e("Failed to resolve intent for package %s", frequentlyUsedItem.getIntent().getComponent().getPackageName());
            return false;
        } catch (Exception e) {
            Timber.e("Invalid view passed to createShortcut: %s", e.getMessage());
            return false;
        }
    }

    /* JADX WARN: Code duplicated, block: B:21:0x0050  */
    public boolean createShortcut(View view, LocalSearchItem localSearchItem) {
        byte b;
        Intent intent;
        Bitmap bitmap;
        if (localSearchItem.getImageUrl() == null) {
            return false;
        }
        SimpleDraweeView simpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.search_item_image);
        Uri uri = Uri.parse(localSearchItem.getLink());
        String type = localSearchItem.getType();
        int iHashCode = type.hashCode();
        if (iHashCode != -1066417278) {
            if (iHashCode != 1582659341) {
                if (iHashCode == 1669509120 && type.equals("CONTACT")) {
                    b = 0;
                } else {
                    b = -1;
                }
            } else if (type.equals("LOCAL_APP")) {
                b = 1;
            } else {
                b = -1;
            }
        } else if (type.equals("SETTINGS_DEEPLINK")) {
            b = 2;
        } else {
            b = -1;
        }
        String str = null;
        switch (b) {
            case 0:
                str = "shortcut";
                intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse("content://com.android.contacts/contacts/" + uri.getSchemeSpecificPart()));
                break;
            case 1:
                str = "app";
                String[] strArrSplit = uri.getSchemeSpecificPart().split("\\|");
                intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");
                intent.setClassName(strArrSplit[0], strArrSplit[1]);
                break;
            case 2:
                str = "shortcut";
                String schemeSpecificPart = uri.getSchemeSpecificPart();
                if (!schemeSpecificPart.contains("|")) {
                    intent = new Intent(schemeSpecificPart);
                } else {
                    String[] strArrSplit2 = uri.getSchemeSpecificPart().split("\\|");
                    intent = new Intent();
                    intent.setClassName(strArrSplit2[0], strArrSplit2[1]);
                }
                break;
            default:
                intent = null;
                break;
        }
        if (intent == null) {
            return false;
        }
        intent.setFlags(268533760);
        intent.putExtra("com.evie.search.link", localSearchItem.getLink());
        intent.putExtra("com.evie.search.itemLabel", localSearchItem.getLabel());
        intent.putExtra("com.evie.search.itemType", localSearchItem.getType());
        if (localSearchItem.getImageUrl().startsWith("data:image/bmp;base64,")) {
            byte[] bArrDecode = Base64.decode(localSearchItem.getImageUrl().substring(22), 0);
            bitmap = BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length);
        } else {
            bitmap = BitmapUtil.getBitmap(simpleDraweeView.getDrawable(), simpleDraweeView.getWidth());
        }
        if (bitmap == null) {
            return false;
        }
        return createShortcut(simpleDraweeView, new ItemInfoCommon(localSearchItem.getLabel(), intent, bitmap, str));
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:20:0x003f  */
    public boolean createShortcut(View view, RecentItem recentItem) {
        switch (recentItem.getType()) {
            case "CONTACT":
            case "LOCAL_APP":
            case "SETTINGS_DEEPLINK":
                return createShortcut(view, new LocalSearchItem(recentItem));
            case "ACTION_CONTACT":
                return createShortcut(view, new LocalSearchSubitem(recentItem));
            case "WEB_AUTOCOMPLETE":
                return createShortcut(view, new WebSearchItem(false, recentItem.getLabel()));
            default:
                return false;
        }
    }

    public boolean createShortcut(View view, WebSearchItem webSearchItem) {
        View viewFindViewById = view.findViewById(R.id.search_autocomplete_image);
        if (viewFindViewById == null) {
            viewFindViewById = view.findViewById(R.id.search_item_image);
        }
        Intent intent = new Intent("android.intent.action.WEB_SEARCH");
        intent.setPackage("com.google.android.googlequicksearchbox");
        intent.putExtra("query", webSearchItem.getLabel());
        intent.putExtra("com.evie.search.link", webSearchItem.getLink());
        intent.putExtra("com.evie.search.itemLabel", webSearchItem.getLabel());
        intent.putExtra("com.evie.search.itemType", "WEB_AUTOCOMPLETE");
        if (this.mPackageManager.resolveActivity(intent, 0) == null) {
            intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(268468224);
            try {
                intent.setData(Uri.parse("http://www.google.com/search?q=" + URLEncoder.encode(webSearchItem.getLabel(), "UTF-8")));
            } catch (UnsupportedEncodingException unused) {
                Timber.e("Error encoding search query", new Object[0]);
                return false;
            }
        }
        return createShortcut(viewFindViewById, new ItemInfoCommon(webSearchItem.getLabel(), intent, BitmapUtil.getBitmap(this.mResources.getDrawable(R.drawable.autocomplete_shortcut_icon), this.mResources.getDimensionPixelSize(R.dimen.shortcut_icon_dimension)), "shortcut"));
    }

    void setDragSink(DragSink dragSink) {
        this.mDragSink = dragSink;
    }
}
