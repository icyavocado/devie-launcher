package com.evie.models.frequentlyused.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Maybe;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
@Dao
public interface AppLaunchDAO {
    @Query("DELETE FROM app_launch_entries WHERE id < :limit")
    int deleteAllOlderThanLimit(long limit);

    @Query("SELECT * FROM app_launch_entries ORDER BY id ASC")
    List<AppLaunchEntry> getAllEntries();

    // REWORK: room-rxjava2 was cut with the cloud deps; poll through the DAO instead.
    @Query("SELECT * FROM app_launch_entries ORDER BY id DESC LIMIT 1")
    AppLaunchEntry getMostRecentEntry();

    @Insert
    void insertAppLaunchEntry(AppLaunchEntry appLaunchEntry);
}
