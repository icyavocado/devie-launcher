package com.voxel.launcher3.fresco;

import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;

/* REWORK: jadx kept this state in the enclosing class; the nested-class
 * declaration refused to link against the outer generic extends clause under
 * javac, so it moved to a top-level class of the same package. */
public class RetryOkHttpNetworkFetchState extends FetchState {
    public long fetchCompleteTime;
    public long responseTime;
    public long submitTime;

    public RetryOkHttpNetworkFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        super(consumer, producerContext);
    }
}
