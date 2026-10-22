package com.evie.models.frequentlyused.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/* JADX INFO: loaded from: classes.dex */
@Database(entities = {AppLaunchEntry.class}, version = 1, exportSchema = false)
public abstract class AppLaunchDatabase extends RoomDatabase {
    public abstract AppLaunchDAO appLaunchDAO();
}
