package dk.nodes.nstack.util.translation;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.model.Language;


/**
 * Created by joso on 25/02/15.
 */
public class TranslationManager {

    private static TranslationManager instance = null;
    private static Class<?> classType;
    private TranslationOptions translationOptions = new TranslationOptions();

    public TranslationManager() {

    }

    public void setTranslationClass(Class<?> translationClass) {
        classType = translationClass;
    }

    public TranslationOptions options() {
        return translationOptions;
    }

    public static TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }

        return instance;
    }

    public static void translate(@NonNull Object view) {
        Field[] fields = view.getClass().getDeclaredFields();

        for (Field f : fields) {
            Translate annotation = f.getAnnotation(Translate.class);

            if (annotation != null) {

                if (f.getType() == Button.class || f.getType() == TextView.class || f.getType() == AppCompatButton.class || f.getType() == AppCompatTextView.class || f.getType() == SwitchCompat.class) {

                    try {
                        f.setAccessible(true);
                        TextView fieldTextView = (TextView) f.get(view);

                        try {
                            fieldTextView.setText(findValue(annotation.value()));
                            fieldTextView.setContentDescription(findValue(annotation.value()));
                        } catch (IllegalArgumentException e) {
                            fieldTextView.setText(annotation.value());
                            fieldTextView.setContentDescription(annotation.value());
                        }

                    } catch (Exception e) {
                        Logger.d("Method.invoke error: " + e.toString());
                    }
                } else if (f.getType() == TextInputEditText.class || f.getType() == EditText.class || f.getType() == AppCompatEditText.class) {

                    try {
                        f.setAccessible(true);
                        EditText fieldEditText = (EditText) f.get(view);

                        try {
                            if (fieldEditText.getParent() instanceof TextInputLayout) {
                                TextInputLayout til = (TextInputLayout) fieldEditText.getParent();
                                til.setHint(annotation.value());
                                try {
                                    til.setHint(findValue(annotation.value()));
                                    til.setContentDescription(findValue(annotation.value()));
                                } catch (IllegalArgumentException e) {
                                    til.setHint(annotation.value());
                                    til.setContentDescription(annotation.value());
                                }
                            } else {
                                try {
                                    fieldEditText.setHint(findValue(annotation.value()));
                                } catch (IllegalArgumentException e) {
                                    fieldEditText.setHint(annotation.value());
                                }
                            }

                            try {
                                fieldEditText.setContentDescription(findValue(annotation.value()));
                            } catch (IllegalArgumentException e) {
                                fieldEditText.setContentDescription(annotation.value());
                            }
                        } catch (Exception e) {
                            Logger.d("TextInputLayout error: " + e.toString());
                        }

                    } catch (Exception e) {
                        Logger.d("Method.invoke error: " + e.toString());
                    }


                } else if (f.getType() == ToggleButton.class) {

                    try {
                        f.setAccessible(true);
                        ToggleButton fieldToggleButton = (ToggleButton) f.get(view);
                        Field translationField = null;

                        try {
                            String value = findValue(annotation.value());
                            fieldToggleButton.setText(value);
                            fieldToggleButton.setContentDescription(value);
                            fieldToggleButton.setTextOn(value);
                            fieldToggleButton.setTextOff(value);
                        } catch (IllegalArgumentException e) {
                            fieldToggleButton.setText(annotation.value());
                            fieldToggleButton.setContentDescription(annotation.value());
                        }

                        if (annotation.toggleOn().length() > 0) {
                            try {
                                String value = findValue(annotation.toggleOn());
                                fieldToggleButton.setTextOn(value);
                            } catch (IllegalArgumentException e) {
                            }
                        }

                        if (annotation.toggleOff().length() > 0) {
                            try {
                                String value = findValue(annotation.toggleOn());
                                fieldToggleButton.setTextOff(value);
                            } catch (IllegalArgumentException e) {
                            }
                        }

                    } catch (Exception e) {
                        Logger.d("Method.invoke error: " + e.toString());
                    }
                } else if (f.getType() == Toolbar.class) {

                    try {
                        f.setAccessible(true);
                        Toolbar toolbar = (Toolbar) f.get(view);
                        try {
                            toolbar.setTitle(findValue((annotation.value())));
                            toolbar.setContentDescription(findValue((annotation.value())));
                        } catch (IllegalArgumentException e) {
                            toolbar.setTitle(annotation.value());
                            toolbar.setContentDescription(annotation.value());
                        }

                    } catch (Exception e) {
                        Logger.d("Method.invoke error: " + e.toString());
                    }
                } // check these only on lollipop or newer (API 21)
                else if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                {
                    if(f.getType() == android.widget.Toolbar.class)
                    {
                        try {
                            f.setAccessible(true);
                            Toolbar toolbar = (Toolbar) f.get(view);
                            try {
                                toolbar.setTitle(findValue((annotation.value())));
                                toolbar.setContentDescription(findValue((annotation.value())));
                            } catch (IllegalArgumentException e) {
                                toolbar.setTitle(annotation.value());
                                toolbar.setContentDescription(annotation.value());
                            }

                        } catch (Exception e) {
                            Logger.d("Method.invoke error: " + e.toString());
                        }
                    }
                }
            }
        }
    }


    private static String findValue(String key) throws IllegalArgumentException {
        // Flat / No sections
        if (TranslationManager.getInstance().options().isFlattenKeys()) {
            try {
                Field field = classType.getField(key);
                String value = String.valueOf(field.get(null));
                return value;
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
                String value = String.valueOf(field.get(null));
                return value;
            } catch (Exception e) {
                Logger.e("findValue failed on key: " + key + ". Exception -> " + e.toString());
                throw new IllegalArgumentException();
            }
        }
    }

    public <T> void updateTranslations(final OnTranslationResultListener listener) {
        try {
            BackendManager.getInstance().getTranslation(translationOptions.getContentURL(), translationOptions.getLanguageHeader(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    TranslationManager.this.updateTranslationClass(response.body().string());

                    if (listener != null) {
                        listener.onSuccess();
                    }
                }
            });
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure();
            }
        }
    }


    public void updateTranslationsSilently() {
        try {
            BackendManager.getInstance().getTranslation(translationOptions.getContentURL(), translationOptions.getLanguageHeader(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    TranslationManager.this.updateTranslationClass(response.body().string());
                }
            });
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    /**
     * Get all the languages in an ArrayList<Language> - use .getLocale to get the locale
     *
     * @param listener OnLanguageResultListener returns on onSuccess the ArrayList<Language> with all the languages
     */
    public void getAllLanguages(@NonNull final OnLanguageResultListener listener) {
        try {
            BackendManager.getInstance().getAllLanguages(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    try {
                        JSONArray data = new JSONObject(response.body().string()).optJSONArray("data");

                        final ArrayList<Language> languages = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            languages.add(Language.parseFrom(data.optJSONObject(i)));
                        }

                        listener.onSuccess(languages);
                    } catch (JSONException e) {
                        Logger.d(e.toString());
                        listener.onFailure();
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(e);
            listener.onFailure();
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
        public void onSuccess();

        public void onFailure();
    }

    public interface OnLanguageResultListener {
        public void onSuccess(ArrayList<Language> languages);

        public void onFailure();

    }

    public void updateTranslationsFromAppOpen( JSONObject root ) {

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

    private void updateTranslationClass(String jsonData) {
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