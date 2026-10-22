package com.evie.common.licenses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import timber.log.Timber;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class LicenseDialogFragment extends DialogFragment {
    private int licenseRawRes;

    public static LicenseDialogFragment newInstance(int i) {
        LicenseDialogFragment licenseDialogFragment = new LicenseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("license_raw_resource_extra", i);
        licenseDialogFragment.setArguments(bundle);
        return licenseDialogFragment;
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        /* REWORK: jadx tangled the license read into nested rethrow chains; restored
         * to a simple read-all-lines that falls back to empty text. */
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.licenseRawRes = arguments.getInt("license_raw_resource_extra");
        }
        String string;
        InputStream inputStreamOpenRawResource = null;
        try {
            inputStreamOpenRawResource = getActivity().getResources().openRawResource(this.licenseRawRes);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamOpenRawResource, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                sb.append(line);
                sb.append('\n');
            }
            string = sb.toString();
            try {
                bufferedReader.close();
            } catch (IOException unused) {
                Timber.e("Error closing bufferedReader.", new Object[0]);
            }
        } catch (Resources.NotFoundException | IOException unused2) {
            Timber.e("Error reading from licenses.txt", new Object[0]);
            string = null;
        } catch (RuntimeException unused3) {
            Timber.e("Error reading from licenses.txt", new Object[0]);
            string = null;
        } finally {
            if (inputStreamOpenRawResource != null) {
                try {
                    inputStreamOpenRawResource.close();
                } catch (IOException unused4) {
                    Timber.e("Error closing inputStream.", new Object[0]);
                }
            }
        }
        View viewInflate = LayoutInflater.from(getActivity()).inflate(R.layout.open_source_licenses, (ViewGroup) null);
        ((TextView) viewInflate.findViewById(R.id.text)).setText(string);
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_open_source_title).setView(viewInflate).create();
    }
}
