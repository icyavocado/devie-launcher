package com.evie.screen;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import com.ovacoda.devie.R;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/* JADX INFO: loaded from: classes.dex */
public class TermsAndConditionsActivity extends AppCompatActivity {
    OkHttpClient mClient;

    FrameLayout mDialogLayout;

    ProgressBar mProgress;

    TextView mTextView;

    /* JADX INFO: Access modifiers changed from: private */
    public void loadText() {
        this.mTextView.setVisibility(8);
        this.mDialogLayout.removeAllViews();
        this.mDialogLayout.setVisibility(8);
        this.mProgress.setVisibility(0);
        new AsyncTask<Void, Void, String>() { // from class: com.evie.screen.TermsAndConditionsActivity.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public String doInBackground(Void... voidArr) {
                try {
                    Response responseExecute = TermsAndConditionsActivity.this.mClient.newCall(new Request.Builder().url("http://www.evie.com/appflash/tc.html").build()).execute();
                    if (!responseExecute.isSuccessful()) {
                        return null;
                    }
                    String strString = responseExecute.body().string();
                    responseExecute.close();
                    return strString;
                } catch (IOException unused) {
                    return null;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(String str) {
                if (str == null) {
                    TermsAndConditionsActivity.this.showErrorDialog();
                    return;
                }
                TermsAndConditionsActivity.this.mTextView.setVisibility(0);
                TermsAndConditionsActivity.this.mTextView.setText(Html.fromHtml(str));
                TermsAndConditionsActivity.this.mTextView.setMovementMethod(LinkMovementMethod.getInstance());
                TermsAndConditionsActivity.this.mDialogLayout.removeAllViews();
                TermsAndConditionsActivity.this.mDialogLayout.setVisibility(8);
                TermsAndConditionsActivity.this.mProgress.setVisibility(8);
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showErrorDialog() {
        this.mTextView.setVisibility(8);
        this.mDialogLayout.removeAllViews();
        this.mDialogLayout.setVisibility(0);
        this.mProgress.setVisibility(8);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.confab_generic_error_text)
                .setMessage(R.string.confab_generic_error_subtext)
                .setPositiveButton(R.string.confab_generic_error_button, (dialog, which) -> loadText())
                .show();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        AndroidInjection.inject(this);
        super.onCreate(bundle);
        setContentView(R.layout.terms_and_conditions);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.terms_and_conditions_title);
        loadText();
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        onBackPressed();
        return true;
    }
}
