package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.view.View;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.MessageViewHolder;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class MessagePresenter implements BaseItemPresenter {
    private MessageViewHolder mBoundViewHolder;
    private int mHeight;
    private final String mText;
    private final String mTitle;

    public MessagePresenter(Context context, String str, String str2) {
        this.mTitle = str;
        this.mText = str2;
        this.mHeight = context.getResources().getDimensionPixelSize(R.dimen.search_message_item_height);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (MessageViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        this.mBoundViewHolder.bindItem(this.mTitle, this.mText);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return MessageViewHolder.ID;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        return false;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void unbind(BaseViewHolder baseViewHolder) {
        MessageViewHolder messageViewHolder = this.mBoundViewHolder;
        if (messageViewHolder == baseViewHolder) {
            messageViewHolder.bindPresenter(null);
            this.mBoundViewHolder = null;
        }
    }
}
