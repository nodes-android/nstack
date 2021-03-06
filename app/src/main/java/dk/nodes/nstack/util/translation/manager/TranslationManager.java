package dk.nodes.nstack.util.translation.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
        List<Field> fields = getFieldsFromClassAndSuper(view);
        for (Field f : fields) {
            Translate annotation = f.getAnnotation(Translate.class);
            if (annotation != null) {
                try {
                    String translation = findValue(annotation.value());
                    f.setAccessible(true);
                    String viewClass = f.getType().getSimpleName();
                    if (viewClass.contentEquals("Toolbar")) {
                        f.getType().getMethod("setTitle", CharSequence.class).invoke(f.get(view), translation);
                    } else if (viewClass.contentEquals("EditText") ||
                            viewClass.contentEquals("AppCompatEditText")
                            || viewClass.contentEquals("TextInputEditText")
                            || viewClass.contentEquals("TextInputLayout")) {
                        f.getType().getMethod("setHint", CharSequence.class).invoke(f.get(view), translation);
                    } else {
                        f.getType().getMethod("setText", CharSequence.class).invoke(f.get(view), translation);
                    }
                    f.getType().getMethod("setContentDescription", CharSequence.class).invoke(f.get(view), translation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Field> getFieldsFromClassAndSuper(@NonNull Object view) {
        List<Field> list = new LinkedList<>();
        list.addAll(Arrays.asList(view.getClass().getDeclaredFields()));
        if(view.getClass().getSuperclass() != null)
            list.addAll(Arrays.asList(view.getClass().getSuperclass().getDeclaredFields()));
        return list;
    }

    private String findValue(String key) throws IllegalArgumentException {
        // Flat / No sections
        if (translationOptions.isFlattenKeys()) {
            try {
                return String.valueOf(classType.getField(key).get(null));
            } catch (Exception e) {
                Logger.e("flat findValue failed on key: " + key + ". Exception -> " + e.toString());
                throw new IllegalArgumentException();
            }
        }

        // Sections
        try {
            String innerClassName = key.split("\\.")[0];
            String sectionKey = key.split("\\.")[1];
            Class<?> sectionClass = Class.forName(classType.getName() + "$" + innerClassName);
            Field field = sectionClass.getField(sectionKey);
            return String.valueOf(field.get(null));
        } catch (Exception e) {
            Logger.e("sections findValue failed on key: " + key + ". Exception -> " + e.toString());
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
        if (translationOptions.isFlattenKeys()) {
            parseFlatTranslations(jsonObject);
            return;
        }
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

    private void parseFlatTranslations(JSONObject jsonObject) {
        Iterator<String> translationKeys = jsonObject.keys();

        while (translationKeys.hasNext()) {
            String translationKey = translationKeys.next();

            // Reached actual translation string
            try {
                if (jsonObject.get(translationKey) instanceof String) {
                    updateField(classType, translationKey, jsonObject.getString(translationKey));
                }
            } catch (Exception e) {
                Logger.e("Parsing failed for key = " + translationKey);
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
                data = data.optJSONObject("data");
            }
            if (data != null) {
                cacheManager.setJsonTranslation(languageLocale, data.toString());
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
                data = data.optJSONObject("data");
            }

            Iterator<String> iterator = data.keys();
            while (iterator.hasNext()) {
                String languageLocale = iterator.next();
                JSONObject jsonTranslation = data.optJSONObject(languageLocale);
                if (jsonTranslation != null) {
                    cacheManager.setJsonTranslation(languageLocale, jsonTranslation.toString());
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