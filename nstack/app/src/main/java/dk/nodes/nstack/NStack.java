package dk.nodes.nstack;

import android.content.Context;

import dk.nodes.nstack.util.backend.BackendManager;

/**
 * Created by joso on 02/10/2015.
 */
public final class NStack {

    private Context applicationContext = null;
    protected static boolean debugMode = false;
    private static NStack instance = null;

    private String applicationKey = "";
    private String apiKey = "";

    public static void init(Context context, String applicationKey, String apiKey) {
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
            throw new RuntimeException("init() was not called");
        }

        if( instance.getApiKey() == null || instance.getApplicationKey() == null ) {
            throw new RuntimeException("applicationKey or apiKey was not set");
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

}
