package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.view.GestureDetectorCompat;
import com.voxel.recyclerview.widget.SpringRecyclerView;

/* JADX INFO: loaded from: classes2.dex */
public class NoItemClickRecyclerView extends SpringRecyclerView {
    private GestureDetectorCompat gestureDetector;
    private boolean isTouchDownEmptyRegion;
    private OnNoItemClickListener mOnNoItemClickListener;

    public interface OnNoItemClickListener {
        void onNoItemClicked();
    }

    public NoItemClickRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.isTouchDownEmptyRegion = false;
        initialize();
    }

    private void initialize() {
        this.gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() { // from class: com.voxel.simplesearchlauncher.view.NoItemClickRecyclerView.1
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if (!NoItemClickRecyclerView.this.isTouchDownEmptyRegion || NoItemClickRecyclerView.this.mOnNoItemClickListener == null) {
                    return true;
                }
                NoItemClickRecyclerView.this.mOnNoItemClickListener.onNoItemClicked();
                return true;
            }
        });
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.isTouchDownEmptyRegion = findChildViewUnder((float) ((int) motionEvent.getX()), (float) ((int) motionEvent.getY())) == null;
        }
        this.gestureDetector.onTouchEvent(motionEvent);
        return super.dispatchTouchEvent(motionEvent);
    }

    public void setOnNoItemClickListener(OnNoItemClickListener onNoItemClickListener) {
        this.mOnNoItemClickListener = onNoItemClickListener;
    }
}
