package dk.nodes.nstack.util.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
public class PrefsManager {

    SharedPreferences prefs;
    Context context;

    public enum Key {
        TRANSLATIONS,
        VERSION_INFO,
        GUID,
        LAST_UPDATED,
        SHOW_MESSAGE,
        RATE_REMINDER
    }

    public PrefsManager(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    private void save(String key, String data) {
        prefs.edit().putString(key, data).apply();
    }

    private String load(String key) {
        return prefs.getString(key, null);
    }

    public boolean hasTranslations() {
        return prefs.contains(Key.TRANSLATIONS.name());
    }

    public String getTranslations() {
        return prefs.getString(Key.TRANSLATIONS.name(), null);
    }

    public void saveTranslations(String translationsJson) {
        prefs.edit().putString(Key.TRANSLATIONS.name(), translationsJson).commit();
    }

    public String loadJsonTranslation(String languageLocale) {
        return prefs.getString(languageLocale, null);
    }

    public void saveJsonTranslation(String languageLocale, String translationsJson) {
        prefs.edit().putString(languageLocale, translationsJson).commit();
    }

    public boolean getShowMessage() {
        return prefs.getBoolean(Key.SHOW_MESSAGE.name(), true);
    }

    public void setShowMessage(boolean showMessage) {
        prefs.edit().putBoolean(Key.SHOW_MESSAGE.name(), showMessage).commit();
    }

    public boolean getRateReminder() {
        return prefs.getBoolean(Key.RATE_REMINDER.name(), true);
    }

    public void setRateReminder(boolean rateReminder) {
        prefs.edit().putBoolean(Key.RATE_REMINDER.name(), rateReminder).commit();
    }

    public String getLastUpdated() {
        return prefs.getString(Key.LAST_UPDATED.name(), null);
    }

    public void setLastUpdated(String lastUpdated) {
        prefs.edit().putString(Key.LAST_UPDATED.name(), lastUpdated).commit();
    }

    public void clearLastUpdated() {
        prefs.edit().remove(Key.LAST_UPDATED.name()).commit();
    }

    public String getGUI() {
        return prefs.getString(Key.GUID.name(), null);
    }

    public void setGUI(String gui) {
        prefs.edit().putString(Key.GUID.name(), gui).commit();
    }

    public String getVersionInfo() {
        return prefs.getString(Key.VERSION_INFO.name(), null);
    }

    public void setVersionInfo(String versionInfo) {
        prefs.edit().putString(Key.VERSION_INFO.name(), versionInfo).commit();
    }

    // File Cache
    public static void saveObject(Context context, String key, Object object) {
       FileCache.saveObject(context, key, object);
    }

    public static Object loadObject (Context context, String key) {
        return FileCache.loadObject(context, key);
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
