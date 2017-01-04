package dk.nodes.nstack.util.translation.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.translation.Translate;
import dk.nodes.nstack.util.translation.options.TranslationOptions;

//import android.support.design.widget.TextInputEditText;
//import android.support.design.widget.TextInputLayout;


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
                String view_class = f.getType().getSimpleName();
                Class<?> cls = f.getType();
                try {
                    if (f.getType() == Toolbar.class || f.getType() == android.widget.Toolbar.class) {
                        Toolbar toolbar = (Toolbar) f.get(view);
                        toolbar.setTitle(translation);
                        toolbar.setContentDescription(translation);
                    } else if (f.getType() == EditText.class ||
                            f.getType() == AppCompatEditText.class
                            || view_class.contentEquals("TextInputEditText")) {
                        EditText editText = (EditText) f.get(view);
                        editText.setHint(translation);
                        editText.setContentDescription(translation);
                    } else if (view_class.contentEquals("TextInputLayout")) {
                        Class[] cArg = new Class[1];
                        cArg[0] = CharSequence.class;
                        Method setHint = cls.getMethod("setHint", cArg);
                        setHint.invoke(f.get(view), translation);

                        Method set_content_description = cls.getMethod("setContentDescription", cArg);
                        set_content_description.invoke(f.get(view), translation);
                    } else {
                        TextView textView = (TextView) f.get(view);
                        textView.setText(translation);
                        textView.setContentDescription(translation);
                    }
                } catch (Exception e) {
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


    public void switchFallbackLanguage(@NonNull String languageHeader, @NonNull final OnTranslationResultListener callback) {
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
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFailure();
        }
    }

    public void updateTranslationsFromAppOpen(JSONObject root) {
        parseTranslations(root);
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
                parseTranslations(translationObject);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void parseTranslations(JSONObject jsonObject) {
        if (translationOptions.isFlattenKeys()) {
            parseFlatTranslation(jsonObject);
        } else {
            parseSections(jsonObject);
        }
    }

    private void parseFlatTranslation(JSONObject jsonObject) {
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String translationKey = iterator.next();
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

    private void parseSections(JSONObject jsonObject) {
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
                parseTranslations(data);
            }
        } catch (Exception e) {
            Logger.e(e);
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