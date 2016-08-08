package dk.nodes.nstack.util.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by joaoalves on 08/08/2016.
 */
public class TranslationsManager {

    public static final String PREFERENCES_DEFAULT = "translations_cache";

    SharedPreferences sharedPreferences;

    public enum Key {
        TRANSLATIONS
    }

    public static TranslationsManager with(@NonNull final Context context) {
        return new TranslationsManager(context.getSharedPreferences(PREFERENCES_DEFAULT, Context.MODE_PRIVATE));
    }

    public static TranslationsManager with(@NonNull final Context context, final String name) {
        return new TranslationsManager(context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    private TranslationsManager(final SharedPreferences sharedPreferences) {
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

    public TranslationsManager putString(@NonNull final String key, @NonNull final String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, string);
        editor.commit();
        return this;
    }

    public TranslationsManager putString(@NonNull final Key data, @NonNull final String string) {
        putString(data.name(), string);
        return this;
    }
}
