package com.voxel.simplesearchlauncher.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.evie.common.dragndrop.DragObjectAbstraction;
import com.evie.common.dragndrop.DragSource;
import com.voxel.launcher3.DropTarget;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherApplication;
import com.voxel.launcher3.PendingAddItemInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.WidgetPreviewLoader;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.util.List;
import timber.log.Timber;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetsFragment extends LauncherStackableFragment implements DragSource {
    private static final String TAG = "WidgetsFragment";
    private WidgetsListAdapter mAdapter;
    private LauncherAppsCompat.OnAppsChangedCallbackCompat mAppsChangedCallback;
    IconPackManager mIconPackManager;
    private DataSetObserver mIconPackObserver;
    private Launcher mLauncher;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;
    private ProgressBar mProgressBar;
    private WidgetsRecyclerView mRecyclerView;
    private Toast mWidgetInstructionToast;
    private WidgetPreviewLoader mWidgetPreviewLoader;
    private WidgetsModel mWidgetsModel;

    public interface OnFragmentInteractionListener {
        boolean onAllWidgetsDropCompleted(WidgetsFragment widgetsFragment, View view, DropTarget.DragObject dragObject, boolean z, boolean z2);

        boolean onAllWidgetsLongPress(WidgetsFragment widgetsFragment, View view, PendingAddItemInfo pendingAddItemInfo, Bitmap bitmap, Bitmap bitmap2, boolean z);
    }

    private boolean beginDragging(View view) {
        if (view instanceof WidgetCell) {
            return beginDraggingWidget((WidgetCell) view);
        }
        Log.e(TAG, "Unexpected dragging view: " + view);
        return true;
    }

    private boolean beginDraggingWidget(WidgetCell widgetCell) {
        Bitmap bitmapGenerateWidgetPreview;
        ImageView imageView = (ImageView) widgetCell.findViewById(R.id.widget_preview);
        PendingAddItemInfo pendingAddItemInfo = (PendingAddItemInfo) widgetCell.getTag();
        if (imageView.getDrawable() == null) {
            return false;
        }
        boolean z = pendingAddItemInfo instanceof PendingAddWidgetInfo;
        if (z) {
            PendingAddWidgetInfo pendingAddWidgetInfo = (PendingAddWidgetInfo) pendingAddItemInfo;
            bitmapGenerateWidgetPreview = getWidgetPreviewLoader().generateWidgetPreview(this.mLauncher, pendingAddWidgetInfo.info, Math.min((int) (((BitmapDrawable) imageView.getDrawable()).getBitmap().getWidth() * 1.25f), this.mLauncher.getWorkspace().estimateItemSize(pendingAddWidgetInfo.spanX, pendingAddWidgetInfo.spanY, true)[0]), null, new int[1]);
        } else {
            Bitmap bitmapCreateIconBitmap = Utilities.createIconBitmap(LauncherAppState.getInstance().getIconCache().getFullResIcon(((PendingAddShortcutInfo) widgetCell.getTag()).activityInfo), this.mLauncher);
            pendingAddItemInfo.spanY = 1;
            pendingAddItemInfo.spanX = 1;
            bitmapGenerateWidgetPreview = bitmapCreateIconBitmap;
        }
        boolean z2 = (z && ((PendingAddWidgetInfo) pendingAddItemInfo).previewImage == 0) ? false : true;
        this.mLauncher.lockScreenOrientation();
        OnFragmentInteractionListener onFragmentInteractionListener = this.mOnFragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onAllWidgetsLongPress(this, widgetCell, pendingAddItemInfo, bitmapGenerateWidgetPreview, bitmapGenerateWidgetPreview, z2);
        }
        bitmapGenerateWidgetPreview.recycle();
        return true;
    }

    private DataSetObserver getIconPackObserver(final Context context) {
        return new DataSetObserver() { // from class: com.voxel.simplesearchlauncher.widget.WidgetsFragment.2
            @Override // android.database.DataSetObserver
            public void onChanged() {
                WidgetsFragment.this.refreshList(context);
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                WidgetsFragment.this.refreshList(context);
            }
        };
    }

    private WidgetPreviewLoader getWidgetPreviewLoader() {
        if (this.mWidgetPreviewLoader == null) {
            this.mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();
        }
        return this.mWidgetPreviewLoader;
    }

    public void lambda$onCreate$0(View view) {
        if (this.isAdded() && !this.isHidden() && (view instanceof WidgetCell)) {
            Toast toast = this.mWidgetInstructionToast;
            if (toast != null) {
                toast.cancel();
            }
            this.mWidgetInstructionToast = Toast.makeText(this.getContext(), R.string.long_press_widget_to_add, 0);
            this.mWidgetInstructionToast.show();
        }
    }

    public boolean lambda$onCreate$1(View view) {
        Timber.d(String.format("onLonglick [v=%s]", view), new Object[0]);
        if (!view.isInTouchMode() || !this.isAdded() || this.isHidden() || !this.mLauncher.isDraggingEnabled()) {
            return false;
        }
        boolean zBeginDragging = this.beginDragging(view);
        if (zBeginDragging && (view.getTag() instanceof PendingAddWidgetInfo)) {
            WidgetHostViewLoader widgetHostViewLoader = new WidgetHostViewLoader(this.mLauncher, view);
            Timber.d(TAG, String.format("preloading widget [status=%s]", Boolean.valueOf(widgetHostViewLoader.preloadWidget())));
            this.mLauncher.getDragController().addDragListener(widgetHostViewLoader);
        }
        return zBeginDragging;
    }

    public static WidgetsFragment newInstance() {
        WidgetsFragment widgetsFragment = new WidgetsFragment();
        widgetsFragment.setArguments(new Bundle());
        return widgetsFragment;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshList(final Context context) {
        Log.d(TAG, "refreshList");
        new AsyncTask<Void, Void, WidgetsModel>() { // from class: com.voxel.simplesearchlauncher.widget.WidgetsFragment.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public WidgetsModel doInBackground(Void... voidArr) {
                /* REWORK: AsyncTasks cannot throw checked exceptions; the dex version
                 * let the framework's worker catch it. Model the same way:
                 * fall back to a fresh (empty) model on failure. */
                try {
                    return new WidgetsModel(context, LauncherAppState.getInstance().getIconCache()).updateAndClone(context);
                } catch (Exception e) {
                    Log.e(WidgetsFragment.TAG, "failed to update widget list", e);
                    return new WidgetsModel(context, LauncherAppState.getInstance().getIconCache());
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(WidgetsModel widgetsModel) {
                Log.d(WidgetsFragment.TAG, "onPostExecute");
                WidgetsFragment.this.mWidgetsModel = widgetsModel;
                WidgetsFragment.this.mRecyclerView.setWidgets(widgetsModel);
                WidgetsFragment.this.mAdapter.setWidgetsModel(WidgetsFragment.this.mWidgetsModel);
                if (WidgetsFragment.this.isAdded()) {
                    if (WidgetsFragment.this.mProgressBar != null) {
                        WidgetsFragment.this.mProgressBar.setVisibility(8);
                    }
                    WidgetsFragment.this.mAdapter.notifyDataSetChanged();
                    if (WidgetsFragment.this.mRecyclerView != null) {
                        ((LinearLayoutManager) WidgetsFragment.this.mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    }
                }
            }
        }.execute(new Void[0]);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
    }

    @Override // com.evie.common.dragndrop.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            this.mOnFragmentInteractionListener = (OnFragmentInteractionListener) context;
            this.mLauncher = (Launcher) context;
        } catch (ClassCastException unused) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate");
        super.onCreate(bundle);
        this.mAdapter = new WidgetsListAdapter(getActivity(), new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.widget.-$$Lambda$WidgetsFragment$nwtOH1WmRaDDiogOJMY-rWIbL2k
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WidgetsFragment.this.lambda$onCreate$0( view);
            }
        }, new View.OnLongClickListener() { // from class: com.voxel.simplesearchlauncher.widget.-$$Lambda$WidgetsFragment$rbb5g3QQnq1sOuFEjRWz7t17rGY
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return WidgetsFragment.this.lambda$onCreate$1( view);
            }
        }, this.mLauncher);
        this.mAppsChangedCallback = new LauncherAppsCompat.OnAppsChangedCallbackCompat() { // from class: com.voxel.simplesearchlauncher.widget.WidgetsFragment.1
            Context applicationContext;

            {
                this.applicationContext = WidgetsFragment.this.getContext().getApplicationContext();
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageAdded(String str, UserHandleCompat userHandleCompat) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageChanged(String str, UserHandleCompat userHandleCompat) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageRemoved(String str, UserHandleCompat userHandleCompat) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesAvailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesSuspended(String[] strArr, UserHandleCompat userHandleCompat) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesUnavailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesUnsuspended(String[] strArr, UserHandleCompat userHandleCompat) {
                WidgetsFragment.this.refreshList(this.applicationContext);
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onShortcutsChanged(String str, List<DeepShortcutInfoCompat> list, UserHandleCompat userHandleCompat) {
            }
        };
        LauncherAppsCompat.getInstance(getContext()).addOnAppsChangedCallback(this.mAppsChangedCallback);
        this.mIconPackObserver = getIconPackObserver(getActivity());
        this.mIconPackManager.registerDataSetObserver(this.mIconPackObserver);
        refreshList(getActivity());
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Log.d(TAG, "onCreateView " + this.mAdapter.getItemCount());
        View viewInflate = layoutInflater.inflate(R.layout.widgets_fragment, viewGroup, false);
        this.mRecyclerView = (WidgetsRecyclerView) viewInflate.findViewById(R.id.list);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mRecyclerView.setAdapter(this.mAdapter);
        WidgetsModel widgetsModel = this.mWidgetsModel;
        if (widgetsModel != null) {
            this.mAdapter.setWidgetsModel(widgetsModel);
        }
        this.mProgressBar = (ProgressBar) viewInflate.findViewById(R.id.progress_bar);
        if (this.mAdapter.getItemCount() != 0) {
            this.mProgressBar.setVisibility(8);
        }
        return viewInflate;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment, androidx.fragment.app.Fragment
    public void onDestroy() {
        this.mIconPackManager.unregisterDataSetObserver(this.mIconPackObserver);
        LauncherAppsCompat.getInstance(getContext()).removeOnAppsChangedCallback(this.mAppsChangedCallback);
        super.onDestroy();
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onDropCompleted(View view, DragObjectAbstraction dragObjectAbstraction, boolean z, boolean z2) {
        Log.d(TAG, "onDropCompleted " + z2 + " target: " + view);
        DropTarget.DragObject dragObject = (DropTarget.DragObject) dragObjectAbstraction;
        if (z) {
            return;
        }
        if (!z2) {
            dragObject.deferDragViewCleanupPostAnimation = false;
        }
        OnFragmentInteractionListener onFragmentInteractionListener = this.mOnFragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onAllWidgetsDropCompleted(this, view, dragObject, z, z2);
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onFlingToDeleteCompleted() {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
        ((Launcher) fragmentActivity).hideBlurImage();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
        ((Launcher) onFragmentInteractionListener).showBlurImage();
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
    }

    @Override // androidx.fragment.app.Fragment
    public void onStart() {
        super.onStart();
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void runExitAnimation() {
        notifyFragmentWillRequestClose();
        notifyFragmentRequestClose();
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsRemoveDropTarget() {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
    }
}
