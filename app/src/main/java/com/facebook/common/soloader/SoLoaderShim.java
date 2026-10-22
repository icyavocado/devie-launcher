package com.facebook.common.soloader;

/* REWORK(vendor): this decompiled one-file shim is INTENTIONALLY kept under the
 * com.facebook package. Fresco's compiled AAR classes
 * (com.facebook.imagepipeline.nativecode.ImagePipelineNativeLoader) call
 * SoLoaderShim.loadLibrary by this exact package/name at runtime, so it cannot
 * be renamed or inlined into app code. BaseApplication registers a handler that
 * routes the loads through ReLinker. No network/account/telemetry surface. */
public class SoLoaderShim {
    private static volatile Handler sHandler = new DefaultHandler();

    public static class DefaultHandler implements Handler {
        @Override // com.facebook.common.soloader.SoLoaderShim.Handler
        public void loadLibrary(String str) {
            System.loadLibrary(str);
        }
    }

    public interface Handler {
        void loadLibrary(String str);
    }

    public static void loadLibrary(String str) {
        sHandler.loadLibrary(str);
    }

    public static void setHandler(Handler handler) {
        if (handler == null) {
            throw new NullPointerException("Handler cannot be null");
        }
        sHandler = handler;
    }
}
