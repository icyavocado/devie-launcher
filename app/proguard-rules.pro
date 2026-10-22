# Custom R8 rules (rework base):
# ButterKnife 8.8.1 binds views reflectively: Class.forName(host + "_ViewBinding").
-keep class **_ViewBinding { *; }
-keep class **_ViewBinding$* { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Launcher items cross process/bundle boundaries.
-if class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
}

# Fresco: native pipeline + AAR bytecode calls SoLoaderShim by exact FQCN.
-keep class com.facebook.** { *; }
-keep class com.facebook.common.soloader.SoLoaderShim$Handler { *; }
-keep interface com.facebook.common.soloader.SoLoaderShim$Handler { *; }

# Rx subjects/disposables are reflected by schedulers (defensive; R8 usually fine).
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**

# TransparentSettingsActivity receives fragment class names via the
# "show_fragment" intent extra and instantiates them reflectively
# (Fragment.instantiate -> Class.forName); keep their obfuscation names.
-keepnames class * extends androidx.fragment.app.Fragment

# Room: the runtime reflects "<db>_Impl" by name and its constructor.
-keepnames class * extends androidx.room.RoomDatabase
-keep class **_Impl { <init>(...); }
-dontoptimize

# ButterKnife 8.x binds reflectively at runtime: Class.forName(host.getName() + "_ViewBinding").
# R8 renamed holder/fragment classes while the *_ViewBinding classes kept their original
# names, so the lookups missed and view fields stayed null (settings list NPE).
# Keeping all our class names keeps every runtime name-lookup intact; size savings come
# from tree-shaking + resource shrink, not renaming.
-keepnames class com.voxel.**
-keepnames class com.evie.**
-keepnames class com.ovacoda.**
