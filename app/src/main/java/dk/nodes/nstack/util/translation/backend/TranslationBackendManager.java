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
import dk.nodes.nstack.util.translation.manager.TranslationManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by joaoalves on 13/12/2016.
 * Edited by Mario on 30/12/2016
 */

public class TranslationBackendManager {

    private BackendManager backendManager;
    private TranslationManager translationManager;

    public TranslationBackendManager(BackendManager backendManager, TranslationManager translationManager) {
        this.backendManager = backendManager;
        this.translationManager = translationManager;
    }

    public void updateTranslations(@NonNull String locale, @NonNull final OnTranslationResultListener callback) {
        try {
            translationManager.getTranslationOptions().setAllLanguages(false);
            translationManager.getTranslationOptions().setLanguageHeader(locale);

            backendManager.getTranslation(translationManager.getTranslationOptions().getContentUrl(),
                    translationManager.getTranslationOptions().getLanguageHeader(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (translationManager.getCacheLanguageTranslation(translationManager.getTranslationOptions().getLanguageHeader())) {
                                translationManager.getCacheManager().setCurrentLanguageLocale(translationManager.getTranslationOptions().getLanguageHeader());
                                translationManager.getCacheManager().clearLastUpdated();
                                callback.onSuccess(true);
                                return;
                            }
                            callback.onFailure();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                if (onFailureGetTranslation()){
                                    callback.onSuccess(true);
                                    return;
                                }
                                callback.onFailure();
                                throw new IOException("Unexpected code " + response);
                            }
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(response.body().string());
                            } catch (JSONException e) {
                                if (onFailureGetTranslation()) {
                                    callback.onSuccess(true);
                                    return;
                                }
                                callback.onFailure();
                                return;
                            }
                            translationManager.updateTranslationClass(jsonObject.toString());
                            translationManager.getCacheManager().setCurrentLanguageLocale(translationManager.getTranslationOptions().getLanguageHeader());
                            translationManager.saveLanguageTranslation(translationManager.getTranslationOptions().getLanguageHeader(), jsonObject.toString());
                            translationManager.getCacheManager().clearLastUpdated();
                            callback.onSuccess(false);
                        }
                    });
        } catch (Exception e) {
            callback.onFailure();
        }
    }

    public boolean onFailureGetTranslation(){
        if (translationManager.getCacheLanguageTranslation(translationManager.getTranslationOptions().getLanguageHeader())) {
            translationManager.getCacheManager().setCurrentLanguageLocale(translationManager.getTranslationOptions().getLanguageHeader());
            translationManager.getCacheManager().clearLastUpdated();
            return true;
        }
        return false;
    }


    public void getAllTranslations() {
        try {
            translationManager.getTranslationOptions().setAllLanguages(true);
            backendManager.getAllTranslations(translationManager.getTranslationOptions().getContentUrl(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonObject != null) {
                        translationManager.saveLanguagesTranslation(jsonObject.toString());
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    /**
     * Get all the languages in an ArrayList<Language> - use .getLocale to get the locale
     *
     * @param callback OnLanguageResultListener returns on onSuccess the ArrayList<Language> with all the languages
     */
    public void getAllLanguages(@NonNull final OnLanguageResultListener callback) {
        try {
            //Cached languages straight away instead of waiting to onFailure
            if (translationManager.getCacheManager().getJsonLanguages() != null) {
                JSONObject jsonObject = translationManager.getCacheLanguages();
                JSONArray jsonArray = jsonObject.optJSONArray("data");
                if (jsonArray != null) {
                    callback.onSuccess(parseLanguages(jsonArray), true);
                }
            }
            backendManager.getAllLanguages(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onFailure();
                        throw new IOException("Unexpected code " + response);
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        translationManager.saveLanguages(jsonObject.toString());
                        JSONArray jsonArray = jsonObject.optJSONArray("data");
                        if (jsonArray != null) {
                            callback.onSuccess(parseLanguages(jsonArray), false);
                            return;
                        }
                        callback.onFailure();
                    } catch (JSONException e) {
                        callback.onFailure();
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            Logger.e(e);
            callback.onFailure();
        }
    }

    //Used only on app open
    public void getAllLanguages() {
        try {
            backendManager.getAllLanguages(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        translationManager.saveLanguages(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private ArrayList<Language> parseLanguages(JSONArray jsonArray) {
        final ArrayList<Language> languages = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            languages.add(Language.parseFrom(jsonArray.optJSONObject(i)));
        }
        if (!languages.isEmpty()) {
            if (languages.size() == 1) {
                languages.get(0).setPicked(true);
            } else {
                if (translationManager.getCacheManager().getCurrentLanguageLocale() != null) {
                    String currentLanguageLocale = translationManager.getCacheManager().getCurrentLanguageLocale();
                    for (Language language : languages) {
                        if (language.getLocale().equals(currentLanguageLocale)) {
                            language.setPicked(true);
                            break;
                        }
                    }
                }
            }
        }
        return languages;
    }
}
