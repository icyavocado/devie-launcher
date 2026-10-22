package com.evie.search.recyclerview.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class MessageViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_message;
    private TextView mText;
    private TextView mTitle;

    private MessageViewHolder(View view) {
        super(view);
        this.mTitle = (TextView) view.findViewById(R.id.search_message_title);
        this.mText = (TextView) view.findViewById(R.id.search_message_text);
    }

    public static MessageViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new MessageViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(String str, String str2) {
        this.mTitle.setText(str);
        this.mText.setText(str2);
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mTitle.setText((CharSequence) null);
        this.mText.setText((CharSequence) null);
    }
}
