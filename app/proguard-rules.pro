# Custom R8 rules (rework base):

# Launcher items cross process/bundle boundaries.
-if class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
}

# Fresco: removed. REWORK(fresco): the pipeline is gone; rebound still lives
# under com.facebook.rebound but is plain Java (no reflection binding).

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
