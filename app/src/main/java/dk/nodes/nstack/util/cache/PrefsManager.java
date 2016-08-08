package dk.nodes.nstack.util.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by joaoalves on 08/08/2016.
 */
public class PrefsManager {

    public static final String PREFERENCES_DEFAULT = "shared_prefs";

    SharedPreferences sharedPreferences;

    public enum Key {
        TRANSLATIONS
    }

    public static PrefsManager with(@NonNull final Context context) {
        return new PrefsManager(context.getSharedPreferences(PREFERENCES_DEFAULT, Context.MODE_PRIVATE));
    }

    public static PrefsManager with(@NonNull final Context context, final String name) {
        return new PrefsManager(context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    private PrefsManager(final SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /* Contains */

    public boolean contains(@NonNull final String key) {
        return sharedPreferences.contains(key);
    }

    public boolean contains(@NonNull final Key data) {
        return sharedPreferences.contains(data.name());
    }

    /* Clear */

    public void clear(@NonNull final String key) {
        sharedPreferences.edit().remove(key);
    }

    public void clear(@NonNull final Key data) {
        sharedPreferences.edit().remove(data.name()).commit();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().commit();
    }


    /* String */

    public String getString(@NonNull final String key) {
        return sharedPreferences.getString(key, null);
    }

    public String getString(@NonNull final Key data) {
        return sharedPreferences.getString(data.name(), null);
    }

    public PrefsManager putString(@NonNull final String key, @NonNull final String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, string);
        editor.commit();
        return this;
    }

    public PrefsManager putString(@NonNull final Key data, @NonNull final String string) {
        putString(data.name(), string);
        return this;
    }

    /* Boolean */

    public boolean getBoolean(@NonNull final Key data) {
        return sharedPreferences.getBoolean(data.name(), false);
    }

    public PrefsManager putBoolean(@NonNull final Key data, @NonNull final Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(data.name(), value);
        editor.commit();
        return this;
    }

    /* Int */

    public int getInt(@NonNull final Key data) {
        return sharedPreferences.getInt(data.name(), -1);
    }

    public PrefsManager putInt(@NonNull final Key data, @NonNull final int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(data.name(), value);
        editor.commit();
        return this;
    }
}
