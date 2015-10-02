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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.log.NLog;


/**
 * Created by joso on 25/02/15.
 */
public class TranslationManager<T> {

    private HashMap<String, T> translations = new HashMap<>();
    private T currentTranslation = null;
    private static TranslationManager instance = null;
    private Class<T> classType;

    private String languageHeader = "da-DK";
    private final String NSTACK_LANGUAGES_URL = "https://baas.like.st/api/v1/translate/mobile/languages/best_fit";
    private final String NSTACK_CONTENT_URL = "https://baas.like.st/api/v1/translate/mobile/keys?all=true&flat=false";

    private TranslationManager() {

    }

    public void setTranslation( Object translation ) {
        classType = (Class<T>)translation.getClass();
    }

    public static TranslationManager getInstance() {
        if( instance == null ) {
            instance = new TranslationManager();
        }

        return instance;
    }

    public T getCurrentTranslation() {
        return currentTranslation == null ? translations.get(languageHeader) : currentTranslation;
    }

    public T getTranslationFromAcceptHeader( String acceptHeader ) {
        T t = translations.get(acceptHeader);
        NLog.e("","returning translation instance: " + t.toString());
        return t;
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
                        fieldTextView.setText( annotation.value() );
                    } catch( Exception e ) {
                        NLog.d("Method.invoke error: " + e.toString());
                    }
                }

                else if( f.getType() == EditText.class ) {

                    try {
                        f.setAccessible(true);
                        EditText fieldEditText = (EditText) f.get(view);
                        fieldEditText.setHint( annotation.value() );

                        try {
                            if( fieldEditText.getParent() instanceof TextInputLayout) {
                                TextInputLayout til = (TextInputLayout) fieldEditText.getParent();
                                til.setHint( annotation.value() );
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

                        fieldToggleButton.setText( annotation.value() );
                        fieldToggleButton.setTextOn(
                                annotation.toggleOn().length() > 0 ?
                                        annotation.toggleOn() :
                                        annotation.value()
                        );
                        fieldToggleButton.setTextOff(
                                annotation.toggleOff().length() > 0 ?
                                        annotation.toggleOff() :
                                        annotation.value()
                        );

                    } catch( Exception e ) {
                        NLog.d("Method.invoke error: " + e.toString());
                    }
                }
            }
        }
    }

    public void updateTranslationsSilently() {
        try {
            BackendManager.getInstance().getTranslation(NSTACK_CONTENT_URL, languageHeader, new Callback() {
                @Override public void onFailure(Request request, IOException e) {

                }

                @Override public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    TranslationManager.this.updateTranslationClass(response);
                }
            });
        } catch( Exception e ) {
            NLog.e(e);
        }
    }

    public void updateTranslations(final OnTranslationResultListener listener) {
        try {
            BackendManager.getInstance().getTranslation(NSTACK_CONTENT_URL, languageHeader, new Callback() {
                @Override public void onFailure(Request request, IOException e) {

                }

                @Override public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    TranslationManager.this.updateTranslationClass(response);

                    if( listener != null ) {
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

    private void updateTranslationClass(Response response) {
        try {
            JSONObject root = new JSONObject(response.body().string());
            JSONObject data = root.getJSONObject("data");
            Iterator<String> languageKeys = data.keys();

            while( languageKeys.hasNext() ) {
                T translationInstance = classType.newInstance();
                String languageName = languageKeys.next();
                JSONObject translationObject = data.getJSONObject(languageName);
                Iterator<String> translationKeys = translationObject.keys();

                while( translationKeys.hasNext() ) {
                    String translationKey = translationKeys.next();

                    // Reached actual translation string
                    if( translationObject.get(translationKey) instanceof String ) {
                        Field field = translationInstance.getClass().getField(translationKey);
                        field.setAccessible(true);
                        field.set(translationInstance, translationObject.getString(translationKey));
                    }

                    // Translation has sections, we have to go deeper
                    else if( translationObject.get(translationKey) instanceof JSONObject ) {
                        JSONObject sectionObject = translationObject.getJSONObject(translationKey);
                        Iterator<String> sectionKeys = sectionObject.keys();

                        if( translationKey.equalsIgnoreCase("default") ) {
                            translationKey = "defaultSection";
                        }

                        //Class<?> sectionClass = Class.forName( translationInstance.getClass().getName() + "$" + translationKey );

                        Field sectionField = translationInstance.getClass().getField(translationKey);
                        sectionField.setAccessible(true);
                        Object sectionInstance = sectionField.get(translationInstance);

                        while( sectionKeys.hasNext() ) {
                            String sectionKey = sectionKeys.next();

                            // Reached actual translation string
                            if( sectionObject.get(sectionKey) instanceof String ) {
                                updateField(sectionInstance, sectionKey, sectionObject.getString(sectionKey));
                            }
                        }
                    }
                }
                NLog.e("","languageName --> " + languageName + " on translationInstance: " + translationInstance.toString());
                translations.put(languageName, translationInstance);
            }

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
            NLog.e("TranslationManager", "Error updating field: " + key + " : " + value);
        }
    }

    private void updateField( Class<?> classType, String key, String value ) {
        try {
            Field field = classType.getField(key);
            field.setAccessible(true);
            field.set(classType, value);
        } catch( Exception e ) {
            NLog.e("TranslationManager", "Error updating field: " + key + " : " + value);
        }
    }

}
