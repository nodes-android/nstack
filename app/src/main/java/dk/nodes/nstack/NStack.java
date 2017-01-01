package dk.nodes.nstack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dk.nodes.nstack.util.appopen.AppOpenListener;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.backend.ClientProvider;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.translation.backend.OnLanguageResultListener;
import dk.nodes.nstack.util.translation.backend.TranslationBackendManager;
import dk.nodes.nstack.util.translation.backend.OnTranslationResultListener;
import dk.nodes.nstack.util.translation.manager.TranslationManager;
import dk.nodes.nstack.util.translation.options.TranslationOptions;
import okhttp3.Callback;

/**
 * Created by joso on 02/10/2015.
 */
public final class NStack {

    //TODO GET RID OF THE INSTANCE, INIT COULD JUST BE A METHOD THAT SAVES THOSE KEYS TO SHARED PREFS
    //AND WHENEVER YOU WANT TO USE IT YOU HAVE TO DO NSTACK NSTACK = NEW NSTACK(CONTEXT);
    //NSTACK.WHATEVER

    private Context applicationContext = null;
    protected static boolean debugMode = false;
    private static NStack instance = null;

    private String applicationId;
    private String restApiKey;

    private TranslationManager translationManager;
    private AppOpenManager appOpenManager;

    private TranslationOptions translationOptions;
    private CacheManager cacheManager;
    private BackendManager backendManager;
    private TranslationBackendManager translationBackendManager;

    /**
     * Initializes the singleton
     * @param context Use the application context to avoid leaks
     * @param applicationId Get this from the NStack.io site in keys (Application id)
     * @param restApiKey Get this from the NStack.io site in keys (Rest API key)
     */
    public static void init( @NonNull Context context, @NonNull String applicationId, @NonNull String restApiKey) {
        instance = new NStack(context, applicationId, restApiKey);
    }

    private NStack(Context context, String applicationId, String restApiKey) {
        this.applicationContext = context.getApplicationContext();
        this.applicationId = applicationId;
        this.restApiKey = restApiKey;
        this.cacheManager = new CacheManager(applicationContext);
        this.backendManager = new BackendManager(ClientProvider.provideHttpClient(cacheManager.initCache(), false));
        this.translationOptions = new TranslationOptions(applicationContext);
        this.translationManager = new TranslationManager(applicationContext, translationOptions);
        this.translationBackendManager = new TranslationBackendManager(backendManager, translationManager);
    }

    public static NStack getStack() {
        if( instance == null ) {
            throw new IllegalStateException("init() was not called");
        }

        if( instance.getApplicationId() == null || instance.getRestApiKey() == null ) {
            throw new IllegalStateException("applicationKey or apiKey was not set");
        }

        return instance;
    }

    public NStack enableDebug() {
        debugMode = true;
        backendManager.updateHttpClient(ClientProvider.provideHttpClient(cacheManager.initCache(), true));
        return this;
    }

    public NStack disableDebug() {
        debugMode = false;
        backendManager.updateHttpClient(ClientProvider.provideHttpClient(cacheManager.initCache(), false));
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getRestApiKey() {
        return restApiKey;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public AppOpenManager getAppOpenManager() {
        if( appOpenManager == null ) {
            appOpenManager = new AppOpenManager(applicationContext, backendManager, translationManager, cacheManager, translationOptions);
        }
        return appOpenManager;
    }

    public TranslationOptions getTranslationOptions() {
        return translationOptions;
    }

    /**
     * Delegates calls to managers
     */
    
    public void openApp(@Nullable AppOpenListener appOpenListener) {
        if (cacheManager.getJsonLanguages() == null){
            translationBackendManager.getAllLanguages();
            translationBackendManager.getAllTranslations();
        }
        getAppOpenManager().openApp(appOpenListener);
    }

    public TranslationOptions translationClass(Class<?> translationClass) {
        translationManager.setTranslationClass(translationClass);
        return translationOptions;
    }

    public void changeLanguage(String locale, OnTranslationResultListener callback) {
        translationBackendManager.updateTranslations(locale, callback);
    }

    public void getAllLanguages(@NonNull final OnLanguageResultListener callback){
        translationBackendManager.getAllLanguages(callback);
    }

    public void getAllTranslations(){
        translationBackendManager.getAllTranslations();
    }

    public void getContentResponse(int id, Callback callback) throws Exception {
        backendManager.getContentResponse(id, callback);
    }

    public void translate(Object view){
        translationManager.translate(view);
    }

    public void clearLastUpdated(){
        cacheManager.clearLastUpdated();
    }

}
