package dk.nodes.nstack;

import android.content.Context;
import android.support.annotation.NonNull;

import dk.nodes.nstack.util.appopen.AppOpen;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.content.ContentManager;
import dk.nodes.nstack.util.translation.TranslationManager;

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
    private ContentManager contentManager;
    private AppOpenManager appOpenManager;

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

        BackendManager.getInstance().initCache(this.applicationContext);
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
        return this;
    }

    public NStack disableDebug() {
        debugMode = false;
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

    public TranslationManager getTranslationManager() {
        if( translationManager == null ) {
            translationManager = new TranslationManager();
        }

        return translationManager;
    }

    public ContentManager getContentManager() {
        if( contentManager == null ) {
            contentManager = new ContentManager(applicationContext);
        }

        return contentManager;
    }

    public AppOpenManager getAppOpenManager() {
        if( appOpenManager == null ) {
            appOpenManager = new AppOpenManager();
        }

        return appOpenManager;
    }

}
