package dk.nodes.nstack.util.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dk.nodes.nstack.util.log.Logger;
import okhttp3.Cache;

/**
 * Created by joso on 19/11/15.
 */
public class CacheManager {

    public static final String PREFERENCES_DEFAULT = "shared_prefs";

    SharedPreferences sharedPreferences;
    Context context;

    public enum Key {
        TRANSLATIONS,
        VERSION_INFO_KEY,
        GUID_KEY,
        LAST_UPDATED_KEY,
        SHOW_MESSAGE_KEY,
        RATE_REMINDER_KEY
    }

    public CacheManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_DEFAULT, Context.MODE_PRIVATE);
        this.context = context;
    }

    public boolean hasTranslations() {
        return contains(Key.TRANSLATIONS);
    }

    public String getTranslations() {
        return getString(Key.TRANSLATIONS);
    }

    public void saveTranslations(String translationsJson) {
        putString(CacheManager.Key.TRANSLATIONS, translationsJson);
    }

    public boolean showMessage() {
        return getBoolean(Key.SHOW_MESSAGE_KEY);
    }

    public void updateShowMessage(boolean showMessage) {
        putBoolean(Key.SHOW_MESSAGE_KEY, showMessage);
    }

    public boolean showRateReminder() {
        return getBoolean(Key.RATE_REMINDER_KEY);
    }

    public void updateShowRateReminder(boolean showMessage) {
        putBoolean(Key.RATE_REMINDER_KEY, showMessage);
    }

    public String getLastUpdated() {
        return getString(Key.LAST_UPDATED_KEY);
    }

    public void setLastUpdated(String lastUpdated) {
        putString(Key.LAST_UPDATED_KEY, lastUpdated);
    }

    public void clearLastUpdated() {
        clear(Key.LAST_UPDATED_KEY);
    }

    public String getGUI() {
        return getString(Key.GUID_KEY);
    }

    public void setGUI(String gui) {
        putString(Key.GUID_KEY, gui);
    }

    public String getVersionInfo() {
        return getString(Key.VERSION_INFO_KEY);
    }

    public void setVersionInfo(String versionInfo) {
        putString(Key.VERSION_INFO_KEY, versionInfo);
    }

    // File Cache
    public static void saveObject(Context context, String key, Object object) {
        try {
            FileOutputStream fos = context.openFileOutput (key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch( Exception e ) {
            Logger.e(e);
        }
    }

    public static Object loadObject (Context context, String key) {
        try {
            FileInputStream fis = context.openFileInput(key);
            ObjectInputStream ois = new ObjectInputStream (fis);
            Object object = ois.readObject ();
            return object;
        } catch( Exception e ) {
            Logger.e(e);
        }

        return null;
    }

    // Shared Prefs handling

    private boolean contains(@NonNull final CacheManager.Key data) {
        return sharedPreferences.contains(data.name());
    }

    private String getString(@NonNull final Key data) {
        return sharedPreferences.getString(data.name(), null);
    }

    private void putString(@NonNull final String key, @NonNull final String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, string);
        editor.commit();
    }

    private void putString(@NonNull final Key data, @NonNull final String string) {
        putString(data.name(), string);
    }

    // Used for rate reminder and message and should return true by default
    private boolean getBoolean(@NonNull final Key data) {
        return sharedPreferences.getBoolean(data.name(), true);
    }

    private void putBoolean(@NonNull final Key data, @NonNull final Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(data.name(), value);
        editor.commit();
    }

    private void clear(@NonNull final Key data) {
        sharedPreferences.edit().remove(data.name()).commit();
    }

    public Cache initCache() {
        try {
            File cacheDirectory = context.getCacheDir();

            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(cacheDirectory, cacheSize);

            return cache;
        } catch (Exception e) {
            Logger.e(e);
        }

        return null;
    }

}
