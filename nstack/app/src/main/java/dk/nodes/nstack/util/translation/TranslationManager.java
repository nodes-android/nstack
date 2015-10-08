package dk.nodes.nstack.util.translation;

import android.support.design.widget.TextInputLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.log.NLog;


/**
 * Created by joso on 25/02/15.
 */
public class TranslationManager {

    private static TranslationManager instance = null;
    private static Class<?> classType;

    private TranslationOptions translationOptions = new TranslationOptions();

    public TranslationManager() {

    }

    public void setTranslationClass( Class<?> translationClass ) {
        classType = translationClass;
    }

    public TranslationOptions options() {
        return translationOptions;
    }

    public static TranslationManager getInstance() {
        if( instance == null ) {
            instance = new TranslationManager();
        }

        return instance;
    }

    public static void translate(Object view) {
        Field[] fields = view.getClass().getDeclaredFields();

        for (Field f : fields) {
            Translate annotation = f.getAnnotation(Translate.class);

            if (annotation != null) {

                if( f.getType() == Button.class || f.getType() == TextView.class ) {

                    try {
                        f.setAccessible(true);
                        TextView fieldTextView = (TextView) f.get(view);

                        try {
                            fieldTextView.setText( findValue(annotation.value()) );
                        } catch (IllegalArgumentException e) {
                            fieldTextView.setText( annotation.value() );
                        }

                    } catch( Exception e ) {
                        NLog.d("Method.invoke error: " + e.toString());
                    }
                }

                else if( f.getType() == EditText.class ) {

                    try {
                        f.setAccessible(true);
                        EditText fieldEditText = (EditText) f.get(view);

                        try {
                            if( fieldEditText.getParent() instanceof TextInputLayout) {
                                TextInputLayout til = (TextInputLayout) fieldEditText.getParent();
                                til.setHint( annotation.value() );
                                try {
                                    til.setHint(findValue(annotation.value()));
                                } catch (IllegalArgumentException e) {
                                    til.setHint(annotation.value());
                                }
                            } else {
                                try {
                                    fieldEditText.setHint( findValue(annotation.value()) );
                                } catch (IllegalArgumentException e) {
                                    fieldEditText.setHint( annotation.value() );
                                }
                            }
                        } catch ( Exception e ) {
                            NLog.d("TextInputLayout error: " + e.toString());
                        }

                    } catch( Exception e ) {
                        NLog.d("Method.invoke error: " + e.toString());
                    }


                }

                else if( f.getType() == ToggleButton.class ) {

                    try {
                        f.setAccessible(true);
                        ToggleButton fieldToggleButton = (ToggleButton) f.get(view);
                        Field translationField  = null;

                        try {
                            String value = findValue(annotation.value());
                            fieldToggleButton.setText( value );
                            fieldToggleButton.setTextOn(value);
                            fieldToggleButton.setTextOff(value);
                        } catch (IllegalArgumentException e) {
                            fieldToggleButton.setText( annotation.value() );
                        }

                        if( annotation.toggleOn().length() > 0 ) {
                            try {
                                String value = findValue(annotation.toggleOn());
                                fieldToggleButton.setTextOn(value);
                            } catch (IllegalArgumentException e) {
                            }
                        }

                        if( annotation.toggleOff().length() > 0 ) {
                            try {
                                String value = findValue(annotation.toggleOn());
                                fieldToggleButton.setTextOff(value);
                            } catch (IllegalArgumentException e) {
                            }
                        }

                    } catch( Exception e ) {
                        NLog.d("Method.invoke error: " + e.toString());
                    }
                }
            }
        }
    }

    public void updateTranslationsSilently() {
        try {
            BackendManager.getInstance().getTranslation(translationOptions.getContentURL(), translationOptions.getLanguageHeader(), new Callback() {
                @Override public void onFailure(Request request, IOException e) {

                }

                @Override public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    TranslationManager.this.updateTranslationClass(response.body().string());
                }
            });
        } catch( Exception e ) {
            NLog.e(e);
        }
    }

    private static String findValue(String key) throws IllegalArgumentException {
        // Flat / No sections
        if( TranslationManager.getInstance().options().isFlattenKeys() ) {
            try {
                Field field = classType.getField(key);
                String value = String.valueOf(field.get(null));
                return value;
            } catch( Exception e ) {
                NLog.e("findValue failed on key: " + key + ". Exception -> " + e.toString());
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
            } catch( Exception e ) {
                NLog.e("findValue failed on key: " + key + ". Exception -> " + e.toString());
                throw new IllegalArgumentException();
            }
        }
    }

    public void switchFallbackLanguage( String languageHeader, final OnTranslationResultListener listener ) {
        try {
            InputStream stream = NStack.getStack().getApplicationContext().getAssets().open(translationOptions.getFallbackFile());

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            String fallbackContents = new String(buffer);

            updateTranslationClass(fallbackContents);

            if( listener != null ) {
                listener.onSuccess();
            }
        } catch( Exception e ) {
            if( listener != null ) {
                listener.onFailure();
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
        } catch( Exception e ) {
            if( listener != null ) {
                listener.onFailure();
            }
        }
    }

    public interface OnTranslationResultListener {
        public void onSuccess();
        public void onFailure();
    }

    private void updateTranslationLanguageKeys( JSONObject data ) {
        try {
            Iterator<String> languageKeys = data.keys();

            while( languageKeys.hasNext() ) {
                String languageName = languageKeys.next();

                // Only update current language
                if( ! languageName.equalsIgnoreCase(translationOptions.getLanguageHeader()) ) {
                    continue;
                }

                NLog.d("updateTranslationLanguageKeys on: " + languageName);
                JSONObject translationObject = data.getJSONObject(languageName);

                // No sections
                if( translationOptions.isFlattenKeys() ) {
                    parseFlatTranslations( translationObject );
                }

                // Sections
                else {
                    parseSections(translationObject);
                }
            }
        } catch( Exception e ) {
            NLog.e(e);
        }
    }

    private void parseSections( JSONObject sectionsObject ) {

        Iterator<String> sectionKeys = sectionsObject.keys();
        while( sectionKeys.hasNext() ) {
            String sectionKey = sectionKeys.next();

            try {
                JSONObject sectionObject = sectionsObject.getJSONObject(sectionKey);
                Iterator<String> translationKeys = sectionObject.keys();

                if( sectionKey.equalsIgnoreCase("default") ) {
                    sectionKey = "defaultSection";
                }

                Class<?> sectionClass = Class.forName(classType.getName() + "$" + sectionKey);
                while( translationKeys.hasNext() ) {
                    String translationKey = translationKeys.next();

                    // Reached actual translation string
                    if( sectionObject.get(translationKey) instanceof String ) {
                        updateField(sectionClass, translationKey, sectionObject.getString(translationKey));
                    }
                }
            } catch( Exception e ) {
                NLog.e("Parsing failed for section -> " + sectionKey + " | " + e.toString());
            }
        }
    }

    private void parseFlatTranslations( JSONObject jsonLanguage ) {
        Iterator<String> translationKeys = jsonLanguage.keys();

        while( translationKeys.hasNext() ) {
            String translationKey = translationKeys.next();

            // Reached actual translation string
            try {
                if( jsonLanguage.get(translationKey) instanceof String ) {
                    updateField(classType, translationKey, jsonLanguage.getString(translationKey));
                }
            } catch( Exception e ) {
                NLog.e("Parsing failed for key = " + translationKey);
            }
        }
    }

    private void updateTranslationClass(String jsonData) {
        try {
            JSONObject root = new JSONObject(jsonData);
            JSONObject data = root.getJSONObject("data");

            // Fetched more than one language
            if( data.has( translationOptions.getLanguageHeader() ) ) {
                updateTranslationLanguageKeys(data);
            }

            // Only one language




            NLog.d(root.toString());
/*
            if( translations.containsKey( TranslationManager.languageHeader ) ) {
                this.currentTranslation = translations.get(TranslationManager.languageHeader);
            }
            */
        } catch( Exception e ) {
            NLog.e(e);
        }
    }

    private void updateField( Object object, String key, String value ) {
        try {
            Field field = object.getClass().getField(key);
            field.setAccessible(true);
            field.set(object, value);
        } catch( Exception e ) {
            NLog.e(e);
            NLog.e("TranslationManager", "Error updating field: " + key + " : " + value);
        }
    }

    private void updateField( Class<?> classType, String key, String value ) {
        try {
            Field field = classType.getField(key);
            field.setAccessible(true);
            field.set(null, value);
        } catch( Exception e ) {
            NLog.e(e);
            NLog.e("TranslationManager", "Error updating field: " + key + " : " + value);
        }
    }

}
