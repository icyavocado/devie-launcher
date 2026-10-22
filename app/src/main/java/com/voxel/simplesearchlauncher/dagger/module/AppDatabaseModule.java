package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import androidx.room.Room;
import com.evie.models.frequentlyused.room.AppLaunchDatabase;

/* JADX INFO: loaded from: classes2.dex */
public abstract class AppDatabaseModule {
    public static AppLaunchDatabase providesAppDatabase(Context context) {
        return (AppLaunchDatabase) Room.databaseBuilder(context.getApplicationContext(), AppLaunchDatabase.class, "app_database").build();
    }
}
