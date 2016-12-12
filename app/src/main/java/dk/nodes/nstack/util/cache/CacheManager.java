package dk.nodes.nstack.util.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dk.nodes.nstack.util.log.Logger;
/**
 * Created by joso on 19/11/15.
 */
public class CacheManager {

    public static final String PREFERENCES_DEFAULT = "shared_prefs";

    SharedPreferences sharedPreferences;

    public enum Key {
        TRANSLATIONS,
        VERSION_INFO_KEY,
        GUID_KEY,
        LAST_UPDATED_KEY,
        SHOW_MESSAGE_KEY,
        RATE_REMINDER_KEY
    }

    public static CacheManager with(@NonNull final Context context) {
        return new CacheManager(context.getSharedPreferences(PREFERENCES_DEFAULT, Context.MODE_PRIVATE));
    }

    public static CacheManager with(@NonNull final Context context, final String name) {
        return new CacheManager(context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    private CacheManager(final SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    // Shared Prefs

    public boolean contains(@NonNull final CacheManager.Key data) {
        return sharedPreferences.contains(data.name());
    }

    public String getString(@NonNull final Key data) {
        return sharedPreferences.getString(data.name(), null);
    }

    public void putString(@NonNull final String key, @NonNull final String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, string);
        editor.commit();
    }

    public void putString(@NonNull final Key data, @NonNull final String string) {
        putString(data.name(), string);
    }

    // Used for rate reminder and message and should return true by default
    public boolean getBoolean(@NonNull final Key data) {
        return sharedPreferences.getBoolean(data.name(), true);
    }

    public void putBoolean(@NonNull final Key data, @NonNull final Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(data.name(), value);
        editor.commit();
    }

    public void clear(@NonNull final Key data) {
        sharedPreferences.edit().remove(data.name()).commit();
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

}
