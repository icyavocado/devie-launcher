package com.voxel.simplesearchlauncher.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.voxel.simplesearchlauncher.notification.extractors.GmailExtractor;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationSettingsGmailConfigFragment extends Fragment {
    AccountListAdapter mAdapter;
    NotificationController mController;
    Set<String> mEnabledAccounts;
    GmailExtractor mExtractor;
    RecyclerView mRecyclerView;

    /* JADX INFO: Access modifiers changed from: private */
    /* REWORK: javac resolves the extends-clause generics before the class's own
     * nested members are in scope; qualify the view holder. */
    class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.SwitchViewHolder> {
        List<String> mAccounts = Collections.synchronizedList(new ArrayList());

        class SwitchViewHolder extends RecyclerView.ViewHolder {
            TextView mLabel;
            TextView mSubLabel;
            SwitchCompat mSwitch;

            public SwitchViewHolder(View view) {
                super(view);
                this.mLabel = (TextView) view.findViewById(R.id.line1);
                this.mSubLabel = (TextView) view.findViewById(R.id.line2);
                this.mSwitch = (SwitchCompat) view.findViewById(R.id.item_switch);
            }
        }

        public AccountListAdapter() {
        }

        public void lambda$onBindViewHolder$0(String str, CompoundButton compoundButton, boolean z) {
            if (z) {
                NotificationSettingsGmailConfigFragment.this.mEnabledAccounts.add(str);
            } else {
                NotificationSettingsGmailConfigFragment.this.mEnabledAccounts.remove(str);
            }
            NotificationSettingsGmailConfigFragment.this.mExtractor.setEnabledAccounts((String[]) NotificationSettingsGmailConfigFragment.this.mEnabledAccounts.toArray(new String[0]));
        }

        public void addAccount(String str) {
            this.mAccounts.add(str);
            notifyDataSetChanged();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mAccounts.size();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(SwitchViewHolder switchViewHolder, int i) {
            final String str = this.mAccounts.get(i);
            boolean zContains = NotificationSettingsGmailConfigFragment.this.mEnabledAccounts.contains(str);
            switchViewHolder.mLabel.setText(str);
            switchViewHolder.mSwitch.setChecked(zContains);
            switchViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$NotificationSettingsGmailConfigFragment$AccountListAdapter$-2eqs2KTEYyGdqr4zPDaSgOMelY
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    AccountListAdapter.this.lambda$onBindViewHolder$0( str, compoundButton, z);
                }
            });
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public SwitchViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new SwitchViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_activity_switch_item, viewGroup, false));
        }
    }

    public void lambda$onCreate$0(AccountManagerFuture accountManagerFuture) {
        try {
            Account[] accountArr = (Account[]) accountManagerFuture.getResult();
            if (accountArr == null || accountArr.length <= 0) {
                return;
            }
            for (Account account : accountArr) {
                this.mAdapter.addAccount(account.name);
            }
        } catch (Exception unused) {
            this.getFragmentManager().popBackStack();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mController = NotificationController.getInstance(getActivity());
        this.mExtractor = (GmailExtractor) this.mController.getExtractorByKey("extractor.gmail");
        String[] strArr = {"service_mail"};
        if (ActivityCompat.checkSelfPermission(getActivity(), "android.permission.GET_ACCOUNTS") != 0) {
            getFragmentManager().popBackStack();
            return;
        }
        this.mEnabledAccounts = new HashSet();
        String[] enabledAccounts = this.mExtractor.getEnabledAccounts();
        if (enabledAccounts != null) {
            for (String str : enabledAccounts) {
                this.mEnabledAccounts.add(str);
            }
        }
        this.mAdapter = new AccountListAdapter();
        AccountManager.get(getActivity()).getAccountsByTypeAndFeatures("com.google", strArr, new AccountManagerCallback() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$NotificationSettingsGmailConfigFragment$MHmOGAcbi2JGEzHekyr_G5ESUqg
            @Override // android.accounts.AccountManagerCallback
            public final void run(AccountManagerFuture accountManagerFuture) {
                NotificationSettingsGmailConfigFragment.this.lambda$onCreate$0( accountManagerFuture);
            }
        }, null);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_notification_gmail_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
