package com.voxel.recyclerview.widget;

import androidx.core.util.Pools;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class AdapterHelper implements OpReorderer.Callback {
    final Callback mCallback;
    final boolean mDisableRecycler;
    Runnable mOnItemProcessedCallback;
    final OpReorderer mOpReorderer;
    final ArrayList<UpdateOp> mPendingUpdates;
    final ArrayList<UpdateOp> mPostponedList;
    private Pools.Pool<UpdateOp> mUpdateOpPool;

    interface Callback {
        RecyclerView.ViewHolder findViewHolder(int i);

        void markViewHoldersUpdated(int i, int i2);

        void offsetPositionsForAdd(int i, int i2);

        void offsetPositionsForMove(int i, int i2);

        void offsetPositionsForRemovingInvisible(int i, int i2);

        void offsetPositionsForRemovingLaidOutOrNewView(int i, int i2);

        void onDispatchFirstPass(UpdateOp updateOp);

        void onDispatchSecondPass(UpdateOp updateOp);
    }

    static class UpdateOp {
        int cmd;
        int itemCount;
        int positionStart;

        UpdateOp(int i, int i2, int i3) {
            this.cmd = i;
            this.positionStart = i2;
            this.itemCount = i3;
        }

        String cmdToString() {
            switch (this.cmd) {
                case 0:
                    return "add";
                case 1:
                    return "rm";
                case 2:
                    return "up";
                case 3:
                    return "mv";
                default:
                    return "??";
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            UpdateOp updateOp = (UpdateOp) obj;
            int i = this.cmd;
            if (i != updateOp.cmd) {
                return false;
            }
            if (i == 3 && Math.abs(this.itemCount - this.positionStart) == 1 && this.itemCount == updateOp.positionStart && this.positionStart == updateOp.itemCount) {
                return true;
            }
            return this.itemCount == updateOp.itemCount && this.positionStart == updateOp.positionStart;
        }

        public int hashCode() {
            return (((this.cmd * 31) + this.positionStart) * 31) + this.itemCount;
        }

        public String toString() {
            return "[" + cmdToString() + ",s:" + this.positionStart + "c:" + this.itemCount + "]";
        }
    }

    AdapterHelper(Callback callback) {
        this(callback, false);
    }

    AdapterHelper(Callback callback, boolean z) {
        this.mUpdateOpPool = new Pools.SimplePool(30);
        this.mPendingUpdates = new ArrayList<>();
        this.mPostponedList = new ArrayList<>();
        this.mCallback = callback;
        this.mDisableRecycler = z;
        this.mOpReorderer = new OpReorderer(this);
    }

    private void applyAdd(UpdateOp updateOp) {
        postponeAndUpdateViewHolders(updateOp);
    }

    private void applyMove(UpdateOp updateOp) {
        postponeAndUpdateViewHolders(updateOp);
    }

    private void applyRemove(UpdateOp updateOp) {
        boolean z;
        byte b;
        int i = updateOp.positionStart;
        int i2 = updateOp.positionStart + updateOp.itemCount;
        int i3 = updateOp.positionStart;
        int i4 = 0;
        byte b2 = -1;
        while (i3 < i2) {
            if (this.mCallback.findViewHolder(i3) != null || canFindInPreLayout(i3)) {
                if (b2 == 0) {
                    dispatchAndUpdateViewHolders(obtainUpdateOp(1, i, i4));
                    z = true;
                } else {
                    z = false;
                }
                b = 1;
            } else {
                if (b2 == 1) {
                    postponeAndUpdateViewHolders(obtainUpdateOp(1, i, i4));
                    z = true;
                } else {
                    z = false;
                }
                b = 0;
            }
            if (z) {
                i3 -= i4;
                i2 -= i4;
                i4 = 1;
            } else {
                i4++;
            }
            i3++;
            b2 = b;
        }
        if (i4 != updateOp.itemCount) {
            recycleUpdateOp(updateOp);
            updateOp = obtainUpdateOp(1, i, i4);
        }
        if (b2 == 0) {
            dispatchAndUpdateViewHolders(updateOp);
        } else {
            postponeAndUpdateViewHolders(updateOp);
        }
    }

    private void applyUpdate(UpdateOp updateOp) {
        int i = updateOp.positionStart;
        int i2 = updateOp.positionStart + updateOp.itemCount;
        int i3 = i;
        int i4 = 0;
        byte b = -1;
        for (int i5 = updateOp.positionStart; i5 < i2; i5++) {
            if (this.mCallback.findViewHolder(i5) != null || canFindInPreLayout(i5)) {
                if (b == 0) {
                    dispatchAndUpdateViewHolders(obtainUpdateOp(2, i3, i4));
                    i3 = i5;
                    i4 = 0;
                }
                b = 1;
            } else {
                if (b == 1) {
                    postponeAndUpdateViewHolders(obtainUpdateOp(2, i3, i4));
                    i3 = i5;
                    i4 = 0;
                }
                b = 0;
            }
            i4++;
        }
        if (i4 != updateOp.itemCount) {
            recycleUpdateOp(updateOp);
            updateOp = obtainUpdateOp(2, i3, i4);
        }
        if (b == 0) {
            dispatchAndUpdateViewHolders(updateOp);
        } else {
            postponeAndUpdateViewHolders(updateOp);
        }
    }

    private boolean canFindInPreLayout(int i) {
        int size = this.mPostponedList.size();
        for (int i2 = 0; i2 < size; i2++) {
            UpdateOp updateOp = this.mPostponedList.get(i2);
            if (updateOp.cmd == 3) {
                if (findPositionOffset(updateOp.itemCount, i2 + 1) == i) {
                    return true;
                }
            } else if (updateOp.cmd == 0) {
                int i3 = updateOp.positionStart + updateOp.itemCount;
                for (int i4 = updateOp.positionStart; i4 < i3; i4++) {
                    if (findPositionOffset(i4, i2 + 1) == i) {
                        return true;
                    }
                }
            } else {
                continue;
            }
        }
        return false;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void dispatchAndUpdateViewHolders(UpdateOp updateOp) {
        int i;
        boolean z;
        if (updateOp.cmd == 0 || updateOp.cmd == 3) {
            throw new IllegalArgumentException("should not dispatch add or move for pre layout");
        }
        int iUpdatePositionWithPostponed = updatePositionWithPostponed(updateOp.positionStart, updateOp.cmd);
        int i2 = updateOp.positionStart;
        switch (updateOp.cmd) {
            case 1:
                i = 0;
                break;
            case 2:
                i = 1;
                break;
            default:
                throw new IllegalArgumentException("op should be remove or update." + updateOp);
        }
        int i3 = iUpdatePositionWithPostponed;
        int i4 = i2;
        int i5 = 1;
        for (int i6 = 1; i6 < updateOp.itemCount; i6++) {
            int iUpdatePositionWithPostponed2 = updatePositionWithPostponed(updateOp.positionStart + (i * i6), updateOp.cmd);
            switch (updateOp.cmd) {
                case 1:
                    z = iUpdatePositionWithPostponed2 == i3;
                    break;
                case 2:
                    z = iUpdatePositionWithPostponed2 == i3 + 1;
                    break;
                default:
                    z = false;
                    break;
            }
            if (z) {
                i5++;
            } else {
                UpdateOp updateOpObtainUpdateOp = obtainUpdateOp(updateOp.cmd, i3, i5);
                dispatchFirstPassAndUpdateViewHolders(updateOpObtainUpdateOp, i4);
                recycleUpdateOp(updateOpObtainUpdateOp);
                if (updateOp.cmd == 2) {
                    i4 += i5;
                }
                i3 = iUpdatePositionWithPostponed2;
                i5 = 1;
            }
        }
        recycleUpdateOp(updateOp);
        if (i5 > 0) {
            UpdateOp updateOpObtainUpdateOp2 = obtainUpdateOp(updateOp.cmd, i3, i5);
            dispatchFirstPassAndUpdateViewHolders(updateOpObtainUpdateOp2, i4);
            recycleUpdateOp(updateOpObtainUpdateOp2);
        }
    }

    private void postponeAndUpdateViewHolders(UpdateOp updateOp) {
        this.mPostponedList.add(updateOp);
        switch (updateOp.cmd) {
            case 0:
                this.mCallback.offsetPositionsForAdd(updateOp.positionStart, updateOp.itemCount);
                return;
            case 1:
                this.mCallback.offsetPositionsForRemovingLaidOutOrNewView(updateOp.positionStart, updateOp.itemCount);
                return;
            case 2:
                this.mCallback.markViewHoldersUpdated(updateOp.positionStart, updateOp.itemCount);
                return;
            case 3:
                this.mCallback.offsetPositionsForMove(updateOp.positionStart, updateOp.itemCount);
                return;
            default:
                throw new IllegalArgumentException("Unknown update op type for " + updateOp);
        }
    }

    private int updatePositionWithPostponed(int i, int i2) {
        int i3;
        int i4;
        for (int size = this.mPostponedList.size() - 1; size >= 0; size--) {
            UpdateOp updateOp = this.mPostponedList.get(size);
            if (updateOp.cmd == 3) {
                if (updateOp.positionStart < updateOp.itemCount) {
                    i3 = updateOp.positionStart;
                    i4 = updateOp.itemCount;
                } else {
                    i3 = updateOp.itemCount;
                    i4 = updateOp.positionStart;
                }
                if (i < i3 || i > i4) {
                    if (i < updateOp.positionStart) {
                        if (i2 == 0) {
                            updateOp.positionStart++;
                            updateOp.itemCount++;
                        } else if (i2 == 1) {
                            updateOp.positionStart--;
                            updateOp.itemCount--;
                        }
                    }
                } else if (i3 == updateOp.positionStart) {
                    if (i2 == 0) {
                        updateOp.itemCount++;
                    } else if (i2 == 1) {
                        updateOp.itemCount--;
                    }
                    i++;
                } else {
                    if (i2 == 0) {
                        updateOp.positionStart++;
                    } else if (i2 == 1) {
                        updateOp.positionStart--;
                    }
                    i--;
                }
            } else if (updateOp.positionStart <= i) {
                if (updateOp.cmd == 0) {
                    i -= updateOp.itemCount;
                } else if (updateOp.cmd == 1) {
                    i += updateOp.itemCount;
                }
            } else if (i2 == 0) {
                updateOp.positionStart++;
            } else if (i2 == 1) {
                updateOp.positionStart--;
            }
        }
        for (int size2 = this.mPostponedList.size() - 1; size2 >= 0; size2--) {
            UpdateOp updateOp2 = this.mPostponedList.get(size2);
            if (updateOp2.cmd == 3) {
                if (updateOp2.itemCount == updateOp2.positionStart || updateOp2.itemCount < 0) {
                    this.mPostponedList.remove(size2);
                    recycleUpdateOp(updateOp2);
                }
            } else if (updateOp2.itemCount <= 0) {
                this.mPostponedList.remove(size2);
                recycleUpdateOp(updateOp2);
            }
        }
        return i;
    }

    public int applyPendingUpdatesToPosition(int i) {
        int size = this.mPendingUpdates.size();
        for (int i2 = 0; i2 < size; i2++) {
            UpdateOp updateOp = this.mPendingUpdates.get(i2);
            int i3 = updateOp.cmd;
            if (i3 != 3) {
                switch (i3) {
                    case 0:
                        if (updateOp.positionStart <= i) {
                            i += updateOp.itemCount;
                        }
                        break;
                    case 1:
                        if (updateOp.positionStart > i) {
                            continue;
                        } else {
                            if (updateOp.positionStart + updateOp.itemCount > i) {
                                return -1;
                            }
                            i -= updateOp.itemCount;
                        }
                        break;
                }
            } else if (updateOp.positionStart == i) {
                i = updateOp.itemCount;
            } else {
                if (updateOp.positionStart < i) {
                    i--;
                }
                if (updateOp.itemCount <= i) {
                    i++;
                }
            }
        }
        return i;
    }

    void consumePostponedUpdates() {
        int size = this.mPostponedList.size();
        for (int i = 0; i < size; i++) {
            this.mCallback.onDispatchSecondPass(this.mPostponedList.get(i));
        }
        recycleUpdateOpsAndClearList(this.mPostponedList);
    }

    void consumeUpdatesInOnePass() {
        consumePostponedUpdates();
        int size = this.mPendingUpdates.size();
        for (int i = 0; i < size; i++) {
            UpdateOp updateOp = this.mPendingUpdates.get(i);
            switch (updateOp.cmd) {
                case 0:
                    this.mCallback.onDispatchSecondPass(updateOp);
                    this.mCallback.offsetPositionsForAdd(updateOp.positionStart, updateOp.itemCount);
                    break;
                case 1:
                    this.mCallback.onDispatchSecondPass(updateOp);
                    this.mCallback.offsetPositionsForRemovingInvisible(updateOp.positionStart, updateOp.itemCount);
                    break;
                case 2:
                    this.mCallback.onDispatchSecondPass(updateOp);
                    this.mCallback.markViewHoldersUpdated(updateOp.positionStart, updateOp.itemCount);
                    break;
                case 3:
                    this.mCallback.onDispatchSecondPass(updateOp);
                    this.mCallback.offsetPositionsForMove(updateOp.positionStart, updateOp.itemCount);
                    break;
            }
            Runnable runnable = this.mOnItemProcessedCallback;
            if (runnable != null) {
                runnable.run();
            }
        }
        recycleUpdateOpsAndClearList(this.mPendingUpdates);
    }

    void dispatchFirstPassAndUpdateViewHolders(UpdateOp updateOp, int i) {
        this.mCallback.onDispatchFirstPass(updateOp);
        switch (updateOp.cmd) {
            case 1:
                this.mCallback.offsetPositionsForRemovingInvisible(i, updateOp.itemCount);
                return;
            case 2:
                this.mCallback.markViewHoldersUpdated(i, updateOp.itemCount);
                return;
            default:
                throw new IllegalArgumentException("only remove and update ops can be dispatched in first pass");
        }
    }

    int findPositionOffset(int i) {
        return findPositionOffset(i, 0);
    }

    int findPositionOffset(int i, int i2) {
        int size = this.mPostponedList.size();
        while (i2 < size) {
            UpdateOp updateOp = this.mPostponedList.get(i2);
            if (updateOp.cmd == 3) {
                if (updateOp.positionStart == i) {
                    i = updateOp.itemCount;
                } else {
                    if (updateOp.positionStart < i) {
                        i--;
                    }
                    if (updateOp.itemCount <= i) {
                        i++;
                    }
                }
            } else if (updateOp.positionStart > i) {
                continue;
            } else if (updateOp.cmd == 1) {
                if (i < updateOp.positionStart + updateOp.itemCount) {
                    return -1;
                }
                i -= updateOp.itemCount;
            } else if (updateOp.cmd == 0) {
                i += updateOp.itemCount;
            }
            i2++;
        }
        return i;
    }

    boolean hasPendingUpdates() {
        return this.mPendingUpdates.size() > 0;
    }

    @Override // com.voxel.recyclerview.widget.OpReorderer.Callback
    public UpdateOp obtainUpdateOp(int i, int i2, int i3) {
        UpdateOp updateOpAcquire = this.mUpdateOpPool.acquire();
        if (updateOpAcquire == null) {
            return new UpdateOp(i, i2, i3);
        }
        updateOpAcquire.cmd = i;
        updateOpAcquire.positionStart = i2;
        updateOpAcquire.itemCount = i3;
        return updateOpAcquire;
    }

    boolean onItemRangeChanged(int i, int i2) {
        this.mPendingUpdates.add(obtainUpdateOp(2, i, i2));
        return this.mPendingUpdates.size() == 1;
    }

    boolean onItemRangeInserted(int i, int i2) {
        this.mPendingUpdates.add(obtainUpdateOp(0, i, i2));
        return this.mPendingUpdates.size() == 1;
    }

    boolean onItemRangeRemoved(int i, int i2) {
        this.mPendingUpdates.add(obtainUpdateOp(1, i, i2));
        return this.mPendingUpdates.size() == 1;
    }

    void preProcess() {
        this.mOpReorderer.reorderOps(this.mPendingUpdates);
        int size = this.mPendingUpdates.size();
        for (int i = 0; i < size; i++) {
            UpdateOp updateOp = this.mPendingUpdates.get(i);
            switch (updateOp.cmd) {
                case 0:
                    applyAdd(updateOp);
                    break;
                case 1:
                    applyRemove(updateOp);
                    break;
                case 2:
                    applyUpdate(updateOp);
                    break;
                case 3:
                    applyMove(updateOp);
                    break;
            }
            Runnable runnable = this.mOnItemProcessedCallback;
            if (runnable != null) {
                runnable.run();
            }
        }
        this.mPendingUpdates.clear();
    }

    @Override // com.voxel.recyclerview.widget.OpReorderer.Callback
    public void recycleUpdateOp(UpdateOp updateOp) {
        if (this.mDisableRecycler) {
            return;
        }
        this.mUpdateOpPool.release(updateOp);
    }

    void recycleUpdateOpsAndClearList(List<UpdateOp> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            recycleUpdateOp(list.get(i));
        }
        list.clear();
    }

    void reset() {
        recycleUpdateOpsAndClearList(this.mPendingUpdates);
        recycleUpdateOpsAndClearList(this.mPostponedList);
    }
}
