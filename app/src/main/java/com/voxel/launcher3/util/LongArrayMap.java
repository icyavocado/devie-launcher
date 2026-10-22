package com.voxel.launcher3.util;

import android.util.LongSparseArray;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class LongArrayMap<E> extends LongSparseArray<E> implements Iterable<E> {

    class ValueIterator implements Iterator<E> {
        private int mNextIndex = 0;

        ValueIterator() {
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.mNextIndex < LongArrayMap.this.size();
        }

        @Override // java.util.Iterator
        public E next() {
            /* REWORK: jadx dropped the generic qualifier; the iterator returns E. */
            LongArrayMap<E> longArrayMap = LongArrayMap.this;
            int i = this.mNextIndex;
            this.mNextIndex = i + 1;
            return longArrayMap.valueAt(i);
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override // android.util.LongSparseArray
    public LongArrayMap<E> clone() {
        return (LongArrayMap) super.clone();
    }

    public boolean containsKey(long j) {
        return indexOfKey(j) >= 0;
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override // java.lang.Iterable
    public Iterator<E> iterator() {
        return new ValueIterator();
    }
}
