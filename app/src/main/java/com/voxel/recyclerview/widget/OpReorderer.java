package com.voxel.recyclerview.widget;

import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class OpReorderer {
    final Callback mCallback;

    interface Callback {
        AdapterHelper.UpdateOp obtainUpdateOp(int i, int i2, int i3);

        void recycleUpdateOp(AdapterHelper.UpdateOp updateOp);
    }

    public OpReorderer(Callback callback) {
        this.mCallback = callback;
    }

    private int getLastMoveOutOfOrder(List<AdapterHelper.UpdateOp> list) {
        boolean z = false;
        for (int size = list.size() - 1; size >= 0; size--) {
            if (list.get(size).cmd != 3) {
                z = true;
            } else if (z) {
                return size;
            }
        }
        return -1;
    }

    private void swapMoveAdd(List<AdapterHelper.UpdateOp> list, int i, AdapterHelper.UpdateOp updateOp, int i2, AdapterHelper.UpdateOp updateOp2) {
        int i3 = updateOp.itemCount < updateOp2.positionStart ? -1 : 0;
        if (updateOp.positionStart < updateOp2.positionStart) {
            i3++;
        }
        if (updateOp2.positionStart <= updateOp.positionStart) {
            updateOp.positionStart += updateOp2.itemCount;
        }
        if (updateOp2.positionStart <= updateOp.itemCount) {
            updateOp.itemCount += updateOp2.itemCount;
        }
        updateOp2.positionStart += i3;
        list.set(i, updateOp2);
        list.set(i2, updateOp);
    }

    private void swapMoveOp(List<AdapterHelper.UpdateOp> list, int i, int i2) {
        AdapterHelper.UpdateOp updateOp = list.get(i);
        AdapterHelper.UpdateOp updateOp2 = list.get(i2);
        switch (updateOp2.cmd) {
            case 0:
                swapMoveAdd(list, i, updateOp, i2, updateOp2);
                break;
            case 1:
                swapMoveRemove(list, i, updateOp, i2, updateOp2);
                break;
            case 2:
                swapMoveUpdate(list, i, updateOp, i2, updateOp2);
                break;
        }
    }

    void reorderOps(List<AdapterHelper.UpdateOp> list) {
        while (true) {
            int lastMoveOutOfOrder = getLastMoveOutOfOrder(list);
            if (lastMoveOutOfOrder == -1) {
                return;
            } else {
                swapMoveOp(list, lastMoveOutOfOrder, lastMoveOutOfOrder + 1);
            }
        }
    }

    /* JADX WARN: Code duplicated, block: B:33:0x008c  */
    /* JADX WARN: Code duplicated, block: B:35:0x0098 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:36:0x009a  */
    /* JADX WARN: Code duplicated, block: B:37:0x009c  */
    /* JADX WARN: Code duplicated, block: B:39:0x00a2  */
    /* JADX WARN: Code duplicated, block: B:42:0x00af  */
    /* JADX WARN: Code duplicated, block: B:45:0x00bc  */
    /* JADX WARN: Code duplicated, block: B:48:0x00c9  */
    /* JADX WARN: Code duplicated, block: B:49:0x00d1  */
    /* JADX WARN: Code duplicated, block: B:50:0x00d3  */
    /* JADX WARN: Code duplicated, block: B:52:0x00d9  */
    /* JADX WARN: Code duplicated, block: B:55:0x00e6  */
    /* JADX WARN: Code duplicated, block: B:58:0x00f3  */
    /* JADX WARN: Code duplicated, block: B:61:0x0100  */
    /* JADX WARN: Code duplicated, block: B:64:0x0110  */
    /* JADX WARN: Code duplicated, block: B:65:0x0114  */
    /* JADX WARN: Code duplicated, block: B:67:0x0119  */
    /* JADX WARN: Code duplicated, block: B:70:? A[RETURN, SYNTHETIC] */
    void swapMoveRemove(List<AdapterHelper.UpdateOp> list, int i, AdapterHelper.UpdateOp updateOp, int i2, AdapterHelper.UpdateOp updateOp2) {
        boolean z;
        AdapterHelper.UpdateOp updateOpObtainUpdateOp = null; /* REWORK: jadx blank register */
        boolean z2 = false;
        if (updateOp.positionStart < updateOp.itemCount) {
            if (updateOp2.positionStart == updateOp.positionStart && updateOp2.itemCount == updateOp.itemCount - updateOp.positionStart) {
                z = false;
                z2 = true;
            } else {
                z = false;
            }
        } else if (updateOp2.positionStart == updateOp.itemCount + 1 && updateOp2.itemCount == updateOp.positionStart - updateOp.itemCount) {
            z = true;
            z2 = true;
        } else {
            z = true;
        }
        if (updateOp.itemCount < updateOp2.positionStart) {
            updateOp2.positionStart--;
        } else if (updateOp.itemCount < updateOp2.positionStart + updateOp2.itemCount) {
            updateOp2.itemCount--;
            updateOp.cmd = 1;
            updateOp.itemCount = 1;
            if (updateOp2.itemCount == 0) {
                list.remove(i2);
                this.mCallback.recycleUpdateOp(updateOp2);
                return;
            }
            return;
        }
        if (updateOp.positionStart > updateOp2.positionStart) {
            if (updateOp.positionStart < updateOp2.positionStart + updateOp2.itemCount) {
                updateOpObtainUpdateOp = this.mCallback.obtainUpdateOp(1, updateOp.positionStart + 1, (updateOp2.positionStart + updateOp2.itemCount) - updateOp.positionStart);
                updateOp2.itemCount = updateOp.positionStart - updateOp2.positionStart;
            }
            if (z2) {
                list.set(i, updateOp2);
                list.remove(i2);
                this.mCallback.recycleUpdateOp(updateOp);
                return;
            }
            if (z) {
                if (updateOpObtainUpdateOp != null) {
                    if (updateOp.positionStart > updateOpObtainUpdateOp.positionStart) {
                        updateOp.positionStart -= updateOpObtainUpdateOp.itemCount;
                    }
                    if (updateOp.itemCount > updateOpObtainUpdateOp.positionStart) {
                        updateOp.itemCount -= updateOpObtainUpdateOp.itemCount;
                    }
                }
                if (updateOp.positionStart > updateOp2.positionStart) {
                    updateOp.positionStart -= updateOp2.itemCount;
                }
                if (updateOp.itemCount > updateOp2.positionStart) {
                    updateOp.itemCount -= updateOp2.itemCount;
                }
            } else {
                if (updateOpObtainUpdateOp != null) {
                    if (updateOp.positionStart >= updateOpObtainUpdateOp.positionStart) {
                        updateOp.positionStart -= updateOpObtainUpdateOp.itemCount;
                    }
                    if (updateOp.itemCount >= updateOpObtainUpdateOp.positionStart) {
                        updateOp.itemCount -= updateOpObtainUpdateOp.itemCount;
                    }
                }
                if (updateOp.positionStart >= updateOp2.positionStart) {
                    updateOp.positionStart -= updateOp2.itemCount;
                }
                if (updateOp.itemCount >= updateOp2.positionStart) {
                    updateOp.itemCount -= updateOp2.itemCount;
                }
            }
            list.set(i, updateOp2);
            if (updateOp.positionStart != updateOp.itemCount) {
                list.set(i2, updateOp);
            } else {
                list.remove(i2);
            }
            if (updateOpObtainUpdateOp != null) {
                list.add(i, updateOpObtainUpdateOp);
            }
        }
        updateOp2.positionStart++;
        updateOpObtainUpdateOp = null;
        if (z2) {
            list.set(i, updateOp2);
            list.remove(i2);
            this.mCallback.recycleUpdateOp(updateOp);
            return;
        }
        if (z) {
            if (updateOpObtainUpdateOp != null) {
                if (updateOp.positionStart > updateOpObtainUpdateOp.positionStart) {
                    updateOp.positionStart -= updateOpObtainUpdateOp.itemCount;
                }
                if (updateOp.itemCount > updateOpObtainUpdateOp.positionStart) {
                    updateOp.itemCount -= updateOpObtainUpdateOp.itemCount;
                }
            }
            if (updateOp.positionStart > updateOp2.positionStart) {
                updateOp.positionStart -= updateOp2.itemCount;
            }
            if (updateOp.itemCount > updateOp2.positionStart) {
                updateOp.itemCount -= updateOp2.itemCount;
            }
        } else {
            if (updateOpObtainUpdateOp != null) {
                if (updateOp.positionStart >= updateOpObtainUpdateOp.positionStart) {
                    updateOp.positionStart -= updateOpObtainUpdateOp.itemCount;
                }
                if (updateOp.itemCount >= updateOpObtainUpdateOp.positionStart) {
                    updateOp.itemCount -= updateOpObtainUpdateOp.itemCount;
                }
            }
            if (updateOp.positionStart >= updateOp2.positionStart) {
                updateOp.positionStart -= updateOp2.itemCount;
            }
            if (updateOp.itemCount >= updateOp2.positionStart) {
                updateOp.itemCount -= updateOp2.itemCount;
            }
        }
        list.set(i, updateOp2);
        if (updateOp.positionStart != updateOp.itemCount) {
            list.set(i2, updateOp);
        } else {
            list.remove(i2);
        }
        if (updateOpObtainUpdateOp != null) {
            list.add(i, updateOpObtainUpdateOp);
        }
    }

    /* JADX WARN: Code duplicated, block: B:11:0x002d  */
    /* JADX WARN: Code duplicated, block: B:12:0x0033  */
    /* JADX WARN: Code duplicated, block: B:14:0x003c  */
    /* JADX WARN: Code duplicated, block: B:17:0x0059  */
    /* JADX WARN: Code duplicated, block: B:18:0x005d  */
    /* JADX WARN: Code duplicated, block: B:20:0x0067  */
    /* JADX WARN: Code duplicated, block: B:22:0x006c  */
    /* JADX WARN: Code duplicated, block: B:24:? A[RETURN, SYNTHETIC] */
    void swapMoveUpdate(List<AdapterHelper.UpdateOp> list, int i, AdapterHelper.UpdateOp updateOp, int i2, AdapterHelper.UpdateOp updateOp2) {
        AdapterHelper.UpdateOp updateOpObtainUpdateOp = null; /* REWORK: jadx blank register */
        AdapterHelper.UpdateOp updateOpObtainUpdateOp2 = null;
        if (updateOp.itemCount >= updateOp2.positionStart) {
            if (updateOp.itemCount < updateOp2.positionStart + updateOp2.itemCount) {
                updateOp2.itemCount--;
                updateOpObtainUpdateOp = this.mCallback.obtainUpdateOp(2, updateOp.positionStart, 1);
            }
            if (updateOp.positionStart <= updateOp2.positionStart) {
                updateOp2.positionStart++;
            } else if (updateOp.positionStart < updateOp2.positionStart + updateOp2.itemCount) {
                int i3 = (updateOp2.positionStart + updateOp2.itemCount) - updateOp.positionStart;
                updateOpObtainUpdateOp2 = this.mCallback.obtainUpdateOp(2, updateOp.positionStart + 1, i3);
                updateOp2.itemCount -= i3;
            }
            list.set(i2, updateOp);
            if (updateOp2.itemCount > 0) {
                list.set(i, updateOp2);
            } else {
                list.remove(i);
                this.mCallback.recycleUpdateOp(updateOp2);
            }
            if (updateOpObtainUpdateOp != null) {
                list.add(i, updateOpObtainUpdateOp);
            }
            if (updateOpObtainUpdateOp2 != null) {
                list.add(i, updateOpObtainUpdateOp2);
            }
        }
        updateOp2.positionStart--;
        updateOpObtainUpdateOp = null;
        if (updateOp.positionStart <= updateOp2.positionStart) {
            updateOp2.positionStart++;
        } else if (updateOp.positionStart < updateOp2.positionStart + updateOp2.itemCount) {
            int i4 = (updateOp2.positionStart + updateOp2.itemCount) - updateOp.positionStart;
            updateOpObtainUpdateOp2 = this.mCallback.obtainUpdateOp(2, updateOp.positionStart + 1, i4);
            updateOp2.itemCount -= i4;
        }
        list.set(i2, updateOp);
        if (updateOp2.itemCount > 0) {
            list.set(i, updateOp2);
        } else {
            list.remove(i);
            this.mCallback.recycleUpdateOp(updateOp2);
        }
        if (updateOpObtainUpdateOp != null) {
            list.add(i, updateOpObtainUpdateOp);
        }
        if (updateOpObtainUpdateOp2 != null) {
            list.add(i, updateOpObtainUpdateOp2);
        }
    }
}
