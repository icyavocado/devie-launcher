package com.voxel.simplesearchlauncher.model.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherBackupHelper;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class UserProfileManager {
    private static final String TAG = "UserProfileManager";
    private static UserProfileManager instance;
    private Context context;

    /* JADX INFO: loaded from: classes2.dex */
    public enum LauncherDefaultState {
        UNKNOWN,
        DEFAULT,
        NOT_DEFAULT
    }

    /* JADX INFO: loaded from: classes2.dex */
    public enum UserDataType {
        PROFILE_DATA,
        HOMESCREEN_ITEMS_DATA
    }

    private UserProfileManager(Context context) {
        this.context = context;
    }

    private SharedPreferences getActionDefaultItemsSharedPref() {
        return this.context.getSharedPreferences("action_default_items_pref", 0);
    }

    @Deprecated
    public static synchronized UserProfileManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance must be initialized before trying to use.");
        }
        return instance;
    }

    private SharedPreferences getUserProfilePref() {
        return this.context.getSharedPreferences("user_profile_pref", 0);
    }

    public static synchronized void init(Context context) {
        if (instance != null) {
            return;
        }
        if (context == null) {
            throw new IllegalArgumentException("Context must be provided.");
        }
        instance = new UserProfileManager(context);
    }

    public void clearDefaultActionItem(String str) {
        SharedPreferences.Editor editorEdit = getActionDefaultItemsSharedPref().edit();
        editorEdit.remove(str);
        editorEdit.apply();
    }

    public JSONObject getBackupUserData(String str) {
        JSONObject serializedFavorites;
        Map<String, ?> all = this.context.getSharedPreferences(str, 0).getAll();
        try {
            if (str.equals("launcher_data_favorites")) {
                try {
                    serializedFavorites = new LauncherBackupHelper(this.context, true).getSerializedFavorites();
                } catch (IOException e) {
                    e.printStackTrace();
                    serializedFavorites = null;
                }
            } else {
                JSONObject jSONObject = null;
                for (Map.Entry<String, ?> entry : all.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (key != null && value != null) {
                        if (jSONObject == null) {
                            jSONObject = new JSONObject();
                        }
                        JSONObject jSONObject2 = new JSONObject();
                        jSONObject2.put("class", value.getClass().getName());
                        jSONObject2.put("value", value);
                        jSONObject.put(key, jSONObject2);
                    }
                }
                serializedFavorites = jSONObject;
            }
            if (serializedFavorites == null) {
                return null;
            }
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("dataType", str);
            jSONObject3.put("data", serializedFavorites);
            return jSONObject3;
        } catch (JSONException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public String getDefaultActionItem(String str) {
        return getActionDefaultItemsSharedPref().getString(str, null);
    }

    public String getGoogleAccountEmail() {
        return getUserProfilePref().getString("google_account_email", null);
    }

    public LauncherDefaultState getLauncherDefaultState() {
        String string = getUserProfilePref().getString("launcher_default_state", null);
        return TextUtils.isEmpty(string) ? LauncherDefaultState.UNKNOWN : LauncherDefaultState.valueOf(string);
    }

    public String[] getRestorableUserDataTypeIds() {
        UserDataType[] restorableUserDataTypes = getRestorableUserDataTypes();
        String[] strArr = new String[restorableUserDataTypes.length + 4];
        for (int i = 0; i < restorableUserDataTypes.length; i++) {
            strArr[i] = getUserDataTypeId(restorableUserDataTypes[i]);
        }
        strArr[restorableUserDataTypes.length] = LauncherAppState.getSharedPreferencesKey();
        strArr[restorableUserDataTypes.length + 1] = LayoutSettingsManager.getSharedPreferencesKey();
        strArr[restorableUserDataTypes.length + 2] = IconPackManager.getSharedPreferencesKey();
        strArr[restorableUserDataTypes.length + 3] = SettingsStorage.getSharedPreferencesKey();
        return strArr;
    }

    public UserDataType[] getRestorableUserDataTypes() {
        return new UserDataType[]{UserDataType.PROFILE_DATA};
    }

    public String getUserDataTypeId(UserDataType userDataType) {
        switch (userDataType) {
            case PROFILE_DATA:
                return "user_profile_pref";
            case HOMESCREEN_ITEMS_DATA:
                return "launcher_data_favorites";
            default:
                throw new IllegalArgumentException("User data type not handled: " + userDataType.name());
        }
    }

    public void saveRestoreLauncherFavoritesData(JSONObject jSONObject) {
        HashMap map = new HashMap();
        Iterator<String> itKeys = jSONObject.keys();
        while (itKeys.hasNext()) {
            try {
                String next = itKeys.next();
                map.put(next, jSONObject.getString(next));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        new LauncherBackupHelper(this.context, true).restoreSerializedFavorites(map);
    }

    public void saveRestoredUserData(JSONObject jSONObject) {
        try {
            /* Whitelist shared-pref file names coming from a restore zip: only the
               prefs files the four managers actually own may be cleared/written. */
            String string = jSONObject.getString("dataType");
            JSONObject jSONObject2 = new JSONObject(jSONObject.getString("data"));
            if (string.equals("launcher_data_favorites")) {
                saveRestoreLauncherFavoritesData(jSONObject2);
                return;
            }
            if (!java.util.Arrays.asList(getRestorableUserDataTypeIds()).contains(string)) {
                Log.w(TAG, "Refusing to restore unlisted user data type " + string);
                return;
            }
            SharedPreferences.Editor editorEdit = this.context.getSharedPreferences(string, 0).edit();
            editorEdit.clear();
            Iterator<String> itKeys = jSONObject2.keys();
            while (itKeys.hasNext()) {
                try {
                    String next = itKeys.next();
                    JSONObject jSONObject3 = jSONObject2.getJSONObject(next);
                    String string2 = jSONObject3.getString("class");
                    Object obj = jSONObject3.get("value");
                    if (string2.equals(Boolean.class.getName())) {
                        editorEdit.putBoolean(next, ((Boolean) obj).booleanValue());
                    } else if (string2.equals(String.class.getName())) {
                        editorEdit.putString(next, (String) obj);
                    } else if (string2.equals(Float.class.getName())) {
                        if (obj instanceof Double) {
                            editorEdit.putFloat(next, ((Double) obj).floatValue());
                        } else if (obj instanceof Integer) {
                            editorEdit.putFloat(next, ((Integer) obj).floatValue());
                        } else {
                            editorEdit.putFloat(next, ((Float) obj).floatValue());
                        }
                    } else if (string2.equals(Integer.class.getName())) {
                        editorEdit.putInt(next, ((Integer) obj).intValue());
                    } else if (string2.equals(Long.class.getName())) {
                        if (obj instanceof Integer) {
                            editorEdit.putLong(next, Long.valueOf(((Integer) obj).intValue()).longValue());
                        } else {
                            editorEdit.putLong(next, ((Long) obj).longValue());
                        }
                    } else if (string2.equals(Set.class.getName())) {
                        editorEdit.putStringSet(next, (Set) obj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            editorEdit.apply();
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
    }

    public void setDefaultActionItem(String str, String str2) {
        SharedPreferences.Editor editorEdit = getActionDefaultItemsSharedPref().edit();
        editorEdit.putString(str, str2);
        editorEdit.putBoolean("has_set_default_action_item", true);
        editorEdit.apply();
    }

    public void setLauncherDefaultState(LauncherDefaultState launcherDefaultState) {
        SharedPreferences.Editor editorEdit = getUserProfilePref().edit();
        editorEdit.putString("launcher_default_state", launcherDefaultState.name());
        editorEdit.apply();
    }
}
