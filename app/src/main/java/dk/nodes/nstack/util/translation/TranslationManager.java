package dk.nodes.nstack.util.translation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.log.Logger;


/**
 * Created by joso on 25/02/15.
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
                    } else if (f.getType() == EditText.class) {
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
        // Flat / No sections
        if (translationOptions.isFlattenKeys()) {
            try {
                return String.valueOf(classType.getField(key).get(null));
            } catch (Exception e) {
                Logger.e("findValue failed on key: " + key + ". Exception -> " + e.toString());
                throw new IllegalArgumentException();
            }
        }
        // Sections
        else {
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
    }


    public void switchFallbackLanguage(@NonNull String languageHeader, final OnTranslationResultListener listener) {
        try {
            InputStream stream = NStack.getStack().getApplicationContext().getAssets().open(translationOptions.getFallbackFile());
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            String fallbackContents = new String(buffer);
            // Work around for something we should fix
            String oldLanguageHeader = translationOptions.getLanguageHeader();
            translationOptions.locale(languageHeader);
            updateTranslationClass(fallbackContents);
            translationOptions.locale(oldLanguageHeader);
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure();
            }
        }
    }

    public interface OnTranslationResultListener {
        void onSuccess();
        void onFailure();
    }

    public void updateTranslationsFromAppOpen(JSONObject root) {
        try {
            // No sections
            if (translationOptions.isFlattenKeys()) {
                parseFlatTranslations(root);
            }
            // Sections
            else {
                parseSections(root);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void updateTranslationLanguageKeys(JSONObject data) {
        try {
            Iterator<String> languageKeys = data.keys();
            boolean localeExists = data.has(translationOptions.getLanguageHeader());
            boolean fallbackLocaleExists = data.has(translationOptions.getFallbackLocale());

            while (languageKeys.hasNext()) {
                String languageName = languageKeys.next();
                // Only update current language, if we have more than one language
                if (localeExists && !languageName.equalsIgnoreCase(translationOptions.getLanguageHeader())) {
                    continue;
                }
                // Selected locale doesnt exist, continue to fallback
                if (!localeExists && fallbackLocaleExists && !languageName.equalsIgnoreCase(translationOptions.getFallbackLocale())) {
                    continue;
                }
                // fallback doesnt exist either, continue until we find something that matches fallbacks, ie: en-**
                if (!localeExists && !fallbackLocaleExists && !translationOptions.getFallbackLocale().startsWith(languageName.substring(0, 2))) {
                    continue;
                }
                Logger.d("updateTranslationLanguageKeys on: " + languageName);
                JSONObject translationObject = data.getJSONObject(languageName);
                // Save translation data into the App open cache, now that we have the correct language
                cacheManager.saveTranslations(translationObject.toString());
                translationOptions.setPickedLanguage(languageName);
                // No sections
                if (translationOptions.isFlattenKeys()) {
                    parseFlatTranslations(translationObject);
                }
                // Sections
                else {
                    parseSections(translationObject);
                }
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void parseSections(JSONObject sectionsObject) {
        Iterator<String> sectionKeys = sectionsObject.keys();
        while (sectionKeys.hasNext()) {
            String sectionKey = sectionKeys.next();

            try {
                JSONObject sectionObject = sectionsObject.getJSONObject(sectionKey);
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

    private void parseFlatTranslations(JSONObject jsonLanguage) {
        Iterator<String> translationKeys = jsonLanguage.keys();
        while (translationKeys.hasNext()) {
            String translationKey = translationKeys.next();
            // Reached actual translation string
            try {
                if (jsonLanguage.get(translationKey) instanceof String) {
                    updateField(classType, translationKey, jsonLanguage.getString(translationKey));
                }
            } catch (Exception e) {
                Logger.e("Parsing failed for key = " + translationKey);
            }
        }
    }

    public void updateTranslationClass(String jsonData) {
        try {
            JSONObject data = new JSONObject(jsonData);
            if (data.has("data")) {
                data = data.getJSONObject("data");
            }
            // Fetched more than one language
            if (translationOptions.allLanguages()) {
                // We have our locale in the response
                if (data.has(translationOptions.getLanguageHeader())) {

                }
                updateTranslationLanguageKeys(data);
                // Only one language
            } else {
                translationOptions.setPickedLanguage(translationOptions.getLanguageHeader());
                // Save translation data into the App open cache
                cacheManager.saveTranslations(jsonData);

                // No sections
                if (translationOptions.isFlattenKeys()) {
                    parseFlatTranslations(data);
                }
                // Sections
                else {
                    parseSections(data);
                }
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void updateField(Object object, String key, String value) {
        try {
            Field field = object.getClass().getField(key);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            Logger.e(e);
            Logger.e("TranslationManager", "Error updating field: " + key + " : " + value);
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

}