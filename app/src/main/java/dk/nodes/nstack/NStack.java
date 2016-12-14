package dk.nodes.nstack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.backend.ClientProvider;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.translation.TranslationBackendManager;
import dk.nodes.nstack.util.translation.TranslationManager;
import dk.nodes.nstack.util.translation.TranslationOptions;
import okhttp3.Callback;

/**
 * Created by joso on 02/10/2015.
 */
public final class NStack {

    private Context applicationContext = null;
    protected static boolean debugMode = false;
    private static NStack instance = null;

    private String applicationKey;
    private String apiKey;

    private TranslationManager translationManager;
    private AppOpenManager appOpenManager;

    private TranslationOptions translationOptions;
    private CacheManager cacheManager;
    private BackendManager backendManager;
    private TranslationBackendManager translationBackendManager;

    /**
     * Initializes the singleton
     * @param context Use the application context to avoid leaks
     * @param applicationKey Get this from the NStack.io site in keys
     * @param apiKey Get this from the NStack.io site in keys
     */
    public static void init( @NonNull Context context, @NonNull String applicationKey, @NonNull String apiKey) {
        instance = new NStack(context, applicationKey, apiKey);
    }

    private NStack( Context context, String applicationKey, String apiKey ) {
        this.applicationContext = context.getApplicationContext();
        this.applicationKey = applicationKey;
        this.apiKey = apiKey;
        this.cacheManager = new CacheManager(applicationContext);
        this.backendManager = new BackendManager(ClientProvider.provideHttpClient(cacheManager.initCache(), false));
        this.translationOptions = new TranslationOptions(applicationContext);
        this.translationManager = new TranslationManager(applicationContext, translationOptions);
        this.translationBackendManager = new TranslationBackendManager(backendManager, translationManager, translationOptions);
    }

    public static NStack getStack() {
        if( instance == null ) {
            throw new IllegalStateException("init() was not called");
        }

        if( instance.getApiKey() == null || instance.getApplicationKey() == null ) {
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

    public String getApplicationKey() {
        return applicationKey;
    }

    public String getApiKey() {
        return apiKey;
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

    public void openApp() {
        getAppOpenManager().openApp();
    }

    public void openApp(@Nullable AppOpenManager.AppOpenCallbacks translationsListener) {
        getAppOpenManager().openApp(translationsListener);
    }

    public TranslationOptions translationClass(Class<?> translationClass) {
        translationManager.setTranslationClass(translationClass);
        return translationOptions;
    }

    public void changeLanguage(String locale, TranslationManager.OnTranslationResultListener callback) {
        translationOptions.locale(locale);
        translationBackendManager.updateTranslations(callback);
    }

    public void getContentResponse(int id, Callback callback) throws Exception {
        backendManager.getContentResponse(id, callback);
    }

}
