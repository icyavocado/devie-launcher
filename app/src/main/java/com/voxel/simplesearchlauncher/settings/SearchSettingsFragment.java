package com.voxel.simplesearchlauncher.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.evie.common.SearchSettings;
import com.evie.search.local.LocalSearchManager;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.ovacoda.devie.R;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes2.dex */
public class SearchSettingsFragment extends Fragment {
    private int REQUEST_DONE_BUTTON_DIALOG = 1;
    private int REQUEST_SEARCH_BAR_STYLE_DIALOG = 2;
    private int REQUEST_SEARCH_ENGINE_DIALOG = 3;
    SettingsSwitchViewHolder mAutocompleteBehavior;
    SettingsTextViewHolder mDoneButtonSetting;
    SettingsTextViewHolder mSearchBarStyle;
    SettingsTextViewHolder mSearchEngine;

    public static class ClearRecentsDialogFragment extends DialogFragment {
        public void lambda$onCreateDialog$0(DialogInterface dialogInterface, int i) {
            LocalSearchManager.getInstance(this.getContext().getApplicationContext(), true).clearRecentItems();
            Toast.makeText(this.getActivity(), R.string.app_settings_item_toast_clear_recent_results, 0).show();
            AnalyticsHandler.getInstance().logEvent("ev_settings_clear_recent_items", null);
        }

        @Override // androidx.fragment.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 5);
            builder.setTitle(R.string.app_settings_item_confirm_dialog_clear_recent_results_title);
            builder.setMessage(R.string.app_settings_item_confirm_dialog_clear_recent_results_message);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$ClearRecentsDialogFragment$S-VImVZk0iBkjeOKwtlD9tg5AH8
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    ClearRecentsDialogFragment.this.lambda$onCreateDialog$0( dialogInterface, i);
                }
            });
            builder.setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener) null);
            return builder.create();
        }
    }

    private static int getResourceIdForSearchDoneType(int i) {
        return i != 1 ? R.string.app_settings_item_search_done_button_close_keyboard : R.string.app_settings_item_search_done_button_open_first_result;
    }

    private static int getResourceIdForSearchEngine(int i) {
        switch (i) {
            case 1:
                return R.string.app_settings_item_search_engine_bing;
            case 2:
                return R.string.app_settings_item_search_engine_duck_duck_go;
            default:
                return R.string.app_settings_item_search_engine_google;
        }
    }

    private static int getTextResourceIdForSearchBarStyle(int i) {
        return i != 1 ? R.string.app_settings_item_search_bar_style_white : R.string.app_settings_item_search_bar_style_subtle;
    }

    public void lambda$onViewCreated$0(View view) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(1, 0, R.string.app_settings_item_search_bar_style_subtle, R.string.app_settings_item_search_bar_style_subtle_description));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(0, 0, R.string.app_settings_item_search_bar_style_white, R.string.app_settings_item_search_bar_style_white_description));
        MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_item_search_done_button_dialog_title, SettingsStorage.getInstance().getSearchBarStyle() != 0 ? 0 : 1, arrayList);
        multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, this.REQUEST_SEARCH_BAR_STYLE_DIALOG);
        multipleChoiceDialogFragmentNewInstance.show(this.getChildFragmentManager(), null);
    }

    public void lambda$onViewCreated$1(SearchSettings searchSettings, View view) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(0, R.drawable.google, R.string.app_settings_item_search_engine_google, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(1, R.drawable.bing, R.string.app_settings_item_search_engine_bing, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(2, R.drawable.duck_duck_go, R.string.app_settings_item_search_engine_duck_duck_go, 0));
        MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_item_search_engine_dialog_title, searchSettings.getSearchEngine(), arrayList, R.layout.dialog_icon_radiobutton_item);
        multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, this.REQUEST_SEARCH_ENGINE_DIALOG);
        multipleChoiceDialogFragmentNewInstance.show(this.getChildFragmentManager(), null);
    }

    public void lambda$onViewCreated$2(View view) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(0, 0, R.string.app_settings_item_search_done_button_close_keyboard, 0));
        arrayList.add(new MultipleChoiceDialogFragment.ListItem(1, 0, R.string.app_settings_item_search_done_button_open_first_result, 0));
        MultipleChoiceDialogFragment multipleChoiceDialogFragmentNewInstance = MultipleChoiceDialogFragment.newInstance(R.string.app_settings_item_search_done_button_dialog_title, SettingsStorage.getInstance().getSearchDoneButtonBehavior() == 1 ? 1 : 0, arrayList);
        multipleChoiceDialogFragmentNewInstance.setTargetFragment(null, this.REQUEST_DONE_BUTTON_DIALOG);
        multipleChoiceDialogFragmentNewInstance.show(this.getChildFragmentManager(), null);
    }

    private void updateDoneButtonSubtext() {
        this.mDoneButtonSetting.mSubLabel.setText(getResourceIdForSearchDoneType(SettingsStorage.getInstance().getSearchDoneButtonBehavior()));
    }

    private void updateSearchBarButtonSubtext() {
        this.mSearchBarStyle.mSubLabel.setText(getTextResourceIdForSearchBarStyle(SettingsStorage.getInstance().getSearchBarStyle()));
    }

    private void updateSearchEngineResult(int i) {
        this.mSearchEngine.mSubLabel.setText(getResourceIdForSearchEngine(i));
        this.mAutocompleteBehavior.setEnabled(i == 0);
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == this.REQUEST_DONE_BUTTON_DIALOG) {
            int longExtra = (int) intent.getLongExtra("ID", -1L);
            if (longExtra != -1) {
                SettingsStorage.getInstance().saveSearchDoneButtonBehavior(longExtra);
            }
            updateDoneButtonSubtext();
            return;
        }
        if (i == this.REQUEST_SEARCH_BAR_STYLE_DIALOG) {
            int longExtra2 = (int) intent.getLongExtra("ID", -1L);
            if (longExtra2 != -1) {
                SettingsStorage.getInstance().setSearchBarStyle(longExtra2);
            }
            updateSearchBarButtonSubtext();
            return;
        }
        if (i == this.REQUEST_SEARCH_ENGINE_DIALOG) {
            int longExtra3 = (int) intent.getLongExtra("ID", -1L);
            if (longExtra3 != -1) {
                SearchSettings.getInstance(getContext()).setSearchEngine(longExtra3);
            }
            updateSearchEngineResult(longExtra3);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_search_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        final SearchSettings searchSettings = SearchSettings.getInstance(getContext());
        this.mSearchBarStyle = new SettingsTextViewHolder(view.findViewById(R.id.search_bar_style));
        this.mSearchEngine = new SettingsTextViewHolder(view.findViewById(R.id.search_engine));
        this.mDoneButtonSetting = new SettingsTextViewHolder(view.findViewById(R.id.done_button_behavior));
        this.mAutocompleteBehavior = new SettingsSwitchViewHolder(view.findViewById(R.id.autocomplete_behavior));
        SettingsSwitchViewHolder settingsSwitchViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.recents_include_web_results));
        SettingsSwitchViewHolder settingsSwitchViewHolder2 = new SettingsSwitchViewHolder(view.findViewById(R.id.include_frequently_used));
        SettingsTextViewHolder settingsTextViewHolder = new SettingsTextViewHolder(view.findViewById(R.id.clear_recent));
        this.mSearchBarStyle.mLabel.setText(R.string.app_settings_item_search_bar_style);
        updateSearchBarButtonSubtext();
        this.mSearchBarStyle.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$FRGr9DXEUOBXRgG3adstZQuyHSQ
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                SearchSettingsFragment.this.lambda$onViewCreated$0( view2);
            }
        });
        this.mSearchEngine.mLabel.setText(R.string.app_settings_item_search_engine);
        updateSearchEngineResult(searchSettings.getSearchEngine());
        this.mSearchEngine.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$EPVoRXM_QDKPbveES1qxy_h6VZk
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                SearchSettingsFragment.this.lambda$onViewCreated$1( searchSettings, view2);
            }
        });
        this.mDoneButtonSetting.mLabel.setText(R.string.app_settings_item_search_done_button_title);
        updateDoneButtonSubtext();
        this.mDoneButtonSetting.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$CFAXJXmqrOwB63uzNenEaPKge84
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                SearchSettingsFragment.this.lambda$onViewCreated$2( view2);
            }
        });
        this.mAutocompleteBehavior.mLabel.setText(R.string.app_settings_item_search_auto_complete_title);
        this.mAutocompleteBehavior.mSwitch.setChecked(searchSettings.getOpenWebResultsInBrowser());
        this.mAutocompleteBehavior.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$63ppX2u-0DWg63tJ3o92fhLnK3Q
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                searchSettings.setOpenWebResultsInBrowser(z);
            }
        });
        settingsSwitchViewHolder.mLabel.setText(R.string.app_settings_item_search_recents_include_web_results);
        settingsSwitchViewHolder.mSwitch.setChecked(searchSettings.getRecentsIncludeWebResults());
        settingsSwitchViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$zKrp6FB3cpmJlnyAuO7adqp0ZGs
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                searchSettings.setRecentsIncludeWebResults(z);
            }
        });
        settingsSwitchViewHolder2.mLabel.setText(R.string.app_settings_item_search_include_frequently_used);
        settingsSwitchViewHolder2.mSubLabel.setText(R.string.app_settings_item_search_include_frequently_used_subtext);
        settingsSwitchViewHolder2.mSubLabel.setVisibility(0);
        settingsSwitchViewHolder2.mSwitch.setChecked(searchSettings.getIncludeFrequentlyUsed());
        settingsSwitchViewHolder2.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$z19oLFN0YZGcIXn6jj_A4dRD0JE
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                searchSettings.setIncludeFrequentlyUsed(z);
            }
        });
        settingsTextViewHolder.mLabel.setText(R.string.app_settings_item_search_clear_recent);
        settingsTextViewHolder.mSubLabel.setText(R.string.app_settings_item_search_clear_recent_subtext);
        settingsTextViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SearchSettingsFragment$xCkFth3rEBBJTh0w6YNFb2kNzIQ
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                new SearchSettingsFragment.ClearRecentsDialogFragment().show(SearchSettingsFragment.this.getChildFragmentManager(), null);
            }
        });
    }
}
