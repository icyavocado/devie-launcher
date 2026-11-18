import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "com.ovacoda.devie"

    /* REWORK: English-only packaging was reverted by user decision — the
       donor's locale tables ship again (all values-dirs included). */
    compileSdk = 35

    /* REWORK: short commit id baked into BuildConfig for the settings version
       footer. Falls back when git is unavailable (e.g. bare CI export). */
    val gitSha: String = try {
        providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }.standardOutput.asText.get().trim()
    } catch (t: Throwable) {
        "unknown"
    }

    defaultConfig {
        applicationId = "com.ovacoda.devie"
        minSdk = 21
        targetSdk = 35
        versionCode = 21408029
        versionName = "2.14.8-rw10.3"
        buildConfigField("String", "GIT_SHA", "\"$gitSha\"")
    }

    signingConfigs {
        getByName("debug") {
            /* REWORK: pin the debug signing key to the persistent .docker-home
               keystore — the container HOME is recreated per run, so default
               debug keys changed every rebuild. */
            storeFile = rootProject.file(".docker-home/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("evie") {
            // REWORK: generate a fresh self-signed key for this reworked package;
            // passwords live in keystore/evie-release.properties (git-ignored).
            val props = Properties().apply {
                val f = rootProject.file("keystore/evie-release.properties")
                if (f.exists()) {
                    FileInputStream(f).use { load(it) }
                }
            }
            storeFile = rootProject.file("keystore/evie-release.jks")
            storePassword = props.getProperty("storePassword")
            keyAlias = props.getProperty("keyAlias")
            keyPassword = props.getProperty("keyPassword")
        }
        // REWORK: pin debug signing to the persisted keystore. The gradle
        // daemon's user.home is /home/gradle (not HOME=/tmp/evie), so the
        // default debug keystore lived in the ephemeral container and every
        // build re-signed with a fresh key, forcing uninstalls per rebuild.
        // Standard debug credentials apply (android / androiddebugkey).
        getByName("debug") {
            val f = rootProject.file(".docker-home/debug.keystore")
            if (f.exists()) {
                storeFile = f
            }
        }
    }

    buildTypes {
        release {
            /* REWORK: R8 on — the rework compiles clean; shrinkResources drops
               res-table entries the compiler cannot reach. */
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            /* REWORK: ship the phone ABIs only — the donor APK carries the same
               pair; x86/x86_64 stay available in debug builds for emulator
               testing (release on an x86 emulator image won't install). */
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
            /* REWORK(ci): sign only when the keystore is present — CI runs without
               the git-ignored keystore and still needs to publish the APK. */
            if (rootProject.file("keystore/evie-release.properties").exists()) {
                signingConfig = signingConfigs.getByName("evie")
            }
        }
        /* REWORK: release-signed but debuggable — installs as an in-place update
           over release (same signature, data preserved) while run-as keeps the
           app DB inspectable for live device debugging. */
        create("phoneDebug") {
            initWith(getByName("release"))
            isDebuggable = true
            matchingFallbacks += listOf("release")
        }
    }

    lint {
        /* Release lint re-enabled after the 2026-11 res pass. Policy:
           translation/decompile-style noise is warnings (the donor ships the
           whole locale table and int-literal constants are decompile style);
           crash-risk checks stay fatal. */
        checkReleaseBuilds = true
        disable += listOf(
            /* AGP 9.4.1 vendor bug: timber.lint.WrongTimberUsageDetector calls
               context.getMainProject() during module analysis; its own report
               recommends disabling until upstream fixes it. */
            "LogNotTimber",
            "StringFormatInTimber",
            "ThrowableNotAtBeginning",
            "BinaryOperationInTimber",
            "TimberArgCount",
            "TimberArgTypes",
            "TimberTagLength",
            "TimberExceptionLogging",
        )
        warning += listOf(
            "ExtraTranslation",
            "MissingTranslation",
            "WrongConstant",
            "UnusedResources",
            "PrivateResource",
            "TypographyEllipsis",
            "TypographyDashes",
            "ObsoleteSdkInt",
            "GradleDependency",
            "RedundantNamespace",
            "DiscouragedApi",
            "RestrictedApi",
            "UseRequiresApi",
            "NotifyDataSetChanged",
            "LongLogTag",
            "ContentDescription",
            "ClickableViewAccessibility",
            "DefaultLocale",
            "ObsoleteLayoutParam",
            "UselessParent",
            /* Full res-auto/com.voxel.launcher3 namespaces in the
               default_workspace XMLs are donor-required: the dex
               attribute-lookup helpers match that exact form. */
            "ResAuto",
            /* Cycles live in the libraries' own copies of the material styles
               (decompile-merged res table). */
            "ResourceCycle",
            /* Donor-carried lib res that only exists in qualifier folders
               (design_snackbar, app_widget_preview, qsb); the app runs on all
               devices without querying them in unqualified configs. */
            "MissingDefaultResource",
            /* Vendor res copies carried in our tree (abc_*, design_*). */
            "UseAndroidAlpha",
        )
        /* Donor-arch facts that lint cannot verify (reconstructed styleables,
           decompile flow overrides, donor permissions) are snapshotted in the
           baseline; new issues still fail the build. */
        baseline = file("lint-baseline.xml")
    }

    buildFeatures {
        buildConfig = true // AGP 9 defaults it off; launcher code imports com.ovacoda.devie.BuildConfig
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Grind aid: show the whole error inventory per pass, not javac's 100-error cap.
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-Xmaxwarns", "10000"))
}

dependencies {
    // AndroidX (versions pinned to those resolvable for compileSdk 35)
    implementation("androidx.appcompat:appcompat:1.7.0")
    // REWORK: layout classes the decompile merge shipped as res-table entries
    // only (styles shadow fine; attrs and material styleables were deduped out
    // of values/attrs.xml — libs now provide them, with the R classes the
    // library code needs at runtime).
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment:1.8.6")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.collection:collection:1.4.4")
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    // REWORK: the decompiled AppLaunchDAO_Impl/AppLaunchDatabase_Impl refused to
    // build against Room 2.6.1's API, so the _Impl classes are regenerated by the
    // annotation processor from the annotated @Dao/@Database/@Entity sources.
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Third-party as originally used (decompiled generated classes target these exact versions)
    // REWORK(fresco): Fresco + okhttp + ReLinker dropped — all image loads are
    // now handled by com.ovacoda.devie.images.IconLoader (local URIs only).
    implementation("com.facebook.rebound:rebound:0.3.8")
    implementation("com.google.dagger:dagger:2.22.1")
    implementation("com.google.dagger:dagger-android:2.22.1")
    implementation("com.google.dagger:dagger-android-support:2.22.1")
    implementation("com.google.protobuf.nano:protobuf-javanano:3.1.0")
    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")

    // androidx.appcompat pins profileinstaller 1.3.1, which reports
    // RESULT_UNSUPPORTED_ART_VERSION on API 34+ (the user's phone runs 16).
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    // REWORK(fresco): Retrofit/OkHttp removed — no network consumers remain
    // after the fresco pipeline deletion (ConnectionPoolKiller died with it).
}
