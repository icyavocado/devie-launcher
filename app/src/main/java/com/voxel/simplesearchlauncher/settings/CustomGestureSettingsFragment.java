package com.voxel.simplesearchlauncher.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import com.voxel.simplesearchlauncher.gestures.GestureType;
import com.voxel.simplesearchlauncher.gestures.action.CustomGestureAction;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.gestures.SelectGestureActionFragment;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class CustomGestureSettingsFragment extends Fragment {
    LocalAppsManager localAppsManager;
    GesturesAdapter mAdapter;

    RecyclerView mRecyclerView;

    /* JADX INFO: Access modifiers changed from: private */
    class GesturesAdapter extends RecyclerView.Adapter<SettingsTextViewHolder> {
        private GesturesAdapter() {
        }

        public void lambda$onBindViewHolder$0(GestureType gestureType, View view) {
            if (CustomGestureSettingsFragment.this.getActivity() == null) {
                return;
            }
            String simpleName = SelectGestureActionFragment.class.getSimpleName();
            SelectGestureActionFragment selectGestureActionFragmentNewInstance = SelectGestureActionFragment.newInstance(gestureType, simpleName);
            FragmentTransaction fragmentTransactionBeginTransaction = CustomGestureSettingsFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransactionBeginTransaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short, R.anim.fade_in_short, R.anim.fade_out_short);
            fragmentTransactionBeginTransaction.replace(R.id.container, selectGestureActionFragmentNewInstance);
            fragmentTransactionBeginTransaction.setBreadCrumbTitle(gestureType.titleResId);
            fragmentTransactionBeginTransaction.addToBackStack(simpleName);
            fragmentTransactionBeginTransaction.commit();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return GestureType.values().length;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(SettingsTextViewHolder settingsTextViewHolder, int i) {
            final GestureType gestureType = GestureType.values()[i];
            settingsTextViewHolder.mLabel.setText(gestureType.titleResId);
            CustomGestureAction actionForGestureType = SettingsStorage.getInstance().getActionForGestureType(gestureType);
            int i2 = actionForGestureType.titleResId;
            if (CustomGestureAction.LAUNCH.equals(actionForGestureType)) {
                Intent intentForGestureType = SettingsStorage.getInstance().getIntentForGestureType(gestureType);
                if (intentForGestureType == null) {
                    CustomGestureAction customGestureAction = CustomGestureAction.NONE;
                    SettingsStorage.getInstance().setIntentForGestureType(gestureType, null);
                    SettingsStorage.getInstance().setActionForGestureType(gestureType, customGestureAction);
                } else {
                    ComponentName component = intentForGestureType.getComponent();
                    if (component == null) {
                        CustomGestureAction customGestureAction2 = CustomGestureAction.NONE;
                        SettingsStorage.getInstance().setIntentForGestureType(gestureType, null);
                        SettingsStorage.getInstance().setActionForGestureType(gestureType, customGestureAction2);
                    } else {
                        LocalAppData app = CustomGestureSettingsFragment.this.localAppsManager.getApp(component.getPackageName(), component.getClassName());
                        if (app != null) {
                            settingsTextViewHolder.mSubLabel.setText(CustomGestureSettingsFragment.this.getResources().getString(R.string.gesture_action_launch_app_with_name, app.getLabel()));
                        } else {
                            settingsTextViewHolder.mSubLabel.setText(i2);
                        }
                    }
                }
            } else {
                settingsTextViewHolder.mSubLabel.setText(i2);
            }
            settingsTextViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$CustomGestureSettingsFragment$GesturesAdapter$qDfv6Xs62KY2tdEZGNM_5tL5xIM
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    GesturesAdapter.this.lambda$onBindViewHolder$0( gestureType, view);
                }
            });
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public SettingsTextViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            SettingsTextViewHolder settingsTextViewHolder = new SettingsTextViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_activity_text_item, viewGroup, false));
            settingsTextViewHolder.mSubLabel.setVisibility(0);
            return settingsTextViewHolder;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_custom_gestures_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        this.mAdapter.notifyDataSetChanged();
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        ButterKnife.bind(this, view);
        this.mAdapter = new GesturesAdapter();
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mRecyclerView.setAdapter(this.mAdapter);
    }
}
