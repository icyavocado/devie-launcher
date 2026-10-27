package com.voxel.simplesearchlauncher.settings.gestures;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.gestures.GestureType;
import com.voxel.simplesearchlauncher.gestures.action.CustomGestureAction;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.SettingsTextViewHolder;
import com.voxel.simplesearchlauncher.settings.lock.ChooseLockMethodDialogFragment;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class SelectGestureActionFragment extends Fragment {
    ActionsAdapter mAdapter;
    private String mBackstackId;
    private GestureType mGestureType;

    RecyclerView mRecyclerView;
    private SettingsStorage mSettingsStorage;

    /* JADX INFO: Access modifiers changed from: private */
    class ActionsAdapter extends RecyclerView.Adapter<SettingsTextViewHolder> {
        List<CustomGestureAction> mActions = new ArrayList();

        ActionsAdapter() {
            this.mActions.addAll(Arrays.asList(CustomGestureAction.values()));
            this.mActions.remove(CustomGestureAction.SCREEN_LOCK_WRITE_SETTINGS);
        }

        public void lambda$onBindViewHolder$0(View view) {
            ChooseLockMethodDialogFragment chooseLockMethodDialogFragmentNewInstance = ChooseLockMethodDialogFragment.newInstance(SelectGestureActionFragment.this.mGestureType);
            chooseLockMethodDialogFragmentNewInstance.setTargetFragment(null, 1);
            chooseLockMethodDialogFragmentNewInstance.show(SelectGestureActionFragment.this.getChildFragmentManager(), "lock_dialog");
        }

        public void lambda$onBindViewHolder$1(CustomGestureAction customGestureAction, View view) {
            if (SelectGestureActionFragment.this.getActivity() == null) {
                return;
            }
            SelectAppFragment selectAppFragment = new SelectAppFragment();
            selectAppFragment.setTargetFragment(SelectGestureActionFragment.this, 2);
            FragmentTransaction fragmentTransactionBeginTransaction = SelectGestureActionFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransactionBeginTransaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short, R.anim.fade_in_short, R.anim.fade_out_short);
            fragmentTransactionBeginTransaction.replace(R.id.container, selectAppFragment);
            fragmentTransactionBeginTransaction.setBreadCrumbTitle(customGestureAction.titleResId);
            fragmentTransactionBeginTransaction.addToBackStack(null);
            fragmentTransactionBeginTransaction.commit();
        }

        public void lambda$onBindViewHolder$2(CustomGestureAction customGestureAction, View view) {
            SelectGestureActionFragment.this.mSettingsStorage.setActionForGestureType(SelectGestureActionFragment.this.mGestureType, customGestureAction);
            SelectGestureActionFragment.this.mSettingsStorage.setIntentForGestureType(SelectGestureActionFragment.this.mGestureType, null);
            if (SelectGestureActionFragment.this.getActivity() != null) {
                SelectGestureActionFragment.this.getActivity().getSupportFragmentManager().popBackStack();
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mActions.size();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(SettingsTextViewHolder settingsTextViewHolder, int i) {
            final CustomGestureAction customGestureAction = this.mActions.get(i);
            switch (customGestureAction) {
                case SCREEN_LOCK_DEVICE_ADMIN:
                    settingsTextViewHolder.mLabel.setText(R.string.gesture_action_screen_lock_generic);
                    settingsTextViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectGestureActionFragment$ActionsAdapter$a1UCyzx60wOLGQr992RpSSUonME
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            ActionsAdapter.this.lambda$onBindViewHolder$0( view);
                        }
                    });
                    break;
                case LAUNCH:
                    settingsTextViewHolder.mLabel.setText(customGestureAction.titleResId);
                    settingsTextViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectGestureActionFragment$ActionsAdapter$LBO6X-F-dE7PdixPv0RS3zTEnzM
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            ActionsAdapter.this.lambda$onBindViewHolder$1( customGestureAction, view);
                        }
                    });
                    break;
                default:
                    settingsTextViewHolder.mLabel.setText(customGestureAction.titleResId);
                    settingsTextViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectGestureActionFragment$ActionsAdapter$IVJeR97ETQM27crPJLxQmYEXPRY
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            ActionsAdapter.this.lambda$onBindViewHolder$2( customGestureAction, view);
                        }
                    });
                    break;
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public SettingsTextViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            SettingsTextViewHolder settingsTextViewHolder = new SettingsTextViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_activity_text_item, viewGroup, false));
            settingsTextViewHolder.mSubLabel.setVisibility(8);
            return settingsTextViewHolder;
        }
    }

    public static SelectGestureActionFragment newInstance(GestureType gestureType, String str) {
        SelectGestureActionFragment selectGestureActionFragment = new SelectGestureActionFragment();
        Bundle bundle = new Bundle();
        bundle.putString("gesture", gestureType.name());
        bundle.putString("backstack", str);
        selectGestureActionFragment.setArguments(bundle);
        return selectGestureActionFragment;
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 == -1 && getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        if (i == 2 && i2 == -1) {
            this.mSettingsStorage.setActionForGestureType(this.mGestureType, CustomGestureAction.LAUNCH);
            this.mSettingsStorage.setIntentForGestureType(this.mGestureType, intent);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack(this.mBackstackId, 1);
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mSettingsStorage = SettingsStorage.getInstance();
        Bundle arguments = getArguments();
        if (arguments != null) {
            String string = arguments.getString("gesture");
            if (!TextUtils.isEmpty(string)) {
                this.mGestureType = GestureType.valueOf(string);
            }
            this.mBackstackId = arguments.getString("backstack");
        }
        if (this.mGestureType == null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
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
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mAdapter = new ActionsAdapter();
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mRecyclerView.setAdapter(this.mAdapter);
    }
}
