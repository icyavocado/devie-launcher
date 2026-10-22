package com.voxel.simplesearchlauncher.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.ovacoda.devie.DevieLinks;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class FeedbackFragment extends Fragment {
    /* REWORK(github): community row now opens the GitHub issue tracker. */
    public void lambda$onViewCreated$0(View view) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(268468224);
        try {
            FragmentActivity activity = this.getActivity();
            intent.setData(Uri.parse(DevieLinks.ISSUES_URL));
            if (activity instanceof SafeActivityStarter) {
                ((SafeActivityStarter) activity).startActivitySafely(intent);
            } else {
                activity.startActivity(intent);
            }
        } catch (ActivityNotFoundException unused) {
        }
    }

    /* REWORK(github): rating row now opens the GitHub repo (star it there);
       the play-store fallback + analytics event are gone with the store. */
    public void lambda$onViewCreated$1(View view) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(268468224);
        try {
            FragmentActivity activity = this.getActivity();
            intent.setData(Uri.parse(DevieLinks.REPO_URL));
            if (activity instanceof SafeActivityStarter) {
                ((SafeActivityStarter) activity).startActivitySafely(intent);
            } else {
                activity.startActivity(intent);
            }
        } catch (ActivityNotFoundException unused) {
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_feedback_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        SettingsTextViewHolder settingsTextViewHolder = new SettingsTextViewHolder(view.findViewById(R.id.join_community));
        SettingsTextViewHolder settingsTextViewHolder2 = new SettingsTextViewHolder(view.findViewById(R.id.rate_app));
        settingsTextViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FeedbackFragment$dwIKanqFyyVyYIGLqQ4GG7kr-1Y
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                FeedbackFragment.this.lambda$onViewCreated$0( view2);
            }
        });
        settingsTextViewHolder.mLabel.setText(R.string.app_settings_item_community_text);
        settingsTextViewHolder.mSubLabel.setText(R.string.app_settings_item_community_subtext);
        settingsTextViewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$FeedbackFragment$T5VUPyxHa85fV7klGic0rE6u2dk
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                FeedbackFragment.this.lambda$onViewCreated$1( view2);
            }
        });
        settingsTextViewHolder2.mLabel.setText(R.string.app_settings_item_rating);
        settingsTextViewHolder2.mSubLabel.setText(R.string.app_settings_item_rating_subtext);
    }
}
