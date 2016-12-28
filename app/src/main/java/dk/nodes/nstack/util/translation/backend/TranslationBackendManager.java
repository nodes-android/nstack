package dk.nodes.nstack.util.translation.backend;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.model.Language;
import dk.nodes.nstack.util.translation.manager.OnTranslationResultListener;
import dk.nodes.nstack.util.translation.manager.TranslationManager;
import dk.nodes.nstack.util.translation.options.TranslationOptions;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by joaoalves on 13/12/2016.
 */

public class TranslationBackendManager {

    private BackendManager backendManager;
    private TranslationManager translationManager;
    private TranslationOptions translationOptions;

    public TranslationBackendManager(BackendManager backendManager, TranslationManager translationManager, TranslationOptions translationOptions) {
        this.backendManager = backendManager;
        this.translationManager = translationManager;
        this.translationOptions = translationOptions;
    }

    public <T> void updateTranslations(@NonNull final OnTranslationResultListener callback) {
        try {
            backendManager.getTranslation(translationOptions.getContentURL(), translationOptions.getLanguageHeader(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    translationManager.updateTranslationClass(response.body().string());
                    callback.onSuccess();
                }
            });
        } catch (Exception e) {
            callback.onFailure();
        }
    }


    public void updateTranslationsSilently() {
        try {

            backendManager.getTranslation(translationOptions.getContentURL(), translationOptions.getLanguageHeader(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    translationManager.updateTranslationClass(response.body().string());
                }
            });
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    /**
     * Get all the languages in an ArrayList<Language> - use .getLocale to get the locale
     *
     * @param callback OnLanguageResultListener returns on onSuccess the ArrayList<Language> with all the languages
     */
    public void getAllLanguages(@NonNull final OnLanguageResultListener callback) {
        try {
            backendManager.getAllLanguages(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                    try {
                        JSONArray data = new JSONObject(response.body().string()).optJSONArray("data");
                        final ArrayList<Language> languages = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            languages.add(Language.parseFrom(data.optJSONObject(i)));
                        }
                        callback.onSuccess(languages);
                    } catch (JSONException e) {
                        Logger.d(e.toString());
                        callback.onFailure();
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(e);
            callback.onFailure();
        }
    }
}
