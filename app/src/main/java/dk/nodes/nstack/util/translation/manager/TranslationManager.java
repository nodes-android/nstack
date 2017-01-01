package dk.nodes.nstack.util.translation.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Iterator;

import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.translation.Translate;
import dk.nodes.nstack.util.translation.options.TranslationOptions;


/**
 * Created by joso on 25/02/15.
 * Edited by Mario on 30/12/16
 */
public class TranslationManager {

    private static Class<?> classType;
    private TranslationOptions translationOptions;
    private CacheManager cacheManager;

    public TranslationManager(Context context, TranslationOptions translationOptions) {
        cacheManager = new CacheManager(context);
        this.translationOptions = translationOptions;
    }

    public void setTranslationClass(Class<?> translationClass) {
        classType = translationClass;
    }

    public void translate(@NonNull Object view) {
        Field[] fields = view.getClass().getDeclaredFields();
        for (Field f : fields) {
            Translate annotation = f.getAnnotation(Translate.class);
            if (annotation != null) {
                String translation = findValue(annotation.value());
                f.setAccessible(true);
                try {
                    if (f.getType() == Toolbar.class || f.getType() == android.widget.Toolbar.class) {
                        Toolbar toolbar = (Toolbar) f.get(view);
                        toolbar.setTitle(translation);
                    } else if (f.getType() == EditText.class ||
                            f.getType() == AppCompatEditText.class ||
                            f.getType() == TextInputEditText.class) {
                        EditText editText = (EditText) f.get(view);
                        editText.setHint(translation);
                    } else if (f.getType() == TextInputLayout.class) {
                        TextInputLayout til = (TextInputLayout) f.get(view);
                        til.setHint(translation);
                    } else {
                        TextView textView = (TextView) f.get(view);
                        textView.setText(translation);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String findValue(String key) throws IllegalArgumentException {
        try {
            String innerClassName = key.split("\\.")[0];
            String sectionKey = key.split("\\.")[1];
            Class<?> sectionClass = Class.forName(classType.getName() + "$" + innerClassName);
            Field field = sectionClass.getField(sectionKey);
            return String.valueOf(field.get(null));
        } catch (Exception e) {
            Logger.e("findValue failed on key: " + key + ". Exception -> " + e.toString());
            throw new IllegalArgumentException();
        }
    }

    public void updateTranslationClass(String jsonData) {
        try {
            JSONObject data = new JSONObject(jsonData);
            if (data.has("data")) {
                data = data.getJSONObject("data");
            }
            parseTranslations(data);
        } catch (Exception e) {
            Logger.e(e);
        }
    }


    private void parseTranslations(JSONObject jsonObject) {

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String sectionKey = iterator.next();
            try {
                JSONObject sectionObject = jsonObject.getJSONObject(sectionKey);
                Iterator<String> translationKeys = sectionObject.keys();
                if (sectionKey.equalsIgnoreCase("default")) {
                    sectionKey = "defaultSection";
                }
                Class<?> sectionClass = Class.forName(classType.getName() + "$" + sectionKey);
                while (translationKeys.hasNext()) {
                    String translationKey = translationKeys.next();
                    // Reached actual translation string
                    if (sectionObject.get(translationKey) instanceof String) {
                        updateField(sectionClass, translationKey, sectionObject.getString(translationKey));
                    }
                }
            } catch (Exception e) {
                Logger.e("Parsing failed for section -> " + sectionKey + " | " + e.toString());
            }
        }
    }

    private void updateField(Class<?> classType, String key, String value) {
        try {
            Field field = classType.getField(key);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            Logger.e(e);
            Logger.e("TranslationManager", "Error updating field: " + key + " : " + value);
        }
    }

    /**
     * Saves 1 languages translations into prefs
     *
     * @param languageLocale
     * @param jsonData
     */
    public void saveLanguageTranslation(String languageLocale, String jsonData) {
        try {
            JSONObject data = new JSONObject(jsonData);
            if (data.has("data")) {
                JSONObject jsonTranslation = data.optJSONObject("data");
                if (jsonTranslation != null) {
                    cacheManager.setJsonTranslation(languageLocale, jsonTranslation.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all of the languages translations into prefs
     *
     * @param jsonData
     */
    public void saveLanguagesTranslation(String jsonData) {
        try {
            JSONObject data = new JSONObject(jsonData);
            if (data.has("data")) {
                JSONObject jsonTranslations = data.getJSONObject("data");
                Iterator<String> iterator = jsonTranslations.keys();
                while (iterator.hasNext()) {
                    String languageLocale = iterator.next();
                    JSONObject jsonTranslation = jsonTranslations.optJSONObject(languageLocale);
                    if (jsonTranslation != null) {
                        cacheManager.setJsonTranslation(languageLocale, jsonTranslation.toString());
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    /**
     * Gets language translations from prefs if exists
     *
     * @param languageLocale
     * @return
     */
    public boolean getCacheLanguageTranslation(String languageLocale) {
        if (cacheManager.getJsonTranslation(languageLocale) != null) {
            JSONObject jsonTranslation;
            try {
                jsonTranslation = new JSONObject(cacheManager.getJsonTranslation(languageLocale));
            } catch (JSONException e) {
                return false;
            }
            parseTranslations(jsonTranslation);
            return true;
        }
        return false;
    }


    public void saveLanguages(String jsonData) {
        cacheManager.setJsonLanguages(jsonData);
    }

    public JSONObject getCacheLanguages() {
        if (cacheManager.getJsonLanguages() != null) {
            try {
                return new JSONObject(cacheManager.getJsonLanguages());
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }

    public TranslationOptions getTranslationOptions() {
        return translationOptions;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}