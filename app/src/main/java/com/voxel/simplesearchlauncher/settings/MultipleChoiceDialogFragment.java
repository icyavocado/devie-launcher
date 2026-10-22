package com.voxel.simplesearchlauncher.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class MultipleChoiceDialogFragment extends DialogFragment {
    private int mSelectedIndex;

    private class ListAdapter extends ArrayAdapter<ListItem> {
        final int mResourceId;

        ListAdapter(Context context, int i, List<ListItem> list) {
            super(context, i, list);
            this.mResourceId = i;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public long getItemId(int i) {
            return getItem(i).id;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(this.mResourceId, viewGroup, false);
            }
            ListItem item = getItem(i);
            ((TextView) view.findViewById(R.id.line1)).setText(item.labelResId);
            if (item.iconResId != 0) {
                ImageView imageView = (ImageView) view.findViewById(R.id.icon);
                imageView.setImageResource(item.iconResId);
                imageView.setVisibility(0);
            }
            if (item.subLabelResId != 0) {
                TextView textView = (TextView) view.findViewById(R.id.line2);
                textView.setText(item.subLabelResId);
                textView.setVisibility(0);
            }
            ((RadioButton) view.findViewById(R.id.radio_button)).setChecked(i == MultipleChoiceDialogFragment.this.mSelectedIndex);
            return view;
        }
    }

    public static class ListItem {
        final int iconResId;
        final int id;
        final int labelResId;
        final int subLabelResId;

        public ListItem(int i, int i2, int i3, int i4) {
            this.id = i;
            this.iconResId = i2;
            this.labelResId = i3;
            this.subLabelResId = i4;
        }
    }

    public void lambda$onCreateDialog$0(AdapterView adapterView, View view, int i, long j) {
        Intent intent = new Intent();
        intent.putExtra("ID", j);
        this.dismiss();
        this.getParentFragment().onActivityResult(this.getTargetRequestCode(), -1, intent);
    }

    public static MultipleChoiceDialogFragment newInstance(int i, int i2, List<ListItem> list) {
        return newInstance(i, i2, list, R.layout.dialog_radiobutton_item);
    }

    public static MultipleChoiceDialogFragment newInstance(int i, int i2, List<ListItem> list, int i3) {
        MultipleChoiceDialogFragment multipleChoiceDialogFragment = new MultipleChoiceDialogFragment();
        Bundle bundle = new Bundle();
        ArrayList<Integer> arrayList = new ArrayList<>(list.size());
        ArrayList<Integer> arrayList2 = new ArrayList<>(list.size());
        ArrayList<Integer> arrayList3 = new ArrayList<>(list.size());
        ArrayList<Integer> arrayList4 = new ArrayList<>(list.size());
        for (ListItem listItem : list) {
            arrayList.add(Integer.valueOf(listItem.id));
            arrayList2.add(Integer.valueOf(listItem.iconResId));
            arrayList3.add(Integer.valueOf(listItem.labelResId));
            arrayList4.add(Integer.valueOf(listItem.subLabelResId));
        }
        bundle.putInt("TITLE", i);
        bundle.putInt("SELECTED", i2);
        bundle.putIntegerArrayList("IDS", arrayList);
        bundle.putIntegerArrayList("ICONS", arrayList2);
        bundle.putIntegerArrayList("LABELS", arrayList3);
        bundle.putIntegerArrayList("SUB_LABELS", arrayList4);
        bundle.putInt("ITEM_RES_ID", i3);
        multipleChoiceDialogFragment.setArguments(bundle);
        return multipleChoiceDialogFragment;
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View viewInflate = getActivity().getLayoutInflater().inflate(R.layout.dialog_list, (ViewGroup) null);
        this.mSelectedIndex = getArguments().getInt("SELECTED", -1);
        int i = getArguments().getInt("ITEM_RES_ID", R.layout.dialog_radiobutton_item);
        ArrayList<Integer> integerArrayList = getArguments().getIntegerArrayList("IDS");
        ArrayList<Integer> integerArrayList2 = getArguments().getIntegerArrayList("ICONS");
        ArrayList<Integer> integerArrayList3 = getArguments().getIntegerArrayList("LABELS");
        ArrayList<Integer> integerArrayList4 = getArguments().getIntegerArrayList("SUB_LABELS");
        ArrayList arrayList = new ArrayList();
        if (integerArrayList != null && integerArrayList3 != null) {
            for (int i2 = 0; i2 < integerArrayList.size() && i2 < integerArrayList3.size(); i2++) {
                arrayList.add(new ListItem(integerArrayList.get(i2).intValue(), integerArrayList2.get(i2).intValue(), integerArrayList3.get(i2).intValue(), integerArrayList4.get(i2).intValue()));
            }
        }
        ListAdapter listAdapter = new ListAdapter(getActivity(), i, arrayList);
        ListView listView = (ListView) viewInflate.findViewById(R.id.list);
        listView.setAdapter((android.widget.ListAdapter) listAdapter);
        listView.setChoiceMode(1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$MultipleChoiceDialogFragment$1oeXaKwqxmwLwA-lDxGSOdtQI9Y
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i3, long j) {
                MultipleChoiceDialogFragment.this.lambda$onCreateDialog$0( adapterView, view, i3, j);
            }
        });
        builder.setTitle(getArguments().getInt("TITLE")).setView(viewInflate).setCancelable(true).setNegativeButton(R.string.button_label_cancel, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$MultipleChoiceDialogFragment$tYpgJErBCVO1DQhlyNFjxSZadys
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }
}
