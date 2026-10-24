package com.voxel.simplesearchlauncher.appoptions;

import timber.log.Timber;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.evie.common.iconpack.IconPackFilter;
import com.ovacoda.devie.images.IconLoader;
import com.evie.common.HybridNetworkFetcherCommon;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.evie.common.HybridNetworkFetcherCommon;
import com.voxel.recyclerview.widget.RecyclerView;
import com.voxel.recyclerview.widget.SpringRecyclerView;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.iconpack.IconPack;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.view.OldEmptyViewHolder;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class ChangeIconFragment extends LauncherStackableFragment {
    private ChangeIconAdapter mAdapter;
    private View mContainer;
    IconPackManager mIconPackManager;
    private ItemInfo mItemInfo;
    private View mLoadingIndicator;
    LocalAppsManager mLocalAppsManager;
    private SpringRecyclerView mRecyclerView;
    private List<Bitmap> mSuggestedIcons;

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.appoptions.ChangeIconFragment$1, reason: invalid class name */
    class AnonymousClass1 extends AsyncTask<Void, Void, LoadIconPackResult> {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context context) {
            this.val$context = context;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public LoadIconPackResult doInBackground(Void... voidArr) {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable eReflow = null;
            Bitmap bitmapCreateIconBitmap;
            AnonymousClass1 anonymousClass1 = null;
            if (ChangeIconFragment.this.getActivity() == null) {
                return null;
            }
            List<IconPack> allIconPacks = ChangeIconFragment.this.mIconPackManager.getAllIconPacks();
            ArrayList arrayList = new ArrayList(allIconPacks.size());
            ComponentName component = ChangeIconFragment.this.mItemInfo.getIntent().getComponent();
            ArrayList arrayList2 = new ArrayList();
            try {
                bitmapCreateIconBitmap = Utilities.createIconBitmap(this.val$context.getPackageManager().getActivityIcon(ChangeIconFragment.this.mItemInfo.getIntent()), this.val$context);
                /* REWORK: jadx wrapped the non-throwing list add in a dead catch. */
                arrayList2.add(bitmapCreateIconBitmap);
            } catch (PackageManager.NameNotFoundException e2) {
                eReflow = e2;
                bitmapCreateIconBitmap = null;
            }
            for (IconPack iconPack : allIconPacks) {
                if (ChangeIconFragment.this.getActivity() == null) {
                    break;
                }
                if (!iconPack.isSystemDefault()) {
                    IconPackFilter iconPackFilter = new IconPackFilter(this.val$context, iconPack.getPackageName(), iconPack.getVersionCode());
                    if (iconPackFilter.getDrawableList().size() != 0) {
                        arrayList.add(new Pair(iconPack, iconPackFilter));
                    }
                    Bitmap iconForPackage = iconPackFilter.getIconForPackage(component.getPackageName(), component.getClassName(), bitmapCreateIconBitmap, Utilities.getIconSize(this.val$context));
                    if (bitmapCreateIconBitmap != iconForPackage && iconForPackage != null) {
                        arrayList2.add(iconForPackage);
                    }
                    iconPackFilter.releaseMemory();
                }
            }
            LoadIconPackResult loadIconPackResult = new LoadIconPackResult(ChangeIconFragment.this, anonymousClass1);
            loadIconPackResult.mIconPackFilters = arrayList;
            loadIconPackResult.mSuggestedIcons = arrayList2;
            return loadIconPackResult;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(LoadIconPackResult loadIconPackResult) {
            if (ChangeIconFragment.this.getActivity() == null) {
                return;
            }
            ChangeIconFragment changeIconFragment = ChangeIconFragment.this;
            changeIconFragment.mAdapter = changeIconFragment.new ChangeIconAdapter(loadIconPackResult);
            ChangeIconFragment.this.mRecyclerView.setAdapter(ChangeIconFragment.this.mAdapter);
            ChangeIconFragment.this.mLoadingIndicator.animate().alpha(0.0f).withLayer().withEndAction(new Runnable() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$ChangeIconFragment$1$9n4K7cgCa11st_oxeGbZrVr9Lto
                @Override // java.lang.Runnable
                public final void run() {
                    ChangeIconFragment.this.mLoadingIndicator.setVisibility(8);
                }
            });
            ChangeIconFragment.this.mContainer.setVisibility(0);
            ChangeIconFragment.this.mContainer.setAlpha(0.0f);
            ChangeIconFragment.this.mContainer.animate().alpha(1.0f).withLayer();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class ChangeIconAdapter extends RecyclerView.Adapter implements SpringRecyclerView.SpringItemPositioner {
        private final View.OnClickListener mIconPackClickListener;
        private final List<Pair<IconPack, IconPackFilter>> mIconPackFilters;
        private int mIconPackItemHeight;
        private final View.OnClickListener mMarketSearchClickListener;
        private int mSectionHeaderItemHeight;
        private int mSuggestedItemHeight;

        class IconPackViewHolder extends RecyclerView.ViewHolder {
            TextView mCountTextView;
            ImageView mImageView;
            TextView mLabelTextView;

            public IconPackViewHolder(View view) {
                super(view);
                this.mImageView = (ImageView) view.findViewById(R.id.icon);
                this.mLabelTextView = (TextView) view.findViewById(R.id.text);
                this.mCountTextView = (TextView) view.findViewById(R.id.text2);
            }
        }

        class TitleViewHolder extends RecyclerView.ViewHolder {
            TextView mTitle;

            public TitleViewHolder(View view) {
                super(view);
                this.mTitle = (TextView) view.findViewById(R.id.text);
            }
        }

        public ChangeIconAdapter(LoadIconPackResult loadIconPackResult) {
            this.mIconPackFilters = loadIconPackResult.mIconPackFilters;
            ChangeIconFragment.this.mSuggestedIcons = loadIconPackResult.mSuggestedIcons;
            this.mIconPackClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$ChangeIconFragment$ChangeIconAdapter$7NUQG_suqSWSucklrh3I6sGskvY
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ChangeIconAdapter.this.lambda$new$0( view);
                }
            };
            this.mMarketSearchClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$ChangeIconFragment$ChangeIconAdapter$ZYA5Qe4CrcKDc_rWIQVnI8IqbDI
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ChangeIconAdapter.this.lambda$new$1( view);
                }
            };
            this.mSuggestedItemHeight = ChangeIconFragment.this.getContext().getResources().getDimensionPixelSize(R.dimen.change_icon_suggested_icon_size);
            this.mIconPackItemHeight = ChangeIconFragment.this.getContext().getResources().getDimensionPixelSize(R.dimen.search_list_item_height);
            Paint paint = new Paint();
            paint.setTextSize(ChangeIconFragment.this.getContext().getResources().getDimensionPixelSize(R.dimen.change_icon_section_header_font_size));
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            this.mSectionHeaderItemHeight = ((int) Math.ceil(fontMetrics.bottom - fontMetrics.top)) + (ChangeIconFragment.this.getContext().getResources().getDimensionPixelSize(R.dimen.change_icon_section_header_margin_size) * 2);
        }

        public void lambda$new$0(View view) {
            Pair<IconPack, IconPackFilter> pair = this.mIconPackFilters.get(ChangeIconFragment.this.mRecyclerView.getChildAdapterPosition(view) - 3);
            ChangeIconFragment.this.pushStackFragment(IconPickerFragment.newInstance(pair.first, pair.second));
        }

        /* JADX WARN: Multi-variable type inference failed */
        public void lambda$new$1(View view) {
            FragmentActivity activity = ChangeIconFragment.this.getActivity();
            if (activity instanceof SafeActivityStarter) {
                ((SafeActivityStarter) activity).startActivitySafely(ChangeIconFragment.this.mIconPackManager.getMarketSearchIntent());
            } else {
                activity.startActivity(ChangeIconFragment.this.mIconPackManager.getMarketSearchIntent());
            }
            ChangeIconFragment.this.runExitAnimation();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mIconPackFilters.size() + 3 + 1;
        }

        @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
        public int getItemViewHeightForPosition(int i) {
            switch (getItemViewType(i)) {
                case 0:
                case 2:
                    return this.mSectionHeaderItemHeight;
                case 1:
                    return this.mSuggestedItemHeight;
                case 3:
                case 4:
                    return this.mIconPackItemHeight;
                default:
                    throw new IllegalArgumentException("Unhandled item view type!");
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public int getItemViewType(int i) {
            switch (i) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                case 2:
                    return 2;
                default:
                    return i == getItemCount() - 1 ? 4 : 3;
            }
        }

        @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
        public boolean hasUniformViewHeight() {
            return false;
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            int itemViewType = getItemViewType(i);
            if (itemViewType == 0) {
                ((TitleViewHolder) viewHolder).mTitle.setText(R.string.change_icon_suggested);
            }
            switch (itemViewType) {
                case 2:
                    ((TitleViewHolder) viewHolder).mTitle.setText(R.string.change_icon_packs);
                    break;
                case 3:
                    Pair<IconPack, IconPackFilter> pair = this.mIconPackFilters.get(i - 3);
                    IconPack iconPack = pair.first;
                    IconPackFilter iconPackFilter = pair.second;
                    IconPackViewHolder iconPackViewHolder = (IconPackViewHolder) viewHolder;
                    iconPackViewHolder.mImageView.setImageDrawable(iconPack.getIcon());
                    iconPackViewHolder.mLabelTextView.setText(iconPack.getLabel());
                    iconPackViewHolder.mCountTextView.setText(iconPackViewHolder.itemView.getContext().getString(R.string.change_icon_icon_count, Integer.valueOf(iconPackFilter.getDrawableList().size())));
                    break;
                case 4:
                    Resources resources = ChangeIconFragment.this.getResources();
                    IconPackViewHolder iconPackViewHolder2 = (IconPackViewHolder) viewHolder;
                    iconPackViewHolder2.mImageView.setImageDrawable(resources.getDrawable(R.drawable.rollup_play_store));
                    iconPackViewHolder2.mLabelTextView.setText(resources.getString(R.string.icon_pack_market_search));
                    iconPackViewHolder2.mCountTextView.setVisibility(8);
                    break;
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i == 0 || i == 2) {
                return new TitleViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.change_icon_section_header, viewGroup, false));
            }
            if (i == 1) {
                androidx.recyclerview.widget.RecyclerView recyclerView = new androidx.recyclerview.widget.RecyclerView(viewGroup.getContext());
                recyclerView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
                recyclerView.setLayoutManager(new LinearLayoutManager(viewGroup.getContext(), 0, false));
                ChangeIconFragment changeIconFragment = ChangeIconFragment.this;
                recyclerView.setAdapter(changeIconFragment.new SuggestedIconsAdapter(changeIconFragment.mSuggestedIcons));
                recyclerView.setPadding(viewGroup.getResources().getDimensionPixelSize(R.dimen.search_list_item_row_padding_left), 0, 0, 0);
                return new OldEmptyViewHolder(recyclerView);
            }
            if (i == 3) {
                View viewInflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.change_icon_icon_pack_item, viewGroup, false);
                viewInflate.setOnClickListener(this.mIconPackClickListener);
                return new IconPackViewHolder(viewInflate);
            }
            if (i != 4) {
                throw new IllegalArgumentException("invalid viewtype");
            }
            View viewInflate2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.change_icon_icon_pack_item, viewGroup, false);
            viewInflate2.setOnClickListener(this.mMarketSearchClickListener);
            return new IconPackViewHolder(viewInflate2);
        }
    }

    private class LoadIconPackResult {
        List<Pair<IconPack, IconPackFilter>> mIconPackFilters;
        List<Bitmap> mSuggestedIcons;

        private LoadIconPackResult() {
        }

        /* synthetic */ LoadIconPackResult(ChangeIconFragment changeIconFragment, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class SuggestedIconsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter {
        private List<Bitmap> mIcons;
        private View.OnClickListener mOnClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$ChangeIconFragment$SuggestedIconsAdapter$MUD_xLLGd-dvoMOmZq_utlR-H9c
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ChangeIconFragment.this.saveIconToDB(((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap());
            }
        };

        class IconViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView mImageView;

            public IconViewHolder(View view) {
                super(view);
                this.mImageView = (ImageView) view;
            }
        }

        public SuggestedIconsAdapter(List<Bitmap> list) {
            this.mIcons = list;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mIcons.size();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int i) {
            ((IconViewHolder) viewHolder).mImageView.setImageBitmap(this.mIcons.get(i));
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            IconViewHolder iconViewHolder = new IconViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.change_icon_suggested_icon_item, viewGroup, false));
            iconViewHolder.mImageView.setOnClickListener(this.mOnClickListener);
            return iconViewHolder;
        }
    }

    static /* synthetic */ void lambda$onViewCreated$0(View view) {
    }

    public static ChangeIconFragment newInstance(ItemInfo itemInfo) {
        ChangeIconFragment changeIconFragment = new ChangeIconFragment();
        changeIconFragment.mItemInfo = itemInfo;
        return changeIconFragment;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveIconToDB(Bitmap bitmap) {
        ComponentName component = this.mItemInfo.getIntent().getComponent();
        AppsInfoDataHelper appsInfoDataHelper = new AppsInfoDataHelper(getContext());
        AppsInfoDataHelper.AppInfo app = appsInfoDataHelper.getApp(component.getPackageName(), component.getClassName());
        if (app != null) {
            app.icon = bitmap;
            appsInfoDataHelper.update(app);
        }
        LocalAppData app2 = this.mLocalAppsManager.getApp(component.getPackageName(), component.getClassName());
        if (app2 != null) {
            app2.setCustomIcon(bitmap);
        }
        if ((app == null) != (app2 == null)) {
            StringBuilder sb = new StringBuilder();
            sb.append("appinfo(");
            sb.append(app == null);
            sb.append(") vs localappdata ");
            sb.append(app2 == null);
            sb.append("mismatch! ");
            sb.append(component);
            Timber.e(new IllegalStateException(sb.toString()));
        }
        LauncherAppState.getInstance().getIconCache().flush();
        Uri magicResourceUri = null;
        if (app != null) {
            magicResourceUri = HybridNetworkFetcherCommon.getMagicResourceUri(component.getPackageName(), component.getClassName(), app.appVersion);
        } else if (app2 != null) {
            magicResourceUri = HybridNetworkFetcherCommon.getMagicResourceUri(app2);
        }
        if (magicResourceUri != null) {
            IconLoader.getInstance().evict(magicResourceUri);
        }
        LauncherAppState.getInstance().getModel().forceReload();
        this.mIconPackManager.notifyDataChanged();
        setResultCode(-1);
        runExitAnimation();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public boolean canSaveInstanceState() {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
        if (getView() != null) {
            getView().animate().alpha(1.0f).withLayer();
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment, androidx.fragment.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AndroidSupportInjection.inject(this);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.change_icon_fragment, viewGroup, false);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onStackableFragmentResult(int i, int i2, Intent intent) {
        Bitmap bitmap;
        if (i2 != -1 || (bitmap = (Bitmap) intent.getParcelableExtra("ICON")) == null) {
            return;
        }
        saveIconToDB(bitmap);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        view.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$ChangeIconFragment$A-fbPEHmlm3XJH2yDgJ-w10cb5U
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                ChangeIconFragment.lambda$onViewCreated$0(view2);
            }
        });
        ((TextView) view.findViewById(R.id.change_icon_title)).setText(getContext().getString(R.string.change_icon_title, this.mItemInfo.title));
        this.mLoadingIndicator = view.findViewById(R.id.loading);
        this.mContainer = view.findViewById(R.id.container);
        this.mRecyclerView = (SpringRecyclerView) view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setLayoutManager(new com.voxel.recyclerview.widget.LinearLayoutManager(getContext()));
        Context context = getContext();
        Toast.makeText(context, R.string.change_icon_loading, 0).show();
        new AnonymousClass1(context).execute(new Void[0]);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public boolean requiresFullScreen() {
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void runExitAnimation() {
        notifyFragmentWillRequestClose();
        notifyFragmentRequestClose();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
        if (getView() != null) {
            getView().animate().alpha(0.0f).withLayer();
        }
    }
}
